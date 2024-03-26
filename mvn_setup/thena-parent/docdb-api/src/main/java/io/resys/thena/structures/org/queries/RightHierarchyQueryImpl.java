package io.resys.thena.structures.org.queries;

import java.util.ArrayList;
import java.util.Collections;

import io.resys.thena.api.actions.OrgQueryActions.RightHierarchyQuery;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgProjectObjects;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgRightHierarchy;
import io.resys.thena.api.envelope.ImmutableQueryEnvelope;
import io.resys.thena.api.envelope.ImmutableQueryEnvelopeList;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.resys.thena.api.envelope.ThenaContainer;
import io.resys.thena.api.envelope.OrgTreeContainer.OrgAnyTreeContainerVisitor;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.spi.DbState;
import io.resys.thena.structures.org.anytree.AnyTreeContainerContextImpl;
import io.resys.thena.structures.org.anytree.AnyTreeContainerImpl;
import io.resys.thena.structures.org.anytree.RightHierarchyContainerVisitor;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RightHierarchyQueryImpl implements RightHierarchyQuery {
  private final DbState state;
  private final String repoId;
  
  @Override
  public Uni<QueryEnvelope<OrgRightHierarchy>> get(String roleIdOrNameOrExternalId) {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    
    return new OrgProjectQueryImpl(state, repoId).get()
        .onItem().transform(resp -> {
          if(resp.getStatus() != QueryEnvelopeStatus.OK) {
            return resp.toType();
          }
          
          try {
            final QueryEnvelope<OrgRightHierarchy> success = createRoleHierarchy(resp, roleIdOrNameOrExternalId);
            return success;
          } catch(Exception e) {
            return QueryEnvelope.fatalError(resp.getRepo(), "Failed to build hierarchy for all roles", log, e);
          }
        });
  }
  @Override
  public Uni<QueryEnvelopeList<OrgRightHierarchy>> findAll() {
    return new OrgProjectQueryImpl(state, repoId).get()
        .onItem().transform(resp -> {
          if(resp.getStatus() != QueryEnvelopeStatus.OK) {
            return resp.toListOfType();
          }
          
          try {
            final QueryEnvelopeList<OrgRightHierarchy> success = createRoleHierarchy(resp);
            return success;
          } catch(Exception e) {
            final QueryEnvelope<OrgRightHierarchy> fail = QueryEnvelope.fatalError(resp.getRepo(), "Failed to build hierarchy for all roles", log, e);
            return fail.toList();
          }
        });
  }

  private QueryEnvelopeList<OrgRightHierarchy> createRoleHierarchy(QueryEnvelope<OrgProjectObjects> init) {
    final var result = new ArrayList<OrgRightHierarchy>();
    final var ctx = new AnyTreeContainerContextImpl(init.getObjects());
    final var container = new AnyTreeContainerImpl(ctx);
    
    for(final var roleCriteria : init.getObjects().getRights().values().stream().sorted((a, b) -> a.getRightName().compareTo(b.getRightName())).toList()) {
      final OrgRightHierarchy roleHierarchy = container.accept(new RightHierarchyContainerVisitor(roleCriteria.getId()));
      result.add(roleHierarchy);
    }
    return ImmutableQueryEnvelopeList.<OrgRightHierarchy>builder()
        .objects(Collections.unmodifiableList(result))
        .repo(init.getRepo())
        .status(QueryEnvelope.QueryEnvelopeStatus.OK)
        .build();
  }
  
  private QueryEnvelope<OrgRightHierarchy> createRoleHierarchy(
      QueryEnvelope<OrgProjectObjects> init, 
      String roleIdOrNameOrExternalId) {
    
    final var ctx = new AnyTreeContainerContextImpl(init.getObjects());
    final OrgRightHierarchy group = new AnyTreeContainerImpl(ctx).accept(new RightHierarchyContainerVisitor(roleIdOrNameOrExternalId));
    return ImmutableQueryEnvelope.<OrgRightHierarchy>builder()
        .objects(group)
        .repo(init.getRepo())
        .status(QueryEnvelope.QueryEnvelopeStatus.OK)
        .build();
  }
  @Override
  public <T extends ThenaContainer> Uni<QueryEnvelope<T>> get(String roleIdOrNameOrExternalId,
      OrgAnyTreeContainerVisitor<T> visitor) {
    
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    
    return new OrgProjectQueryImpl(state, repoId).get()
        .onItem().transform(resp -> {
          if(resp.getStatus() != QueryEnvelopeStatus.OK) {
            return resp.toType();
          }
          
          try {
            final var ctx = new AnyTreeContainerContextImpl(resp.getObjects());
            final T group = new AnyTreeContainerImpl(ctx).accept(visitor);
            return ImmutableQueryEnvelope.<T>builder()
                .objects(group)
                .repo(resp.getRepo())
                .status(QueryEnvelope.QueryEnvelopeStatus.OK)
                .build();
          } catch(Exception e) {
            return QueryEnvelope.fatalError(resp.getRepo(), "Failed to build hierarchy for all roles", log, e);
          }
        });
  }
}
