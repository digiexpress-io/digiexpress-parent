package io.resys.permission.client.tests;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.model.ImmutableCreatePermission;
import io.resys.permission.client.api.model.ImmutableCreatePrincipal;
import io.resys.permission.client.api.model.Principal;
import io.resys.permission.client.api.model.Principal.Permission;
import io.resys.permission.client.tests.config.DbTestTemplate;
import io.resys.permission.client.tests.config.OrgPgProfile;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuarkusTest
@TestProfile(OrgPgProfile.class)
public class PrincipalCreateAndUpdateTest extends DbTestTemplate {
  
  
  private Permission createPermissionforPrincipal(PermissionClient client, String name) {
    
    return client.createPermission().createOne(ImmutableCreatePermission.builder()
        .comment("Needed for training purposes")
        .name(name)
        .description("Frontoffice resource viewing")
        .build())
        .await().atMost(Duration.ofMinutes(1));
  }
  
  
  private Principal createPrincipalForUpdating(PermissionClient client) {
    
    return client.createPrincipal().createOne(ImmutableCreatePrincipal.builder()
        .name("Dwane Johnson")
        .email("the-rock@muscles.org")
        .comment("created new user")
        .addDirectPermissions(
            createPermissionforPrincipal(client, "VIEW_RESOURCES").getName(),
            createPermissionforPrincipal(client, "EDIT_LIMITED").getName()
            )
        .build())
        .await().atMost(Duration.ofMinutes(1));
  }
  
  
  @Test
  public void createPrincipalAndUpdateTest() {
    final PermissionClient client = getClient().repoQuery()
        .repoName("PrincipalCreateAndUpdateTest-1")
        .create()
        .await().atMost(Duration.ofMinutes(1));
    
    final var createdPrincipal = createPrincipalForUpdating(client);
  
    
    log.debug(Json.encodePrettily(createdPrincipal));
    Assertions.assertEquals("Dwane Johnson", createdPrincipal.getName());
    Assertions.assertEquals(2, createdPrincipal.getDirectPermissions().size());
  }
}
