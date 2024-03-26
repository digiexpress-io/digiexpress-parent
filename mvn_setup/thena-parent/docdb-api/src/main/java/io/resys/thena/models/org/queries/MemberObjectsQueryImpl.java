package io.resys.thena.models.org.queries;

import java.util.List;

import io.resys.thena.api.actions.OrgQueryActions.MemberObjectsQuery;
import io.resys.thena.api.models.ImmutableQueryEnvelope;
import io.resys.thena.api.models.ImmutableQueryEnvelopeList;
import io.resys.thena.api.models.QueryEnvelope;
import io.resys.thena.api.models.QueryEnvelopeList;
import io.resys.thena.api.models.Repo;
import io.resys.thena.api.models.ThenaEnvelope;
import io.resys.thena.api.models.QueryEnvelope.DocNotFoundException;
import io.resys.thena.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.models.ThenaOrgObject.OrgMember;
import io.resys.thena.models.org.OrgQueries;
import io.resys.thena.spi.DbState;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MemberObjectsQueryImpl implements MemberObjectsQuery {
  private final DbState state;
  private final String repoId;

  @Override
  public Uni<QueryEnvelope<OrgMember>> get(String userId) {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");

    return state.project().getByNameOrId(repoId)
    .onItem().transformToUni((Repo existing) -> {
      if(existing == null) {
        return Uni.createFrom().item(QueryEnvelope.repoNotFound(repoId, log));
      }
      return state.toOrgState().query(repoId)
        .onItem().transformToUni((OrgQueries repo) -> repo.members().getById(userId))
        .onItem().transformToUni(data -> {
          if(data == null) {
            return Uni.createFrom().item(docNotFound(existing, userId, new DocNotFoundException()));
          }
          return getUserObject(existing, data);
        });
    });
  }
 
  @Override
  public Uni<QueryEnvelopeList<OrgMember>> findAll() {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    return state.project().getByNameOrId(repoId)
    .onItem().transformToUni((Repo existing) -> {
      if(existing == null) {
        return Uni.createFrom().item(QueryEnvelope.repoNotFoundList(repoId, log));
      }
      return state.toOrgState().query(repoId)
        .onItem().transformToUni((OrgQueries repo) -> repo.members().findAll().collect().asList())
        .onItem().transformToUni(data -> getUserObjects(existing, data));
    });
  }
  
  private Uni<QueryEnvelope<OrgMember>> getUserObject(Repo existing, OrgMember user) {
    return Uni.createFrom().item(ImmutableQueryEnvelope.<OrgMember>builder()
        .repo(existing)
        .status(QueryEnvelopeStatus.OK)
        .objects(user)
        .build());
  }  
  private Uni<QueryEnvelopeList<OrgMember>> getUserObjects(Repo existing, List<OrgMember> users) {
    
    return Uni.createFrom().item(ImmutableQueryEnvelopeList.<OrgMember>builder()
        .repo(existing)
        .status(QueryEnvelopeStatus.OK)
        .objects(users)
        .build());
  }

  private <T extends ThenaEnvelope.ThenaObjects> QueryEnvelope<T> docNotFound(Repo existing, String userId, DocNotFoundException ex) {
    final var msg = new StringBuilder()
        .append("User not found by given id = '").append(userId).append("', from repo: '").append(existing.getId()).append("'!")
        .toString();
    return QueryEnvelope.docNotFound(existing, log, msg, ex);
  }
}