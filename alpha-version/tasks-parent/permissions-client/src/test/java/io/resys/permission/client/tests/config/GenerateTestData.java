package io.resys.permission.client.tests.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;

import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.actions.OrgCommitActions.ModType;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.Tenant;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GenerateTestData {
  private final ThenaClient docDb;
  

  public void populate(Tenant repo) {
    createUsers(repo);
    createGroups(repo);
    createUsermemberships(repo);
    createRoles(repo);
    createUserGroupRoles(repo);
  }

  public void createUsermemberships(Tenant repo) {
    final var groupsByUsers = new LinkedHashMap<String, List<String>>();
    final var memberships = new JsonArray(DbTestTemplate.toString(getClass(), "org_test_data_json/test_data_user_groups.json"));    
    for(final var membershipRaw : memberships) {
      final var membership = (JsonObject) membershipRaw;
      final var userId = membership.getString("user_external_id");
      final var groupId = membership.getString("group_external_id");
      
      if(!groupsByUsers.containsKey(userId)) {
        groupsByUsers.put(userId, new ArrayList<>());
      }
      
      groupsByUsers.get(userId).add(groupId);
    }
    
    for(final var entry : groupsByUsers.entrySet()) {
      final var builder = docDb.org(repo.getId()).commit().modifyOneMember()
          .memberId(entry.getKey())
          .author("ar-")
          .message("created membership");
      entry.getValue().forEach(p -> builder.modifyParties(ModType.ADD, p));
          
      final var result = builder.build().await().atMost(Duration.ofMinutes(1));
      Assertions.assertEquals(CommitResultStatus.OK, result.getStatus()); 
    }
    
  }
  
  
  public void createGroups(Tenant repo) {
    final var roots = new LinkedHashMap<String, JsonObject>();
    final var groupsByParent = new LinkedHashMap<String, List<JsonObject>>();
    final var groups = new JsonArray(DbTestTemplate.toString(getClass(), "org_test_data_json/test_data_groups.json"));
    for(final var groupRaw : groups) {
      final var group = (JsonObject) groupRaw;
      final var parentGroupExtId = group.getString("parent_group_external_id");
      final var parentGroupName = group.getString("parent_group_name");
      final var isRoot = parentGroupName.endsWith("::tenant");
      
      if(isRoot && !roots.containsKey(parentGroupExtId)) {
        final var root = new JsonObject();
        root.put("external_id", parentGroupExtId);
        root.put("group_name", parentGroupName);
        roots.put(parentGroupExtId, root);
      }
      
      if(!groupsByParent.containsKey(parentGroupExtId)) {
        groupsByParent.put(parentGroupExtId, new ArrayList<>());
      }
      groupsByParent.get(parentGroupExtId).add(group);
    }
    
    final var takenGroupNames = new ArrayList<String>();
    for(final var root : roots.values()) {
      final var groupNameInit = root.getString("group_name");
      
      final var index = takenGroupNames.stream().filter(e -> e.equals(groupNameInit)).count();
      final var suffix = takenGroupNames.contains(groupNameInit) ? "_" + index : "";
      final var groupName = groupNameInit + suffix;
      takenGroupNames.add(groupNameInit);
      
      
      final var externalId = root.getString("external_id");
      final var result = docDb.org(repo.getId()).commit().createOneParty()
          .partyName(groupName)
          .partyDescription("created from tenant")
          .externalId(externalId)
          .author("ar-")
          .message("created group")
          .build().await().atMost(Duration.ofMinutes(1));
      Assertions.assertEquals(CommitResultStatus.OK, result.getStatus());

      final var children = Optional.ofNullable(groupsByParent.get(externalId)).orElse(Collections.emptyList());
      for(final var child : children) {
        createChildGroup(child, repo, groupsByParent, takenGroupNames);
      }
    }
  }
  
  
  public void createChildGroup(
      JsonObject json, 
      Tenant repo, 
      Map<String, 
      List<JsonObject>> groupsByParent, 
      List<String> takenGroupNames) {

    
    final var groupNameInit = json.getString("group_name");
    final var index = takenGroupNames.stream().filter(e -> e.equals(groupNameInit)).count();
    final var suffix = takenGroupNames.contains(groupNameInit) ? "_" + index : "";
    final var groupName = groupNameInit + suffix;
    takenGroupNames.add(groupNameInit);
    
    final var externalId = json.getString("external_id");
    final var result = docDb.org(repo.getId()).commit().createOneParty()
        .parentId(json.getString("parent_group_external_id"))
        .partyName(groupName)
        .partyDescription("location")
        .externalId(externalId)
        .author("ar-")
        .message("created group")
        .build().await().atMost(Duration.ofMinutes(1));

    
    Assertions.assertEquals(CommitResultStatus.OK, result.getStatus());
    final var children = Optional.ofNullable(groupsByParent.get(externalId)).orElse(Collections.emptyList());
    for(final var child : children) {
      createChildGroup(child, repo, groupsByParent, takenGroupNames);
    }
    
  }
  
  
  public void createUsers(Tenant repo) {
    final var users = new JsonArray(DbTestTemplate.toString(getClass(), "org_test_data_json/test_data_users.json"));
    final var takenUsernames = new ArrayList<String>();
    var index = 0;
    for(final var userRaw : users) {
      final var user = (JsonObject) userRaw;
      final var userNameInit = user.getString("first_name") + "." + user.getString("last_name");
      
      final var suffix = takenUsernames.contains(userNameInit) ? "_" + index : "";
      index++;
      takenUsernames.add(userNameInit);
      final var userName = userNameInit + suffix;
      
      final var result = docDb.org(repo.getId()).commit().createOneMember()
        .userName(userName)
        .email(userName + "@digiexpress.io")
        .externalId(user.getString("external_id"))
        .author("au-")
        .message("created user")
        .build().await().atMost(Duration.ofMinutes(1));
      Assertions.assertEquals(CommitResultStatus.OK, result.getStatus());
 
    }
  }
  
  public void createRoles(Tenant repo) {
    var result = docDb.org(repo.getId()).commit().createOneRight()
        .externalId("0")
        .rightName("VIEWER")
        .rightDescription("direct user and group role")
        .author("au-")
        .message("created role")
        .build().await().atMost(Duration.ofMinutes(1));
      Assertions.assertEquals(CommitResultStatus.OK, result.getStatus());
 
    result = docDb.org(repo.getId()).commit().createOneRight()
        .externalId("1")
        .rightName("USER")
        .rightDescription("direct user and group role")
        .author("au-")
        .message("created role")
        .build().await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals(CommitResultStatus.OK, result.getStatus());
    
    result = docDb.org(repo.getId()).commit().createOneRight()
        .externalId("2")
        .rightName("MANAGER")
        .rightDescription("direct user and group role")
        .author("au-")
        .message("created role")
        .build().await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals(CommitResultStatus.OK, result.getStatus());
  }
  
  public void createUserGroupRoles(Tenant repo) {
    final var users = new JsonArray(DbTestTemplate.toString(getClass(), "org_test_data_json/test_data_users_group_role.json"));

    for(final var userRaw : users) {
      final var user = (JsonObject) userRaw;
      
      final var result = docDb.org(repo.getId()).commit().modifyOneMember()
        .memberId(user.getString("user_external_id"))

        .modifyPartyRight(ModType.ADD, user.getString("group_external_id"), user.getString("role_external_id"))
        .author("au-")
        .message("created user group role association")
        .build().await().atMost(Duration.ofMinutes(1));
      if(result.getStatus() == CommitResultStatus.NO_CHANGES) {
        continue;
      }
      Assertions.assertEquals(CommitResultStatus.OK, result.getStatus());
 
    } 
  }
}
