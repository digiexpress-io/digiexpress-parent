package io.resys.thena.docdb.test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.docdb.api.actions.OrgCommitActions.ModType;
import io.resys.thena.docdb.api.actions.RepoActions.RepoResult;
import io.resys.thena.docdb.api.actions.RepoActions.RepoStatus;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.Repo.CommitResultStatus;
import io.resys.thena.docdb.api.models.Repo.RepoType;
import io.resys.thena.docdb.test.config.DbTestTemplate;
import io.resys.thena.docdb.test.config.PgProfile;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@QuarkusTest
@TestProfile(PgProfile.class)
@Slf4j
public class HierarchicalOrgTest extends DbTestTemplate {

  @Test  
  public void populate() {
    // create project
    RepoResult repo = getClient().repo().projectBuilder()
        .name("HierarchicalOrgTest-1", RepoType.org)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(RepoStatus.OK, repo.getStatus());

    createUsers(repo.getRepo());
    createGroups(repo.getRepo());
    createUsermemberships(repo.getRepo());
    
    final var groupHierarchy = getClient().org().find().groupHierarchyQuery()
        .repoId(repo.getRepo().getId())
        .findAll().await().atMost(Duration.ofMinutes(1));
    
    final var groups = groupHierarchy.getObjects()
      .stream()
      .sorted((a, b) -> a.getExternalId().compareTo(b.getExternalId()))
      .filter(e -> e.getParentGroupId() == null)
      .map(e -> e.getLog())
      .toList();
    
    Assertions.assertEquals("""
Testitestiasiakas Oyj::tenant <= you are here
+--- Muutoksen lokakuu
|    `--- users
|         +--- ulla lv.j6
|         +--- ulla ltp.j8
|         +--- emilia op.ke
|         +--- marko.vm
|         +--- jari.sj
|         `--- mikki.td
+--- Testiasiakas Oy
|    +--- users
|    |    +--- tomi.p6
|    |    +--- ulla op.j9
|    |    +--- mika.l5
|    |    +--- villu.vc
|    |    +--- ullan admin.t6
|    |    +--- ulla lv.j6
|    |    +--- ulla ltp.j8
|    |    +--- mikki.t4
|    |    +--- emilia op.ke
|    |    `--- emilia lv.kf
|    +--- Head Office
|    |    `--- users
|    |         +--- tomi.p6
|    |         +--- ulla op.j9
|    |         +--- mika.l5
|    |         +--- villu.vc
|    |         +--- ullan admin.t6
|    |         +--- ulla lv.j6
|    |         +--- ulla ltp.j8
|    |         +--- mikki.t4
|    |         +--- emilia op.ke
|    |         `--- emilia lv.kf
|    +--- Riskipäälliköt
|    |    +--- users
|    |    |    +--- tomi.p6
|    |    |    +--- ulla op.j9
|    |    |    +--- mika.l5
|    |    |    +--- villu.vc
|    |    |    +--- ullan admin.t6
|    |    |    +--- ulla lv.j6
|    |    |    +--- ulla ltp.j8
|    |    |    +--- mikki.t4
|    |    |    +--- emilia op.ke
|    |    |    +--- emilia lv.kf
|    |    |    `--- ari.ra
|    |    +--- Mikon testi
|    |    |    `--- users
|    |    |         `--- ari.ra
|    |    +--- Riskipaikka 1
|    |    |    `--- users
|    |    |         `--- ari.ra
|    |    `--- Riskipaikka 1_1
|    |         `--- users
|    |              `--- ari.ra
|    +--- Testi joulukuu
|    |    `--- users
|    |         +--- tomi.p6
|    |         +--- ulla op.j9
|    |         +--- mika.l5
|    |         +--- villu.vc
|    |         +--- ullan admin.t6
|    |         +--- ulla lv.j6
|    |         +--- ulla ltp.j8
|    |         +--- mikki.t4
|    |         +--- emilia op.ke
|    |         `--- emilia lv.kf
|    +--- Testipaikka
|    |    +--- users
|    |    |    +--- tomi.p6
|    |    |    +--- ulla op.j9
|    |    |    +--- mika.l5
|    |    |    +--- villu.vc
|    |    |    +--- ullan admin.t6
|    |    |    +--- ulla lv.j6
|    |    |    +--- ulla ltp.j8
|    |    |    +--- mikki.t4
|    |    |    +--- emilia op.ke
|    |    |    `--- emilia lv.kf
|    |    `--- Tyomaa 2
|    |         `--- testipaikka
|    `--- Uusi testipaikka
|         +--- users
|         |    +--- tomi.p6
|         |    +--- ulla op.j9
|         |    +--- mika.l5
|         |    +--- villu.vc
|         |    +--- ullan admin.t6
|         |    +--- ulla lv.j6
|         |    +--- ulla ltp.j8
|         |    +--- mikki.t4
|         |    +--- emilia op.ke
|         |    +--- emilia lv.kf
|         |    +--- sanna.ns
|         |    `--- anton.aa
|         +--- Tyomaa 1
|         |    `--- users
|         |         +--- sanna.ns
|         |         +--- anton.aa
|         |         `--- ilkka.hi
|         `--- Varasto
|              `--- users
|                   +--- sanna.ns
|                   `--- anton.aa
+--- Testipäivä 170124
|    `--- users
|         +--- mika.l5
|         +--- ulla ltp.j8
|         +--- emilia op.ke
|         `--- teppo.t8
`--- Valmistuneet työmaat
     +--- users
     |    +--- ullan admin.t6
     |    `--- ulla ltp.j8
     `--- Ratapiha
          `--- users
               +--- ullan admin.t6
               `--- ulla ltp.j8



Testiasiakas 2::tenant <= you are here
+--- kakkulaari
|    `--- testitalo
+--- Testimesta
|    +--- users
|    |    `--- ulla manager ee.j0
|    +--- Hiekkalaatikko
|    |    +--- users
|    |    |    +--- ulla manager ee.j0
|    |    |    `--- ursula test2.ue
|    |    +--- Ämpäritehdas
|    |    |    `--- users
|    |    |         `--- ursula test2.ue
|    |    `--- Lapiotehdas
|    |         `--- users
|    |              `--- ursula test2.ue
|    +--- Keinumaailma
|    |    +--- users
|    |    |    `--- ulla manager ee.j0
|    |    +--- Lautakeinut
|    |    `--- Vauvakeinut
|    `--- Leikkikehä
|         +--- users
|         |    `--- ulla manager ee.j0
|         +--- Autot
|         `--- Nuket
`--- Testorganisation 1



OX Testausyritys::tenant <= you are here
+--- 2.5.11
|    `--- users
|         +--- anni.lb
|         +--- proto.ne
|         `--- proto.v7
+--- 2.5.2 lokaatio
+--- Keskusvarasto 1
|    +--- users
|    |    `--- anni.ee
|    `--- Testilokaatio 2.0
|         +--- users
|         |    +--- proto.k1
|         |    `--- anni.ee
|         +--- Paikka 2.1 *¨*
|         |    `--- users
|         |         `--- proto.k1
|         `--- Uusi paikka 2.2.
|              +--- users
|              |    `--- proto.k1
|              `--- Uusin paikka 2.2.1.
+--- newTopLeve
|    +--- users
|    |    +--- mika.l5
|    |    `--- anni.pe
|    `--- nextLevel
|         `--- users
|              +--- mika.l5
|              `--- anni.pe
+--- Päätoimipaikka
|    +--- users
|    |    +--- mika.l5
|    |    +--- anni.lb
|    |    `--- protonen.l2
|    +--- Keskitason toimipiste
|    |    +--- users
|    |    |    +--- mika.l5
|    |    |    +--- anni.lb
|    |    |    `--- protonen.l2
|    |    `--- #Erikoismerkki,;?   {"__"} ¨¨
|    |         `--- users
|    |              `--- protonen.l2
|    `--- Testauskeskus
|         +--- users
|         |    +--- mika.l5
|         |    +--- anni.lb
|         |    +--- protonen.l2
|         |    `--- anni.a1
|         +--- Laitevarasto
|         |    `--- users
|         |         +--- anni.a1
|         |         `--- proto.l0
|         `--- Pienenpieni ja erittäin pitkäniminen testauspiste?! Pienenpieni ja erittäin pitkäniminen testauspiste?!Pienenpieni ja erittäin pitkäniminen testauspis
|              `--- users
|                   +--- anni.a1
|                   +--- proto.k1
|                   `--- proto.l0
`--- Paikka roolitesti
     `--- Paikka roolitesti, alempi
          +--- users
          |    +--- proto.k1
          |    `--- proto.ne
          `--- Uusin paikka, versio 2.3.0
               `--- users
                    +--- anni.lb
                    +--- proto.k1
                    `--- proto.ne



OX Audit::tenant <= you are here
+--- asd
|    `--- asd_1
|         `--- asd_2
|              `--- asd_3
|                   `--- asd_4
|                        `--- asd_5
|                             `--- asd_6
|                                  `--- asd_7
|                                       `--- kkkkk
`--- test
     `--- Kalakauppa
          +--- asd_8
          `--- sdfd



Oskar Test::tenant <= you are here
`--- Test Location



Ullan uusi testi::tenant <= you are here
`--- Default location



Annin UAT-Yritys::tenant <= you are here
`--- Oletuspaikka
     `--- users
          +--- proto.ne
          `--- anni.pe



Acme corporation::tenant <= you are here
+--- Organisation X
|    `--- users
|         +--- kaur.t7
|         `--- john.se
`--- Organisation Y
     +--- users
     |    +--- mikki.lc
     |    +--- kaur.t7
     |    `--- jocelyn.m4
     `--- TestiOne
          `--- users
               +--- mikki.lc
               +--- kaur.t7
               `--- jocelyn.m4



Fooly::tenant <= you are here
`--- New location



Testi 1::tenant <= you are here
`--- Paikka
        """, String.join("\n\n\n", groups));
    
  }
  
  
  public void assertEquals(String expected, String actual) {
    Assertions.assertEquals(
        expected.replace("\r", ""), 
        actual.replace("\r", ""));
  }

