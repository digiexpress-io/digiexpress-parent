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
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import io.resys.thena.api.actions.TenantActions.CommitStatus;
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
public class HierarchicalOrgRightQueryTest extends DbTestTemplate {

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
        .name("HierarchicalOrgRightQueryTest-1", StructureType.org)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(CommitStatus.OK, repo.getStatus());

    final var jailer1 = createRight(repo, "jailer-1");
    final var jailer2 = createRight(repo, "jailer-2");
    final var jailer3 = createRight(repo, "jailer-3");
    final var jailer4 = createRight(repo, "jailer-main");
    final var bakerMain = createRight(repo, "baker-main");
        
    final var root1 = createRootParty("group-1", repo, jailer1);
    final var child1_1 = createChildParty("child-1.1", root1.getId(), repo);
    final var child1_2 = createChildParty("child-1.2", root1.getId(), repo);
    final var child1_2_1 = createChildParty("child-1.2.1", child1_2.getId(), repo);
    final var child1_2_2 = createChildParty("child-1.2.2", child1_2.getId(), repo, jailer2, jailer3);
    final var child1_3 = createChildParty("child-1.3", root1.getId(), repo);
    final var child1_4 = createChildParty("child-1.4", root1.getId(), repo);
    
    final var root2 = createRootParty("group-2", repo);
    final var child2_1 = createChildParty("child-2.1", root2.getId(), repo);
    final var child2_2 = createChildParty("child-2.2", root2.getId(), repo);
    final var child2_3 = createChildParty("child-2.3", root2.getId(), repo);
    final var child2_4 = createChildParty("child-2.4", root2.getId(), repo);
    
    final var root3 = createRootParty("group-3", repo);
    final var child3_1 = createChildParty("child-3.1", root3.getId(), repo);
    final var child3_2 = createChildParty("child-3.2", root3.getId(), repo);
    final var child3_3 = createChildParty("child-3.3", root3.getId(), repo);
    final var child3_4 = createChildParty("child-3.4", root3.getId(), repo);
    
    
    final var userId1 = createMember("user-1", repo, Arrays.asList(root1), Collections.emptyList());
    final var userId2 = createMember("user-2", repo, Arrays.asList(child1_2_2), Arrays.asList(jailer4));
    assertRepo(repo.getRepo(), "HierarchicalOrgRightQueryTest/data-created.txt");
    
    { // user 2 sanity
    final var roleHierarchy = getClient().org(repo).find().rightHierarchyQuery()
        .get(jailer3.getId()).await().atMost(Duration.ofMinutes(1)).getObjects();
    
    Assertions.assertEquals("""
jailer-3
`--- child-1.2.2 <= direct role
     `--- user-2
        """, roleHierarchy.getLog());
    }
    
    { // modify user 2
    getClient().org(repo).commit().modifyOneMember()
        .memberId(userId2.getId())
        .modifyParties(ModType.ADD, root1.getId())
        .modifyRights(ModType.ADD, bakerMain.getId())
        .userName("super-user")
        .externalId("ext-1")
        .email("em@mod.com")
        .author("au")
        .message("mod for user")
        .build().await().atMost(Duration.ofMinutes(1))
        .getMember();
    assertRepo(repo.getRepo(), "HierarchicalOrgRightQueryTest/data-add-rights-parties.txt");
    
    
    final var roleHierarchy = getClient().org(repo).find().rightHierarchyQuery()
        .get(jailer1.getId()).await().atMost(Duration.ofMinutes(1)).getObjects(); 
    Assertions.assertEquals("""
jailer-1
`--- group-1 <= direct role
     +--- user-1
     +--- super-user
     `--- child-1.1
        """, roleHierarchy.getLog());
    }
    
    {// remove user 2 from child-1.2.2 group
    getClient().org(repo).commit().modifyOneMember()

        .memberId(userId2.getId())
        .modifyParties(ModType.DISABLED, child1_2_2.getId())
        .author("au")
        .message("mod for user")
        .build().await().atMost(Duration.ofMinutes(1))
        .getMember();
    assertRepo(repo.getRepo(), "HierarchicalOrgRightQueryTest/data-remove-party.txt");
    
    
    final var roleHierarchy = getClient().org(repo).find().rightHierarchyQuery()
        .get(jailer2.getId()).await().atMost(Duration.ofMinutes(1)).getObjects(); 
    Assertions.assertEquals("""
jailer-2
`--- child-1.2.2 <= direct role
        """, roleHierarchy.getLog());
    }
    
    {// Reject changes because there are non
    final var rejectNoChanges = getClient().org(repo).commit().modifyOneMember()
      .memberId(userId2.getId())
      .modifyParties(ModType.DISABLED, child1_2_2.getId())
      .author("au")
      .message("mod for user")
      .build().await().atMost(Duration.ofMinutes(1))
      .getStatus();
    Assertions.assertEquals(CommitResultStatus.NO_CHANGES, rejectNoChanges);
    assertRepo(repo.getRepo(), "HierarchicalOrgRightQueryTest/no-changes.txt");
    
    
    final var roleHierarchy = getClient().org(repo).find().rightHierarchyQuery()
        .get(bakerMain.getId()).await().atMost(Duration.ofMinutes(1)).getObjects(); 
    Assertions.assertEquals("""
baker-main
        """, roleHierarchy.getLog());
    }
  }

  
  private OrgMember createMember(String userName, TenantCommitResult repo, List<OrgParty> groups, List<OrgRight> roles) {
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
  
  private OrgParty createRootParty(String groupName, TenantCommitResult repo, OrgRight ...roles) {
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
  
  private OrgParty createChildParty(String groupName, String parentId, TenantCommitResult repo, OrgRight ...roles) {
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
