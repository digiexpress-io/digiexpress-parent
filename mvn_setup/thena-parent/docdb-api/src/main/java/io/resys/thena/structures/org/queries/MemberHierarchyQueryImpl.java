package io.resys.thena.structures.org.queries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.resys.thena.api.actions.OrgQueryActions.MemberHierarchyQuery;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgMember;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgMemberFlattened;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgMemberHierarchyEntry;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgRightFlattened;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgMemberHierarchy;
import io.resys.thena.api.models.ImmutableQueryEnvelope;
import io.resys.thena.api.models.ImmutableQueryEnvelopeList;
import io.resys.thena.api.models.QueryEnvelope;
import io.resys.thena.api.models.QueryEnvelopeList;
import io.resys.thena.api.models.ThenaEnvelope;
import io.resys.thena.api.models.QueryEnvelope.DocNotFoundException;
import io.resys.thena.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.spi.DbState;
import io.resys.thena.structures.org.OrgQueries;
import io.resys.thena.structures.org.memberhierarchy.MemberTreeBuilder;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MemberHierarchyQueryImpl implements MemberHierarchyQuery {
  private final DbState state;
  private final String repoId;

	@Override
	public Uni<QueryEnvelope<OrgMemberHierarchy>> get(String userId) {
		RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
		
    return state.project().getByNameOrId(repoId)
    .onItem().transformToUni((Tenant existing) -> {
      if(existing == null) {
        return Uni.createFrom().item(QueryEnvelope.repoNotFound(repoId, log));
      }
      
      return getUser(state.toOrgState().query(existing), existing, userId);
    });
	}
  @Override
  public Uni<QueryEnvelopeList<OrgMemberHierarchy>> findAll() {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    
    return state.project().getByNameOrId(repoId)
    .onItem().transformToUni((Tenant existing) -> {
      if(existing == null) {
        return Uni.createFrom().item(QueryEnvelopeList.repoNotFound(repoId, log));
      }
      
      final var state = this.state.toOrgState().query(existing);
      final Multi<OrgMember> users = state.members().findAll();
      final Multi<QueryEnvelope<OrgMemberHierarchy>> userAndGroups = users.onItem().transformToUni(user -> getUser(state, existing, user.getId())).merge();
      return userAndGroups.collect().asList().onItem().transform(this::createUsersResult);
    });
  }
	
	private Uni<QueryEnvelope<OrgMemberHierarchy>> getUser(OrgQueries org, Tenant existing, String userId) {
    return org.members().getStatusById(userId).onItem().transformToUni(user -> {
      if(user == null) {
        return Uni.createFrom().item(docNotFound(existing, userId, new DocNotFoundException()));
      }
      return Uni.combine().all().unis(
          org.members().findAllRightsByMemberId(user.getId()),
          org.members().findAllMemberHierarchyEntries(user.getId())
        ).asTuple()
        .onItem().transform(tuple -> createUserResult(user, tuple.getItem2(), tuple.getItem1()));
    });
	}
	

	private QueryEnvelope<OrgMemberHierarchy> createUserResult(OrgMemberFlattened user, List<OrgMemberHierarchyEntry> groups, List<OrgRightFlattened> roles) {
    return ImmutableQueryEnvelope
        .<OrgMemberHierarchy>builder()
        .status(QueryEnvelopeStatus.OK)
        .objects(new MemberTreeBuilder().member(user).partyData(groups).rightData(roles).build())
        .build();
	}
	
	private QueryEnvelopeList<OrgMemberHierarchy> createUsersResult(List<QueryEnvelope<OrgMemberHierarchy>> users) {
	  final var builder = ImmutableQueryEnvelopeList.<OrgMemberHierarchy>builder()
	      .status(QueryEnvelopeStatus.OK);
	  
	  final List<OrgMemberHierarchy> objects = new ArrayList<>();
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
      Tenant existing, String userId,
      DocNotFoundException ex
      ) {
    final var msg = new StringBuilder()
        .append("User groups and roles not found by given id = '").append(userId).append("', from repo: '").append(existing.getId()).append("'!")
        .toString();
    return QueryEnvelope.docNotFound(existing, log, msg, ex);
  }
}
