package io.resys.thena.docdb.models.org.queries;

import java.util.List;

import io.resys.thena.docdb.api.actions.OrgQueryActions.UserGroupsAndRolesQuery;
import io.resys.thena.docdb.api.exceptions.RepoException;
import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.ImmutableQueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.ThenaEnvelope;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroupAndRoleFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRoleFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserGroupsAndRolesWithLog;
import io.resys.thena.docdb.models.org.OrgQueries;
import io.resys.thena.docdb.models.org.support.OrgTreeBuilderForUser;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OrgUserGroupsAndRolesQueryImpl implements UserGroupsAndRolesQuery {
  private final DbState state;
  private String repoId;

  @Override
  public UserGroupsAndRolesQuery repoId(String repoId) {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    this.repoId = repoId;
    return this;
  }

	@Override
	public Uni<QueryEnvelope<OrgUserGroupsAndRolesWithLog>> get(String userId) {
		RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
		
    return state.project().getByNameOrId(repoId)
    .onItem().transformToUni((Repo existing) -> {
      if(existing == null) {
        return Uni.createFrom().item(repoNotFound());
      }
      
      return state.toOrgState().query(repoId).onItem()
          .transformToUni(state -> getUser(state, existing, userId));
    });
	}
	
	private Uni<QueryEnvelope<OrgUserGroupsAndRolesWithLog>> getUser(OrgQueries org, Repo existing, String userId) {
    return org.users().getStatusById(userId).onItem().transformToUni(user -> {
      if(user == null) {
        return Uni.createFrom().item(docNotFound(existing));
      }
      return Uni.combine().all().unis(
          org.users().findAllRolesByUserId(user.getId()),
          org.users().findAllGroupsAndRolesByUserId(user.getId())
        ).asTuple()
        .onItem().transform(tuple -> createResult(user, tuple.getItem2(), tuple.getItem1()));
    });

	}
	

	private QueryEnvelope<OrgUserGroupsAndRolesWithLog> createResult(OrgUserFlattened user, List<OrgGroupAndRoleFlattened> groups, List<OrgRoleFlattened> roles) {
    return ImmutableQueryEnvelope
        .<OrgUserGroupsAndRolesWithLog>builder()
        .status(QueryEnvelopeStatus.OK)
        .objects(new OrgTreeBuilderForUser(user, groups, roles).build())
        .build();
	}
	
  private <T extends ThenaEnvelope.ThenaObjects> QueryEnvelope<T> repoNotFound() {
    final var ex = RepoException.builder().notRepoWithName(repoId);
    log.warn(ex.getText());
    return ImmutableQueryEnvelope
        .<T>builder()
        .status(QueryEnvelopeStatus.ERROR)
        .addMessages(ex)
        .build();
  }
  
  
  private <T extends ThenaEnvelope.ThenaObjects> QueryEnvelope<T> docNotFound(Repo existing) {
    return ImmutableQueryEnvelope.<T>builder()
    .repo(existing)
    .status(QueryEnvelopeStatus.ERROR)
    .addMessages(ImmutableMessage.builder()
        .text(new StringBuilder()
            .append("User groups and roles not found by given id, from repo: '").append(existing.getId()).append("'!")
            .toString())
        .build())
    .build();
  }
}
