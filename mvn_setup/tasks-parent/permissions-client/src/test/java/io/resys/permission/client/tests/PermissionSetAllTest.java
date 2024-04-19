package io.resys.permission.client.tests;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.model.ChangeType;
import io.resys.permission.client.api.model.ImmutableChangePermissionPrincipals;
import io.resys.permission.client.api.model.ImmutableChangePermissionRoles;
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
public class PermissionSetAllTest extends DbTestTemplate {

  private Role createRole(PermissionClient client, String name) {
    
    return client.createRole().createOne(ImmutableCreateRole.builder()
        .name(name)
        .description("desc of role")
        .comment("Created this for testing")
        .build())
        .await().atMost(Duration.ofMinutes(1)); 
  }
  
  
private Principal createPrincipal(PermissionClient client, String name) {
    
    return client.createPrincipal().createOne(ImmutableCreatePrincipal.builder()
        .name(name)
        .email("james@gmail.com")
        .comment("created new user")
        .build())
        .await().atMost(Duration.ofMinutes(5));
  }
  
  
  private Permission createPermission(PermissionClient client, String name) {
    return client.createPermission().createOne(ImmutableCreatePermission.builder()
        .name(name)
        .description("desc of permission for set all")
        .comment("created a new permission")
        .build())
        .await().atMost(Duration.ofMinutes(1));
}
  
  @Test
  public void createPermissionSetAllTest() {
    
    final PermissionClient client = getClient().tenantQuery()
        .repoName("PermissionSetAllTest-1")
        .create()
        .await().atMost(Duration.ofMinutes(1));
    
    final var permission1 = createPermission(client, "permissionForSetAll");
    
    final var updateRoles = ImmutableChangePermissionRoles.builder()
        .id(permission1.getId())
        .comment("added all roles")
        .changeType(ChangeType.SET_ALL)
        .addRoles(
            createRole(client, "role1").getName(),
            createRole(client, "role2").getName(),
            createRole(client, "role3").getName()
            )
        .build();
    
    final var updatePrincipals = ImmutableChangePermissionPrincipals.builder()
        .id(permission1.getId())
        .comment("added all principals")
        .changeType(ChangeType.SET_ALL)
        .addPrincipals(
            createPrincipal(client, "AmySmith").getName(),
            createPrincipal(client, "JohnDoe").getName(),
            createPrincipal(client, "CommanderONeil").getName()
            )
        .build();
    
    
    final var updatedPermission1 = client.updatePermission().updateOne(updatePrincipals).await().atMost(Duration.ofMinutes(1));
    
    
    log.debug(Json.encodePrettily(permission1));
    log.debug(Json.encodePrettily(updatedPermission1));

    
    
  }
  
}
