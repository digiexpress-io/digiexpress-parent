package io.resys.thena.structures.org.queries;

import java.util.ArrayList;
import java.util.Collections;

import io.resys.thena.api.actions.OrgQueryActions.PartyHierarchyQuery;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgPartyHierarchy;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgProjectObjects;
import io.resys.thena.api.envelope.ImmutableQueryEnvelope;
import io.resys.thena.api.envelope.ImmutableQueryEnvelopeList;
import io.resys.thena.api.envelope.OrgPartyLogVisitor;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.DocNotFoundException;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.resys.thena.spi.DbState;
import io.resys.thena.structures.org.anytree.AnyTreeContainerContextImpl;
import io.resys.thena.structures.org.anytree.AnyTreeContainerImpl;
import io.resys.thena.structures.org.anytree.PartyHierarchyContainerVisitor;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class PartyHierarchyQueryImpl implements PartyHierarchyQuery {
  private final DbState state;
  private final String repoId;
  
  @Override
  public Uni<QueryEnvelope<OrgPartyHierarchy>> get(String groupIdOrNameOrExternalId) {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    
    return new OrgProjectQueryImpl(state, repoId).get()
      .onItem().transform(resp -> {
        if(resp.getStatus() != QueryEnvelopeStatus.OK) {
          return resp.toType();
        }
        
        try {
          final QueryEnvelope<OrgPartyHierarchy> success = createOneHierarchy(resp, groupIdOrNameOrExternalId);
          return success;
        } catch(Exception e) {
          return QueryEnvelope.fatalError(resp.getRepo(), "Failed to build hierarchy for all groups", log, e);
        }
      });
  }
  @Override
  public Uni<QueryEnvelopeList<OrgPartyHierarchy>> findAll() {
    return new OrgProjectQueryImpl(state, repoId).get()
        .onItem().transform(resp -> {
          if(resp.getStatus() != QueryEnvelopeStatus.OK) {
            return resp.toListOfType();
          }
          
          try {
            final QueryEnvelopeList<OrgPartyHierarchy> success = createManyHierarchies(resp);
            return success;
          } catch(Exception e) {
            final QueryEnvelope<OrgPartyHierarchy> fail = QueryEnvelope.fatalError(resp.getRepo(), "Failed to build hierarchy for all groups", log, e);
            return fail.toList();
          }
        });
  }

  private QueryEnvelopeList<OrgPartyHierarchy> createManyHierarchies(QueryEnvelope<OrgProjectObjects> init) {
    final var groups = new ArrayList<OrgPartyHierarchy>();
    final var ctx = new AnyTreeContainerContextImpl(init.getObjects());
    final var container = new AnyTreeContainerImpl(ctx);
    for(final var criteria : init.getObjects().getParties().values().stream().sorted((a, b) -> a.getPartyName().compareTo(b.getPartyName())).toList()) {
      
      final var group = container.accept(new PartyHierarchyContainerVisitor(criteria.getId()));
      final var log = container.accept(new OrgPartyLogVisitor(criteria.getId(), true));
      groups.add(group.withLog(log));
    }
    return ImmutableQueryEnvelopeList.<OrgPartyHierarchy>builder()
        .objects(Collections.unmodifiableList(groups))
        .repo(init.getRepo())
        .status(QueryEnvelope.QueryEnvelopeStatus.OK)
        .build();
  }
  
  private QueryEnvelope<OrgPartyHierarchy> createOneHierarchy(QueryEnvelope<OrgProjectObjects> init, String groupIdOrNameOrExternalId) {
    
    final var ctx = new AnyTreeContainerContextImpl(init.getObjects());
    final var container = new AnyTreeContainerImpl(ctx);
    final var log = container.accept(new OrgPartyLogVisitor(groupIdOrNameOrExternalId, true));
    final var visited = container.accept(new PartyHierarchyContainerVisitor(groupIdOrNameOrExternalId));
    final OrgPartyHierarchy group = visited == null ? null : visited.withLog(log);
    if(group == null) {
      return QueryEnvelope
          .docNotFound(
              init.getRepo(), 
              PartyHierarchyQueryImpl.log, 
              "Can't find party by id: '" + groupIdOrNameOrExternalId + "'!", 
              new DocNotFoundException());      
    }
    
    return ImmutableQueryEnvelope.<OrgPartyHierarchy>builder()
        .objects(group)
        .repo(init.getRepo())
        .status(QueryEnvelope.QueryEnvelopeStatus.OK)
        .build();
  }
}
