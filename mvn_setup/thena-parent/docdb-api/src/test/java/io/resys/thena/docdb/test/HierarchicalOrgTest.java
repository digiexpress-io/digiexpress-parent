package io.resys.thena.docdb.test;

import java.io.Serializable;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.immutables.value.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.docdb.api.actions.OrgCommitActions.ModType;
import io.resys.thena.docdb.api.actions.RepoActions.RepoResult;
import io.resys.thena.docdb.api.actions.RepoActions.RepoStatus;
import io.resys.thena.docdb.api.models.Repo;
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

    final var jailer1 = createRole(repo, "jailer-1");
    final var jailer2 = createRole(repo, "jailer-2");
    final var jailer3 = createRole(repo, "jailer-3");
    final var jailer4 = createRole(repo, "jailer-main");
    final var bakerMain = createRole(repo, "baker-main");
        
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
    
    
    final var userId1 = createUser("user-1", repo, Arrays.asList(root1), Collections.emptyList());
    final var userId2 = createUser("user-2", repo, Arrays.asList(child1_2_2), Arrays.asList(jailer4));

    /*
    final var userGroupsAndRoles1 = getClient().org().find().userGroupsAndRolesQuery()
        .repoId(repo.getRepo().getId())
        .get(userId1.getId()).await().atMost(Duration.ofMinutes(1));
    */
    
    // user 2 sanity
    var userGroupsAndRoles2 = getClient().org().find().userGroupsAndRolesQuery()
        .repoId(repo.getRepo().getId())
        .get(userId2.getId()).await().atMost(Duration.ofMinutes(1)).getObjects();
    
    Assertions.assertEquals(userId2.getId(), userGroupsAndRoles2.getUserId());
    Assertions.assertEquals("[group-1, child-1.2, child-1.2.2]", userGroupsAndRoles2.getGroupNames().toString());
    Assertions.assertEquals("[jailer-main, jailer-1, jailer-2, jailer-3]", userGroupsAndRoles2.getRoleNames().toString());
    
    Assertions.assertEquals("[child-1.2.2]", userGroupsAndRoles2.getDirectGroupNames().toString());
    Assertions.assertEquals("[jailer-main, jailer-2]", userGroupsAndRoles2.getDirectRoleNames().toString());
    Assertions.assertEquals("""
user-2
+--- group-1
|    +--- roles
|    |    `--- jailer-1
|    `--- child-1.2
|         `--- child-1.2.2
|              +--- roles
|              |    +--- jailer-2
|              |    `--- jailer-3
|              `--- direct-membership
`--- roles
     +--- direct
     |    `--- jailer-main
     `--- inherited
          +--- jailer-3
          `--- jailer-1
        """, userGroupsAndRoles2.getLog());
    
    // modify user 2
    getClient().org().commit().modifyOneUser()
        .repoId(repo.getRepo().getId())
        .userId(userGroupsAndRoles2.getUserId())
        .groups(ModType.ADD, Arrays.asList(root1.getId()))
        .roles(ModType.ADD, Arrays.asList(bakerMain.getId()))
        .userName("super-user")
        .externalId("ext-1")
        .email("em@mod.com")
        .author("au")
        .message("mod for user")
        .build().await().atMost(Duration.ofMinutes(1))
        .getUser();
    
    userGroupsAndRoles2 = getClient().org().find().userGroupsAndRolesQuery()
        .repoId(repo.getRepo().getId())
        .get(userId2.getId()).await().atMost(Duration.ofMinutes(1)).getObjects(); 
    Assertions.assertEquals("""
super-user
+--- group-1
|    +--- roles
|    |    `--- jailer-1
|    +--- direct-membership
|    `--- child-1.2
|         `--- child-1.2.2
|              +--- roles
|              |    +--- jailer-2
|              |    `--- jailer-3
|              `--- direct-membership
`--- roles
     +--- direct
     |    +--- baker-main
     |    `--- jailer-main
     `--- inherited
          +--- jailer-3
          `--- jailer-1
        """, userGroupsAndRoles2.getLog());
    
    
    // remove user 2 from child-1.2.2 group
    getClient().org().commit().modifyOneUser()
        .repoId(repo.getRepo().getId())
        .userId(userGroupsAndRoles2.getUserId())
        .groups(ModType.REMOVE, Arrays.asList(child1_2_2.getId()))
        .author("au")
        .message("mod for user")
        .build().await().atMost(Duration.ofMinutes(1))
        .getUser();
    
    userGroupsAndRoles2 = getClient().org().find().userGroupsAndRolesQuery()
        .repoId(repo.getRepo().getId())
        .get(userId2.getId()).await().atMost(Duration.ofMinutes(1)).getObjects(); 
    Assertions.assertEquals("""
super-user
+--- group-1
|    +--- roles
|    |    `--- jailer-1
|    +--- direct-membership
|    `--- child-1.2
`--- roles
     +--- direct
     |    +--- baker-main
     |    `--- jailer-main
     `--- inherited
          `--- jailer-1
        """, userGroupsAndRoles2.getLog());
    
    Assertions.assertEquals(userId2.getId(), userGroupsAndRoles2.getUserId());
    Assertions.assertEquals("[group-1, child-1.2]", userGroupsAndRoles2.getGroupNames().toString());
    Assertions.assertEquals("[jailer-main, baker-main, jailer-1]", userGroupsAndRoles2.getRoleNames().toString());
    
    Assertions.assertEquals("[group-1]", userGroupsAndRoles2.getDirectGroupNames().toString());
    Assertions.assertEquals("[jailer-main, baker-main, jailer-1]", userGroupsAndRoles2.getDirectRoleNames().toString());
    
    
    
    // Reject changes because there are non
    final var rejectNoChanges = getClient().org().commit().modifyOneUser()
      .repoId(repo.getRepo().getId())
      .userId(userGroupsAndRoles2.getUserId())
      .groups(ModType.REMOVE, Arrays.asList(child1_2_2.getId()))
      .author("au")
      .message("mod for user")
      .build().await().atMost(Duration.ofMinutes(1))
      .getStatus();
    Assertions.assertEquals(Repo.CommitResultStatus.NO_CHANGES, rejectNoChanges);
    
    userGroupsAndRoles2 = getClient().org().find().userGroupsAndRolesQuery()
        .repoId(repo.getRepo().getId())
        .get(userId2.getId()).await().atMost(Duration.ofMinutes(1)).getObjects(); 
    Assertions.assertEquals("""
super-user
+--- group-1
|    +--- roles
|    |    `--- jailer-1
|    +--- direct-membership
|    `--- child-1.2
`--- roles
     +--- direct
     |    +--- baker-main
     |    `--- jailer-main
     `--- inherited
          `--- jailer-1
        """, userGroupsAndRoles2.getLog());
    
    Assertions.assertEquals(userId2.getId(), userGroupsAndRoles2.getUserId());
    Assertions.assertEquals("[group-1, child-1.2]", userGroupsAndRoles2.getGroupNames().toString());
    Assertions.assertEquals("[jailer-main, baker-main, jailer-1]", userGroupsAndRoles2.getRoleNames().toString());
    
    Assertions.assertEquals("[group-1]", userGroupsAndRoles2.getDirectGroupNames().toString());
    Assertions.assertEquals("[jailer-main, baker-main, jailer-1]", userGroupsAndRoles2.getDirectRoleNames().toString());
  }

  
  private OrgUser createUser(String userName, RepoResult repo, List<OrgGroup> groups, List<OrgRole> roles) {
    return getClient().org().commit().createOneUser()
        .repoId(repo.getRepo().getId())
        .addUserToGroups(groups.stream().map(group -> group.getId()).toList())
        .addUserToRoles(roles.stream().map(role -> role.getId()).toList())
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
