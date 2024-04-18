package io.resys.permission.client.tests;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.model.ImmutableCreatePermission;
import io.resys.permission.client.api.model.ImmutableCreateRole;
import io.resys.permission.client.api.model.Principal.Permission;
import io.resys.permission.client.api.model.Principal.Role;
import io.resys.permission.client.tests.config.DbTestTemplate;
import io.resys.permission.client.tests.config.OrgPgProfile;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuarkusTest
@TestProfile(OrgPgProfile.class)
public class ParentRoleWithChildrenAndPermissionsTest extends DbTestTemplate {

  private Permission createPermission(PermissionClient client, String name) {
      return client.createPermission().createOne(ImmutableCreatePermission.builder()
          .name(name)
          .description("desc of permission")
          .comment("created a new permission")
          .build())
          .await().atMost(Duration.ofMinutes(1));
  }
  
  private Role createChildRole(PermissionClient client, String roleName, String description, String parentId, String permName) {
    return client.createRole().createOne(ImmutableCreateRole.builder()
        .name(roleName)
        .description(description)
        .parentId(parentId)
        .addPermissions(
            createPermission(client, permName).getName()
            )
        .comment("created new child")
        .build())
        .await().atMost(Duration.ofMinutes(1));
  }
  
  
  private Role createParentRole(PermissionClient client, String name) {
    
    return client.createRole().createOne(ImmutableCreateRole.builder()
        .name(name)
        .description("parentRole-desc")
        .addPermissions(
            createPermission(client, "parentRole-perm3").getName()
            )
        .comment("Created this for testing")
        .build())
        .await().atMost(Duration.ofMinutes(1)); 
  }
  
  
  
  @Test
  public void createParentRoleTest() {
    
    final PermissionClient client = getClient().tenantQuery()
        .repoName("CreateParentRoleWithChildrenAndPermissionsTest-1")
        .create()
        .await().atMost(Duration.ofMinutes(1));
    
    final var parentRole = createParentRole(client, "parentRole");
    final var childRole1 = createChildRole(client, "childRole1", "childRole1-desc", parentRole.getId(), "childRole1-perm1");
    final var childRole2 = createChildRole(client, "childRole2", "childRole2-desc", parentRole.getId(), "childRole2-perm2");
    
    /*
    final var updatedChildRole1 = client.updateRole().updateOne(ImmutableChangeRolePermissions.builder()
        .id(childRole1.getId())
        .comment("added parent permissions")
        .addAllPermissions(parentRole.getPermissions())
        .changeType(ChangeType.ADD)
        .build())
        .await().atMost(Duration.ofMinutes(1));
    */
    
    log.debug(Json.encodePrettily(parentRole));
    log.debug(Json.encodePrettily(childRole1));
    log.debug(Json.encodePrettily(childRole2));
    

    Assertions.assertEquals(1, childRole1.getPermissions().size());
    Assertions.assertEquals(parentRole.getId(), childRole1.getParentId());
    
  }
  
}
