package io.resys.permission.client.tests;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.model.ImmutableChangeRoleName;
import io.resys.permission.client.api.model.ImmutableCreateRole;
import io.resys.permission.client.api.model.Principal.Role;
import io.resys.permission.client.tests.config.DbTestTemplate;
import io.resys.permission.client.tests.config.OrgPgProfile;


@QuarkusTest
@TestProfile(OrgPgProfile.class)
public class RoleUpdateTest extends DbTestTemplate {
  
  public Role createRoleForTest(PermissionClient client) {
    return client.createRole().createOne(ImmutableCreateRole.builder()
        .userId("user-1")
        .comment("New role needed")
        .name("front-office-trainee")
        .description("temporary for 3 weeks")
        .build()).await().atMost(Duration.ofMinutes(1));
  }

  @Test
  public void getRoleAndUpdateName() {
 
    final PermissionClient client = getClient().repoQuery()
      .repoName("RoleUpdateTest-1")
      .create()
      .await().atMost(Duration.ofMinutes(1));

    final var createdRole = createRoleForTest(client);

    
    final var updated = client.updateRole().updateOne(ImmutableChangeRoleName.builder()
      .id(createdRole.getId())
      .name("The cool kids")
      .comment("This role is only for awesome people now")
      .build())
    .await().atMost(Duration.ofMinutes(1));
    
   Assertions.assertEquals("The cool kids", client.roleQuery().get(updated.getId()).await().atMost(Duration.ofMinutes(1)).getName());

  }
}
