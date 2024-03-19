package io.resys.thena.docdb.models.org.queries;

import java.util.ArrayList;
import java.util.Collections;

import io.resys.thena.docdb.api.actions.OrgQueryActions.RoleHierarchyQuery;
import io.resys.thena.docdb.api.models.ImmutableQueryEnvelope;
import io.resys.thena.docdb.api.models.ImmutableQueryEnvelopeList;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.QueryEnvelopeList;
import io.resys.thena.docdb.api.models.ThenaEnvelope.ThenaObjects;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgProjectObjects;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgRoleHierarchy;
import io.resys.thena.docdb.api.visitors.OrgTreeContainer.OrgAnyTreeContainerVisitor;
import io.resys.thena.docdb.models.org.anytree.AnyTreeContainerContextImpl;
import io.resys.thena.docdb.models.org.anytree.AnyTreeContainerImpl;
import io.resys.thena.docdb.models.org.anytree.RoleHierarchyContainerVisitor;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OrgRoleHierarchyQueryImpl implements RoleHierarchyQuery {
  private final DbState state;
  private String repoId;
  
  @Override
  public RoleHierarchyQuery repoId(String repoId) {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    this.repoId = repoId;
    return this;
  }
  @Override
  public Uni<QueryEnvelope<OrgRoleHierarchy>> get(String roleIdOrNameOrExternalId) {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    
    return new OrgProjectQueryImpl(state).projectName(repoId).get()
        .onItem().transform(resp -> {
          if(resp.getStatus() != QueryEnvelopeStatus.OK) {
            return resp.toType();
          }
          
          try {
            final QueryEnvelope<OrgRoleHierarchy> success = createRoleHierarchy(resp, roleIdOrNameOrExternalId);
            return success;
          } catch(Exception e) {
            return QueryEnvelope.fatalError(resp.getRepo(), "Failed to build hierarchy for all roles", log, e);
          }
        });
  }
  @Override
  public Uni<QueryEnvelopeList<OrgRoleHierarchy>> findAll() {
    return new OrgProjectQueryImpl(state).projectName(repoId).get()
        .onItem().transform(resp -> {
          if(resp.getStatus() != QueryEnvelopeStatus.OK) {
            return resp.toListOfType();
          }
          
          try {
            final QueryEnvelopeList<OrgRoleHierarchy> success = createRoleHierarchy(resp);
            return success;
          } catch(Exception e) {
            final QueryEnvelope<OrgRoleHierarchy> fail = QueryEnvelope.fatalError(resp.getRepo(), "Failed to build hierarchy for all roles", log, e);
            return fail.toList();
          }
        });
  }

  private QueryEnvelopeList<OrgRoleHierarchy> createRoleHierarchy(QueryEnvelope<OrgProjectObjects> init) {
    final var result = new ArrayList<OrgRoleHierarchy>();
    final var ctx = new AnyTreeContainerContextImpl(init.getObjects());
    final var container = new AnyTreeContainerImpl(ctx);
    
    for(final var roleCriteria : init.getObjects().getRoles().values().stream().sorted((a, b) -> a.getRightName().compareTo(b.getRightName())).toList()) {
      final OrgRoleHierarchy roleHierarchy = container.accept(new RoleHierarchyContainerVisitor(roleCriteria.getId()));
      result.add(roleHierarchy);
    }
    return ImmutableQueryEnvelopeList.<OrgRoleHierarchy>builder()
        .objects(Collections.unmodifiableList(result))
        .repo(init.getRepo())
        .status(QueryEnvelope.QueryEnvelopeStatus.OK)
        .build();
  }
  
  private QueryEnvelope<OrgRoleHierarchy> createRoleHierarchy(
      QueryEnvelope<OrgProjectObjects> init, 
      String roleIdOrNameOrExternalId) {
    
    final var ctx = new AnyTreeContainerContextImpl(init.getObjects());
    final OrgRoleHierarchy group = new AnyTreeContainerImpl(ctx).accept(new RoleHierarchyContainerVisitor(roleIdOrNameOrExternalId));
    return ImmutableQueryEnvelope.<OrgRoleHierarchy>builder()
        .objects(group)
        .repo(init.getRepo())
        .status(QueryEnvelope.QueryEnvelopeStatus.OK)
        .build();
  }
  @Override
  public <T extends ThenaObjects> Uni<QueryEnvelope<T>> get(String roleIdOrNameOrExternalId,
      OrgAnyTreeContainerVisitor<T> visitor) {
    
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    
    return new OrgProjectQueryImpl(state).projectName(repoId).get()
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
