package io.digiexpress.eveli.permission.client.tests.config;

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

import java.util.Arrays;
import java.util.List;

import io.digiexpress.eveli.permission.client.api.PermissionRestApi;
import io.digiexpress.eveli.permission.client.api.model.ImmutablePermission;
import io.digiexpress.eveli.permission.client.api.model.ImmutablePrincipal;
import io.digiexpress.eveli.permission.client.api.model.ImmutableRole;
import io.digiexpress.eveli.permission.client.api.model.Principal;
import io.digiexpress.eveli.permission.client.api.model.PermissionCommand.CreatePermission;
import io.digiexpress.eveli.permission.client.api.model.PermissionCommand.PermissionUpdateCommand;
import io.digiexpress.eveli.permission.client.api.model.Principal.Permission;
import io.digiexpress.eveli.permission.client.api.model.Principal.Role;
import io.digiexpress.eveli.permission.client.api.model.PrincipalCommand.CreatePrincipal;
import io.digiexpress.eveli.permission.client.api.model.PrincipalCommand.PrincipalUpdateCommand;
import io.digiexpress.eveli.permission.client.api.model.RoleCommand.CreateRole;
import io.digiexpress.eveli.permission.client.api.model.RoleCommand.RoleUpdateCommand;
import io.resys.thena.api.entities.org.OrgActorStatusType;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Path;

@Path("q/digiexpress/api")
@ApplicationScoped
public class TestResource implements PermissionRestApi {

  private final Permission permission = ImmutablePermission.builder()
      .id("permissionId-1")
      .version("1")
      .name("test-permission")
      .description("desc")
      .status(OrgActorStatusType.IN_FORCE)
      .build();
  
  private final Role role = ImmutableRole.builder()
      .id("roleId-1")
      .version("1")
      .name("test-role")
      .description("role desc")
      .parentId(null)
      .status(OrgActorStatusType.IN_FORCE)
      .build();
  
  private final Principal principal = ImmutablePrincipal.builder()
      .id("principalId-1")
      .version("1")
      .name("Thomas McShane")
      .email("t.mcshane@email.com")
      .status(OrgActorStatusType.IN_FORCE)
      .build();
  
  @Override
  public Uni<List<Principal>> findAllPrincipals() {
    return Uni.createFrom().item(Arrays.asList(principal));
  }
  @Override
  public Uni<Principal> getPrincipalById(String principalId) {
    return Uni.createFrom().item(principal);
  }

  @Override
  public Uni<Principal> updatePrincipal(String principalId, List<PrincipalUpdateCommand> commands) {
    return Uni.createFrom().item(principal);
  }
  
  @Override
  public Uni<Principal> createPrincipal(CreatePrincipal command) {
    return Uni.createFrom().item(principal);
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
