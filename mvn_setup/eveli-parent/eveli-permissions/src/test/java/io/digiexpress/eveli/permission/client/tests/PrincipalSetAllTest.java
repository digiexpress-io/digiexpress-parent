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
import io.digiexpress.eveli.permission.client.api.model.ImmutableChangePrincipalPermissions;
import io.digiexpress.eveli.permission.client.api.model.ImmutableChangePrincipalRoles;
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
public class PrincipalSetAllTest extends DbTestTemplate {

 private Role createRole(PermissionClient client, String name) {
    
    return client.createRole().createOne(ImmutableCreateRole.builder()
        .name(name)
        .description("desc of role")
        .comment("Created this for testing")
        .build())
        .await().atMost(Duration.ofMinutes(1)); 
  }
  
  private Permission createPermission(PermissionClient client, String name) {
    return client.createPermission().createOne(ImmutableCreatePermission.builder()
        .name(name)
        .description("desc of permission for set all")
        .comment("created a new permission")
        .build())
        .await().atMost(Duration.ofMinutes(1));
  }
  
 private Principal createPrincipal(PermissionClient client, String name) {
    
    return client.createPrincipal().createOne(ImmutableCreatePrincipal.builder()
        .name(name)
        .email("james@gmail.com")
        .comment("created new user")
        .addRoles(
            createRole(client, "Frontoffice-team").getName()
            )
        .addPermissions(
            createPermission(client, "DB-READ").getName()
            )
        .build())
        .await().atMost(Duration.ofMinutes(5));
  }
  
 
 @Test
 public void createPrincipalSetAllTest() {
   final PermissionClient client = getClient().tenantQuery()
       .repoName("PrincipalSetAllTest-1")
       .create()
       .await().atMost(Duration.ofMinutes(1));
   
   final var createdPrincipal1 = createPrincipal(client, "JamesOverton");
   log.debug(Json.encodePrettily(createdPrincipal1));
   
   final var updatePermissions = ImmutableChangePrincipalPermissions.builder()
       .id(createdPrincipal1.getId())
       .comment("adding all permissions")
       .changeType(ChangeType.SET_ALL)
       .permissions(Arrays.asList(
           createPermission(client, "addedPerm1").getName(),
           createPermission(client, "addedPerm2").getName())
           )
       .build();
   
   final var updateRoles = ImmutableChangePrincipalRoles.builder()
       .id(createdPrincipal1.getId())
       .comment("adding all roles")
       .changeType(ChangeType.SET_ALL)
       .roles(Arrays.asList(
           createRole(client, "addedRole1").getName(),
           createRole(client, "addedRole2").getName())
           )
       .build();   
   
   final var updatedPrincipal1 = client.updatePrincipal().updateOne(Arrays.asList(
       updateRoles,
       updatePermissions
       )).await().atMost(Duration.ofMinutes(1));
   
   log.debug(Json.encodePrettily(updatedPrincipal1));
   Assertions.assertEquals("[addedPerm1, addedPerm2]", updatedPrincipal1.getPermissions().toString());
   Assertions.assertEquals("[addedRole1, addedRole2]", updatedPrincipal1.getRoles().toString()); 
   
   Assertions.assertEquals("[addedPerm1, addedPerm2]", updatedPrincipal1.getDirectPermissions().toString()); 
   Assertions.assertEquals("[addedRole1, addedRole2]", updatedPrincipal1.getDirectRoles().toString()); 
   
   Assertions.assertEquals("[addedPerm1, addedPerm2]", client.principalQuery().get(updatedPrincipal1.getId()).await().atMost(Duration.ofMinutes(1)).getPermissions().toString());
   Assertions.assertEquals("[addedRole1, addedRole2]", client.principalQuery().get(updatedPrincipal1.getId()).await().atMost(Duration.ofMinutes(1)).getRoles().toString());

 }
  
}
