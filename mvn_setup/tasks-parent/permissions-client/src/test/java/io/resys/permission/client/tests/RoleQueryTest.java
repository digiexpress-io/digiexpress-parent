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

@QuarkusTest
@TestProfile(OrgPgProfile.class)
public class RoleQueryTest extends DbTestTemplate {
  
  public Role createRoleForQuery(PermissionClient client) {
    
    return client.createRole().createOne(ImmutableCreateRole.builder()
        .userId("user-1")
        .comment("Created role")
        .name("Role-1")
        .description("Description of Role-1")
        .build()).await().atMost(Duration.ofMinutes(1));
  }

  @Test 
  public void roleQueryTest() {
    
    final PermissionClient client = getClient().repoQuery()
      .repoName("RoleQueryTest-1")
      .create()
      .await().atMost(Duration.ofMinutes(1));
    
    final var createdRole = createRoleForQuery(client);
    
    Assertions.assertEquals("Role-1", client.roleQuery().get(createdRole.getId()).await().atMost(Duration.ofMinutes(1)).getName());

    
    final List<Role> allRoles = client
        .roleQuery()
        .findAllRoles()
        .await().atMost(Duration.ofMinutes(1));
    
    Assertions.assertEquals(1, allRoles.size());
  }
}
