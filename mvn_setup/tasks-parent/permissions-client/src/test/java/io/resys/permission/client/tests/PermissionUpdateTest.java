package io.resys.permission.client.tests;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.model.ImmutableChangePermissionName;
import io.resys.permission.client.tests.config.DbTestTemplate;
import io.resys.permission.client.tests.config.GenerateTestData;
import io.resys.permission.client.tests.config.OrgPgProfile;
import io.resys.thena.api.entities.Tenant;
import lombok.extern.slf4j.Slf4j;

@QuarkusTest
@TestProfile(OrgPgProfile.class)
@Slf4j
public class PermissionUpdateTest extends DbTestTemplate {

  @Test
  public void getPermissionAndUpdateName() {
    final PermissionClient client = getClient().repoQuery()
      .repoName("PermissionUpdateTest-1")
      .create()
      .await().atMost(Duration.ofMinutes(1));

    final Tenant repo = client.getRepo().await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    new GenerateTestData(getDocDb()).populate(repo);
    
    final var updated = client.updatePermission().updateOne(ImmutableChangePermissionName.builder()
      .id("MANAGER")
      .name("SUPER USER AND MANAGER")
      .comment("Changed permission name for reasons")
      .build())
    .await().atMost(Duration.ofMinutes(5));
    
    log.debug("Updated permission: {}", updated);
  }
}
