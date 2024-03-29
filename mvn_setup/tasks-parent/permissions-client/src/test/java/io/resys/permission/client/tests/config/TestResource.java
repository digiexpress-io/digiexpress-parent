package io.resys.permission.client.tests.config;

import java.util.Arrays;
import java.util.List;

import io.resys.permission.client.api.model.ImmutablePermission;
import io.resys.permission.client.api.model.ImmutableRole;
import io.resys.permission.client.api.model.PermissionCommand.CreatePermission;
import io.resys.permission.client.api.model.PermissionCommand.PermissionUpdateCommand;
import io.resys.permission.client.api.model.Principal;
import io.resys.permission.client.api.model.Principal.Permission;
import io.resys.permission.client.api.model.Principal.Role;
import io.resys.permission.client.api.model.PrincipalCommand.PrincipalUpdateCommand;
import io.resys.permission.client.api.model.RoleCommand.CreateRole;
import io.resys.permission.client.api.model.RoleCommand.RoleUpdateCommand;
import io.resys.permission.client.rest.PermissionRestApi;
import io.resys.thena.api.entities.org.OrgActorStatus;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Path;

@Path("q/digiexpress/api")
@ApplicationScoped
public class TestResource implements PermissionRestApi {

  private final Permission permission = ImmutablePermission.builder()
      .name("test-permission")
      .version("1")
      .description("desc")
      .status(OrgActorStatus.OrgActorStatusType.IN_FORCE)
      .id("permissionId-1")
      .build();
  
  private final Role role = ImmutableRole.builder()
      .name("test-role")
      .version("1")
      .description("role desc")
      .parentId(null)
      .status(OrgActorStatus.OrgActorStatusType.IN_FORCE)
      .id("roleId-1")
      .build();
  
  @Override
  public Uni<List<Principal>> findAllPrincipals() {
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
    return Uni.createFrom().item(Arrays.asList(permission));
  }

  @Override
  public Uni<Permission> createPermission(CreatePermission command) {
    return Uni.createFrom().item(permission);
  }

  @Override
  public Uni<Permission> getPermissionById(String permissionId) {
    return Uni.createFrom().item(permission);
  }

  @Override
  public Uni<Permission> updatePermission(String permissionId, List<PermissionUpdateCommand> commands) {
    return Uni.createFrom().item(permission);
  }

  @Override
  public Uni<Role> createRole(CreateRole command) {
    return Uni.createFrom().item(role);
  }

  @Override
  public Uni<Role> updateRole(String roleId, List<RoleUpdateCommand> commands) {
    return Uni.createFrom().item(role);
  }

  @Override
  public Uni<Role> getRoleById(String roleId) {
    return Uni.createFrom().item(role);
  }
  @Override
  public Uni<List<Role>> findAllRoles() {
    return Uni.createFrom().item(Arrays.asList(role));
  }

}
