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
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroup;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUser;
import io.resys.thena.docdb.test.config.DbTestTemplate;
import io.resys.thena.docdb.test.config.PgProfile;
import lombok.extern.slf4j.Slf4j;


@QuarkusTest
@TestProfile(PgProfile.class)
@Slf4j
public class HierarchicalOrgTest extends DbTestTemplate {

  @Value.Immutable
  public interface TestContent extends Serializable {
    String getId();
    String getName();
  }

  
  @Test
  public void createRepoAndUserGroups() {
    // create project
    RepoResult repo = getClient().repo().projectBuilder()
        .name("HierarchicalOrgTest-1", RepoType.org)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(RepoStatus.OK, repo.getStatus());

    final var jailer1 = createRole(repo, "failer-1");
    final var jailer2 = createRole(repo, "failer-2");
    final var jailer3 = createRole(repo, "failer-3");
        
    final var root1 = createRootGroup("group-1", repo, jailer1);
    final var child1_1 = createChildGroup("child-1.1", root1.getId(), repo);
    final var child1_2 = createChildGroup("child-1.2", root1.getId(), repo);
    final var child1_2_1 = createChildGroup("child-1.2.1", child1_2.getId(), repo);
    final var child1_2_2 = createChildGroup("child-1.2.2", child1_2.getId(), repo, jailer2, jailer3);
    final var child1_3 = createChildGroup("child-1.3", root1.getId(), repo);
    final var child1_4 = createChildGroup("child-1.4", root1.getId(), repo);
    
    final var root2 = createRootGroup("group-2", repo);
    final var child2_1 = createChildGroup("child-2.1", root1.getId(), repo);
    final var child2_2 = createChildGroup("child-2.2", root1.getId(), repo);
    final var child2_3 = createChildGroup("child-2.3", root1.getId(), repo);
    final var child2_4 = createChildGroup("child-2.4", root1.getId(), repo);
    
    final var root3 = createRootGroup("group-3", repo);
    final var child3_1 = createChildGroup("child-3.1", root1.getId(), repo);
    final var child3_2 = createChildGroup("child-3.2", root1.getId(), repo);
    final var child3_3 = createChildGroup("child-3.3", root1.getId(), repo);
    final var child3_4 = createChildGroup("child-3.4", root1.getId(), repo);
    
    
    final var userId1 = createUser("user-1", repo, root1);
    final var userId2 = createUser("user-2", repo, child1_2_2);

    /*
    final var userGroupsAndRoles1 = getClient().org().find().userGroupsAndRolesQuery()
        .repoId(repo.getRepo().getId())
        .get(userId1.getId()).await().atMost(Duration.ofMinutes(1));
    */
    final var userGroupsAndRoles2 = getClient().org().find().userGroupsAndRolesQuery()
        .repoId(repo.getRepo().getId())
        .get(userId2.getId()).await().atMost(Duration.ofMinutes(1));
    
    
    //printRepo(repo.getRepo());
    
  }

  
  private OrgUser createUser(String userName, RepoResult repo, OrgGroup ...groups) {
    return getClient().org().commit().createOneUser()
        .repoId(repo.getRepo().getId())
        .addUserToGroups(Arrays.asList(groups).stream().map(group -> group.getId()).toList())
        .userName(userName)
        .email("em-")
        .author("au-")
        .message("me-")
        .externalId(null)
        .build().await().atMost(Duration.ofMinutes(1))
        .getUser();
  }
  
  private OrgGroup createRootGroup(String groupName, RepoResult repo, OrgRole ...roles) {
    return getClient().org().commit().createOneGroup()
        .repoId(repo.getRepo().getId())
        .groupName(groupName)
        .groupDescription("gd-")
        .addRolesToGroup(
      		Arrays.asList(roles).stream()
      		.map(e -> e.getId())
      		.toList()
        )
        .author("ar-")
        .message("me-")
        .externalId(null)
        .build().await().atMost(Duration.ofMinutes(1)).getGroup();
  }
  
  private OrgGroup createChildGroup(String groupName, String parentId, RepoResult repo, OrgRole ...roles) {
    return getClient().org().commit().createOneGroup()
        .repoId(repo.getRepo().getId())
        .groupName(groupName)
        .groupDescription("gd-")
        .parentId(parentId)
        .addRolesToGroup(
      		Arrays.asList(roles).stream()
      		.map(e -> e.getId())
      		.toList()
        )
        .author("ar-")
        .message("me-")
        .externalId(null)
        .build().await().atMost(Duration.ofMinutes(1)).getGroup();
  }
  
  private OrgRole createRole(RepoResult repo, String roleName) {
    return getClient().org().commit().createOneRole()
        .repoId(repo.getRepo().getId())
        .roleName(roleName)
        .roleDescription("rd-")
        .author("ar-")
        .message("me-")
        .externalId(null)
        .build().await().atMost(Duration.ofMinutes(1)).getRole();
  }  

}