  public void createUsermemberships(Repo repo) {
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
      final var result = getClient().org().commit().modifyOneUser()
          .repoId(repo.getId())
          .userId(entry.getKey())
          .groups(ModType.ADD, entry.getValue())
          .author("ar-")
          .message("created membership")
          .build().await().atMost(Duration.ofMinutes(1));
      Assertions.assertEquals(CommitResultStatus.OK, result.getStatus()); 
    }
    
  }
  
  
  public void createGroups(Repo repo) {
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
      final var result = getClient().org().commit().createOneGroup()
          .repoId(repo.getId())
          .groupName(groupName)
          .groupDescription("created from tenant")
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
      Repo repo, 
      Map<String, 
      List<JsonObject>> groupsByParent, 
      List<String> takenGroupNames) {

    
    final var groupNameInit = json.getString("group_name");
    final var index = takenGroupNames.stream().filter(e -> e.equals(groupNameInit)).count();
    final var suffix = takenGroupNames.contains(groupNameInit) ? "_" + index : "";
    final var groupName = groupNameInit + suffix;
    takenGroupNames.add(groupNameInit);
    
    final var externalId = json.getString("external_id");
    final var result = getClient().org().commit().createOneGroup()
        .repoId(repo.getId())
        .parentId(json.getString("parent_group_external_id"))
        .groupName(groupName)
        .groupDescription("location")
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
  
  
  public void createUsers(Repo repo) {
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
      
      final var result = getClient().org().commit().createOneUser()
        .repoId(repo.getId())
        .userName(userName)
        .email(userName + "@digiexpress.io")
        .externalId(user.getString("external_id"))
        .author("au-")
        .message("created user")
        .build().await().atMost(Duration.ofMinutes(1));
      Assertions.assertEquals(CommitResultStatus.OK, result.getStatus());
 
    }
    
  }
}
