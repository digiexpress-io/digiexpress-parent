package io.resys.permission.client.tests;
import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.model.ImmutableChangeRoleDescription;
import io.resys.permission.client.api.model.ImmutableChangeRoleName;
import io.resys.permission.client.api.model.ImmutableChangeRoleParent;
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
public class RoleCreateAndUpdateTest extends DbTestTemplate {
  
  
 private Principal createPrincipalForRole(PermissionClient client, String name) {
    
    return client.createPrincipal().createOne(ImmutableCreatePrincipal.builder()
        .name(name)
        .email("the-rock@muscles.org")
        .comment("created new user")
        .build())
        .await().atMost(Duration.ofMinutes(5));
  }
  
  
  private Permission createPermissionForRole(PermissionClient client, String name) {
    return client.createPermission()
      .createOne(ImmutableCreatePermission.builder()
        .comment("created my first permission")
        .name(name)
        .description("Cool description here")
        .build())
      .await().atMost(Duration.ofMinutes(5));
  }
  
  private Role createParentRole(PermissionClient client, String name) {
    
    return client.createRole()
      .createOne(ImmutableCreateRole.builder()
          .name(name)
          .description("Description of Parent Role")
          .comment("Parent Role created")
        .build())
      .await().atMost(Duration.ofMinutes(5));
  }
  
  private Role createRoleForUpdating(PermissionClient client) {
    
    return client.createRole()
      .createOne(ImmutableCreateRole.builder()
          .name("V1 Role Name")
          .description("V1 Role Description")
          .comment("Role created")
          .parentId(
              createParentRole(client, "1-Parent-Role").getId()
              )
          .addPermissions( 
              createPermissionForRole(client, "perm-1").getName(),
              createPermissionForRole(client, "perm-2").getName(),
              createPermissionForRole(client, "perm-3").getName()
              )
          .addPrincipals(
              createPrincipalForRole(client, "Dwane Johnson").getName()
              )
        .build())
      .await().atMost(Duration.ofMinutes(5));
  }
  
  @Test
  public void createRoleAndUpdateTest() {
    
    final PermissionClient client = getClient().repoQuery()
        .repoName("RoleCreateAndUpdateTest-1")
        .create()
        .await().atMost(Duration.ofMinutes(5));
    
    final var createdRole = createRoleForUpdating(client);
    
    Assertions.assertEquals(1, createdRole.getPrincipals().size());
    
    final var updatedRoleName = client.updateRole().updateOne(ImmutableChangeRoleName.builder()
        .id(createdRole.getId())
        .comment("Name change requested by admin")
        .name("V2 Role Name")
        .build())
      .await().atMost(Duration.ofMinutes(5));
    
    Assertions.assertEquals("V2 Role Name", client.roleQuery().get(createdRole.getId()).await().atMost(Duration.ofMinutes(1)).getName());
    
    final var updatedRoleDescription = client.updateRole().updateOne(ImmutableChangeRoleDescription.builder()
        .id(updatedRoleName.getId())
        .comment("Corrected typo in name")
        .description("V2 Role Description")
        .build())
      .await().atMost(Duration.ofMinutes(5));
 
    
    final var updateRoleParent = client.updateRole().updateOne(ImmutableChangeRoleParent.builder()
        .id(updatedRoleName.getId())
        .comment("changed role parent")
        .parentId(
            createParentRole(client, "2-parent-updated").getId()
            )
        .build())
      .await().atMost(Duration.ofMinutes(5));
    
    final var removeRoleParent = client.updateRole().updateOne(ImmutableChangeRoleParent.builder()
        .id(updatedRoleName.getId())
        .comment("removed role parent")
        .parentId(null)
        .build())
      .await().atMost(Duration.ofMinutes(5));
    
  

    Assertions.assertEquals("V2 Role Description", client.roleQuery().get(updatedRoleDescription.getId()).await().atMost(Duration.ofMinutes(1)).getDescription());
    Assertions.assertEquals(3, createdRole.getPermissions().size());    
    Assertions.assertEquals(3, client.roleQuery().get(createdRole.getId()).await().atMost(Duration.ofMinutes(1)).getPermissions().size());
    Assertions.assertEquals(null, removeRoleParent.getParentId());
    
    log.debug(Json.encodePrettily(createdRole));
    log.debug(Json.encodePrettily(updatedRoleName));
    log.debug(Json.encodePrettily(updatedRoleDescription));
    log.debug(Json.encodePrettily(removeRoleParent));
    log.debug(Json.encodePrettily(updateRoleParent));

  }
}


