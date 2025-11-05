package io.resys.thena.grim.test;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;

import org.immutables.value.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.thena.api.actions.TenantActions.CommitStatus;
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewMission;
import io.resys.thena.grim.test.config.DbTestTemplate;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;



@Slf4j
public class SimpleGrimUpdateMissionTest extends DbTestTemplate {

  @Value.Immutable
  public interface TestContent extends Serializable {
    String getId();
    String getName();
  }
  
  private GrimMissionContainer createMission(TenantCommitResult repo) {
    return getClient().grim(repo).commit()
        .createManyMissions()
        .commitMessage("batching tests")
        .commitAuthor("jane.doe@morgue.com")
        .addMission((NewMission newMission) -> {
          
          newMission
            .title("my first mission to build a house")
            .description("The best house ever")
            .status("OPEN")
            .priority("HIGH")
            .startDate(LocalDate.of(2020, 05, 01))
            .dueDate(LocalDate.of(2020, 06, 01))
            .reporterId("jane.doe@housing.com")
            .addCommands(Arrays.asList(JsonObject.of("commandType", "CREATE_TASK")))
            
            .addLabels(newLabel -> newLabel.labelType("keyword").labelValue("housing").build())
            .addLabels(newLabel -> newLabel.labelType("keyword").labelValue("roofing").build())
            
            .addAssignees(newAssignee -> newAssignee.assignmentType("worker").assignee("sam-from-the-mill").build())
            .addAssignees(newAssignee -> newAssignee.assignmentType("worker").assignee("jane-from-the-roofing").build())
            
            .addLink(newLink -> newLink.linkType("project-plans").linkValue("site.com/plans/1").build())
            .addLink(newLink -> newLink.linkType("permits").linkValue("site.com/permits/5").build())
            
            .addRemark(newRemark -> newRemark.remarkText("Created main task for building a house!").reporterId("jane.doe").build())
            .addRemark(newRemark -> newRemark.remarkText("Waiting for results already!").reporterId("the.bob.clown").build())
            
            .addObjective(newObjective -> newObjective
                .startDate(LocalDate.of(2023, 01, 01))
                .dueDate(LocalDate.of(2024, 01, 01))
                .title("interior design ideas")
                .description("all ideas are welcome how we should design kitchen and bathroom!")
                
                .addAssignees(newAssignee -> newAssignee.assignmentType("objective-worker").assignee("no-name-worker-1").build())
                .addAssignees(newAssignee -> newAssignee.assignmentType("objective-worker").assignee("no-name-worker-2").build())
                
                .addGoal(newGoal -> newGoal
                    .title("kitchen")
                    .description("kitcher plan goes here!")
                    .startDate(LocalDate.of(2023, 01, 02))
                    .dueDate(LocalDate.of(2023, 02, 01))
                    .addAssignees(newAssignee -> newAssignee.assignmentType("goal-worker").assignee("no-name-worker-3").build())
                    .addAssignees(newAssignee -> newAssignee.assignmentType("goal-worker").assignee("no-name-worker-4").build())
                    .build())
                .addGoal(newGoal -> newGoal.title("bathroom").description("kitcher plan goes here!").build())
                
                .build()
             ).build();

        }).build()
        .onItem().transformToUni(resp -> {
          final var missionId = resp.getMissions().iterator().next().getId();
          return getClient().grim(repo).find().missionQuery().get(missionId);
        })
        .onItem().transform(resp -> resp.getObjects())
        .await().atMost(Duration.ofMinutes(1));
  }

