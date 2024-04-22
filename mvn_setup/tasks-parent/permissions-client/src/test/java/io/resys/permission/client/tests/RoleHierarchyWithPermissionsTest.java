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


@QuarkusTest
@TestProfile(OrgPgProfile.class)
public class RoleHierarchyWithPermissionsTest extends DbTestTemplate {

  private Permission createPermission(PermissionClient client, String name) {
    
    return client.createPermission().createOne(ImmutableCreatePermission.builder()
        .name(name)
        .description("permission created")
        .comment("perm for testing")
        .build())
        .await().atMost(Duration.ofMinutes(5));
  }
  
  private Role createChildRole(PermissionClient client, String parentRoleName, String roleName, String permName) {


    return client.createRole().createOne(ImmutableCreateRole.builder()
        .name(roleName)
        .parentId(parentRoleName)
        .description("child role for hierarchy testing")
        .addPermissions(
            createPermission(client, permName).getName()
         )
        .comment("child role for testing")
        .build())
        .await().atMost(Duration.ofMinutes(5));
  }
  
  
  @Test
  public void createRolesWithInheritedPermissionsTest() {
    
    final PermissionClient client = getClient().tenantQuery()
        .repoName("RoleHierarchyWithPermissionsTest-1")
        .create()
        .await().atMost(Duration.ofMinutes(1));
    
    
    { // ROOT role
      final var rootRole = client.createRole().createOne(ImmutableCreateRole.builder()
          .name("ROOT")
          .description("role created")
          .comment("role for testing")
          .addPermissions(
              createPermission(client, "PERM-1").getName(),
              createPermission(client, "PERM-2").getName()
              )
          .build())
          .await().atMost(Duration.ofMinutes(5));
      Assertions.assertEquals("[PERM-1, PERM-2]", rootRole.getPermissions().toString());
      
      final var queryRole = client.roleQuery().get("ROOT").await().atMost(Duration.ofMinutes(1)).getPermissions();
      Assertions.assertEquals("[PERM-1, PERM-2]", queryRole.toString());
    }
    
    
    { // ROOT/CHILD-1
      final var child = createChildRole(client, "ROOT", "CHILD-1", "CHILD-PERM-3");
      Assertions.assertEquals("[PERM-1, PERM-2, CHILD-PERM-3]", child.getPermissions().toString());
      
      final var queryRole = client.roleQuery().get(child.getId()).await().atMost(Duration.ofMinutes(1));
      Assertions.assertEquals("[PERM-1, PERM-2, CHILD-PERM-3]", queryRole.getPermissions().toString());
    }

    { // ROOT/CHILD-1/CHILD-2
      final var child = createChildRole(client, "CHILD-1", "CHILD-2", "CHILD-PERM-4");
      Assertions.assertEquals("[PERM-1, PERM-2, CHILD-PERM-3, CHILD-PERM-4]", child.getPermissions().toString());
      
      final var queryRole = client.roleQuery().get(child.getId()).await().atMost(Duration.ofMinutes(1));
      Assertions.assertEquals("[PERM-1, PERM-2, CHILD-PERM-3, CHILD-PERM-4]", queryRole.getPermissions().toString());
    }
    
    // check that top-level roleId is the same as the first child parentId
    final var tip = client.roleHierarchyQuery().get("ROOT").await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals(tip.getRoles().get(tip.getBottomRoleId()).getName(), "ROOT");
    Assertions.assertEquals(tip.getTopRoleId(), tip.getBottomRoleId());
    Assertions.assertEquals("{}", tip.getPermissions().toString());

  }
}
