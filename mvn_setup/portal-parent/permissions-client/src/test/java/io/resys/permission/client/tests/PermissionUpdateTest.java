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

@QuarkusTest
@TestProfile(OrgPgProfile.class)
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
    final PermissionClient client = getClient().tenantQuery()
      .repoName("PermissionUpdateTest-1")
      .create()
      .await().atMost(Duration.ofMinutes(1));

    final var createdPermission = createPermissionForUpdate(client);
    
    final var updatedPermission = client.updatePermission().updateOne(ImmutableChangePermissionName.builder()
      .id(createdPermission.getId())
      .name("SUPER USER AND MANAGER")
      .comment("Changed permission name for reasons")
      .build())
    .await().atMost(Duration.ofMinutes(5));
    
 
    Assertions.assertEquals("SUPER USER AND MANAGER", client.permissionQuery().get(updatedPermission.getId()).await().atMost(Duration.ofMinutes(1)).getName());
  }
}
