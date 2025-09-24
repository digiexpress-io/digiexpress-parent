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

import io.digiexpress.eveli.permission.client.api.model.Principal;
import io.digiexpress.eveli.permission.client.api.model.PermissionCommand.CreatePermission;
import io.digiexpress.eveli.permission.client.api.model.PermissionCommand.PermissionUpdateCommand;
import io.digiexpress.eveli.permission.client.api.model.Principal.Permission;
import io.digiexpress.eveli.permission.client.api.model.Principal.Role;
import io.digiexpress.eveli.permission.client.api.model.PrincipalCommand.CreatePrincipal;
import io.digiexpress.eveli.permission.client.api.model.PrincipalCommand.PrincipalUpdateCommand;
import io.digiexpress.eveli.permission.client.api.model.RoleCommand.CreateRole;
import io.digiexpress.eveli.permission.client.api.model.RoleCommand.RoleUpdateCommand;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class PermissionsResource implements PermissionRestApi {

  private final PermissionClient permissions;

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
  
  // 
  protected Uni<PermissionClient> getClient() {
    return Uni.createFrom().item(permissions);
  }
  
  protected <T> Uni<T> invalidateCache(T data) {
    // cache not implemented
    return Uni.createFrom().item(data); 
  }
}
