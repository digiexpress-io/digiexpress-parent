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
import io.resys.thena.api.actions.OrgCommitActions.ModType;
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import io.resys.thena.api.actions.TenantActions.TenantStatus;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
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
    TenantCommitResult repo = getClient().tenants().commit()
        .name("HierarchicalOrgTest-1", StructureType.org)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(TenantStatus.OK, repo.getStatus());

    createUsers(repo.getRepo());
    createGroups(repo.getRepo());
    createUsermemberships(repo.getRepo());
    createRoles(repo.getRepo());
    createUserGroupRoles(repo.getRepo());
    
    final var groupHierarchy = getClient().org(repo).find().partyHierarchyQuery()
        .findAll().await().atMost(Duration.ofMinutes(1));
    
    final var groups = groupHierarchy.getObjects()
      .stream()
      .sorted((a, b) -> a.getExternalId().compareTo(b.getExternalId()))
      .filter(e -> e.getParentPartyId() == null)
      .map(e -> e.getLog())
      .toList();
    
    Assertions.assertEquals("""
Testitestiasiakas Oyj::tenant <= you are here
+--- Muutoksen lokakuu
|    `--- users
|         +--- ulla lv.j6 (MANAGER)
|         +--- ulla ltp.j8 (USER)
|         +--- emilia op.ke (USER)
|         +--- marko.vm (VIEWER)
|         +--- jari.sj (MANAGER)
|         `--- mikki.td (MANAGER)
+--- Testiasiakas Oy
|    +--- users
|    |    +--- tomi.p6 (USER)
|    |    +--- ulla op.j9 (VIEWER)
|    |    +--- mika.l5 (MANAGER)
|    |    +--- villu.vc (MANAGER)
|    |    +--- ullan admin.t6 (MANAGER)
|    |    +--- ulla lv.j6 (USER)
|    |    +--- ulla ltp.j8 (MANAGER)
|    |    +--- mikki.t4 (USER)
|    |    +--- emilia op.ke (MANAGER)
|    |    `--- emilia lv.kf (MANAGER)
|    +--- Head Office
|    |    `--- users
|    |         +--- tomi.p6 (USER::inherited)
|    |         +--- ulla op.j9 (MANAGER, VIEWER::inherited)
|    |         +--- mika.l5 (MANAGER::inherited)
|    |         +--- villu.vc (MANAGER::inherited)
|    |         +--- ullan admin.t6 (MANAGER::inherited)
|    |         +--- ulla lv.j6 (USER::inherited)
|    |         +--- ulla ltp.j8 (MANAGER::inherited)
|    |         +--- mikki.t4 (USER::inherited)
|    |         +--- emilia op.ke (MANAGER::inherited)
|    |         `--- emilia lv.kf (MANAGER::inherited)
|    +--- Riskipäälliköt
|    |    +--- users
|    |    |    +--- tomi.p6 (USER::inherited)
|    |    |    +--- ulla op.j9 (VIEWER::inherited)
|    |    |    +--- mika.l5 (MANAGER::inherited)
|    |    |    +--- villu.vc (MANAGER::inherited)
|    |    |    +--- ullan admin.t6 (MANAGER::inherited)
|    |    |    +--- ulla lv.j6 (USER::inherited)
|    |    |    +--- ulla ltp.j8 (MANAGER::inherited)
|    |    |    +--- mikki.t4 (USER::inherited)
|    |    |    +--- emilia op.ke (MANAGER::inherited)
|    |    |    +--- emilia lv.kf (MANAGER::inherited)
|    |    |    `--- ari.ra (MANAGER)
|    |    +--- Mikon testi
|    |    |    `--- users
|    |    |         +--- ari.ra (MANAGER::inherited)
|    |    |         +--- tomi.p6::inherited (USER::inherited)
|    |    |         +--- ulla op.j9::inherited (VIEWER::inherited)
|    |    |         +--- mika.l5::inherited (MANAGER::inherited)
|    |    |         +--- villu.vc::inherited (MANAGER::inherited)
|    |    |         +--- ullan admin.t6::inherited (MANAGER::inherited)
|    |    |         +--- ulla lv.j6::inherited (USER::inherited)
|    |    |         +--- ulla ltp.j8::inherited (MANAGER::inherited)
|    |    |         +--- mikki.t4::inherited (USER::inherited)
|    |    |         +--- emilia op.ke::inherited (MANAGER::inherited)
|    |    |         `--- emilia lv.kf::inherited (MANAGER::inherited)
|    |    +--- Riskipaikka 1
|    |    |    `--- users
|    |    |         +--- ari.ra (MANAGER::inherited)
|    |    |         +--- tomi.p6::inherited (USER::inherited)
|    |    |         +--- ulla op.j9::inherited (VIEWER::inherited)
|    |    |         +--- mika.l5::inherited (MANAGER::inherited)
|    |    |         +--- villu.vc::inherited (MANAGER::inherited)
|    |    |         +--- ullan admin.t6::inherited (MANAGER::inherited)
|    |    |         +--- ulla lv.j6::inherited (USER::inherited)
|    |    |         +--- ulla ltp.j8::inherited (MANAGER::inherited)
|    |    |         +--- mikki.t4::inherited (USER::inherited)
|    |    |         +--- emilia op.ke::inherited (MANAGER::inherited)
|    |    |         `--- emilia lv.kf::inherited (MANAGER::inherited)
|    |    `--- Riskipaikka 1_1
|    |         `--- users
|    |              +--- ari.ra (MANAGER::inherited)
|    |              +--- tomi.p6::inherited (USER::inherited)
|    |              +--- ulla op.j9::inherited (VIEWER::inherited)
|    |              +--- mika.l5::inherited (MANAGER::inherited)
|    |              +--- villu.vc::inherited (MANAGER::inherited)
|    |              +--- ullan admin.t6::inherited (MANAGER::inherited)
|    |              +--- ulla lv.j6::inherited (USER::inherited)
|    |              +--- ulla ltp.j8::inherited (MANAGER::inherited)
|    |              +--- mikki.t4::inherited (USER::inherited)
|    |              +--- emilia op.ke::inherited (MANAGER::inherited)
|    |              `--- emilia lv.kf::inherited (MANAGER::inherited)
|    +--- Testi joulukuu
|    |    `--- users
|    |         +--- tomi.p6 (USER::inherited)
|    |         +--- ulla op.j9 (MANAGER, VIEWER::inherited)
|    |         +--- mika.l5 (MANAGER::inherited)
|    |         +--- villu.vc (MANAGER::inherited)
|    |         +--- ullan admin.t6 (MANAGER::inherited)
|    |         +--- ulla lv.j6 (USER::inherited)
|    |         +--- ulla ltp.j8 (MANAGER::inherited)
|    |         +--- mikki.t4 (USER::inherited)
|    |         +--- emilia op.ke (USER, MANAGER::inherited)
|    |         `--- emilia lv.kf (MANAGER::inherited)
|    +--- Testipaikka
|    |    +--- users
|    |    |    +--- tomi.p6 (USER::inherited)
|    |    |    +--- ulla op.j9 (VIEWER::inherited)
|    |    |    +--- mika.l5 (MANAGER::inherited)
|    |    |    +--- villu.vc (MANAGER::inherited)
|    |    |    +--- ullan admin.t6 (MANAGER::inherited)
|    |    |    +--- ulla lv.j6 (USER::inherited)
|    |    |    +--- ulla ltp.j8 (MANAGER::inherited)
|    |    |    +--- mikki.t4 (USER::inherited)
|    |    |    +--- emilia op.ke (MANAGER::inherited)
|    |    |    `--- emilia lv.kf (MANAGER::inherited)
|    |    `--- Tyomaa 2
|    |         +--- users
|    |         |    +--- tomi.p6::inherited (USER::inherited)
|    |         |    +--- ulla op.j9::inherited (VIEWER::inherited)
|    |         |    +--- mika.l5::inherited (MANAGER::inherited)
|    |         |    +--- villu.vc::inherited (MANAGER::inherited)
|    |         |    +--- ullan admin.t6::inherited (MANAGER::inherited)
|    |         |    +--- ulla lv.j6::inherited (USER::inherited)
|    |         |    +--- ulla ltp.j8::inherited (MANAGER::inherited)
|    |         |    +--- mikki.t4::inherited (USER::inherited)
|    |         |    +--- emilia op.ke::inherited (MANAGER::inherited)
|    |         |    `--- emilia lv.kf::inherited (MANAGER::inherited)
|    |         `--- testipaikka
|    |              `--- users
|    |                   +--- tomi.p6::inherited (USER::inherited)
|    |                   +--- ulla op.j9::inherited (VIEWER::inherited)
|    |                   +--- mika.l5::inherited (MANAGER::inherited)
|    |                   +--- villu.vc::inherited (MANAGER::inherited)
|    |                   +--- ullan admin.t6::inherited (MANAGER::inherited)
|    |                   +--- ulla lv.j6::inherited (USER::inherited)
|    |                   +--- ulla ltp.j8::inherited (MANAGER::inherited)
|    |                   +--- mikki.t4::inherited (USER::inherited)
|    |                   +--- emilia op.ke::inherited (MANAGER::inherited)
|    |                   `--- emilia lv.kf::inherited (MANAGER::inherited)
|    `--- Uusi testipaikka
|         +--- users
|         |    +--- tomi.p6 (USER::inherited)
|         |    +--- ulla op.j9 (VIEWER::inherited)
|         |    +--- mika.l5 (MANAGER::inherited)
|         |    +--- villu.vc (MANAGER::inherited)
|         |    +--- ullan admin.t6 (MANAGER::inherited)
|         |    +--- ulla lv.j6 (USER::inherited)
|         |    +--- ulla ltp.j8 (MANAGER::inherited)
|         |    +--- mikki.t4 (USER::inherited)
|         |    +--- emilia op.ke (MANAGER::inherited)
|         |    +--- emilia lv.kf (MANAGER::inherited)
|         |    +--- sanna.ns (VIEWER)
|         |    `--- anton.aa (USER)
|         +--- Tyomaa 1
|         |    `--- users
|         |         +--- sanna.ns (VIEWER, VIEWER::inherited)
|         |         +--- anton.aa (USER::inherited)
|         |         +--- ilkka.hi (USER)
|         |         +--- tomi.p6::inherited (USER::inherited)
|         |         +--- ulla op.j9::inherited (VIEWER::inherited)
|         |         +--- mika.l5::inherited (MANAGER::inherited)
|         |         +--- villu.vc::inherited (MANAGER::inherited)
|         |         +--- ullan admin.t6::inherited (MANAGER::inherited)
|         |         +--- ulla lv.j6::inherited (USER::inherited)
|         |         +--- ulla ltp.j8::inherited (MANAGER::inherited)
|         |         +--- mikki.t4::inherited (USER::inherited)
|         |         +--- emilia op.ke::inherited (MANAGER::inherited)
|         |         `--- emilia lv.kf::inherited (MANAGER::inherited)
|         `--- Varasto
|              `--- users
|                   +--- sanna.ns (VIEWER::inherited)
|                   +--- anton.aa (USER::inherited)
|                   +--- tomi.p6::inherited (USER::inherited)
|                   +--- ulla op.j9::inherited (VIEWER::inherited)
|                   +--- mika.l5::inherited (MANAGER::inherited)
|                   +--- villu.vc::inherited (MANAGER::inherited)
|                   +--- ullan admin.t6::inherited (MANAGER::inherited)
|                   +--- ulla lv.j6::inherited (USER::inherited)
|                   +--- ulla ltp.j8::inherited (MANAGER::inherited)
|                   +--- mikki.t4::inherited (USER::inherited)
|                   +--- emilia op.ke::inherited (MANAGER::inherited)
|                   `--- emilia lv.kf::inherited (MANAGER::inherited)
+--- Testipäivä 170124
|    `--- users
|         +--- mika.l5 (VIEWER)
|         +--- ulla ltp.j8 (MANAGER)
|         +--- emilia op.ke (MANAGER)
|         `--- teppo.t8 (USER)
`--- Valmistuneet työmaat
     +--- users
     |    +--- ullan admin.t6 (MANAGER)
     |    `--- ulla ltp.j8 (MANAGER)
     `--- Ratapiha
          `--- users
               +--- ullan admin.t6 (MANAGER::inherited)
               `--- ulla ltp.j8 (MANAGER::inherited)



Testiasiakas 2::tenant <= you are here
+--- kakkulaari
|    `--- testitalo
+--- Testimesta
|    +--- users
|    |    `--- ulla manager ee.j0 (MANAGER)
|    +--- Hiekkalaatikko
|    |    +--- users
|    |    |    +--- ulla manager ee.j0 (MANAGER::inherited)
|    |    |    `--- ursula test2.ue (USER)
|    |    +--- Ämpäritehdas
|    |    |    `--- users
|    |    |         +--- ursula test2.ue (USER::inherited)
|    |    |         `--- ulla manager ee.j0::inherited (MANAGER::inherited)
|    |    `--- Lapiotehdas
|    |         `--- users
|    |              +--- ursula test2.ue (USER::inherited)
|    |              `--- ulla manager ee.j0::inherited (MANAGER::inherited)
|    +--- Keinumaailma
|    |    +--- users
|    |    |    `--- ulla manager ee.j0 (MANAGER::inherited)
|    |    +--- Lautakeinut
|    |    |    `--- users
|    |    |         `--- ulla manager ee.j0::inherited (MANAGER::inherited)
|    |    `--- Vauvakeinut
|    |         `--- users
|    |              `--- ulla manager ee.j0::inherited (MANAGER::inherited)
|    `--- Leikkikehä
|         +--- users
|         |    `--- ulla manager ee.j0 (MANAGER::inherited)
|         +--- Autot
|         |    `--- users
|         |         `--- ulla manager ee.j0::inherited (MANAGER::inherited)
|         `--- Nuket
|              `--- users
|                   `--- ulla manager ee.j0::inherited (MANAGER::inherited)
`--- Testorganisation 1



OX Testausyritys::tenant <= you are here
+--- 2.5.11
|    `--- users
|         +--- anni.lb (VIEWER)
|         +--- proto.ne (USER)
|         `--- proto.v7 (USER)
+--- 2.5.2 lokaatio
+--- Keskusvarasto 1
|    +--- users
|    |    `--- anni.ee (MANAGER)
|    `--- Testilokaatio 2.0
|         +--- users
|         |    +--- proto.k1 (USER)
|         |    `--- anni.ee (MANAGER::inherited)
|         +--- Paikka 2.1 *¨*
|         |    `--- users
|         |         +--- proto.k1 (MANAGER, USER::inherited)
|         |         `--- anni.ee::inherited (MANAGER::inherited)
|         `--- Uusi paikka 2.2.
|              +--- users
|              |    +--- proto.k1 (USER::inherited)
|              |    `--- anni.ee::inherited (MANAGER::inherited)
|              `--- Uusin paikka 2.2.1.
|                   `--- users
|                        +--- anni.ee::inherited (MANAGER::inherited)
|                        `--- proto.k1::inherited (USER::inherited)
+--- newTopLeve
|    +--- users
|    |    +--- mika.l5 (MANAGER)
|    |    `--- anni.pe (VIEWER)
|    `--- nextLevel
|         `--- users
|              +--- mika.l5 (MANAGER::inherited)
|              `--- anni.pe (VIEWER::inherited)
+--- Päätoimipaikka
|    +--- users
|    |    +--- mika.l5 (MANAGER)
|    |    +--- anni.lb (VIEWER)
|    |    `--- protonen.l2 (VIEWER)
|    +--- Keskitason toimipiste
|    |    +--- users
|    |    |    +--- mika.l5 (MANAGER::inherited)
|    |    |    +--- anni.lb (VIEWER::inherited)
|    |    |    `--- protonen.l2 (VIEWER::inherited)
|    |    `--- #Erikoismerkki,;?   {"__"} ¨¨
|    |         `--- users
|    |              +--- protonen.l2 (MANAGER, VIEWER::inherited)
|    |              +--- mika.l5::inherited (MANAGER::inherited)
|    |              `--- anni.lb::inherited (VIEWER::inherited)
|    `--- Testauskeskus
|         +--- users
|         |    +--- mika.l5 (MANAGER::inherited)
|         |    +--- anni.lb (VIEWER::inherited)
|         |    +--- protonen.l2 (VIEWER::inherited)
|         |    `--- anni.a1 (USER)
|         +--- Laitevarasto
|         |    `--- users
|         |         +--- anni.a1 (USER::inherited)
|         |         +--- proto.l0 (USER)
|         |         +--- mika.l5::inherited (MANAGER::inherited)
|         |         +--- anni.lb::inherited (VIEWER::inherited)
|         |         `--- protonen.l2::inherited (VIEWER::inherited)
|         `--- Pienenpieni ja erittäin pitkäniminen testauspiste?! Pienenpieni ja erittäin pitkäniminen testauspiste?!Pienenpieni ja erittäin pitkäniminen testauspis
|              `--- users
|                   +--- anni.a1 (USER::inherited)
|                   +--- proto.k1 (MANAGER)
|                   +--- proto.l0 (VIEWER)
|                   +--- mika.l5::inherited (MANAGER::inherited)
|                   +--- anni.lb::inherited (VIEWER::inherited)
|                   `--- protonen.l2::inherited (VIEWER::inherited)
`--- Paikka roolitesti
     `--- Paikka roolitesti, alempi
          +--- users
          |    +--- proto.k1 (USER)
          |    `--- proto.ne (MANAGER)
          `--- Uusin paikka, versio 2.3.0
               `--- users
                    +--- anni.lb (VIEWER)
                    +--- proto.k1 (USER::inherited)
                    `--- proto.ne (MANAGER::inherited)



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
          +--- proto.ne (USER)
          `--- anni.pe (MANAGER)



Acme corporation::tenant <= you are here
+--- Organisation X
|    `--- users
|         +--- kaur.t7 (USER)
|         `--- john.se (USER)
`--- Organisation Y
     +--- users
     |    +--- mikki.lc (USER)
     |    +--- kaur.t7 (MANAGER)
     |    `--- jocelyn.m4 (MANAGER)
     `--- TestiOne
          `--- users
               +--- mikki.lc (USER::inherited)
               +--- kaur.t7 (MANAGER::inherited)
               `--- jocelyn.m4 (MANAGER::inherited)



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
      final var builder = getClient().org(repo).commit().modifyOneMember()
          .memberId(entry.getKey())
          .author("ar-")
          .message("created membership");
      
      entry.getValue().forEach((v) -> builder.modifyParties(ModType.ADD, v));
      
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
      final var result = getClient().org(repo).commit().createOneParty()

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
    final var result = getClient().org(repo).commit().createOneParty()
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
      
      final var result = getClient().org(repo).commit().createOneMember()
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
    var result = getClient().org(repo).commit().createOneRight()
        .externalId("0")
        .rightName("VIEWER")
        .rightDescription("direct user and group role")
        .author("au-")
        .message("created role")
        .build().await().atMost(Duration.ofMinutes(1));
      Assertions.assertEquals(CommitResultStatus.OK, result.getStatus());
 
    result = getClient().org(repo).commit().createOneRight()
        .externalId("1")
        .rightName("USER")
        .rightDescription("direct user and group role")
        .author("au-")
        .message("created role")
        .build().await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals(CommitResultStatus.OK, result.getStatus());
    
    result = getClient().org(repo).commit().createOneRight()
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
      
      final var builder = getClient().org(repo).commit().modifyOneMember()
        .memberId(user.getString("user_external_id"))
        .author("au-")
        .message("created user group role association");
      
      
        builder.modifyPartyRight(ModType.ADD, 
            user.getString("group_external_id"), 
            user.getString("role_external_id"));
        
      final var result = builder.build().await().atMost(Duration.ofMinutes(1));
      if(result.getStatus() == CommitResultStatus.NO_CHANGES) {
        continue;
      }
      Assertions.assertEquals(CommitResultStatus.OK, result.getStatus());
 
    } 
  }
}
