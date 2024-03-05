package io.resys.thena.docdb.models.org.queries;

import java.util.List;
import java.util.Set;

import io.resys.thena.docdb.api.actions.OrgQueryActions.UserObjectsQuery;
import io.resys.thena.docdb.api.exceptions.RepoException;
import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.ImmutableOrgUserObjects;
import io.resys.thena.docdb.api.models.ImmutableQueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.ThenaEnvelope;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUser;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserObjects;
import io.resys.thena.docdb.models.org.OrgQueries;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OrgUserObjectsQueryImpl implements UserObjectsQuery {
  private final DbState state;
  private String repoId;

  @Override
  public UserObjectsQuery repoId(String repoId) {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    this.repoId = repoId;
    return this;
  }
  @Override
  public Uni<QueryEnvelope<OrgUser>> get(String userId) {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");

    return state.project().getByNameOrId(repoId)
    .onItem().transformToUni((Repo existing) -> {
      if(existing == null) {
        return Uni.createFrom().item(repoNotFound());
      }
      return state.toOrgState().query(repoId)
        .onItem().transformToUni((OrgQueries repo) -> repo.users().getById(userId))
        .onItem().transformToUni(data -> {
          if(data == null) {
            return Uni.createFrom().item(docNotFound(existing));
          }
          return getUserObject(existing, data);
        });
    });
  }
 
  @Override
  public Uni<QueryEnvelope<OrgUserObjects>> findAll() {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    return state.project().getByNameOrId(repoId)
    .onItem().transformToUni((Repo existing) -> {
      if(existing == null) {
        return Uni.createFrom().item(repoNotFound());
      }
      return state.toOrgState().query(repoId)
        .onItem().transformToUni((OrgQueries repo) -> repo.users().findAll().collect().asList())
        .onItem().transformToUni(data -> getUserObjects(existing, data));
    });
  }
  
  private Uni<QueryEnvelope<OrgUser>> getUserObject(Repo existing, OrgUser user) {
    return Uni.createFrom().item(ImmutableQueryEnvelope.<OrgUser>builder()
        .repo(existing)
        .status(QueryEnvelopeStatus.OK)
        .objects(user)
        .build());
  }  
  private Uni<QueryEnvelope<OrgUserObjects>> getUserObjects(Repo existing, List<OrgUser> users) {
    final var objects = ImmutableOrgUserObjects.builder().users(users).build();
    
    return Uni.createFrom().item(ImmutableQueryEnvelope.<OrgUserObjects>builder()
        .repo(existing)
        .status(QueryEnvelopeStatus.OK)
        .objects(objects)
        .build());
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
  
  private <T extends ThenaEnvelope.ThenaObjects> QueryEnvelope<T> docUnexpected(Repo existing, Set<String> unexpected) {
    return ImmutableQueryEnvelope.<T>builder()
        .repo(existing)
        .status(QueryEnvelopeStatus.ERROR)
        .addMessages(ImmutableMessage.builder()
            .text(new StringBuilder()
                .append("Expecting: '1' user, but found: '").append(unexpected.size()).append("'")
                .append(", from repo: '").append(existing.getId()).append("'!")
                .toString())
            .build())
        .build();
  }
}
