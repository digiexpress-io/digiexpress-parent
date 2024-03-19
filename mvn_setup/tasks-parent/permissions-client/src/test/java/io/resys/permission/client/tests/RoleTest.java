package io.resys.permission.client.tests;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.model.Principal.Role;
import io.resys.permission.client.tests.config.DbTestTemplate;
import io.resys.permission.client.tests.config.GenerateTestData;
import io.resys.permission.client.tests.config.OrgPgProfile;
import io.resys.thena.docdb.api.models.Repo;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@QuarkusTest
@TestProfile(OrgPgProfile.class)
@Slf4j
public class RoleTest extends DbTestTemplate {

  @Test 
  public void basicTest() {
    
    final PermissionClient client = getClient().repoQuery()
      .repoName("RoleTest-1")
      .create()
      .await().atMost(Duration.ofMinutes(1));
    
    final Repo repo = client.getRepo().await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    new GenerateTestData(getDocDb()).populate(repo);
    
    final List<Role> allRoles = client
      .roleQuery().findAllRoles()
      .await().atMost(Duration.ofMinutes(1));
  
    for(final var role : allRoles) {
      final var foundByName = client
      .roleHierarchyQuery().get(role.getName())
      .await().atMost(Duration.ofMinutes(1));
      
      log.debug(JsonObject.mapFrom(foundByName).encodePrettily());
      
      Assertions.assertEquals(role.getId(), foundByName.getTargetRoleId());
    }
    
  //log.debug(new JsonArray(allRoles).encodePrettily());
  
  }
}
