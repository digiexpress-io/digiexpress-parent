package io.digiexpress.eveli.permission.client.tests;

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

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.digiexpress.eveli.permission.client.api.PermissionClient;
import io.digiexpress.eveli.permission.client.api.model.ImmutableCreatePermission;
import io.digiexpress.eveli.permission.client.api.model.ImmutableCreatePrincipal;
import io.digiexpress.eveli.permission.client.api.model.ImmutableCreateRole;
import io.digiexpress.eveli.permission.client.api.model.Principal.Permission;
import io.digiexpress.eveli.permission.client.api.model.Principal.Role;
import io.digiexpress.eveli.permission.client.tests.config.DbTestTemplate;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class PrincipalHierarchyTest extends DbTestTemplate {
  
  private Permission createPermission(PermissionClient client, String name) {
    
    return client.createPermission().createOne(ImmutableCreatePermission.builder()
        .comment("Needed for training purposes")
        .name(name)
        .description("Frontoffice resource viewing")
        .build())
        .await().atMost(Duration.ofMinutes(5));
  }
  
  private Role createRoleWithoutPermissions(PermissionClient client, String roleName) {
    
    return client.createRole().createOne(ImmutableCreateRole.builder()
        .comment("Trainee-group")
        .name(roleName)
        .description("View-only for interns")
        .build())
        .await().atMost(Duration.ofMinutes(5));
  }
  
  private Role createRoleWithPermissions(PermissionClient client, String roleName) {
    
    return client.createRole().createOne(ImmutableCreateRole.builder()
        .comment("Trainee-group")
        .name(roleName)
        .description("View-only for interns")
        .addPermissions(
            createPermission(client, "PERM-X").getName()
            )
        .build())
        .await().atMost(Duration.ofMinutes(5));
  }
  
  
  @Test
  public void createPrincipalAndUpdateTest() {
    final PermissionClient client = getClient().tenantQuery()
        .repoName("PrincipalHierarchyTest-1")
        .create()
        .await().atMost(Duration.ofMinutes(5));
 
  // Principal with 3 direct permissions, 1 inherited permission, 2 roles
  {
    final var principal = client.createPrincipal().createOne(ImmutableCreatePrincipal.builder()
        .name("Amanda Smith")
        .email("amanda.s@gmail.com")
        .addRoles(
            createRoleWithPermissions(client, "ROLE-1").getName(),
            createRoleWithoutPermissions(client, "ROLE-2").getName()
            )
        .addPermissions(
            createPermission(client, "PERM-1").getName(),
            createPermission(client, "PERM-2").getName(),
            createPermission(client, "PERM-3").getName()
            )
        .comment("principalHierarchyTest")
        .build())
        .await().atMost(Duration.ofMinutes(1));
    
    Assertions.assertEquals("Amanda Smith", principal.getName()); 
    Assertions.assertEquals(2, principal.getRoles().size());
    Assertions.assertEquals(3, principal.getPermissions().size()); // should be 4
    
    log.debug(Json.encodePrettily(principal));
    
    final var query = client.principalQuery().get(principal.getId()).await().atMost(Duration.ofMinutes(1));
    
    // query returns principal name
    Assertions.assertEquals("Amanda Smith", query.getName());  
    
    // query returns roles wherein the principal is a member
    Assertions.assertEquals("[ROLE-1, ROLE-2]", query.getDirectRoles().toString());
      
    // query returns roles wherein the principal is a member
    Assertions.assertEquals("[ROLE-1, ROLE-2]", query.getRoles().toString());
    
    // query returns all permissions of principal (directly assigned AND inherited from role wherein principal is member)
    Assertions.assertEquals("[PERM-1, PERM-2, PERM-3, PERM-X]", query.getPermissions().toString());
    
    // query returns permissions directly assigned to principal (no inherited)
    Assertions.assertEquals("[PERM-1, PERM-2, PERM-3]", query.getDirectPermissions().toString());
    
    }
  }
}
