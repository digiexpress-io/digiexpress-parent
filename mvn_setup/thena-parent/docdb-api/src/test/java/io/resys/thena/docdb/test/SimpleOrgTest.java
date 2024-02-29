package io.resys.thena.docdb.test;

import java.io.Serializable;
import java.time.Duration;
import java.util.Arrays;

import org.immutables.value.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.docdb.api.actions.RepoActions.RepoResult;
import io.resys.thena.docdb.api.actions.RepoActions.RepoStatus;
import io.resys.thena.docdb.api.models.Repo.RepoType;
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
    RepoResult repo = getClient().repo().projectBuilder()
        .name("SimpleOrgTest-1-createRepoAndUser", RepoType.org)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(RepoStatus.OK, repo.getStatus());
    
    getClient().org().commit().createOneUser()
      .repoId(repo.getRepo().getId())
      .userName("sam vimes")
      .email("sam.vimes@digiexpress.io")
      .author("nobby nobbs")
      .message("my first user")
      .externalId("captain-of-the-guard")
      .build().await().atMost(Duration.ofMinutes(1));
    
    final var users = getClient().org().find().userQuery()
        .repoId(repo.getRepo().getId())
        .findAll().await().atMost(Duration.ofMinutes(1));
    
    Assertions.assertEquals(1, users.getObjects().getUsers().size());
    
    printRepo(repo.getRepo());
  }
  
  
  @Test
  public void createRepoAndUserGroups() {
    // create project
    RepoResult repo = getClient().repo().projectBuilder()
        .name("SimpleOrgTest-1-createRepoAndUserGroups", RepoType.org)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(RepoStatus.OK, repo.getStatus());

    
    final var jailerRole = getClient().org().commit().createOneRole()
      .repoId(repo.getRepo().getId())
      .roleName("jailer")
      .roleDescription("role for all jailers")
      .author("nobby nobbs")
      .message("my first role")
      .externalId("role for all the guardsmen")
      .build().await().atMost(Duration.ofMinutes(1)).getRole();

    final var detectiveRole = getClient().org().commit().createOneRole()
      .repoId(repo.getRepo().getId())
      .roleName("detective")
      .roleDescription("role for all the detective doing investigations and things")
      .author("nobby nobbs")
      .message("my second role")
      .externalId("role for all the detective")
      .build().await().atMost(Duration.ofMinutes(1)).getRole();
        
    final var group = getClient().org().commit().createOneGroup()
      .repoId(repo.getRepo().getId())
      .groupName("captains")
      .groupDescription("group for all the captains of the guard")
      .addRolesToGroup(Arrays.asList(jailerRole.getId(), detectiveRole.getId()))
      .author("nobby nobbs")
      .message("my first group")
      .externalId("one-man-group")
      .build().await().atMost(Duration.ofMinutes(1)).getGroup();
      
    
    getClient().org().commit().createOneUser()
      .repoId(repo.getRepo().getId())
      .addUserToGroups(group.getId())
      .userName("sam vimes")
      .email("sam.vimes@digiexpress.io")
      .author("nobby nobbs")
      .message("my first user")
      .externalId("captain-of-the-guard")
      .build().await().atMost(Duration.ofMinutes(1));
    
    final var users = getClient().org().find().userQuery()
        .repoId(repo.getRepo().getId())
        .findAll().await().atMost(Duration.ofMinutes(1));
    
    Assertions.assertEquals(1, users.getObjects().getUsers().size());
    
    printRepo(repo.getRepo());
    
    assertRepo(repo.getRepo(), "org-db-test-cases/org-crud-test-1.txt");
  }
  
  
  

}
