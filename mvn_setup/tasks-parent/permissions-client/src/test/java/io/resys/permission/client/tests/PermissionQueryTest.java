package io.resys.permission.client.tests;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.model.Principal.Permission;
import io.resys.permission.client.tests.config.DbTestTemplate;
import io.resys.permission.client.tests.config.GenerateTestData;
import io.resys.permission.client.tests.config.OrgPgProfile;
import io.resys.thena.docdb.api.models.Repo;
import io.vertx.core.json.JsonArray;
import lombok.extern.slf4j.Slf4j;

@QuarkusTest
@TestProfile(OrgPgProfile.class)
@Slf4j
public class PermissionQueryTest extends DbTestTemplate {

  @Test  
  public void basicTest() {
    // create project
    final PermissionClient client = getClient().repoQuery()
        .repoName("PermissionQueryTest-1")
        .create()
        .await().atMost(Duration.ofMinutes(1));

    final Repo repo = client.getRepo().await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    new GenerateTestData(getDocDb()).populate(repo);

    
    final List<Permission> allPermissions = client
        .permissionQuery().findAllPermissions()
        .await().atMost(Duration.ofMinutes(1));
    
    for(final var permission : allPermissions) {
      final var foundByName = client
        .permissionQuery().get(permission.getName())
        .await().atMost(Duration.ofMinutes(1));
      
      Assertions.assertEquals(permission.getId(), foundByName.getId());
    }

    log.debug(new JsonArray(allPermissions).encodePrettily());
  }
}
