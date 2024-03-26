package io.resys.permission.client.tests;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.model.ImmutableChangePrincipalRoles;
import io.resys.permission.client.api.model.PermissionCommand.ChangeType;
import io.resys.permission.client.tests.config.DbTestTemplate;
import io.resys.permission.client.tests.config.GenerateTestData;
import io.resys.permission.client.tests.config.OrgPgProfile;
import io.resys.thena.api.models.Repo;
import lombok.extern.slf4j.Slf4j;

@QuarkusTest
@TestProfile(OrgPgProfile.class)
@Slf4j
public class PrincipalUpdateTest extends DbTestTemplate {

  @Disabled
  @Test
  public void getPrincipalAndAddRole() {
   
  
    final PermissionClient client = getClient().repoQuery()
    .repoName("PrincipalUpdateTest-1")
    .create()
    .await().atMost(Duration.ofMinutes(1));

    final Repo repo = client.getRepo().await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    new GenerateTestData(getDocDb()).populate(repo);
    
    List<String> roles = Arrays.asList("testitalo", "kakkulaari", "Lapiotehdas");
      
    final var updated = client.updatePrincipal().updateOne(ImmutableChangePrincipalRoles.builder()
      .id(null)
      .roles(roles)
      .changeType(ChangeType.ADD)
      .comment("New roles needed to access ABC")
      .build())
    .await().atMost(Duration.ofMinutes(5));
    
    log.debug("Updated principal: {}", updated);
    
  }
}
   

