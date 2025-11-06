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
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.digiexpress.eveli.permission.client.api.PermissionClient;
import io.digiexpress.eveli.permission.client.api.model.ChangeType;
import io.digiexpress.eveli.permission.client.api.model.ImmutableChangeRolePermissions;
import io.digiexpress.eveli.permission.client.api.model.ImmutableChangeRolePrincipals;
import io.digiexpress.eveli.permission.client.api.model.ImmutableCreatePermission;
import io.digiexpress.eveli.permission.client.api.model.ImmutableCreatePrincipal;
import io.digiexpress.eveli.permission.client.api.model.ImmutableCreateRole;
import io.digiexpress.eveli.permission.client.api.model.Principal;
import io.digiexpress.eveli.permission.client.api.model.Principal.Permission;
import io.digiexpress.eveli.permission.client.api.model.Principal.Role;
import io.digiexpress.eveli.permission.client.tests.config.DbTestTemplate;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RoleSetAllTest extends DbTestTemplate {

  private Principal createPrincipal(PermissionClient client, String name) {
    
    return client.createPrincipal().createOne(ImmutableCreatePrincipal.builder()
        .name(name)
        .email("the-rock@muscles.org")
        .comment("created new user")
        .build())
        .await().atMost(Duration.ofMinutes(5));
  }
  
  private Permission createPermission(PermissionClient client, String name) {
    return client.createPermission().createOne(ImmutableCreatePermission.builder()
        .name(name)
        .description("desc of permission")
        .comment("created a new permission")
        .build())
        .await().atMost(Duration.ofMinutes(1));
}
  
  private Role createRole(PermissionClient client, String name) {
    
    return client.createRole().createOne(ImmutableCreateRole.builder()
        .name(name)
        .description("desc of role")
        .addPermissions(
            createPermission(client, "perm-abc").getName()
            )
        .comment("Created this for testing")
        .build())
        .await().atMost(Duration.ofMinutes(1)); 
  }
  
  @Test
  public void createRoleSetAllTest() {
    
    final PermissionClient client = getClient().tenantQuery()
        .repoName("RoleSetAllTest-1")
        .create()
        .await().atMost(Duration.ofMinutes(1));
    
      
    final var role1 = createRole(client, "roleForSetAll");
    
    final var updatePermissions = ImmutableChangeRolePermissions.builder()
        .id(role1.getId())
        .comment("added all perms")
        .changeType(ChangeType.SET_ALL)
        .permissions(Arrays.asList( 
            createPermission(client, "perm1").getName(),
            createPermission(client, "perm2").getName(),
            createPermission(client, "perm3").getName())
            )
        .build();
    
    final var updatePrincipals = ImmutableChangeRolePrincipals.builder()
        .id(role1.getId())
        .comment("added all principals")
        .changeType(ChangeType.SET_ALL)
        .principals(Arrays.asList(
            createPrincipal(client, "AmySmith").getName(),
            createPrincipal(client, "JohnDoe").getName(),
            createPrincipal(client, "CommanderONeil").getName())
            )
        .build();
    
    final var updatedRole1 = client.updateRole().updateOne(Arrays.asList(
        updatePermissions,
        updatePrincipals
    )).await().atMost(Duration.ofMinutes(1));

    
    log.debug(Json.encodePrettily(updatedRole1));
    
    Assertions.assertEquals("[perm1, perm2, perm3]", updatedRole1.getPermissions().toString());
    Assertions.assertEquals("[AmySmith, JohnDoe, CommanderONeil]", updatedRole1.getPrincipals().toString());
    
    Assertions.assertEquals("[perm1, perm2, perm3]", client.roleQuery().get(updatedRole1.getId()).await().atMost(Duration.ofMinutes(1)).getPermissions().toString());
    Assertions.assertEquals("[AmySmith, JohnDoe, CommanderONeil]", client.roleQuery().get(updatedRole1.getId()).await().atMost(Duration.ofMinutes(1)).getPrincipals().toString());
  }
}
