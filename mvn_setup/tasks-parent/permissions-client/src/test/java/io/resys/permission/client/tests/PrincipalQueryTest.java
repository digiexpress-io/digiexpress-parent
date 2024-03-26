package io.resys.permission.client.tests;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.model.ImmutableCreatePrincipal;
import io.resys.permission.client.api.model.Principal;
import io.resys.permission.client.tests.config.DbTestTemplate;
import io.resys.permission.client.tests.config.OrgPgProfile;
import io.vertx.core.json.JsonArray;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuarkusTest
@TestProfile(OrgPgProfile.class)
public class PrincipalQueryTest extends DbTestTemplate {

  public Principal createPrincipalForTest(PermissionClient client, String name, String email) {
    
    return client.createPrincipal().createOne(ImmutableCreatePrincipal.builder()
        .comment("Added new tester to system")
        .userId("user-1")
        .name(name)
        .email(email)
        .build()).await().atMost(Duration.ofMinutes(1));
  }
  
  
  @Test  
  public void principalQueryTest() {
    
    final PermissionClient client = getClient().repoQuery()
        .repoName("PrincipalQueryTest-1")
        .create()
        .await().atMost(Duration.ofMinutes(1));

    final var principalJohn = createPrincipalForTest(client, "John Cena", "muscles@super-cool.org");
    final var principalMark = createPrincipalForTest(client, "Mark McGamle", "mark.m@gmail.com");
    final var principalAmy = createPrincipalForTest(client, "Amy Anders", "anders@gmail.com");

    Assertions.assertEquals("John Cena", client.principalQuery().get(principalJohn.getId()).await().atMost(Duration.ofMinutes(1)).getName());
    Assertions.assertEquals("Mark McGamle", client.principalQuery().get(principalMark.getId()).await().atMost(Duration.ofMinutes(1)).getName());
    Assertions.assertEquals("Amy Anders", client.principalQuery().get(principalAmy.getId()).await().atMost(Duration.ofMinutes(1)).getName());

    
    final List<Principal> allPrincipals = client
        .principalQuery().findAllPrincipals()
        .await().atMost(Duration.ofMinutes(1));
  
    
    log.debug(new JsonArray(allPrincipals).encodePrettily());
    Assertions.assertEquals(3, allPrincipals.size());
  }

  
}
