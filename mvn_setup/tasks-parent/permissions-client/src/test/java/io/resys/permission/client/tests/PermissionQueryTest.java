package io.resys.permission.client.tests;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.model.ImmutableCreatePermission;
import io.resys.permission.client.api.model.Principal.Permission;
import io.resys.permission.client.tests.config.DbTestTemplate;
import io.resys.permission.client.tests.config.OrgPgProfile;

@QuarkusTest
@TestProfile(OrgPgProfile.class)
public class PermissionQueryTest extends DbTestTemplate {
  
  public Permission createPermissionForQuery(PermissionClient client) {
    
   return client.createPermission().createOne(ImmutableCreatePermission.builder()
       .userId("user-1")
       .comment("Created permission")
       .name("Permission-1")
       .description("New permission 1")
       .build()).await().atMost(Duration.ofMinutes(1));
  }

  @Test  
  public void permissionQueryTest() {
    
    final PermissionClient client = getClient().repoQuery()
        .repoName("CreatePermission-1")
        .create()
        .await().atMost(Duration.ofMinutes(1));
    
    final var createdPermission = createPermissionForQuery(client);
    
    Assertions.assertEquals("Permission-1", client.permissionQuery().get(createdPermission.getId()).await().atMost(Duration.ofMinutes(1)).getName());
   
    
    final List<Permission> allPermissions = client
        .permissionQuery()
        .findAllPermissions()
        .await().atMost(Duration.ofMinutes(1));
    
    Assertions.assertEquals(1, allPermissions.size());

    }
  }

