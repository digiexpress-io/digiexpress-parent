package io.resys.permission.client.tests;

import java.time.Duration;

import org.junit.jupiter.api.Disabled;
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
public class PrincipalCreateAndUpdateTest extends DbTestTemplate {
  
  private Principal createPrincipalForUpdating(PermissionClient client) {
    
    return client.createPrincipal().createOne(ImmutableCreatePrincipal.builder()
        .name("Dwane Johnson")
        .email("the-rock@muscles.org")
        .comment("created new user")
        .build())
        .await().atMost(Duration.ofMinutes(1));
  }
  @SuppressWarnings("unused")
  @Disabled
  @Test
  public void createPrincipalAndUpdateTest() {
    final PermissionClient client = getClient().repoQuery()
        .repoName("PrincipalCreateAndUpdateTest-1")
        .create()
        .await().atMost(Duration.ofMinutes(1));
    
    
    final var createdPrincipal = createPrincipalForUpdating(client); //TODO
          
  }
  

}
