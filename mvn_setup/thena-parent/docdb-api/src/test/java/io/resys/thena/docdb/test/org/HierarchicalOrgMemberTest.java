package io.resys.thena.docdb.test.org;

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
import io.resys.thena.api.actions.OrgCommitActions.ModType;
import io.resys.thena.api.actions.TenantActions.CommitStatus;
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.entities.org.OrgActorStatusType;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.OrgRight;
import io.resys.thena.docdb.test.config.DbTestTemplate;
import io.resys.thena.docdb.test.config.PgProfile;
import lombok.extern.slf4j.Slf4j;



@QuarkusTest
@TestProfile(PgProfile.class)
@Slf4j
public class HierarchicalOrgMemberTest extends DbTestTemplate {

  @Value.Immutable
  public interface TestContent extends Serializable {
    String getId();
    String getName();
  }

  @SuppressWarnings("unused")  
  @Test
  public void createRepoAndUserGroups() {
    // create project
    TenantCommitResult repo = getClient().tenants().commit()
        .name("HierarchicalOrgUserTest-1", StructureType.org)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(CommitStatus.OK, repo.getStatus());

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
    assertRepo(repo.getRepo(), "HierarchicalOrgMemberTest/data-created.txt");
    
    
    // user 2 sanity
    var userGroupsAndRoles2 = getClient().org(repo).find().memberHierarchyQuery()
        .get(userId2.getId()).await().atMost(Duration.ofMinutes(1)).getObjects();
    
    
    Assertions.assertEquals(userId2.getId(), userGroupsAndRoles2.getUserId());
    Assertions.assertEquals("[child-1.2.2, child-1.2, group-1]", userGroupsAndRoles2.getGroupNames().toString());
    Assertions.assertEquals("[jailer-main, jailer-2, jailer-3, jailer-1]", userGroupsAndRoles2.getRoleNames().toString());
    
    Assertions.assertEquals("[child-1.2.2]", userGroupsAndRoles2.getDirectGroupNames().toString());
    Assertions.assertEquals("[jailer-main, jailer-2, jailer-3]", userGroupsAndRoles2.getDirectRoleNames().toString());
    Assertions.assertEquals("""
user-2
+--- group-1
|    +--- roles
|    `--- child-1.2
|         +--- roles
|         `--- child-1.2.2::DIRECT
|              `--- roles
|                   +--- jailer-2
|                   `--- jailer-3
`--- roles
     `--- jailer-main
        """, userGroupsAndRoles2.getLog());
    
    // modify user 2
    getClient().org(repo).commit().modifyOneMember()
        .memberId(userGroupsAndRoles2.getUserId())
        .modifyParties(ModType.ADD, root1.getId())
        .modifyRights(ModType.ADD, bakerMain.getId())
        .userName("super-user")
        .externalId("ext-1")
        .email("em@mod.com")
        .author("au")
        .message("mod for user")
        .build().await().atMost(Duration.ofMinutes(1))
        .getMember();

    assertRepo(repo.getRepo(), "HierarchicalOrgMemberTest/user-modified.txt");

    userGroupsAndRoles2 = getClient().org(repo).find().memberHierarchyQuery()
        .get(userId2.getId()).await().atMost(Duration.ofMinutes(1)).getObjects(); 
    Assertions.assertEquals("""
super-user
+--- group-1::DIRECT
|    +--- roles
|    |    `--- jailer-1
|    `--- child-1.2
|         +--- roles
|         `--- child-1.2.2::DIRECT
|              `--- roles
|                   +--- jailer-2
|                   `--- jailer-3
`--- roles
     +--- jailer-main
     `--- baker-main
        """, userGroupsAndRoles2.getLog());
    
    
    getClient().org(repo).commit().modifyOneParty()
      .partyId(child1_2_2.getId())
      .status(OrgActorStatusType.DISABLED)
      .author("au")
      .message("mod for user")
      .build().await().atMost(Duration.ofMinutes(1));
      
    assertRepo(repo.getRepo(), "HierarchicalOrgMemberTest/membership-disabled.txt");
    
    userGroupsAndRoles2 = getClient().org(repo).find().memberHierarchyQuery()
        .get(userId2.getId()).await().atMost(Duration.ofMinutes(1)).getObjects(); 
    Assertions.assertEquals("""
super-user
+--- group-1::DIRECT
|    `--- roles
|         `--- jailer-1
+--- roles
|    +--- jailer-main
|    `--- baker-main
+--- grey-roles
|    +--- jailer-2
|    `--- jailer-3
`--- grey-groups
     `--- child-1.2.2
        """, userGroupsAndRoles2.getLog());
    
    Assertions.assertEquals(userId2.getId(), userGroupsAndRoles2.getUserId());
    Assertions.assertEquals("[group-1]", userGroupsAndRoles2.getGroupNames().toString());
    Assertions.assertEquals("[jailer-main, baker-main, jailer-1]", userGroupsAndRoles2.getRoleNames().toString());
    
    Assertions.assertEquals("[group-1]", userGroupsAndRoles2.getDirectGroupNames().toString());
    Assertions.assertEquals("[jailer-main, baker-main, jailer-1]", userGroupsAndRoles2.getDirectRoleNames().toString());
    
    
    
    // Reject changes because there are non
    final var rejectNoChanges = getClient().org(repo).commit().modifyOneParty()
        .partyId(child1_2_2.getId())
        .status(OrgActorStatusType.DISABLED)
        .author("au")
        .message("mod for user")
        .build().await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals(CommitResultStatus.NO_CHANGES, rejectNoChanges.getStatus());
    
    userGroupsAndRoles2 = getClient().org(repo).find().memberHierarchyQuery()
        .get(userId2.getId()).await().atMost(Duration.ofMinutes(1)).getObjects(); 
    Assertions.assertEquals("""
super-user
+--- group-1::DIRECT
|    `--- roles
|         `--- jailer-1
+--- roles
|    +--- jailer-main
|    `--- baker-main
+--- grey-roles
|    +--- jailer-2
|    `--- jailer-3
`--- grey-groups
     `--- child-1.2.2
        """, userGroupsAndRoles2.getLog());
    
    Assertions.assertEquals(userId2.getId(), userGroupsAndRoles2.getUserId());
    Assertions.assertEquals("[group-1]", userGroupsAndRoles2.getGroupNames().toString());
    Assertions.assertEquals("[jailer-main, baker-main, jailer-1]", userGroupsAndRoles2.getRoleNames().toString());
    
    Assertions.assertEquals("[group-1]", userGroupsAndRoles2.getDirectGroupNames().toString());
    Assertions.assertEquals("[jailer-main, baker-main, jailer-1]", userGroupsAndRoles2.getDirectRoleNames().toString());

     
    final var users = getClient().org(repo).find().memberHierarchyQuery()

        .findAll().await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals(2, users.getObjects().size());
    
    
    // disable member
    getClient().org(repo).commit().modifyOneMember()
      .memberId(userGroupsAndRoles2.getUserId())
      .status(OrgActorStatusType.DISABLED)
      .author("au")
      .message("mod for user")
      .build().await().atMost(Duration.ofMinutes(1))
      .getMember();
    assertRepo(repo.getRepo(), "HierarchicalOrgMemberTest/user-disabled.txt");
    
    // enable member
    getClient().org(repo).commit().modifyOneMember()
      .memberId(userGroupsAndRoles2.getUserId())
      .status(OrgActorStatusType.IN_FORCE)
      .author("au")
      .message("mod for user")
      .build().await().atMost(Duration.ofMinutes(1))
      .getMember();
    assertRepo(repo.getRepo(), "HierarchicalOrgMemberTest/user-enabled.txt");

    
    getClient().org(repo).commit().modifyOneMember()
      .memberId(userGroupsAndRoles2.getUserId())
      .modifyParties(ModType.REMOVE, child1_2_2.getId())
      .author("au")
      .message("mod for user")
      .build().await().atMost(Duration.ofMinutes(1))
      .getMember();
    assertRepo(repo.getRepo(), "HierarchicalOrgMemberTest/membership-removed.txt");

  }

  
  private OrgMember createUser(String userName, TenantCommitResult repo, List<OrgParty> groups, List<OrgRight> roles) {
    return getClient().org(repo).commit().createOneMember()
        .addMemberToParties(groups.stream().map(group -> group.getId()).toList())
        .addMemberRight(roles.stream().map(role -> role.getId()).toList())
        .userName(userName)
        .email("em-")
        .author("au-")
        .message("me-")
        .externalId(null)
        .build().await().atMost(Duration.ofMinutes(1))
        .getMember();
  }
  
