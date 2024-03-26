package io.resys.thena.structures.org.queries;

import java.util.ArrayList;
import java.util.Collections;

import io.resys.thena.api.actions.OrgQueryActions.PartyHierarchyQuery;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgPartyHierarchy;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgProjectObjects;
import io.resys.thena.api.models.ImmutableQueryEnvelope;
import io.resys.thena.api.models.ImmutableQueryEnvelopeList;
import io.resys.thena.api.models.QueryEnvelope;
import io.resys.thena.api.models.QueryEnvelopeList;
import io.resys.thena.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.visitors.OrgPartyLogVisitor;
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
            final QueryEnvelope<OrgPartyHierarchy> success = createGroupHierarchy(resp, groupIdOrNameOrExternalId);
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
            final QueryEnvelopeList<OrgPartyHierarchy> success = createGroupHierarchy(resp);
            return success;
          } catch(Exception e) {
            final QueryEnvelope<OrgPartyHierarchy> fail = QueryEnvelope.fatalError(resp.getRepo(), "Failed to build hierarchy for all groups", log, e);
            return fail.toList();
          }
        });
  }

  private QueryEnvelopeList<OrgPartyHierarchy> createGroupHierarchy(QueryEnvelope<OrgProjectObjects> init) {
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
  
  private QueryEnvelope<OrgPartyHierarchy> createGroupHierarchy(
      QueryEnvelope<OrgProjectObjects> init, 
      String groupIdOrNameOrExternalId) {
    
    final var ctx = new AnyTreeContainerContextImpl(init.getObjects());
    final var container = new AnyTreeContainerImpl(ctx);
    final var log = container.accept(new OrgPartyLogVisitor(groupIdOrNameOrExternalId, true));
    final OrgPartyHierarchy group = container.accept(new PartyHierarchyContainerVisitor(groupIdOrNameOrExternalId)).withLog(log);
    
    
    return ImmutableQueryEnvelope.<OrgPartyHierarchy>builder()
        .objects(group)
        .repo(init.getRepo())
        .status(QueryEnvelope.QueryEnvelopeStatus.OK)
        .build();
  }
}
