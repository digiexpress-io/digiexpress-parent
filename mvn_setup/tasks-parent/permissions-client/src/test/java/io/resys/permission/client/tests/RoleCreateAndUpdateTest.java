package io.resys.permission.client.tests;
import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.model.ImmutableChangeRoleDescription;
import io.resys.permission.client.api.model.ImmutableChangeRoleName;
import io.resys.permission.client.api.model.ImmutableCreateRole;
import io.resys.permission.client.api.model.Principal.Role;
import io.resys.permission.client.tests.config.DbTestTemplate;
import io.resys.permission.client.tests.config.OrgPgProfile;

@QuarkusTest
@TestProfile(OrgPgProfile.class)
public class RoleCreateAndUpdateTest extends DbTestTemplate {
  
  private Role createRoleForUpdating(PermissionClient client) {
    
    return client.createRole()
      .createOne(ImmutableCreateRole.builder()
          .name("My first role")
          .description("Description for my first role")
          .comment("Role created")
        .build())
      .await().atMost(Duration.ofMinutes(1));
  }
  
  @Test
  public void createRoleAndUpdateTest() {
    
    final PermissionClient client = getClient().repoQuery()
        .repoName("RoleCreateAndUpdateTest-1")
        .create()
        .await().atMost(Duration.ofMinutes(1));
    
    final var created = createRoleForUpdating(client);
    
    final var updatedName = client.updateRole().updateOne(ImmutableChangeRoleName.builder()
        .id(created.getId())

        .comment("Name change requested by admin")
        .name("Updated role name is super good")
        .build())
      .await().atMost(Duration.ofMinutes(1));
    
    Assertions.assertEquals("Updated role name is super good", client.roleQuery().get(created.getId()).await().atMost(Duration.ofMinutes(1)).getName());
    
    final var updatedDescription = client.updateRole().updateOne(ImmutableChangeRoleDescription.builder()
        .id(updatedName.getId())
        .comment("Corrected typo in name")
        .description("New description")
        .build())
      .await().atMost(Duration.ofMinutes(1));
    
    Assertions.assertEquals("New description", client.roleQuery().get(updatedDescription.getId()).await().atMost(Duration.ofMinutes(1)).getDescription());
  }
}


