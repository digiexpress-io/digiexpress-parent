package io.resys.thena.docdb.test;

import java.io.Serializable;
import java.time.Duration;
import java.util.Arrays;

import org.immutables.value.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import io.resys.thena.api.actions.TenantActions.TenantStatus;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.docdb.test.config.DbTestTemplate;
import io.resys.thena.docdb.test.config.PgProfile;
import lombok.extern.slf4j.Slf4j;


@QuarkusTest
@TestProfile(PgProfile.class)
@Slf4j
public class SimpleOrgTest extends DbTestTemplate {

  @Value.Immutable
  public interface TestContent extends Serializable {
    String getId();
    String getName();
  }

  @Test
  public void createRepoAndUser() {
    // create project
    TenantCommitResult repo = getClient().tenants().commit()
        .name("SimpleOrgTest-1-createRepoAndUser", StructureType.org)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(TenantStatus.OK, repo.getStatus());
    
    getClient().org(repo).commit().createOneMember()
      .userName("sam vimes")
      .email("sam.vimes@digiexpress.io")
      .author("nobby nobbs")
      .message("my first user")
      .externalId("captain-of-the-guard")
      .build().await().atMost(Duration.ofMinutes(1));
    
    final var users = getClient().org(repo).find().memberQuery()
        .findAll().await().atMost(Duration.ofMinutes(1));
    
    Assertions.assertEquals(1, users.getObjects().size());
    
    printRepo(repo.getRepo());
  }
  
  
  @Test
  public void createRepoAndUserGroups() {
    // create project
    TenantCommitResult repo = getClient().tenants().commit()
        .name("SimpleOrgTest-1-createRepoAndUserGroups", StructureType.org)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(TenantStatus.OK, repo.getStatus());

    
    final var jailerRole = getClient().org(repo).commit().createOneRight()
      .rightName("jailer")
      .rightDescription("role for all jailers")
      .author("nobby nobbs")
      .message("my first role")
      .externalId("role for all the guardsmen")
      .build().await().atMost(Duration.ofMinutes(1)).getRight();

    final var detectiveRole = getClient().org(repo).commit().createOneRight()
      .rightName("detective")
      .rightDescription("role for all the detective doing investigations and things")
      .author("nobby nobbs")
      .message("my second role")
      .externalId("role for all the detective")
      .build().await().atMost(Duration.ofMinutes(1)).getRight();
        
    final var group = getClient().org(repo).commit().createOneParty()
      .partyName("captains")
      .partyDescription("group for all the captains of the guard")
      .addRightsToParty(Arrays.asList(jailerRole.getId(), detectiveRole.getId()))
      .author("nobby nobbs")
      .message("my first group")
      .externalId("one-man-group")
      .build().await().atMost(Duration.ofMinutes(1)).getParty();
      
    
    getClient().org(repo).commit().createOneMember()
      .addMemberToParties(group.getId())
      .userName("sam vimes")
      .email("sam.vimes@digiexpress.io")
      .author("nobby nobbs")
      .message("my first user")
      .externalId("captain-of-the-guard")
      .build().await().atMost(Duration.ofMinutes(1));
    
    final var users = getClient().org(repo).find().memberQuery()
        .findAll().await().atMost(Duration.ofMinutes(1));
    
    Assertions.assertEquals(1, users.getObjects().size());
    
    printRepo(repo.getRepo());
    
    assertRepo(repo.getRepo(), "org-db-test-cases/org-crud-test-1.txt");
  }
  
  
  

}
