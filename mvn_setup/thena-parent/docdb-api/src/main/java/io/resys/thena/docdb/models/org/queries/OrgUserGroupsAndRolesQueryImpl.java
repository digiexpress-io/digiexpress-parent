package io.resys.thena.docdb.models.org.queries;

import io.resys.thena.docdb.api.actions.OrgQueryActions.UserGroupsAndRolesQuery;
import io.resys.thena.docdb.api.actions.OrgQueryActions.UserObjectsQuery;
import io.resys.thena.docdb.api.exceptions.RepoException;
import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.ImmutableQueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.ThenaEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserGroupsAndRolesWithLog;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserObject;
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
      
      return null; 
      		//state.toOrgState().query(repoId);
    });
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
            .append("User not found by given id, from repo: '").append(existing.getId()).append("'!")
            .toString())
        .build())
    .build();
  }
  
}
