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
import io.vertx.core.json.JsonArray;
import lombok.extern.slf4j.Slf4j;


@QuarkusTest
@TestProfile(OrgPgProfile.class)
@Slf4j
public class PermissionQueryTest extends DbTestTemplate {
  
  
  public Permission createPermissionForQuery(PermissionClient client, String name, String description) {
    
   return client.createPermission().createOne(ImmutableCreatePermission.builder()
       .comment("Created permission 1")
       .name(name)
       .description(description)
       .build()).await().atMost(Duration.ofMinutes(1));
  }

  @Test  
  public void permissionQueryTest() {
    
    final PermissionClient client = getClient().tenantQuery()
        .repoName("CreatePermission-1")
        .create()
        .await().atMost(Duration.ofMinutes(1));
    
    final var createdReadPermission = createPermissionForQuery(client, "ReadPermission", "DB Reading");
    final var createdWritePermission = createPermissionForQuery(client, "WritePermission", "Write into DB");
    final var createdViewPermission = createPermissionForQuery(client, "ViewPermission", "Viewer for content");

    
    Assertions.assertEquals("ReadPermission", client.permissionQuery().get(createdReadPermission.getId()).await().atMost(Duration.ofMinutes(1)).getName());
    Assertions.assertEquals("WritePermission", client.permissionQuery().get(createdWritePermission.getId()).await().atMost(Duration.ofMinutes(1)).getName());
    Assertions.assertEquals("ViewPermission", client.permissionQuery().get(createdViewPermission.getId()).await().atMost(Duration.ofMinutes(1)).getName());

    
    final List<Permission> allPermissions = client
        .permissionQuery()
        .findAllPermissions()
        .await().atMost(Duration.ofMinutes(1));
    
    Assertions.assertEquals(3, allPermissions.size());
    log.debug("created 3 permissions \r\n {}", new JsonArray(allPermissions).encodePrettily());

    }
  }

