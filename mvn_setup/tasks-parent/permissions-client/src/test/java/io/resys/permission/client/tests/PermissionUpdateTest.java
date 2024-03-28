package io.resys.permission.client.tests;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.model.ImmutableChangePermissionName;
import io.resys.permission.client.api.model.ImmutableCreatePermission;
import io.resys.permission.client.api.model.Principal.Permission;
import io.resys.permission.client.tests.config.DbTestTemplate;
import io.resys.permission.client.tests.config.OrgPgProfile;
import lombok.extern.slf4j.Slf4j;

@QuarkusTest
@TestProfile(OrgPgProfile.class)
@Slf4j
public class PermissionUpdateTest extends DbTestTemplate {
  
  public Permission createPermissionForUpdate(PermissionClient client) {
    return client.createPermission().createOne(ImmutableCreatePermission.builder()
        .comment("New permission for update")
        .name("DB-write")
        .description("For admins only!")
        .build()).await().atMost(Duration.ofMinutes(1));
  }

  @Test
  public void getPermissionAndUpdateName() {
    final PermissionClient client = getClient().repoQuery()
      .repoName("PermissionUpdateTest-1")
      .create()
      .await().atMost(Duration.ofMinutes(1));

    final var createdPermission = createPermissionForUpdate(client);
    
    final var updated = client.updatePermission().updateOne(ImmutableChangePermissionName.builder()
      .id(createdPermission.getId())
      .name("SUPER USER AND MANAGER")
      .comment("Changed permission name for reasons")
      .build())
    .await().atMost(Duration.ofMinutes(5));
    
    log.debug("Updated permission: {}", updated);
    Assertions.assertEquals("SUPER USER AND MANAGER", client.permissionQuery().get(updated.getId()).await().atMost(Duration.ofMinutes(1)).getName());
  }
}
