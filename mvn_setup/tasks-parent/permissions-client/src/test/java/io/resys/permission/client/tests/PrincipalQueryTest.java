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

@QuarkusTest
@TestProfile(OrgPgProfile.class)
public class PrincipalQueryTest extends DbTestTemplate {

  public Principal createPrincipalForTest(PermissionClient client) {
    
    return client.createPrincipal().createOne(ImmutableCreatePrincipal.builder()
        .comment("Added new tester to system")
        .userId("user-1")
        .name("John Cena")
        .email("muscles@super-cool.com")
        .build()).await().atMost(Duration.ofMinutes(1));
  }
  
  
  @Test  
  public void basicTest() {
    
    final PermissionClient client = getClient().repoQuery()
        .repoName("PrincipalQueryTest-1")
        .create()
        .await().atMost(Duration.ofMinutes(1));

    final var createdPrincipal = createPrincipalForTest(client);
    
    Assertions.assertEquals("John Cena", client.principalQuery().get(createdPrincipal.getId()).await().atMost(Duration.ofMinutes(1)).getName());
    
    final List<Principal> allPrincipals = client
        .principalQuery().findAllPrincipals()
        .await().atMost(Duration.ofMinutes(1));
  
    
    Assertions.assertEquals(1, allPrincipals.size());
  }

  
}
