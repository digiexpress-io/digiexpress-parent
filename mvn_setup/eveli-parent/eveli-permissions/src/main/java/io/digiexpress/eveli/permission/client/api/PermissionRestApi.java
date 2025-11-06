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

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

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

@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public interface PermissionRestApi {
  
  @GetMapping("/principals")
  Uni<List<Principal>> findAllPrincipals();
  
  @GetMapping("/principals/{principalId}")
  Uni<Principal> getPrincipalById(@PathVariable("principalId") String principalId);
  
  @PutMapping(path = "/principals/{principalId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  Uni<Principal> updatePrincipal(@PathVariable("principalId") String principalId, @RequestBody List<PrincipalUpdateCommand> commands);
  
  @PostMapping(path = "/principals", consumes = MediaType.APPLICATION_JSON_VALUE)
  Uni<Principal> createPrincipal(@RequestBody CreatePrincipal command);
  
  @GetMapping("/permissions")
  Uni<List<Permission>> findAllPermissions();
  
  @PostMapping(path = "/permissions", consumes = MediaType.APPLICATION_JSON_VALUE)
  Uni<Permission> createPermission(@RequestBody CreatePermission command);
  
  @GetMapping("/permissions/{permissionId}")
  Uni<Permission> getPermissionById(@PathVariable("permissionId") String permissionId);
  
  @PutMapping(path = "/permissions/{permissionId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  Uni<Permission> updatePermission(@PathVariable("permissionId") String permissionId, @RequestBody List<PermissionUpdateCommand> commands);
  
  @GetMapping("/roles")
  Uni<List<Role>> findAllRoles();
  
  @PostMapping("/roles")
  Uni<Role> createRole(@RequestBody CreateRole command);
  
  @PutMapping(path = "/roles/{roleId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  Uni<Role> updateRole(@PathVariable("roleId") String roleId, @RequestBody List<RoleUpdateCommand> commands);
  
  @GetMapping("/roles/{roleId}")
  Uni<Role> getRoleById(@PathVariable("roleId") String roleId);
  
}