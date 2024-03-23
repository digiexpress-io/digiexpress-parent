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
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatusType;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMember;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgParty;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRight;
import io.resys.thena.docdb.test.config.DbTestTemplate;
import io.resys.thena.docdb.test.config.PgProfile;
import lombok.extern.slf4j.Slf4j;



@QuarkusTest
@TestProfile(PgProfile.class)
@Slf4j
public class HierarchicalOrgPartyTest extends DbTestTemplate {

  @Value.Immutable
  public interface TestContent extends Serializable {
    String getId();
    String getName();
  }

  
  @SuppressWarnings("unused")
  @Test
  public void modifyParty() {
    // create project
    RepoResult repo = getClient().repo().projectBuilder()
        .name("HierarchicalOrgPartyTest-1", RepoType.org)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(RepoStatus.OK, repo.getStatus());

    final var cityGuards = createRight(repo, "city-guards");    
    final var root1 = createRootParty("group-1", repo);    
    final var child1_1 = createChildParty("child-1.1", root1.getId(), repo);
    final var child1_2 = createChildParty("child-1.2", root1.getId(), repo);
    final var child1_2_1 = createChildParty("child-1.2.1", child1_2.getId(), repo);
    final var child1_2_2 = createChildParty("child-1.2.2", child1_2.getId(), repo);
    final var child1_3 = createChildParty("child-1.3", root1.getId(), repo);
    final var child1_4 = createChildParty("child-1.4", root1.getId(), repo);
    
    final var userId1 = createMember("user-1", repo, Arrays.asList(root1), Collections.emptyList());

    assertRepo(repo.getRepo(), "HierarchicalOrgPartyTest/data-created.txt");
    
    
    { // add right and member to party
      final var changes = getClient().org().commit().modifyOneParty()
        .repoId(repo.getRepo().getId())
        .author("sam-vimes")
        .message("modify rights")
        .partyId(child1_2_2.getId())
        .modifyMember(ModType.ADD, userId1.getId())
        .modifyRight(ModType.ADD, cityGuards.getId())
        .build().await().atMost(Duration.ofMinutes(1));
        
      Assertions.assertEquals(Repo.CommitResultStatus.OK, changes.getStatus());
      assertRepo(repo.getRepo(), "HierarchicalOrgPartyTest/data-add-party-member.txt");
    }
    
    { // modify status
      final var changes = getClient().org().commit().modifyOneParty()
        .repoId(repo.getRepo().getId())
        .author("sam-vimes")
        .message("modify rights")
        .partyId(child1_2_2.getId())
        .status(OrgActorStatusType.DISABLED)
        .modifyMember(ModType.DISABLED, userId1.getId())
        .modifyRight(ModType.DISABLED, cityGuards.getId())
        .build().await().atMost(Duration.ofMinutes(1));
        
      Assertions.assertEquals(Repo.CommitResultStatus.OK, changes.getStatus());
      assertRepo(repo.getRepo(), "HierarchicalOrgPartyTest/data-disable-party-member-and-right.txt");
    }
    
    { // modify status
      final var changes = getClient().org().commit().modifyOneParty()
        .repoId(repo.getRepo().getId())
        .author("sam-vimes")
        .message("modify rights")
        .partyId(child1_2_2.getId())
        .status(OrgActorStatusType.IN_FORCE)
        .modifyMember(ModType.ADD, userId1.getId())
        .modifyRight(ModType.ADD, cityGuards.getId())
        .build().await().atMost(Duration.ofMinutes(1));
        
      Assertions.assertEquals(Repo.CommitResultStatus.OK, changes.getStatus());
      assertRepo(repo.getRepo(), "HierarchicalOrgPartyTest/data-enable-party-member.txt");
    }
    
    { // delete rights from party and member
      final var changes = getClient().org().commit().modifyOneParty()
        .repoId(repo.getRepo().getId())
        .author("sam-vimes")
        .message("modify rights")
        .partyId(child1_2_2.getId())
        .modifyMember(ModType.REMOVE, userId1.getId())
        .modifyRight(ModType.REMOVE, cityGuards.getId())
        .build().await().atMost(Duration.ofMinutes(1));
        
      Assertions.assertEquals(Repo.CommitResultStatus.OK, changes.getStatus());
      assertRepo(repo.getRepo(), "HierarchicalOrgPartyTest/data-delete-party-member-and-right.txt");
    }
  }

  
  private OrgMember createMember(String userName, RepoResult repo, List<OrgParty> groups, List<OrgRight> roles) {
    return getClient().org().commit().createOneMember()
        .repoId(repo.getRepo().getId())
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
  
  private OrgParty createRootParty(String groupName, RepoResult repo, OrgRight ...roles) {
    return getClient().org().commit().createOneParty()
        .repoId(repo.getRepo().getId())
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
  
  private OrgParty createChildParty(String groupName, String parentId, RepoResult repo, OrgRight ...roles) {
    return getClient().org().commit().createOneParty()
        .repoId(repo.getRepo().getId())
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
  
  private OrgRight createRight(RepoResult repo, String roleName) {
    return getClient().org().commit().createOneRight()
        .repoId(repo.getRepo().getId())
        .rightName(roleName)
        .rightDescription("rd-")
        .author("ar-")
        .message("me-")
        .externalId(null)
        .build().await().atMost(Duration.ofMinutes(1)).getRight();
  }  

}
