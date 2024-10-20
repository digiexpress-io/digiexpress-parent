package io.resys.thena.docdb.test.org;

import java.io.Serializable;
import java.time.Duration;
import java.util.Arrays;
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
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.OrgRight;
import io.resys.thena.docdb.test.config.DbTestTemplate;
import io.resys.thena.docdb.test.config.PgProfile;
import lombok.extern.slf4j.Slf4j;



@QuarkusTest
@TestProfile(PgProfile.class)
@Slf4j
public class HierarchicalOrgPartyQueryTest extends DbTestTemplate {

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
        .name("HierarchicalOrgGroupTest-1", StructureType.org)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(CommitStatus.OK, repo.getStatus());

    final var jailer1 = createRight(repo, "jailer-1");
    final var jailer2 = createRight(repo, "jailer-2");
    final var jailer3 = createRight(repo, "jailer-3");
    final var jailer4 = createRight(repo, "jailer-main");
    final var bakerMain = createRight(repo, "baker-main");
        
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
    
    

    final var userId1 =  getClient().org(repo).commit().createOneMember()
        .userName("user-1")
        .email("em-")
        .author("au-")
        .message("me-")
        .addMemberToParties(root1.getId())
        .addMemberToPartyRight(root1.getId(), Arrays.asList("jailer-1")) //permission for party with user constraint
        .build().await().atMost(Duration.ofMinutes(1))
        .getMember();
    
    
    final var userId2 = createUser("user-2", repo, Arrays.asList(child1_2_2), Arrays.asList(jailer4));

    
    // user 2 sanity
    var groupHierarchy = getClient().org(repo).find().partyHierarchyQuery()
        .get(child1_2_1.getId()).await().atMost(Duration.ofMinutes(1)).getObjects();
    
    Assertions.assertEquals("""
group-1
+--- roles
|    `--- jailer-1
+--- users
|    `--- user-1 (jailer-1)
+--- child-1.1
|    `--- users
|         `--- user-1::inherited (jailer-1::inherited)
+--- child-1.2
|    +--- users
|    |    `--- user-1::inherited (jailer-1::inherited)
|    +--- child-1.2.1 <= you are here
|    |    `--- users
|    |         `--- user-1::inherited (jailer-1::inherited)
|    `--- child-1.2.2
|         +--- roles
|         |    +--- jailer-2
|         |    `--- jailer-3
|         `--- users
|              +--- user-2
|              `--- user-1::inherited (jailer-1::inherited)
+--- child-1.3
|    `--- users
|         `--- user-1::inherited (jailer-1::inherited)
`--- child-1.4
     `--- users
          `--- user-1::inherited (jailer-1::inherited)
        """, groupHierarchy.getLog());

    // modify user 2
    getClient().org(repo).commit().modifyOneMember()
        .memberId(userId2.getId())
        .modifyParties(ModType.ADD, root1.getId())
        .modifyRights(ModType.ADD, bakerMain.getId())
        .modifyPartyRight(ModType.ADD, root1.getId(), "jailer-1")
        .userName("super-user")
        .externalId("ext-1")
        .email("em@mod.com")
        .author("au")
        .message("mod for user")
        .build().await().atMost(Duration.ofMinutes(1))
        .getMember();
    
    //.addMemberToPartyRight(root1.getId(), Arrays.asList("jailer-1")) //permission for party with user constraint
    
