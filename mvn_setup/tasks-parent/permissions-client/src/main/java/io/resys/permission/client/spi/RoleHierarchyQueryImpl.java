package io.resys.permission.client.spi;

import io.resys.permission.client.api.PermissionClient.RoleHierarchyQuery;
import io.resys.permission.client.api.model.RoleHierarchyContainer;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RoleHierarchyQueryImpl implements RoleHierarchyQuery {
  private final PermissionStore ctx;

  @Override
  public Uni<RoleHierarchyContainer> get(String roleId) {
    final var repoId = ctx.getConfig().getRepoId();
    final Uni<QueryEnvelope<RoleHierarchyContainer>> roleHierarchy = ctx.getOrg(repoId)
        .find()
        .rightHierarchyQuery()
        .get(roleId, new RoleHierarchyQueryVisitor(roleId));
    
    return roleHierarchy.onItem().transform((response) -> {
      if(response.getStatus() != QueryEnvelopeStatus.OK) {
        final var msg = "failed to find role hierarchy by id = '%s'!".formatted(roleId);
        final var exception = new RoleHierarchyQueryException(msg);
        
        response.getMessages()
          .forEach((e) -> {
            if(e.getException() != null) {
              exception.addSuppressed(e.getException());
            }
          });
        log.error(msg);
        throw exception;
      }
      return response.getObjects();
    });  
  }

  public static class RoleHierarchyQueryException extends RuntimeException {
    private static final long serialVersionUID = 4727517899929638306L;
    
    public RoleHierarchyQueryException(String message) {
      super(message);
    }
  }
  
}
