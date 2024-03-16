package io.resys.permission.client.api;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.resys.permission.client.api.model.PermissionCommand.CreatePermission;
import io.resys.permission.client.api.model.PermissionCommand.PermissionUpdateCommand;
import io.resys.permission.client.api.model.Principal;
import io.resys.permission.client.api.model.Principal.Permission;
import io.resys.permission.client.api.model.Principal.Role;
import io.resys.permission.client.api.model.PrincipalCommand.PrincipalUpdateCommand;
import io.resys.permission.client.api.model.RoleCommand.CreateRole;
import io.resys.permission.client.api.model.RoleCommand.RoleUpdateCommand;
import io.resys.permission.client.api.model.RoleHierarchyContainer;
import io.resys.thena.docdb.api.models.Repo;
import io.smallrye.mutiny.Uni;


public interface PermissionClient {
  RepositoryQuery repoQuery();
  Uni<Repo> getRepo();
  PermissionClient withRepoId(String repoId);

  CreatePermissionAction createPermission();
  CreateRoleAction createRole();

  UpdatePermissionAction updatePermission();
  UpdatePrincipalAction updatePrincipal();
  UpdateRoleAction updateRole();
  
  PermissionQuery permissionQuery();
  RoleQuery roleQuery();
  PrincipalQuery principalQuery();
  RoleHierarchyQuery roleHierarchyQuery();
  
  
  interface CreatePermissionAction {
    Uni<Permission> createOne(CreatePermission command);
    Uni<List<Permission>> createMany(List<? extends CreatePermission> commands);
  }
  
  interface UpdatePermissionAction {
    Uni<Permission> updateOne(PermissionUpdateCommand command);
    Uni<Permission> updateOne(List<PermissionUpdateCommand> commands);
    Uni<List<Permission>> updateMany(List<? extends PermissionUpdateCommand> commands);
  }
  
  interface CreateRoleAction {
    Uni<Role> createOne(CreateRole command);
    Uni<List<Role>> createMany(List<? extends CreateRole> commands);
  }
  
  interface UpdateRoleAction {
    Uni<Role> updateOne(RoleUpdateCommand command);
    Uni<Role> updateOne(List<RoleUpdateCommand> commands);
    Uni<List<Role>> updateMany(List<? extends RoleUpdateCommand> commands);
  }
  
  interface UpdatePrincipalAction {
    Uni<Principal> updateOne(PrincipalUpdateCommand command);
    Uni<Principal> updateOne(List<PrincipalUpdateCommand> commands);
    Uni<List<Principal>> updateMany(List<? extends PrincipalUpdateCommand> commands);
  }
  
  interface PermissionQuery {
    Uni<Permission> get(String permissionId);    
    Uni<List<Permission>> findAllPermissions();
    Uni<List<Permission>> findPermissionsByIds(Collection<String> permissionIds);
  }
  
  interface RoleQuery {
    Uni<Role> get(String roleId);  
    Uni<List<Role>> findAllRoles();
    Uni<List<Role>> findRolesByIds(Collection<String> roleIds);
  }
  
  interface PrincipalQuery {
    Uni<Principal> get(String principalId);  
    Uni<List<Principal>> findAllPrincipals();
    Uni<List<Principal>> findPrincipalsByIds(Collection<String> principalIds);
  }
  
  interface RoleHierarchyQuery {
    Uni<RoleHierarchyContainer> getRoleHierarchy();
  }
  
  public interface RepositoryQuery {
    RepositoryQuery repoName(String repoName);
    PermissionClient build();

    Uni<PermissionClient> deleteAll();
    Uni<PermissionClient> delete();
    Uni<PermissionClient> create();
    Uni<PermissionClient> createIfNot();
    
    Uni<Optional<PermissionClient>> get(String repoId);
  } 
}

