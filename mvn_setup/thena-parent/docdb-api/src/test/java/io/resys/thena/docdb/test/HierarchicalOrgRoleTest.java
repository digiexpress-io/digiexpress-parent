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
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgParty;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMember;
import io.resys.thena.docdb.test.config.DbTestTemplate;
import io.resys.thena.docdb.test.config.PgProfile;
import lombok.extern.slf4j.Slf4j;



@QuarkusTest
@TestProfile(PgProfile.class)
@Slf4j
public class HierarchicalOrgRoleTest extends DbTestTemplate {

  @Value.Immutable
  public interface TestContent extends Serializable {
    String getId();
    String getName();
  }

  
  @SuppressWarnings("unused")
  @Test
  public void createRepoAndUserGroups() {
    // create project
    RepoResult repo = getClient().repo().projectBuilder()
        .name("HierarchicalOrgRoleTest-1", RepoType.org)
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
    final var child2_1 = createChildGroup("child-2.1", root2.getId(), repo);
    final var child2_2 = createChildGroup("child-2.2", root2.getId(), repo);
    final var child2_3 = createChildGroup("child-2.3", root2.getId(), repo);
    final var child2_4 = createChildGroup("child-2.4", root2.getId(), repo);
    
    final var root3 = createRootGroup("group-3", repo);
    final var child3_1 = createChildGroup("child-3.1", root3.getId(), repo);
    final var child3_2 = createChildGroup("child-3.2", root3.getId(), repo);
    final var child3_3 = createChildGroup("child-3.3", root3.getId(), repo);
    final var child3_4 = createChildGroup("child-3.4", root3.getId(), repo);
    
    
    final var userId1 = createUser("user-1", repo, Arrays.asList(root1), Collections.emptyList());
    final var userId2 = createUser("user-2", repo, Arrays.asList(child1_2_2), Arrays.asList(jailer4));

    
    // user 2 sanity
    var roleHierarchy = getClient().org().find().roleHierarchyQuery()
        .repoId(repo.getRepo().getId())
        .get(jailer3.getId()).await().atMost(Duration.ofMinutes(1)).getObjects();
    
    Assertions.assertEquals("""
jailer-3
`--- child-1.2.2 <= direct role
     `--- user-2
        """, roleHierarchy.getLog());
    
    // modify user 2
    getClient().org().commit().modifyOneUser()
        .repoId(repo.getRepo().getId())
        .userId(userId2.getId())
        .groups(ModType.ADD, Arrays.asList(root1.getId()))
        .roles(ModType.ADD, Arrays.asList(bakerMain.getId()))
        .userName("super-user")
        .externalId("ext-1")
        .email("em@mod.com")
        .author("au")
        .message("mod for user")
        .build().await().atMost(Duration.ofMinutes(1))
        .getUser();
    
    roleHierarchy = getClient().org().find().roleHierarchyQuery()
        .repoId(repo.getRepo().getId())
        .get(jailer1.getId()).await().atMost(Duration.ofMinutes(1)).getObjects(); 
    Assertions.assertEquals("""
jailer-1
`--- group-1 <= direct role
     +--- user-1
     +--- super-user
     `--- child-1.1
        """, roleHierarchy.getLog());
    
    
    // remove user 2 from child-1.2.2 group
    getClient().org().commit().modifyOneUser()
        .repoId(repo.getRepo().getId())
        .userId(userId2.getId())
        .groups(ModType.DISABLED, Arrays.asList(child1_2_2.getId()))
        .author("au")
        .message("mod for user")
        .build().await().atMost(Duration.ofMinutes(1))
        .getUser();
    
    roleHierarchy = getClient().org().find().roleHierarchyQuery()
        .repoId(repo.getRepo().getId())
        .get(jailer2.getId()).await().atMost(Duration.ofMinutes(1)).getObjects(); 
    Assertions.assertEquals("""
jailer-2
`--- child-1.2.2 <= direct role
        """, roleHierarchy.getLog());
    
    
    // Reject changes because there are non
    final var rejectNoChanges = getClient().org().commit().modifyOneUser()
      .repoId(repo.getRepo().getId())
      .userId(userId2.getId())
      .groups(ModType.DISABLED, Arrays.asList(child1_2_2.getId()))
      .author("au")
      .message("mod for user")
      .build().await().atMost(Duration.ofMinutes(1))
      .getStatus();
    Assertions.assertEquals(Repo.CommitResultStatus.NO_CHANGES, rejectNoChanges);
    
    roleHierarchy = getClient().org().find().roleHierarchyQuery()
        .repoId(repo.getRepo().getId())
        .get(bakerMain.getId()).await().atMost(Duration.ofMinutes(1)).getObjects(); 
    Assertions.assertEquals("""
baker-main
        """, roleHierarchy.getLog());
    //printRepo(repo.getRepo()); //LOG THE DB 
  }

  
  private OrgMember createUser(String userName, RepoResult repo, List<OrgParty> groups, List<OrgRight> roles) {
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
  
  private OrgParty createRootGroup(String groupName, RepoResult repo, OrgRight ...roles) {
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
  
  private OrgParty createChildGroup(String groupName, String parentId, RepoResult repo, OrgRight ...roles) {
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
  
  private OrgRight createRole(RepoResult repo, String roleName) {
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