  @Test
  public void createAndUpdateMission() {
    // create project
    TenantCommitResult repo = getClient().tenants().commit()
        .name("SimpleGrimUpdateMissionTest-1", StructureType.grim)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(CommitStatus.OK, repo.getStatus());
    
    final var newMission = createMission(repo).getMissions().values().iterator().next();
    
    // add comments
    getClient().grim(repo).commit().modifyManyMissions()
    .commitMessage("forgot to add comments to things")
    .commitAuthor("jane.doe@morgue.com")
    .modifyMission(newMission.getId(), (modifyMission) -> {
      
      modifyMission
      .addCommands(Arrays.asList(JsonObject.of("commandType", "CREATE_REMARK")))
      .addCommands(Arrays.asList(JsonObject.of("commandType", "CREATE_REMARK")))
      
      .addRemark((newRemark) -> 
        newRemark
        .remarkText("Not to self, give feedback to architects")
        .reporterId("jane.doe@morgue.com")
        .build())
      .addRemark((newRemark) -> 
        newRemark
        .remarkText("Note to self, compliment works on after job well done!")
        .reporterId("jane.doe@morgue.com")
        .build())
      .build();
      
    })
    .build()
    .await().atMost(Duration.ofMinutes(1));
        
    
    // modify title
    getClient().grim(repo).commit().modifyManyMissions()
    .commitMessage("changed the title")
    .commitAuthor("jane.doe@morgue.com")
    .modifyMission(newMission.getId(), (modifyMission) -> {
      
      modifyMission
      .title("House plans for customer #198CC")
      .description("Basic house plans for customer")
      .reporterId("jane.doe@morgue.com")
      .startDate(null)
      .dueDate(null)
      
      .addCommands(Arrays.asList(JsonObject.of("commandType", "CHANGE_TITLE")))
      .build();
      
    })
    .build()
    .await().atMost(Duration.ofMinutes(1));
        
    
    Assertions.assertEquals(
"""

Repo
  - id: 1, rev: 2
    name: SimpleGrimUpdateMissionTest-1, prefix: 3, type: grim
Mission: 5
  - 12::GRIM_OBJECTIVE
  - 13::GRIM_OBJECTIVE_GOAL
  - 14::GRIM_OBJECTIVE_GOAL
  - 15::GRIM_REMARK
  - 16::GRIM_REMARK
  - 17::GRIM_REMARK
  - 18::GRIM_REMARK
  - 19::GRIM_COMMANDS
  - 20::GRIM_COMMANDS
  - 21::GRIM_COMMANDS
  - 22::GRIM_COMMANDS
  - 23::GRIM_ASSIGNMENT
  - 24::GRIM_ASSIGNMENT
  - 25::GRIM_ASSIGNMENT
  - 26::GRIM_ASSIGNMENT
  - 27::GRIM_ASSIGNMENT
  - 28::GRIM_ASSIGNMENT
  - 29::GRIM_MISSION_LABEL
  - 30::GRIM_MISSION_LABEL
  - 31::GRIM_MISSION_LINKS
  - 32::GRIM_MISSION_LINKS

commit: 4, tenant: 1
author: jane.doe@morgue.com, message: batching tests
 | created
  + added new: 17 entries
  + 19::GRIM_COMMANDS
    {"docType":"GRIM_COMMANDS","id":"19","commitId":"4","missionId":"5","commands":[{"commandType":"CREATE_TASK"}]}
  + 29::GRIM_MISSION_LABEL
    {"docType":"GRIM_MISSION_LABEL","id":"29","commitId":"4","labelType":"keyword","labelValue":"housing","labelBody":null,"missionId":"5","relation":null}
  + 30::GRIM_MISSION_LABEL
    {"docType":"GRIM_MISSION_LABEL","id":"30","commitId":"4","labelType":"keyword","labelValue":"roofing","labelBody":null,"missionId":"5","relation":null}
  + 28::GRIM_ASSIGNMENT
    {"docType":"GRIM_ASSIGNMENT","id":"28","commitId":"4","missionId":"5","assignee":"sam-from-the-mill","assignmentType":"worker","assigneeContact":null,"relation":null}
  + 27::GRIM_ASSIGNMENT
    {"docType":"GRIM_ASSIGNMENT","id":"27","commitId":"4","missionId":"5","assignee":"jane-from-the-roofing","assignmentType":"worker","assigneeContact":null,"relation":null}
  + 32::GRIM_MISSION_LINKS
    {"docType":"GRIM_MISSION_LINKS","id":"32","commitId":"4","createdWithCommitId":"4","missionId":"5","linkValue":"site.com/plans/1","linkType":"project-plans","linkBody":null,"transitives":null,"relation":null}
  + 31::GRIM_MISSION_LINKS
    {"docType":"GRIM_MISSION_LINKS","id":"31","commitId":"4","createdWithCommitId":"4","missionId":"5","linkValue":"site.com/permits/5","linkType":"permits","linkBody":null,"transitives":null,"relation":null}
  + 15::GRIM_REMARK
    {"docType":"GRIM_REMARK","id":"15","commitId":"4","createdWithCommitId":"4","missionId":"5","parentId":null,"transitives":null,"remarkText":"Created main task for building a house!","reporterId":"jane.doe","remarkStatus":null,"remarkType":null,"remarkSource":null,"relation":null}
  + 16::GRIM_REMARK
    {"docType":"GRIM_REMARK","id":"16","commitId":"4","createdWithCommitId":"4","missionId":"5","parentId":null,"transitives":null,"remarkText":"Waiting for results already!","reporterId":"the.bob.clown","remarkStatus":null,"remarkType":null,"remarkSource":null,"relation":null}
  + 25::GRIM_ASSIGNMENT
    {"docType":"GRIM_ASSIGNMENT","id":"25","commitId":"4","missionId":"5","assignee":"no-name-worker-1","assignmentType":"objective-worker","assigneeContact":null,"relation":{"objectiveId":"12","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}
  + 26::GRIM_ASSIGNMENT
    {"docType":"GRIM_ASSIGNMENT","id":"26","commitId":"4","missionId":"5","assignee":"no-name-worker-2","assignmentType":"objective-worker","assigneeContact":null,"relation":{"objectiveId":"12","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}
  + 23::GRIM_ASSIGNMENT
    {"docType":"GRIM_ASSIGNMENT","id":"23","commitId":"4","missionId":"5","assignee":"no-name-worker-3","assignmentType":"goal-worker","assigneeContact":null,"relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"14","relationType":"GOAL"}}
  + 24::GRIM_ASSIGNMENT
    {"docType":"GRIM_ASSIGNMENT","id":"24","commitId":"4","missionId":"5","assignee":"no-name-worker-4","assignmentType":"goal-worker","assigneeContact":null,"relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"14","relationType":"GOAL"}}
  + 14::GRIM_OBJECTIVE_GOAL
    {"docType":"GRIM_OBJECTIVE_GOAL","id":"14","commitId":"4","createdWithCommitId":"4","objectiveId":"12","goalStatus":null,"startDate":"2023-01-02","dueDate":"2023-02-01","description":"kitcher plan goes here!","title":"kitchen"}
  + 13::GRIM_OBJECTIVE_GOAL
    {"docType":"GRIM_OBJECTIVE_GOAL","id":"13","commitId":"4","createdWithCommitId":"4","objectiveId":"12","goalStatus":null,"startDate":null,"dueDate":null,"description":"kitcher plan goes here!","title":"bathroom"}
  + 12::GRIM_OBJECTIVE
    {"docType":"GRIM_OBJECTIVE","id":"12","commitId":"4","createdWithCommitId":"4","missionId":"5","status":null,"startDate":"2023-01-01","dueDate":"2024-01-01","description":"all ideas are welcome how we should design kitchen and bathroom!","type":null,"externalId":null,"questionnaireId":null,"processId":null,"locale":null,"title":"interior design ideas"}
  + 5::GRIM_MISSION
    {"docType":"GRIM_MISSION","id":"5","commitId":"4","createdWithCommitId":"4","updatedTreeWithCommitId":"4","parentMissionId":null,"externalId":null,"questionnaireId":null,"refId":"11","missionStatus":"OPEN","missionPriority":"HIGH","startDate":"2020-05-01","dueDate":"2020-06-01","reporterId":"jane.doe@housing.com","description":"The best house ever","title":"my first mission to build a house","completedAt":null,"archivedAt":null,"archivedStatus":null}

 | deleted
  - deleted: 0 entries

 | merged
  +- merged: 0 entries


commit: 7, tenant: 1
author: jane.doe@morgue.com, message: forgot to add comments to things
 | created
  + added new: 2 entries
  + 17::GRIM_REMARK
    {"docType":"GRIM_REMARK","id":"17","commitId":"7","createdWithCommitId":"7","missionId":"5","parentId":null,"transitives":null,"remarkText":"Not to self, give feedback to architects","reporterId":"jane.doe@morgue.com","remarkStatus":null,"remarkType":null,"remarkSource":null,"relation":null}
  + 18::GRIM_REMARK
    {"docType":"GRIM_REMARK","id":"18","commitId":"7","createdWithCommitId":"7","missionId":"5","parentId":null,"transitives":null,"remarkText":"Note to self, compliment works on after job well done!","reporterId":"jane.doe@morgue.com","remarkStatus":null,"remarkType":null,"remarkSource":null,"relation":null}

 | deleted
  - deleted: 0 entries

 | merged
  +- merged: 1 entries
  +- 5::GRIM_MISSION
   -  {"docType":"GRIM_MISSION","id":"5","commitId":"4","createdWithCommitId":"4","updatedTreeWithCommitId":"4","parentMissionId":null,"externalId":null,"questionnaireId":null,"refId":"11","missionStatus":"OPEN","missionPriority":"HIGH","startDate":"2020-05-01","dueDate":"2020-06-01","reporterId":"jane.doe@housing.com","description":"The best house ever","title":"my first mission to build a house","completedAt":null,"archivedAt":null,"archivedStatus":null}
   +  {"docType":"GRIM_MISSION","id":"5","commitId":"7","createdWithCommitId":"4","updatedTreeWithCommitId":"7","parentMissionId":null,"externalId":null,"questionnaireId":null,"refId":"11","missionStatus":"OPEN","missionPriority":"HIGH","startDate":"2020-05-01","dueDate":"2020-06-01","reporterId":"jane.doe@housing.com","description":"The best house ever","title":"my first mission to build a house","completedAt":null,"archivedAt":null,"archivedStatus":null}
   diff: updatedTreeWithCommitId :: 4 -> 7


commit: 9, tenant: 1
author: jane.doe@morgue.com, message: changed the title
 | created
  + added new: 0 entries

 | deleted
  - deleted: 0 entries

 | merged
  +- merged: 1 entries
  +- 5::GRIM_MISSION
   -  {"docType":"GRIM_MISSION","id":"5","commitId":"7","createdWithCommitId":"4","updatedTreeWithCommitId":"7","parentMissionId":null,"externalId":null,"questionnaireId":null,"refId":"11","missionStatus":"OPEN","missionPriority":"HIGH","startDate":"2020-05-01","dueDate":"2020-06-01","reporterId":"jane.doe@housing.com","description":"The best house ever","title":"my first mission to build a house","completedAt":null,"archivedAt":null,"archivedStatus":null}
   +  {"docType":"GRIM_MISSION","id":"5","commitId":"9","createdWithCommitId":"4","updatedTreeWithCommitId":"9","parentMissionId":null,"externalId":null,"questionnaireId":null,"refId":"11","missionStatus":"OPEN","missionPriority":"HIGH","startDate":null,"dueDate":null,"reporterId":"jane.doe@morgue.com","description":"Basic house plans for customer","title":"House plans for customer #198CC","completedAt":null,"archivedAt":null,"archivedStatus":null}
   diff: updatedTreeWithCommitId :: 7 -> 9
   diff: startDate :: 2020-05-01 -> null
   diff: dueDate :: 2020-06-01 -> null
   diff: reporterId :: jane.doe@housing.com -> jane.doe@morgue.com
   diff: description :: The best house ever -> Basic house plans for customer
   diff: title :: my first mission to build a house -> House plans for customer #198CC

""", toStaticData(repo.getRepo()));
  }
}
