package io.resys.permission.client.tests;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.model.ImmutableCreatePermission;
import io.resys.permission.client.api.model.ImmutableCreatePrincipal;
import io.resys.permission.client.api.model.ImmutableCreateRole;
import io.resys.permission.client.api.model.Principal;
import io.resys.permission.client.api.model.Principal.Permission;
import io.resys.permission.client.tests.config.DbTestTemplate;
import io.resys.permission.client.tests.config.OrgPgProfile;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuarkusTest
@TestProfile(OrgPgProfile.class)
public class RoleHierarchyTest extends DbTestTemplate {

  private Principal createPrincipal(PermissionClient client, String name, String email) {
    
    return client.createPrincipal().createOne(ImmutableCreatePrincipal.builder()
        .name(name)
        .email(email)
        .comment("created first principal")
        .build())
        .await().atMost(Duration.ofMinutes(1));
  }
  
  private Permission createPermission(PermissionClient client, String name) {
    
    return client.createPermission().createOne(ImmutableCreatePermission.builder()
        .name(name)
        .description("permission created")
        .comment("perm for testing")
        .build())
        .await().atMost(Duration.ofMinutes(5));
  }
  
  
  @Test
  public void roleHierarchyTest() {
    
    final PermissionClient client = getClient().tenantQuery()
        .repoName("RoleHierarchyWithPermissionsTest-1")
        .create()
        .await().atMost(Duration.ofMinutes(1));
    
    
    { // ROOT role
      final var rootRole = client.createRole().createOne(ImmutableCreateRole.builder()
          .name("ROOT")
          .description("ROOT role created")
          .comment("role for testing")
          .addPermissions(
              createPermission(client, "PERM-1").getName(),
              createPermission(client, "PERM-2").getName()
              )
          .addPrincipals(
              createPrincipal(client, "PRINC-1", "princ-1@email.com").getName()
              )
          .build())
          .await().atMost(Duration.ofMinutes(5));
      Assertions.assertEquals("[PERM-1, PERM-2]", rootRole.getPermissions().toString());
      Assertions.assertEquals("[PRINC-1]", rootRole.getPrincipals().toString());
      
      final var queryRole = client.roleQuery().get("ROOT").await().atMost(Duration.ofMinutes(10));
      Assertions.assertEquals("[PERM-1, PERM-2]", queryRole.getPermissions().toString());
      Assertions.assertEquals("[PRINC-1]", queryRole.getPrincipals().toString());  
    }
    
    
    { // ROOT/CHILD-1
     final var child = client.createRole().createOne(ImmutableCreateRole.builder()
          .name("CHILD-1")
          .parentId("ROOT")
          .description("child role for hierarchy testing")
          .addPermissions(
              createPermission(client, "CHILD-PERM-3").getName()
           )
          .addPrincipals(
              createPrincipal(client, "PRINC-2", "princ-2@email.com").getName()
           )
          .comment("child role for testing")
          .build())
          .await().atMost(Duration.ofMinutes(5));
      
      Assertions.assertEquals("[PERM-1, PERM-2, CHILD-PERM-3]", child.getPermissions().toString());
      Assertions.assertEquals("[PRINC-2]", child.getPrincipals().toString());
      
      final var queryRole = client.roleQuery().get(child.getId()).await().atMost(Duration.ofMinutes(1));
      Assertions.assertEquals("[PERM-1, PERM-2, CHILD-PERM-3]", queryRole.getPermissions().toString());
      Assertions.assertEquals("[PRINC-2]", queryRole.getPrincipals().toString());
    }

    { // ROOT/CHILD-1/CHILD-2
      final var child = client.createRole().createOne(ImmutableCreateRole.builder()
          .name("CHILD-2")
          .parentId("CHILD-1")
          .description("child role for hierarchy testing")
          .addPermissions(
              createPermission(client, "CHILD-PERM-4").getName()
           )
          .comment("child role for testing")
          .build())
          .await().atMost(Duration.ofMinutes(5));
      
      Assertions.assertEquals("[PERM-1, PERM-2, CHILD-PERM-3, CHILD-PERM-4]", child.getPermissions().toString());
      
      final var queryRole = client.roleQuery().get(child.getId()).await().atMost(Duration.ofMinutes(1));
      Assertions.assertEquals("[PERM-1, PERM-2, CHILD-PERM-3, CHILD-PERM-4]", queryRole.getPermissions().toString());
    }
    
    // top-level roleId is the same as the first child parentId
    final var tip = client.roleQuery().get("ROOT").await().atMost(Duration.ofMinutes(10));
    final var child1 = client.roleQuery().get("CHILD-1").await().atMost(Duration.ofMinutes(10));
    final var child2 = client.roleQuery().get("CHILD-2").await().atMost(Duration.ofMinutes(10));
        
    // child1 and child2 both descend from same root
    Assertions.assertEquals(child1.getParentId(), tip.getId());
    Assertions.assertEquals(child2.getParentId(), child1.getId());    

    log.debug(Json.encodePrettily(tip));
    
    // query returns all principals created in ROOT
    Assertions.assertEquals("[PRINC-1]", tip.getPrincipals().toString());
    
    // query returns all permissions created in ROOT
    Assertions.assertEquals("[PERM-1, PERM-2]", tip.getPermissions().toString());
    
    // query returns all permissions from ROOT/CHILD-1
    Assertions.assertEquals("[PERM-1, PERM-2, CHILD-PERM-3]", child1.getPermissions().toString());
    
    // query returns all permissions from ROOT/CHILD-1/CHILD-2
    Assertions.assertEquals("[PERM-1, PERM-2, CHILD-PERM-3, CHILD-PERM-4]", child2.getPermissions().toString());
    
    // query returns all principals created in CHILD-2
    Assertions.assertEquals("[PRINC-2]", child1.getPrincipals().toString());
    
    // query returns all permissions from ROOT/CHILD-1/CHILD-2
    Assertions.assertEquals("[PERM-1, PERM-2, CHILD-PERM-3, CHILD-PERM-4]", child2.getPermissions().toString());
    
    
  }
}