  private OrgParty createRootGroup(String groupName, TenantCommitResult repo, OrgRight ...roles) {
    return getClient().org(repo).commit().createOneParty()
        .partyName(groupName)
        .partyDescription("gd-")
        .addRightsToParty(
      		Arrays.asList(roles).stream()
      		.map(e -> e.getId())
      		.toList()
        )
        .author("ar-")
        .message("me-")
        .externalId(null)
        .build().await().atMost(Duration.ofMinutes(1)).getParty();
  }
  
  private OrgParty createChildGroup(String groupName, String parentId, TenantCommitResult repo, OrgRight ...roles) {
    return getClient().org(repo).commit().createOneParty()
        .partyName(groupName)
        .partyDescription("gd-")
        .parentId(parentId)
        .addRightsToParty(
      		Arrays.asList(roles).stream()
      		.map(e -> e.getId())
      		.toList()
        )
        .author("ar-")
        .message("me-")
        .externalId(null)
        .build().await().atMost(Duration.ofMinutes(1)).getParty();
  }
  
  private OrgRight createRole(TenantCommitResult repo, String roleName) {
    return getClient().org(repo).commit().createOneRight()
        .rightName(roleName)
        .rightDescription("rd-")
        .author("ar-")
        .message("me-")
        .externalId(null)
        .build().await().atMost(Duration.ofMinutes(1)).getRight();
  }  

}
