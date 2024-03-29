package io.resys.permission.client.tests;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.model.ImmutableCreateRole;
import io.resys.permission.client.api.model.Principal.Role;
import io.resys.permission.client.tests.config.DbTestTemplate;
import io.resys.permission.client.tests.config.OrgPgProfile;
import io.vertx.core.json.JsonArray;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuarkusTest
@TestProfile(OrgPgProfile.class)
public class RoleQueryTest extends DbTestTemplate {
  
  public Role createRoleForQuery(PermissionClient client, String name, String description) {
    
    return client.createRole().createOne(ImmutableCreateRole.builder()
        .userId("user-1")
        .comment("Created role")
        .name(name)
        .description(description)
        .build()).await().atMost(Duration.ofMinutes(1));
  }

  @Test 
  public void roleQueryTest() {
    
    final PermissionClient client = getClient().repoQuery()
      .repoName("RoleQueryTest-1")
      .create()
      .await().atMost(Duration.ofMinutes(1));
    
    final var createdAdminRole = createRoleForQuery(client, "Admin", "Read/Write to all DB fields");
    final var createdViewerRole = createRoleForQuery(client, "Viewer", "View all DB fields");
    
    Assertions.assertEquals("Admin", client.roleQuery().get(createdAdminRole.getId()).await().atMost(Duration.ofMinutes(1)).getName());
    Assertions.assertEquals("Viewer", client.roleQuery().get(createdViewerRole.getId()).await().atMost(Duration.ofMinutes(1)).getName());

    final List<Role> allRoles = client
        .roleQuery()
        .findAllRoles()
        .await().atMost(Duration.ofMinutes(1));
    
    log.debug(new JsonArray(allRoles).encodePrettily());
    Assertions.assertEquals(2, allRoles.size());
  }
}
