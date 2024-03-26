package io.resys.permission.client.tests;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.model.Principal;
import io.resys.permission.client.tests.config.DbTestTemplate;
import io.resys.permission.client.tests.config.GenerateTestData;
import io.resys.permission.client.tests.config.OrgPgProfile;
import io.resys.thena.api.entities.Tenant;
import io.vertx.core.json.JsonArray;
import lombok.extern.slf4j.Slf4j;

@QuarkusTest
@TestProfile(OrgPgProfile.class)
@Slf4j
public class PrincipalQueryTest extends DbTestTemplate {

  
  @Test  
  public void basicTest() {
    // create project
    final PermissionClient client = getClient().repoQuery()
        .repoName("PrincipalQueryTest-1")
        .create()
        .await().atMost(Duration.ofMinutes(1));

    final Tenant repo = client.getRepo().await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    new GenerateTestData(getDocDb()).populate(repo);

    
    final List<Principal> allPrincipals = client
        .principalQuery().findAllPrincipals()
        .await().atMost(Duration.ofMinutes(1));
  
    for(final var principal: allPrincipals) {
      final var foundByName = client
        .principalQuery().get(principal.getName())
        .await().atMost(Duration.ofMinutes(1));
  
      Assertions.assertEquals(principal.getId(), foundByName.getId());
    }
    log.debug(new JsonArray(allPrincipals).encodePrettily());
  }

  
}
