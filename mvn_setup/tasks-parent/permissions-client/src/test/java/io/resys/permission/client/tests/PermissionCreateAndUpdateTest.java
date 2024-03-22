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
import io.resys.permission.client.api.model.Principal.Permission;
import io.resys.permission.client.tests.config.DbTestTemplate;
import io.resys.permission.client.tests.config.OrgPgProfile;

@QuarkusTest
@TestProfile(OrgPgProfile.class)
public class PermissionCreateAndUpdateTest extends DbTestTemplate {
  
  private Permission createPermissionForUpdating(PermissionClient client) {
    return client.createPermission()
      .createOne(ImmutableCreatePermission.builder()
        .comment("created my first permission")
        .name("My first permission")
        .description("Cool description here")
        .userId("user-1")
        .build())
      .await().atMost(Duration.ofMinutes(1));
  }

  @Test
  public void createPermissionAndUpdateTest() {

    final PermissionClient client = getClient().repoQuery()
        .repoName("PermissionCreateAndUpdateTest-1")
        .create()
        .await().atMost(Duration.ofMinutes(1));
    
    final var created = createPermissionForUpdating(client);

    final var updatedName = client.updatePermission().updateOne(ImmutableChangePermissionName.builder()
        .id(created.getId())  
        .userId("user-1")
        .name("New name for my first permission")
        .comment("Original name was wrong")
        .build())
      .await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals("New name for my first permission", updatedName.getName());
    Assertions.assertEquals("New name for my first permission", client.permissionQuery().get(created.getId()).await().atMost(Duration.ofMinutes(1)).getName());
    
    final var updatedDesc = client.updatePermission().updateOne(ImmutableChangePermissionDescription.builder()
        .id(updatedName.getId())  
        .userId("user-1")
        .description("An even better description here")
        .comment("new description")
        .build())
      .await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals("An even better description here", updatedDesc.getDescription());
    Assertions.assertEquals("An even better description here", client.permissionQuery().get(created.getId()).await().atMost(Duration.ofMinutes(1)).getDescription());
    
    


  }
}
