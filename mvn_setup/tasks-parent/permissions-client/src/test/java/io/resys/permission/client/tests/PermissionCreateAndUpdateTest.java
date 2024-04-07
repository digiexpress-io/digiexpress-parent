package io.resys.permission.client.tests;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.model.ImmutableChangePermissionDescription;
import io.resys.permission.client.api.model.ImmutableChangePermissionName;
import io.resys.permission.client.api.model.ImmutableCreatePermission;
import io.resys.permission.client.api.model.ImmutableCreatePrincipal;
import io.resys.permission.client.api.model.ImmutableCreateRole;
import io.resys.permission.client.api.model.Principal;
import io.resys.permission.client.api.model.Principal.Permission;
import io.resys.permission.client.api.model.Principal.Role;
import io.resys.permission.client.tests.config.DbTestTemplate;
import io.resys.permission.client.tests.config.OrgPgProfile;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuarkusTest
@TestProfile(OrgPgProfile.class)
public class PermissionCreateAndUpdateTest extends DbTestTemplate {
  
  
 private Principal createPrincipalForPermission(PermissionClient client, String name) {
    
    return client.createPrincipal()
      .createOne(ImmutableCreatePrincipal.builder()
          .name(name)
          .email("john.smith@gmail.com")
          .comment("added new user")
        .build())
      .await().atMost(Duration.ofMinutes(1));
  }
  
 private Role createRoleForPermission(PermissionClient client, String name) {
    
    return client.createRole()
      .createOne(ImmutableCreateRole.builder()
          .name(name)
          .description("Role created for adding to permission")
          .comment("This was needed")
        .build())
      .await().atMost(Duration.ofMinutes(1));
  }
  
  
  private Permission createPermissionForUpdating(PermissionClient client) {
    return client.createPermission()
      .createOne(ImmutableCreatePermission.builder()
        .comment("created my first permission")
        .name("My first permission")
        .description("Cool description here")
        .addRoles(
            createRoleForPermission(client, "ROLE_FOR_PERMISSION").getName()
            )
        .addPrincipals(
            createPrincipalForPermission(client, "John Smith").getName()
            )
        .build())
      .await().atMost(Duration.ofMinutes(1));
  }

  @Test
  public void createPermissionAndUpdateTest() {

    final PermissionClient client = getClient().repoQuery()
        .repoName("PermissionCreateAndUpdateTest-1")
        .create()
        .await().atMost(Duration.ofMinutes(1));
    
    final var createdPermission = createPermissionForUpdating(client);
    
    Assertions.assertEquals(1, createdPermission.getRoles().size());
    Assertions.assertEquals(1, createdPermission.getPrincipals().size());
    log.debug(Json.encodePrettily(createdPermission));

    final var updatedName = client.updatePermission().updateOne(ImmutableChangePermissionName.builder()
        .id(createdPermission.getId())  
        .name("New name for my first permission")
        .comment("Original name was wrong")
        .build())
      .await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals("New name for my first permission", updatedName.getName());
    Assertions.assertEquals("New name for my first permission", client.permissionQuery().get(createdPermission.getId()).await().atMost(Duration.ofMinutes(1)).getName());
    
    final var updatedDesc = client.updatePermission().updateOne(ImmutableChangePermissionDescription.builder()
        .id(updatedName.getId())  
        .description("An even better description here")
        .comment("new description")
        .build())
      .await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals("An even better description here", updatedDesc.getDescription());
    Assertions.assertEquals("An even better description here", client.permissionQuery().get(createdPermission.getId()).await().atMost(Duration.ofMinutes(1)).getDescription());
    
  }
}
