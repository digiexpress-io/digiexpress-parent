package io.resys.thena.tasks.dev.app;


import java.util.List;

import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.model.PermissionCommand.CreatePermission;
import io.resys.permission.client.api.model.PermissionCommand.PermissionUpdateCommand;
import io.resys.permission.client.api.model.Principal;
import io.resys.permission.client.api.model.Principal.Permission;
import io.resys.permission.client.api.model.Principal.Role;
import io.resys.permission.client.api.model.PrincipalCommand.PrincipalUpdateCommand;
import io.resys.permission.client.api.model.RoleCommand.CreateRole;
import io.resys.permission.client.api.model.RoleCommand.RoleUpdateCommand;
import io.resys.permission.client.rest.PermissionRestApi;
import io.resys.thena.projects.client.api.TenantConfigClient;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;

@Path("q/digiexpress/api")
public class PermissionsResource implements PermissionRestApi {

  @Inject PermissionClient permissions;
  @Inject CurrentTenant currentTenant;
  @Inject CurrentUser currentUser;
  @Inject TenantConfigClient tenantClient;

  @Override
  public Uni<List<Principal>> findAllPrincipals() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uni<Principal> getPrincipalById(String principalId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uni<Principal> updatePrincipal(String principalId, List<PrincipalUpdateCommand> commands) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uni<List<Permission>> findAllPermissions() {
    return getClient().onItem().transformToUni(client -> client.permissionQuery().findAllPermissions());
  }

  @Override
  public Uni<Permission> createPermission(CreatePermission command) {
    return getClient().onItem().transformToUni(client -> client.createPermission().createOne(command));
  }

  @Override
  public Uni<Permission> getPermissionById(String permissionId) {
    return getClient().onItem().transformToUni(client -> client.permissionQuery().get(permissionId));
  }

  @Override
  public Uni<Permission> updatePermission(String permissionId, List<PermissionUpdateCommand> commands) {
    return getClient().onItem().transformToUni(client -> client.updatePermission().updateOne(commands));
  }

  @Override
  public Uni<List<Role>> findAllRoles() {
    return getClient().onItem().transformToUni(client -> client.roleQuery().findAllRoles());
  }

  @Override
  public Uni<Role> createRole(CreateRole role) {
    return getClient().onItem().transformToUni(client -> client.createRole().createOne(role));
  }

  @Override
  public Uni<Role> updateRole(String roleId, List<RoleUpdateCommand> commands) {
    return getClient().onItem().transformToUni(client -> client.updateRole().updateOne(commands));
  }

  @Override
  public Uni<Role> getRoleById(String roleId) {
    return getClient().onItem().transformToUni(client -> client.roleQuery().get(roleId));
  }
  
  private Uni<PermissionClient> getClient() {
    return getPermissionConfig().onItem().transform(config -> permissions.withRepoId(config.getRepoId()));
  }
  
  private Uni<TenantRepoConfig> getPermissionConfig() {
    return tenantClient.queryActiveTenantConfig().get(currentTenant.tenantId())
      .onItem().transform(config -> config.getRepoConfig(TenantRepoConfigType.PERMISSIONS));
  }

}
