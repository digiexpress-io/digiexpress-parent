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
import io.digiexpress.eveli.permission.client.api.model.ImmutableChangePrincipalEmail;
import io.digiexpress.eveli.permission.client.api.model.ImmutableChangePrincipalName;
import io.digiexpress.eveli.permission.client.api.model.ImmutableCreatePermission;
import io.digiexpress.eveli.permission.client.api.model.ImmutableCreatePrincipal;
import io.digiexpress.eveli.permission.client.api.model.ImmutableCreateRole;
import io.digiexpress.eveli.permission.client.api.model.Principal;
import io.digiexpress.eveli.permission.client.api.model.Principal.Permission;
import io.digiexpress.eveli.permission.client.api.model.Principal.Role;
import io.digiexpress.eveli.permission.client.tests.config.DbTestTemplate;
import io.digiexpress.eveli.permission.client.tests.config.OrgPgProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuarkusTest
@TestProfile(OrgPgProfile.class)
public class PrincipalCreateAndUpdateTest extends DbTestTemplate {
  

  
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
  
  
  
  private Principal createPrincipal(PermissionClient client) {
    
    return client.createPrincipal().createOne(ImmutableCreatePrincipal.builder()
        .name("Dwane Johnson")
        .email("the-rock@muscles.org")
        .comment("created new user")
        .addPermissions(
            createPermission(client, "PERM-1").getName(),
            createPermission(client, "PERM-2").getName(),
            createPermission(client, "PERM-3").getName()

            )
        .addRoles(
            createRoleWithoutPermissions(client, "ROLE-1").getName(),
            createRoleWithPermissions(client, "ROLE-2").getName()
            )
        .build())
        .await().atMost(Duration.ofMinutes(5));
  }
  
  
  @Test
  public void createPrincipalAndUpdateTest() {
    final PermissionClient client = getClient().tenantQuery()
        .repoName("PrincipalCreateAndUpdateTest-1")
        .create()
        .await().atMost(Duration.ofMinutes(5));
    
    final var principal1 = createPrincipal(client);

    log.debug(Json.encodePrettily(principal1));
    Assertions.assertEquals("Dwane Johnson", principal1.getName());   
    Assertions.assertEquals(2, principal1.getRoles().size());
    Assertions.assertEquals(3, principal1.getPermissions().size());
    Assertions.assertEquals("[PERM-1, PERM-2, PERM-3, PERM-X]", client.principalQuery().get(principal1.getId()).await().atMost(Duration.ofMinutes(1)).getPermissions().toString());

    
    final var updatedPrincipalName = client.updatePrincipal().updateOne(ImmutableChangePrincipalName.builder()
        .id(principal1.getId())
        .comment("Needed to update name as spelling was incorrect")
        .name("Amanda McNally")
        .build())
        .await().atMost(Duration.ofMinutes(5));
    
    log.debug(Json.encodePrettily(updatedPrincipalName));
    Assertions.assertEquals("Amanda McNally", updatedPrincipalName.getName());
    Assertions.assertEquals("[PERM-1, PERM-2, PERM-3, PERM-X]", client.principalQuery().get(updatedPrincipalName.getId()).await().atMost(Duration.ofMinutes(1)).getPermissions().toString());
    Assertions.assertEquals("Amanda McNally", client.principalQuery().get(updatedPrincipalName.getId()).await().atMost(Duration.ofMinutes(1)).getName().toString());

    
    final var updatedPrincipalEmail = client.updatePrincipal().updateOne(ImmutableChangePrincipalEmail.builder()
        .id(updatedPrincipalName.getId())
        .comment("Email updated as username has changed")
        .email("a.mcnally@mail.com")
        .build())
        .await().atMost(Duration.ofMinutes(5));
    
    log.debug(Json.encodePrettily(updatedPrincipalEmail));
    Assertions.assertEquals("a.mcnally@mail.com", updatedPrincipalEmail.getEmail());
    Assertions.assertEquals("a.mcnally@mail.com", client.principalQuery().get(updatedPrincipalName.getId()).await().atMost(Duration.ofMinutes(1)).getEmail().toString());

    
    



  }
}
