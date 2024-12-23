package io.resys.thena.tasks.dev.app;


import java.util.List;

import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.PermissionRestApi;
import io.resys.permission.client.api.model.PermissionCommand.CreatePermission;
import io.resys.permission.client.api.model.PermissionCommand.PermissionUpdateCommand;
import io.resys.permission.client.api.model.Principal;
import io.resys.permission.client.api.model.Principal.Permission;
import io.resys.permission.client.api.model.Principal.Role;
import io.resys.permission.client.api.model.PrincipalCommand.CreatePrincipal;
import io.resys.permission.client.api.model.PrincipalCommand.PrincipalUpdateCommand;
import io.resys.permission.client.api.model.RoleCommand.CreateRole;
import io.resys.permission.client.api.model.RoleCommand.RoleUpdateCommand;
import io.resys.thena.projects.client.api.ProjectClient;
import io.resys.thena.projects.client.api.TenantConfig.TenantRepoConfigType;
import io.resys.thena.tasks.dev.app.security.IdentitySupplier;
import io.resys.thena.tasks.dev.app.user.CurrentTenant;
import io.resys.thena.tasks.dev.app.user.CurrentUser;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("q/digiexpress/api/am")
@Singleton
public class PermissionsResource implements PermissionRestApi {

  @Inject private PermissionClient permissions;
  @Inject private CurrentTenant currentTenant;
  @Inject private CurrentUser currentUser;
  @Inject private ProjectClient tenantClient;
  @Inject private IdentitySupplier cache;

  @Override
  public Uni<List<Principal>> findAllPrincipals() {
    return getClient().onItem().transformToUni(client -> client.principalQuery().findAllPrincipals());
  }

  @Override
  public Uni<Principal> createPrincipal(CreatePrincipal command) {
    return getClient().onItem().transformToUni(client -> client.createPrincipal().createOne(command))
        .onItem().transformToUni(this::invalidateCache);
  }
  
  @Override
  public Uni<Principal> getPrincipalById(String principalId) {
    return getClient().onItem().transformToUni(client -> client.principalQuery().get(principalId));
  }

  @Override
  public Uni<Principal> updatePrincipal(String principalId, List<PrincipalUpdateCommand> commands) {
    return getClient().onItem().transformToUni(client -> client.updatePrincipal().updateOne(commands))
        .onItem().transformToUni(this::invalidateCache);
  }

  @Override
  public Uni<List<Permission>> findAllPermissions() {
    return getClient().onItem().transformToUni(client -> client.permissionQuery().findAllPermissions());
  }

  @Override
  public Uni<Permission> createPermission(CreatePermission command) {
    return getClient().onItem().transformToUni(client -> client.createPermission().createOne(command))
        .onItem().transformToUni(this::invalidateCache);
  }

  @Override
  public Uni<Permission> getPermissionById(String permissionId) {
    return getClient().onItem().transformToUni(client -> client.permissionQuery().get(permissionId));
  }

  @Override
  public Uni<Permission> updatePermission(String permissionId, List<PermissionUpdateCommand> commands) {
    return getClient().onItem().transformToUni(client -> client.updatePermission().updateOne(commands))
        .onItem().transformToUni(this::invalidateCache);
  }

  @Override
  public Uni<List<Role>> findAllRoles() {
    return getClient().onItem().transformToUni(client -> client.roleQuery().findAllRoles());
  }

  @Override
  public Uni<Role> createRole(CreateRole role) {
    return getClient().onItem().transformToUni(client -> client.createRole().createOne(role))
        .onItem().transformToUni(this::invalidateCache);
  }

  @Override
  public Uni<Role> updateRole(String roleId, List<RoleUpdateCommand> commands) {
    return getClient().onItem().transformToUni(client -> client.updateRole().updateOne(commands))
        .onItem().transformToUni(this::invalidateCache);
  }

  @Override
  public Uni<Role> getRoleById(String roleId) {
    return getClient().onItem().transformToUni(client -> client.roleQuery().get(roleId));
  }
  
  private Uni<PermissionClient> getClient() {
    return tenantClient.queryActiveTenantConfig().get(currentTenant.tenantId())
        .onItem().transform(config -> config.getRepoConfig(TenantRepoConfigType.PERMISSIONS)) 
        .onItem().transform(config -> permissions.withRepoId(config.getRepoId()));
  }
  
  private <T> Uni<T> invalidateCache(T data) {
    return cache.invalidate()
        .onFailure().transform(e -> {
          log.error("Failed to flush the cache for permissions, {}", e.getMessage(), e);
          return e;
        })
        .onItem().transform(junk -> data); 
  }
}