    groupHierarchy = getClient().org(repo).find().partyHierarchyQuery()
        .get(child1_2_2.getId()).await().atMost(Duration.ofMinutes(1)).getObjects(); 
    Assertions.assertEquals("""
group-1
+--- roles
|    `--- jailer-1
+--- users
|    +--- user-1 (jailer-1)
|    `--- super-user (jailer-1)
+--- child-1.1
|    `--- users
|         +--- user-1::inherited (jailer-1::inherited)
|         `--- super-user::inherited (jailer-1::inherited)
+--- child-1.2
|    +--- users
|    |    +--- user-1::inherited (jailer-1::inherited)
|    |    `--- super-user::inherited (jailer-1::inherited)
|    +--- child-1.2.1
|    |    `--- users
|    |         +--- user-1::inherited (jailer-1::inherited)
|    |         `--- super-user::inherited (jailer-1::inherited)
|    `--- child-1.2.2 <= you are here
|         +--- roles
|         |    +--- jailer-2
|         |    `--- jailer-3
|         `--- users
|              +--- super-user (jailer-1::inherited)
|              `--- user-1::inherited (jailer-1::inherited)
+--- child-1.3
|    `--- users
|         +--- user-1::inherited (jailer-1::inherited)
|         `--- super-user::inherited (jailer-1::inherited)
`--- child-1.4
     `--- users
          +--- user-1::inherited (jailer-1::inherited)
          `--- super-user::inherited (jailer-1::inherited)
        """, groupHierarchy.getLog());
    
    
    // remove user 2 from child-1.2.2 group
    getClient().org(repo).commit().modifyOneMember()
        .memberId(userId2.getId())
        .modifyParties(ModType.REMOVE, child1_2_2.getId())
        .author("au")
        .message("mod for user")
        .build().await().atMost(Duration.ofMinutes(1))
        .getMember();
    
    groupHierarchy = getClient().org(repo).find().partyHierarchyQuery()
        .get(child1_2_2.getId()).await().atMost(Duration.ofMinutes(1)).getObjects(); 
    Assertions.assertEquals("""
group-1
+--- roles
|    `--- jailer-1
+--- users
|    +--- user-1 (jailer-1)
|    `--- super-user (jailer-1)
+--- child-1.1
|    `--- users
|         +--- user-1::inherited (jailer-1::inherited)
|         `--- super-user::inherited (jailer-1::inherited)
+--- child-1.2
|    +--- users
|    |    +--- user-1::inherited (jailer-1::inherited)
|    |    `--- super-user::inherited (jailer-1::inherited)
|    +--- child-1.2.1
|    |    `--- users
|    |         +--- user-1::inherited (jailer-1::inherited)
|    |         `--- super-user::inherited (jailer-1::inherited)
|    `--- child-1.2.2 <= you are here
|         +--- roles
|         |    +--- jailer-2
|         |    `--- jailer-3
|         `--- users
|              +--- user-1::inherited (jailer-1::inherited)
|              `--- super-user::inherited (jailer-1::inherited)
+--- child-1.3
|    `--- users
|         +--- user-1::inherited (jailer-1::inherited)
|         `--- super-user::inherited (jailer-1::inherited)
`--- child-1.4
     `--- users
          +--- user-1::inherited (jailer-1::inherited)
          `--- super-user::inherited (jailer-1::inherited)
        """, groupHierarchy.getLog());
    
    
    // Reject changes because there are non
    final var rejectNoChanges = getClient().org(repo).commit().modifyOneMember()
      .memberId(userId2.getId())
      .modifyParties(ModType.REMOVE, child1_2_2.getId())
      .author("au")
      .message("mod for user")
      .build().await().atMost(Duration.ofMinutes(1))
      .getStatus();
    Assertions.assertEquals(CommitResultStatus.NO_CHANGES, rejectNoChanges);
    
    groupHierarchy = getClient().org(repo).find().partyHierarchyQuery()
        .get(root2.getId()).await().atMost(Duration.ofMinutes(1)).getObjects(); 
    Assertions.assertEquals("""
group-2 <= you are here
+--- child-2.1
+--- child-2.2
+--- child-2.3
`--- child-2.4
        """, groupHierarchy.getLog());
    //printRepo(repo.getRepo()); //LOG THE DB 
  }

  
  private OrgMember createUser(String userName, TenantCommitResult repo, List<OrgParty> parties, List<OrgRight> roles) {
    return getClient().org(repo).commit().createOneMember()
        .addMemberToParties(parties.stream().map(group -> group.getId()).toList())
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
  
  private OrgRight createRight(TenantCommitResult repo, String roleName) {
    return getClient().org(repo).commit().createOneRight()
        .rightName(roleName)
        .rightDescription("rd-")
        .author("ar-")
        .message("me-")
        .externalId(null)
        .build().await().atMost(Duration.ofMinutes(1)).getRight();
  }  

}
