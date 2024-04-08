package io.resys.permission.client.tests;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.model.ImmutableCreatePermission;
import io.resys.permission.client.api.model.ImmutableCreatePrincipal;
import io.resys.permission.client.api.model.ImmutableCreateRole;
import io.resys.permission.client.api.model.Principal;
import io.resys.permission.client.api.model.Principal.Permission;
import io.resys.permission.client.api.model.Principal.Role;
import io.resys.permission.client.tests.config.DbTestTemplate;
import io.resys.permission.client.tests.config.OrgPgProfile;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuarkusTest
@TestProfile(OrgPgProfile.class)
public class PrincipalCreateAndUpdateTest extends DbTestTemplate {
  

  
  private Permission createPermission(PermissionClient client, String name) {
    
    return client.createPermission().createOne(ImmutableCreatePermission.builder()
        .comment("Needed for training purposes")
        .name(name)
        .description("Frontoffice resource viewing")
        .build())
        .await().atMost(Duration.ofMinutes(5));
  }
  
  private Role createRoleWithoutPermissions(PermissionClient client, String roleName) {
    
    return client.createRole().createOne(ImmutableCreateRole.builder()
        .comment("Trainee-group")
        .name(roleName)
        .description("View-only for interns")
        .build())
        .await().atMost(Duration.ofMinutes(5));
  }
  
  
  private Principal createPrincipal(PermissionClient client) {
    
    return client.createPrincipal().createOne(ImmutableCreatePrincipal.builder()
        .name("Dwane Johnson")
        .email("the-rock@muscles.org")
        .comment("created new user")
        .addPermissions(
            createPermission(client, "perm: Aii7777").getName(),
            createPermission(client, "perm: EDIT_LIMITED").getName()
            )
        .addRoles(
            createRoleWithoutPermissions(client, "role: INTERNS_454").getName()
            )
        .build())
        .await().atMost(Duration.ofMinutes(5));
  }
  
  
  @Test
  public void createPrincipalAndUpdateTest() {
    final PermissionClient client = getClient().repoQuery()
        .repoName("PrincipalCreateAndUpdateTest-1")
        .create()
        .await().atMost(Duration.ofMinutes(5));
    
    final var createdPrincipal = createPrincipal(client);

    
    Assertions.assertEquals("Dwane Johnson", createdPrincipal.getName());    
    Assertions.assertEquals(1, createdPrincipal.getRoles().size());
    Assertions.assertEquals(2, createdPrincipal.getPermissions().size());
    
    log.debug(Json.encodePrettily(createdPrincipal));
  }
}
