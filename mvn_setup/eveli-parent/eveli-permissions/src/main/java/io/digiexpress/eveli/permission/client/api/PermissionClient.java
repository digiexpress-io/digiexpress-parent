package io.digiexpress.eveli.permission.client.api;

/*-
 * #%L
 * eveli-permissions
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.List;
import java.util.Optional;

import io.digiexpress.eveli.permission.client.api.model.Principal;
import io.digiexpress.eveli.permission.client.api.model.PermissionCommand.CreatePermission;
import io.digiexpress.eveli.permission.client.api.model.PermissionCommand.PermissionUpdateCommand;
import io.digiexpress.eveli.permission.client.api.model.Principal.Permission;
import io.digiexpress.eveli.permission.client.api.model.Principal.Role;
import io.digiexpress.eveli.permission.client.api.model.PrincipalCommand.CreatePrincipal;
import io.digiexpress.eveli.permission.client.api.model.PrincipalCommand.PrincipalUpdateCommand;
import io.digiexpress.eveli.permission.client.api.model.RoleCommand.CreateRole;
import io.digiexpress.eveli.permission.client.api.model.RoleCommand.RoleUpdateCommand;
import io.resys.thena.api.entities.Tenant;
import io.smallrye.mutiny.Uni;


public interface PermissionClient {
  PermissionTenantQuery tenantQuery();
  Uni<Tenant> getTenant();
  PermissionClient withRepoId(String repoId);

  CreatePermissionAction createPermission();
  CreateRoleAction createRole();
  CreatePrincipalAction createPrincipal();

  UpdatePermissionAction updatePermission();
  UpdatePrincipalAction updatePrincipal();
  UpdateRoleAction updateRole();
  
  PermissionQuery permissionQuery();
  RoleQuery roleQuery();
  PrincipalQuery principalQuery();
  
  
  interface CreatePermissionAction {
    CreatePermissionAction evalAccess(PermissionAccessEvaluator eval);
    Uni<Permission> createOne(CreatePermission command);
  }
  
  interface UpdatePermissionAction {
    UpdatePermissionAction evalAccess(PermissionAccessEvaluator eval);
    Uni<Permission> updateOne(PermissionUpdateCommand command);
    Uni<Permission> updateOne(List<PermissionUpdateCommand> commands);
  }
  
  interface CreateRoleAction {
    CreateRoleAction evalAccess(RoleAccessEvaluator eval);
    Uni<Role> createOne(CreateRole command);
  }
  
  interface UpdateRoleAction {
    UpdateRoleAction evalAccess(RoleAccessEvaluator eval);
    Uni<Role> updateOne(RoleUpdateCommand command);
    Uni<Role> updateOne(List<RoleUpdateCommand> commands);
  }
  
  interface CreatePrincipalAction {
    CreatePrincipalAction evalAccess(PrincipalAccessEvaluator eval);
    Uni<Principal> createOne(CreatePrincipal command);
  }
  
  interface UpdatePrincipalAction {
    UpdatePrincipalAction evalAccess(PrincipalAccessEvaluator eval);
    Uni<Principal> updateOne(PrincipalUpdateCommand command);
    Uni<Principal> updateOne(List<PrincipalUpdateCommand> commands);
  }
  
  interface PermissionQuery {
    PermissionQuery evalAccess(PermissionAccessEvaluator eval);
    Uni<Permission> get(String permissionId);    
    Uni<List<Permission>> findAllPermissions();
  }
  
  interface RoleQuery {
    RoleQuery evalAccess(RoleAccessEvaluator eval);
    Uni<Role> get(String roleId);  
    Uni<List<Role>> findAllRoles();
  }
  
  interface PrincipalQuery {
    PrincipalQuery evalAccess(PrincipalAccessEvaluator eval);
    Uni<Principal> get(String principalId);  
    Uni<List<Principal>> findAllPrincipals();
  }
  
  interface PermissionTenantQuery {
    PermissionTenantQuery repoName(String repoName);
    PermissionClient build();

    Uni<PermissionClient> deleteAll();
    Uni<PermissionClient> delete();
    Uni<PermissionClient> create();
    Uni<PermissionClient> createIfNot();
    
    Uni<Optional<PermissionClient>> get();
  }
  
  interface PrincipalAccessEvaluator {
    boolean evaluate(Principal principal);
  }
  interface RoleAccessEvaluator {
    boolean evaluate(Role role);
  }
  interface PermissionAccessEvaluator {
    boolean evaluate(Permission permission);
  }
  

  class RoleNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 5706579544456750293L;

    public RoleNotFoundException(String message) {
      super(message);
    }
  }
  
  class PrincipalNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 5706579544456750293L;

    public PrincipalNotFoundException(String message) {
      super(message);
    }
  }
}
