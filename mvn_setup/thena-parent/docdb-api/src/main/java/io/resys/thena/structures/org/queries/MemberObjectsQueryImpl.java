package io.resys.thena.structures.org.queries;

import java.util.List;

import io.resys.thena.api.actions.OrgQueryActions.MemberObjectsQuery;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.envelope.ImmutableQueryEnvelope;
import io.resys.thena.api.envelope.ImmutableQueryEnvelopeList;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.DocNotFoundException;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.resys.thena.api.envelope.ThenaContainer;
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

    return state.toOrgState(repoId)
        .onItem().transformToUni(orgState -> {
          final var tenant = orgState.getDataSource().getTenant();
          return orgState.query().members().getById(userId)
            .onItem().transformToUni(data -> {
              if(data == null) {
                return Uni.createFrom().item(docNotFound(tenant, userId, new DocNotFoundException()));
              }
              return getUserObject(tenant, data);
            });
          
        });
  }
 
  @Override
  public Uni<QueryEnvelopeList<OrgMember>> findAll() {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    return state.toOrgState(repoId)
    .onItem().transformToUni((orgState) -> {
      final Tenant existing = orgState.getDataSource().getTenant();
      
      return orgState.query().members().findAll().collect().asList()
        .onItem().transformToUni(data -> getUserObjects(existing, data));
    });
  }
  
  private Uni<QueryEnvelope<OrgMember>> getUserObject(Tenant existing, OrgMember user) {
    return Uni.createFrom().item(ImmutableQueryEnvelope.<OrgMember>builder()
        .repo(existing)
        .status(QueryEnvelopeStatus.OK)
        .objects(user)
        .build());
  }  
  private Uni<QueryEnvelopeList<OrgMember>> getUserObjects(Tenant existing, List<OrgMember> users) {
    
    return Uni.createFrom().item(ImmutableQueryEnvelopeList.<OrgMember>builder()
        .repo(existing)
        .status(QueryEnvelopeStatus.OK)
        .objects(users)
        .build());
  }

  private <T extends ThenaContainer> QueryEnvelope<T> docNotFound(Tenant existing, String userId, DocNotFoundException ex) {
    final var msg = new StringBuilder()
        .append("User not found by given id = '").append(userId).append("', from repo: '").append(existing.getId()).append("'!")
        .toString();
    return QueryEnvelope.docNotFound(existing, log, msg, ex);
  }
}
