package io.resys.permission.client.spi;

import java.util.List;

import io.resys.permission.client.api.PermissionClient.RoleQuery;
import io.resys.permission.client.api.model.ImmutableRole;
import io.resys.permission.client.api.model.Principal.Role;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.QueryEnvelopeList;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgRoleHierarchy;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RoleQueryImpl implements RoleQuery {
  
  private final PermissionStore ctx;

  @Override
  public Uni<Role> get(String roleId) {
    final var repoId = ctx.getConfig().getRepoId();
    final Uni<QueryEnvelope<OrgRoleHierarchy>> role = ctx.getOrg().find().roleHierarchyQuery().repoId(repoId).get(roleId);
    
    return role.onItem().transform((response) -> {
      if(response.getStatus() != QueryEnvelopeStatus.OK) {
        final var msg = "failed to get role by id = '%s'!".formatted(roleId);
        final var exception = new RoleQueryException(msg);
        
        response.getMessages()
          .forEach((e) -> {
            if(e.getException() != null) {
              exception.addSuppressed(e.getException());
            }
          });
        log.error(msg);
        throw exception;
      }
      return mapTo(response.getObjects());
    }) ;
  }

  @Override
  public Uni<List<Role>> findAllRoles() {
    final var repoId = ctx.getConfig().getRepoId();
    final Uni<QueryEnvelopeList<OrgRoleHierarchy>> roles = ctx.getOrg().find().roleHierarchyQuery().repoId(repoId).findAll();
    
    return roles.onItem().transform((response) -> {
      if(response.getStatus() != QueryEnvelopeStatus.OK) {
        final var msg = "failed to find all roles!";
        final var exception = new RoleQueryException(msg);
        
        response.getMessages()
          .forEach((e) -> {
            if(e.getException() != null) {
              exception.addSuppressed(e.getException());
            }
          });
        log.error(msg);
        throw exception;
      }
      return response.getObjects().stream().map(this::mapTo).toList();
    });
  }
  
 private Role mapTo(OrgRoleHierarchy role) {
    
    return ImmutableRole.builder()
      .id(role.getRoleId())
      .version(role.getCommitId())
      .name(role.getRoleName())
      .description(role.getRoleDescription())
      .status(role.getStatus())
      .build();
  }
  
  public static class RoleQueryException extends RuntimeException {
    private static final long serialVersionUID = 4727517899929638306L;

    public RoleQueryException(String message) {
      super(message);
    }
  }

}
