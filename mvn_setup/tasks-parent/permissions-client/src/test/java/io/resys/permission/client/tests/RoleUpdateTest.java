package io.resys.permission.client.tests;

import java.time.Duration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.model.ImmutableChangeRoleName;
import io.resys.permission.client.tests.config.DbTestTemplate;
import io.resys.permission.client.tests.config.GenerateTestData;
import io.resys.permission.client.tests.config.OrgPgProfile;
import io.resys.thena.api.entities.Tenant;
import lombok.extern.slf4j.Slf4j;

@QuarkusTest
@TestProfile(OrgPgProfile.class)
@Slf4j
public class RoleUpdateTest extends DbTestTemplate {

  @Disabled
  @Test
  public void getRoleAndUpdateName() {
 
     final PermissionClient client = getClient().repoQuery()
      .repoName("RoleUpdateTest-1")
      .create()
      .await().atMost(Duration.ofMinutes(1));

    final Tenant repo = client.getRepo().await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    new GenerateTestData(getDocDb()).populate(repo);
    
    final var updated = client.updateRole().updateOne(ImmutableChangeRoleName.builder()
      .id(null)
      .name("The cool kids")
      .comment("This role is only for awesome people now")
      .build())
    .await().atMost(Duration.ofMinutes(5));
    
    log.debug("Updated role: {}", updated);
  
  }
}
