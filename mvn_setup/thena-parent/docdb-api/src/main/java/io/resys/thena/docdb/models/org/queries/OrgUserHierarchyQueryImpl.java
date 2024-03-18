package io.resys.thena.docdb.models.org.queries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.resys.thena.docdb.api.actions.OrgQueryActions.UserHierarchyQuery;
import io.resys.thena.docdb.api.models.ImmutableQueryEnvelope;
import io.resys.thena.docdb.api.models.ImmutableQueryEnvelopeList;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.DocNotFoundException;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.QueryEnvelopeList;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.ThenaEnvelope;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRoleFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUser;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserHierarchyEntry;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserHierarchy;
import io.resys.thena.docdb.models.org.OrgQueries;
import io.resys.thena.docdb.models.org.userhierarchy.UserTreeBuilder;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.support.RepoAssert;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OrgUserHierarchyQueryImpl implements UserHierarchyQuery {
  private final DbState state;
  private String repoId;

  @Override
  public UserHierarchyQuery repoId(String repoId) {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    this.repoId = repoId;
    return this;
  }

	@Override
	public Uni<QueryEnvelope<OrgUserHierarchy>> get(String userId) {
		RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
		
    return state.project().getByNameOrId(repoId)
    .onItem().transformToUni((Repo existing) -> {
      if(existing == null) {
        return Uni.createFrom().item(QueryEnvelope.repoNotFound(repoId, log));
      }
      
      return getUser(state.toOrgState().query(existing), existing, userId);
    });
	}
  @Override
  public Uni<QueryEnvelopeList<OrgUserHierarchy>> findAll() {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    
    return state.project().getByNameOrId(repoId)
    .onItem().transformToUni((Repo existing) -> {
      if(existing == null) {
        return Uni.createFrom().item(QueryEnvelopeList.repoNotFound(repoId, log));
      }
      
      final var state = this.state.toOrgState().query(existing);
      final Multi<OrgUser> users = state.users().findAll();
      final Multi<QueryEnvelope<OrgUserHierarchy>> userAndGroups = users.onItem().transformToUni(user -> getUser(state, existing, user.getId())).merge();
      return userAndGroups.collect().asList().onItem().transform(this::createUsersResult);
    });
  }
	
	private Uni<QueryEnvelope<OrgUserHierarchy>> getUser(OrgQueries org, Repo existing, String userId) {
    return org.users().getStatusById(userId).onItem().transformToUni(user -> {
      if(user == null) {
        return Uni.createFrom().item(docNotFound(existing, userId, new DocNotFoundException()));
      }
      return Uni.combine().all().unis(
          org.users().findAllRolesByUserId(user.getId()),
          org.users().findAllUserHierarchyEntries(user.getId())
        ).asTuple()
        .onItem().transform(tuple -> createUserResult(user, tuple.getItem2(), tuple.getItem1()));
    });
	}
	

	private QueryEnvelope<OrgUserHierarchy> createUserResult(OrgUserFlattened user, List<OrgUserHierarchyEntry> groups, List<OrgRoleFlattened> roles) {
    return ImmutableQueryEnvelope
        .<OrgUserHierarchy>builder()
        .status(QueryEnvelopeStatus.OK)
        .objects(new UserTreeBuilder().user(user).groupData(groups).roleData(roles).build())
        .build();
	}
	
	private QueryEnvelopeList<OrgUserHierarchy> createUsersResult(List<QueryEnvelope<OrgUserHierarchy>> users) {
	  final var builder = ImmutableQueryEnvelopeList.<OrgUserHierarchy>builder()
	      .status(QueryEnvelopeStatus.OK);
	  
	  final List<OrgUserHierarchy> objects = new ArrayList<>();
	  for(final var resp : users) {
	    objects.add(resp.getObjects());
	    builder.addAllMessages(resp.getMessages());
	    
	    if(resp.getStatus() != QueryEnvelopeStatus.OK) {
	      builder.status(resp.getStatus());
	    }
	  }
	  
    return builder.objects(Collections.unmodifiableList(objects)).build();
  }
  
  private <T extends ThenaEnvelope.ThenaObjects> QueryEnvelope<T> docNotFound(
      Repo existing, String userId,
      DocNotFoundException ex
      ) {
    final var msg = new StringBuilder()
        .append("User groups and roles not found by given id = '").append(userId).append("', from repo: '").append(existing.getId()).append("'!")
        .toString();
    return QueryEnvelope.docNotFound(existing, log, msg, ex);
  }
}
