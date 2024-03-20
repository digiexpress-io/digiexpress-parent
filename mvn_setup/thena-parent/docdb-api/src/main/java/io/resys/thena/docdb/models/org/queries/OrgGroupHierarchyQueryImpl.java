package io.resys.thena.docdb.models.org.queries;

import java.util.ArrayList;
import java.util.Collections;

import io.resys.thena.docdb.api.actions.OrgQueryActions.PartyHierarchyQuery;
import io.resys.thena.docdb.api.models.ImmutableQueryEnvelope;
import io.resys.thena.docdb.api.models.ImmutableQueryEnvelopeList;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.QueryEnvelopeList;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgPartyHierarchy;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgProjectObjects;
import io.resys.thena.docdb.api.visitors.OrgPartyLogVisitor;
import io.resys.thena.docdb.models.org.anytree.AnyTreeContainerContextImpl;
import io.resys.thena.docdb.models.org.anytree.AnyTreeContainerImpl;
import io.resys.thena.docdb.models.org.anytree.PartyHierarchyContainerVisitor;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OrgGroupHierarchyQueryImpl implements PartyHierarchyQuery {
  private final DbState state;
  private String repoId;
  
  @Override
  public PartyHierarchyQuery repoId(String repoId) {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    this.repoId = repoId;
    return this;
  }
  @Override
  public Uni<QueryEnvelope<OrgPartyHierarchy>> get(String groupIdOrNameOrExternalId) {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    
    return new OrgProjectQueryImpl(state).projectName(repoId).get()
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
    return new OrgProjectQueryImpl(state).projectName(repoId).get()
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
