package io.resys.thena.docdb.test.grim;

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
import java.util.Collections;

import org.immutables.value.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.api.actions.TenantActions.CommitStatus;
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewMission;
import io.resys.thena.docdb.test.config.DbTestTemplate;
import io.resys.thena.docdb.test.config.PgProfile;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;


@QuarkusTest
@TestProfile(PgProfile.class)
@Slf4j
public class SimpleGrimDeleteGoalTest extends DbTestTemplate {

  @Value.Immutable
  public interface TestContent extends Serializable {
    String getId();
    String getName();
  }
  
  private GrimMissionContainer createMission(TenantCommitResult repo) {
    final var result = getClient().grim(repo).commit()
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
    
    // load ids
    toStaticData(repo.getRepo());
    return result;
  }

  @Test
  public void createAndUpdateMission() {
    // create project
    TenantCommitResult repo = getClient().tenants().commit()
        .name("SimpleGrimUpdateTest-1", StructureType.grim)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(CommitStatus.OK, repo.getStatus());
    
    final var newMission = createMission(repo);
    
    // remove remark
    getClient().grim(repo).commit().modifyManyMissions()
    .commitMessage("remove remark #1")
    .commitAuthor("jane.doe@morgue.com")
    .modifyMission(newMission.getMission().getId(), (modifyMission) -> {

      final var remarkToDelete = newMission.getRemarks().values().stream()
          .filter(e -> e.getRemarkText().equals("Created main task for building a house!"))
          .findFirst().get();
      
      modifyMission
      .removeRemark(remarkToDelete.getId())
      .addCommands(Arrays.asList(JsonObject.of("commandType", "DELETE_REMARK")))
      .build();
      
    })
    .build()
    .await().atMost(Duration.ofMinutes(1));
    
    // remove remark
    getClient().grim(repo).commit().modifyManyMissions()
    .commitMessage("remove remark #2")
    .commitAuthor("jane.doe@morgue.com")
    .modifyMission(newMission.getMission().getId(), (modifyMission) -> {

      final var remarkToDelete = newMission.getRemarks().values().stream()
          .filter(e -> e.getRemarkText().equals("Waiting for results already!"))
          .findFirst().get();
      modifyMission
      .removeRemark(remarkToDelete.getId())
      .addCommands(Arrays.asList(JsonObject.of("commandType", "DELETE_REMARK")))
      .build();
    })
    .build()
    .await().atMost(Duration.ofMinutes(1));
    
    
    // remove kitchen goal
    getClient().grim(repo).commit().modifyManyMissions()
    .commitMessage("remove remark #2")
    .commitAuthor("jane.doe@morgue.com")
    .modifyMission(newMission.getMission().getId(), (modifyMission) -> {

      final var goalToDelete = newMission.getGoals().values().stream()
          .filter(e -> e.getTitle().equals("kitchen"))
          .findFirst().get();
      
      modifyMission
      .removeGoal(goalToDelete.getId())
      .addCommands(Arrays.asList(JsonObject.of("commandType", "DELETE_GOAL")))
      .build();
    })
    .build()
    .await().atMost(Duration.ofMinutes(1));

    
    
    final var refreshedMission = getClient().grim(repo).find().missionQuery().get(newMission.getMission().getId()).await().atMost(Duration.ofMinutes(1)).getObjects();
    
    // remove goals and objectives
    getClient().grim(repo).commit().modifyManyMissions()
    .commitMessage("remove remark #2")
    .commitAuthor("jane.doe@morgue.com")
    .modifyMission(refreshedMission.getMission().getId(), (modifyMission) -> {

      modifyMission.setAllLabels("keyword", Collections.emptyList(), null);
      modifyMission.setAllAssignees("worker", Collections.emptyList(), null);
      modifyMission.setAllLinks("project-plans", Collections.emptyList(), null);
      modifyMission.setAllLinks("permits", Collections.emptyList(), null);
      
      
      refreshedMission.getGoals().values().stream()
      .forEach(goalToDelete -> modifyMission
      .removeGoal(goalToDelete.getId())
      .addCommands(Arrays.asList(JsonObject.of("commandType", "DELETE_GOAL")))
      .build());
      
      refreshedMission.getObjectives().values().stream()
      .forEach(objective -> modifyMission
      .removeObjective(objective.getId())
      .addCommands(Arrays.asList(JsonObject.of("commandType", "DELETE_OBJECTIVE")))
      .build());
      
    })
    .build()
    .await().atMost(Duration.ofMinutes(1));

   
    
    Assertions.assertEquals(
"""

Repo
  - id: 1, rev: 2
    name: SimpleGrimUpdateTest-1, prefix: 3, type: grim
Mission: 5
  - 12::GRIM_COMMANDS
  - 31::GRIM_COMMANDS
  - 32::GRIM_COMMANDS
  - 33::GRIM_COMMANDS
  - 34::GRIM_COMMANDS
  - 35::GRIM_COMMANDS

commit: 4, tenant: 1
author: jane.doe@morgue.com, message: batching tests
 | created
  + added new: 16 entries
  + 19::GRIM_MISSION_LABEL
    {"id":"19","commitId":"4","labelType":"keyword","labelValue":"housing","labelBody":null,"missionId":"5","relation":null}
  + 20::GRIM_MISSION_LABEL
    {"id":"20","commitId":"4","labelType":"keyword","labelValue":"roofing","labelBody":null,"missionId":"5","relation":null}
  + 18::GRIM_ASSIGNMENT
    {"id":"18","commitId":"4","missionId":"5","assignee":"sam-from-the-mill","assignmentType":"worker","assigneeContact":null,"relation":null}
  + 17::GRIM_ASSIGNMENT
    {"id":"17","commitId":"4","missionId":"5","assignee":"jane-from-the-roofing","assignmentType":"worker","assigneeContact":null,"relation":null}
  + 22::GRIM_MISSION_LINKS
    {"id":"22","commitId":"4","createdWithCommitId":"4","missionId":"5","externalId":"site.com/plans/1","linkType":"project-plans","linkBody":null,"transitives":null,"relation":null}
  + 21::GRIM_MISSION_LINKS
    {"id":"21","commitId":"4","createdWithCommitId":"4","missionId":"5","externalId":"site.com/permits/5","linkType":"permits","linkBody":null,"transitives":null,"relation":null}
  + 10::GRIM_REMARK
    {"id":"10","commitId":"4","createdWithCommitId":"4","missionId":"5","parentId":null,"transitives":null,"remarkText":"Created main task for building a house!","reporterId":"jane.doe","remarkStatus":null,"remarkType":null,"remarkSource":null,"relation":null}
  + 11::GRIM_REMARK
    {"id":"11","commitId":"4","createdWithCommitId":"4","missionId":"5","parentId":null,"transitives":null,"remarkText":"Waiting for results already!","reporterId":"the.bob.clown","remarkStatus":null,"remarkType":null,"remarkSource":null,"relation":null}
  + 15::GRIM_ASSIGNMENT
    {"id":"15","commitId":"4","missionId":"5","assignee":"no-name-worker-1","assignmentType":"objective-worker","assigneeContact":null,"relation":{"objectiveId":"7","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}
  + 16::GRIM_ASSIGNMENT
    {"id":"16","commitId":"4","missionId":"5","assignee":"no-name-worker-2","assignmentType":"objective-worker","assigneeContact":null,"relation":{"objectiveId":"7","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}
  + 13::GRIM_ASSIGNMENT
    {"id":"13","commitId":"4","missionId":"5","assignee":"no-name-worker-3","assignmentType":"goal-worker","assigneeContact":null,"relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"9","relationType":"GOAL"}}
  + 14::GRIM_ASSIGNMENT
    {"id":"14","commitId":"4","missionId":"5","assignee":"no-name-worker-4","assignmentType":"goal-worker","assigneeContact":null,"relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"9","relationType":"GOAL"}}
  + 9::GRIM_OBJECTIVE_GOAL
    {"id":"9","commitId":"4","createdWithCommitId":"4","objectiveId":"7","goalStatus":null,"startDate":[2023,1,2],"dueDate":[2023,2,1],"description":"kitcher plan goes here!","title":"kitchen"}
  + 8::GRIM_OBJECTIVE_GOAL
    {"id":"8","commitId":"4","createdWithCommitId":"4","objectiveId":"7","goalStatus":null,"startDate":null,"dueDate":null,"description":"kitcher plan goes here!","title":"bathroom"}
  + 7::GRIM_OBJECTIVE
    {"id":"7","commitId":"4","createdWithCommitId":"4","missionId":"5","objectiveStatus":null,"startDate":[2023,1,1],"dueDate":[2024,1,1],"description":"all ideas are welcome how we should design kitchen and bathroom!","title":"interior design ideas"}
  + 5::GRIM_MISSION
    {"id":"5","commitId":"4","createdWithCommitId":"4","updatedTreeWithCommitId":"4","parentMissionId":null,"externalId":null,"questionnaireId":null,"refId":"202412-1","missionStatus":"OPEN","missionPriority":"HIGH","startDate":[2020,5,1],"dueDate":[2020,6,1],"reporterId":"jane.doe@housing.com","description":"The best house ever","title":"my first mission to build a house","completedAt":null,"archivedAt":null,"archivedStatus":null}

 | deleted
  - deleted: 0 entries

 | merged
  +- merged: 0 entries


commit: 23, tenant: 1
author: jane.doe@morgue.com, message: remove remark #1
 | created
  + added new: 0 entries

 | deleted
  - deleted: 1 entries
  - 10::GRIM_REMARK
    {"id":"10","commitId":"4","createdWithCommitId":"4","missionId":"5","parentId":null,"transitives":null,"remarkText":"Created main task for building a house!","reporterId":"jane.doe","remarkStatus":null,"remarkType":null,"remarkSource":null,"relation":null}

 | merged
  +- merged: 1 entries
  +- 5::GRIM_MISSION
   -  {"id":"5","commitId":"4","createdWithCommitId":"4","updatedTreeWithCommitId":"4","parentMissionId":null,"externalId":null,"questionnaireId":null,"refId":"4","missionStatus":"OPEN","missionPriority":"HIGH","startDate":[2020,5,1],"dueDate":[2020,6,1],"reporterId":"jane.doe@housing.com","description":"The best house ever","title":"my first mission to build a house","completedAt":null,"archivedAt":null,"archivedStatus":null}
   +  {"id":"5","commitId":"23","createdWithCommitId":"4","updatedTreeWithCommitId":"23","parentMissionId":null,"externalId":null,"questionnaireId":null,"refId":"4","missionStatus":"OPEN","missionPriority":"HIGH","startDate":[2020,5,1],"dueDate":[2020,6,1],"reporterId":"jane.doe@housing.com","description":"The best house ever","title":"my first mission to build a house","completedAt":null,"archivedAt":null,"archivedStatus":null}
   diff: commitId :: 4 -> 23
   diff: updatedTreeWithCommitId :: 4 -> 23
   diff: startDate :: [2020, 5, 1] -> [2020,5,1]
   diff: dueDate :: [2020, 6, 1] -> [2020,6,1]


commit: 25, tenant: 1
author: jane.doe@morgue.com, message: remove remark #2
 | created
  + added new: 0 entries

 | deleted
  - deleted: 1 entries
  - 11::GRIM_REMARK
    {"id":"11","commitId":"4","createdWithCommitId":"4","missionId":"5","parentId":null,"transitives":null,"remarkText":"Waiting for results already!","reporterId":"the.bob.clown","remarkStatus":null,"remarkType":null,"remarkSource":null,"relation":null}

 | merged
  +- merged: 1 entries
  +- 5::GRIM_MISSION
   -  {"id":"5","commitId":"23","createdWithCommitId":"4","updatedTreeWithCommitId":"23","parentMissionId":null,"externalId":null,"questionnaireId":null,"refId":"23","missionStatus":"OPEN","missionPriority":"HIGH","startDate":[2020,5,1],"dueDate":[2020,6,1],"reporterId":"jane.doe@housing.com","description":"The best house ever","title":"my first mission to build a house","completedAt":null,"archivedAt":null,"archivedStatus":null}
   +  {"id":"5","commitId":"25","createdWithCommitId":"4","updatedTreeWithCommitId":"25","parentMissionId":null,"externalId":null,"questionnaireId":null,"refId":"23","missionStatus":"OPEN","missionPriority":"HIGH","startDate":[2020,5,1],"dueDate":[2020,6,1],"reporterId":"jane.doe@housing.com","description":"The best house ever","title":"my first mission to build a house","completedAt":null,"archivedAt":null,"archivedStatus":null}
   diff: commitId :: 23 -> 25
   diff: updatedTreeWithCommitId :: 23 -> 25
   diff: startDate :: [2020, 5, 1] -> [2020,5,1]
   diff: dueDate :: [2020, 6, 1] -> [2020,6,1]


commit: 27, tenant: 1
author: jane.doe@morgue.com, message: remove remark #2
 | created
  + added new: 0 entries

 | deleted
  - deleted: 3 entries
  - 9::GRIM_OBJECTIVE_GOAL
    {"id":"9","commitId":"4","createdWithCommitId":"4","objectiveId":"7","goalStatus":null,"startDate":[2023,1,2],"dueDate":[2023,2,1],"description":"kitcher plan goes here!","title":"kitchen"}
  - 13::GRIM_ASSIGNMENT
    {"id":"13","commitId":"4","missionId":"5","assignee":"no-name-worker-3","assignmentType":"goal-worker","assigneeContact":null,"relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"9","relationType":"GOAL"}}
  - 14::GRIM_ASSIGNMENT
    {"id":"14","commitId":"4","missionId":"5","assignee":"no-name-worker-4","assignmentType":"goal-worker","assigneeContact":null,"relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"9","relationType":"GOAL"}}

 | merged
  +- merged: 1 entries
  +- 5::GRIM_MISSION
   -  {"id":"5","commitId":"25","createdWithCommitId":"4","updatedTreeWithCommitId":"25","parentMissionId":null,"externalId":null,"questionnaireId":null,"refId":"25","missionStatus":"OPEN","missionPriority":"HIGH","startDate":[2020,5,1],"dueDate":[2020,6,1],"reporterId":"jane.doe@housing.com","description":"The best house ever","title":"my first mission to build a house","completedAt":null,"archivedAt":null,"archivedStatus":null}
   +  {"id":"5","commitId":"27","createdWithCommitId":"4","updatedTreeWithCommitId":"27","parentMissionId":null,"externalId":null,"questionnaireId":null,"refId":"25","missionStatus":"OPEN","missionPriority":"HIGH","startDate":[2020,5,1],"dueDate":[2020,6,1],"reporterId":"jane.doe@housing.com","description":"The best house ever","title":"my first mission to build a house","completedAt":null,"archivedAt":null,"archivedStatus":null}
   diff: commitId :: 25 -> 27
   diff: updatedTreeWithCommitId :: 25 -> 27
   diff: startDate :: [2020, 5, 1] -> [2020,5,1]
   diff: dueDate :: [2020, 6, 1] -> [2020,6,1]


commit: 29, tenant: 1
author: jane.doe@morgue.com, message: remove remark #2
 | created
  + added new: 0 entries

 | deleted
  - deleted: 4 entries
  - 7::GRIM_OBJECTIVE
    {"id":"7","commitId":"4","createdWithCommitId":"4","missionId":"5","objectiveStatus":null,"startDate":[2023,1,1],"dueDate":[2024,1,1],"description":"all ideas are welcome how we should design kitchen and bathroom!","title":"interior design ideas"}
  - 8::GRIM_OBJECTIVE_GOAL
    {"id":"8","commitId":"4","createdWithCommitId":"4","objectiveId":"7","goalStatus":null,"startDate":null,"dueDate":null,"description":"kitcher plan goes here!","title":"bathroom"}
  - 15::GRIM_ASSIGNMENT
    {"id":"15","commitId":"4","missionId":"5","assignee":"no-name-worker-1","assignmentType":"objective-worker","assigneeContact":null,"relation":{"objectiveId":"7","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}
  - 16::GRIM_ASSIGNMENT
    {"id":"16","commitId":"4","missionId":"5","assignee":"no-name-worker-2","assignmentType":"objective-worker","assigneeContact":null,"relation":{"objectiveId":"7","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}

 | merged
  +- merged: 1 entries
  +- 5::GRIM_MISSION
   -  {"id":"5","commitId":"27","createdWithCommitId":"4","updatedTreeWithCommitId":"27","parentMissionId":null,"externalId":null,"questionnaireId":null,"refId":"27","missionStatus":"OPEN","missionPriority":"HIGH","startDate":[2020,5,1],"dueDate":[2020,6,1],"reporterId":"jane.doe@housing.com","description":"The best house ever","title":"my first mission to build a house","completedAt":null,"archivedAt":null,"archivedStatus":null}
   +  {"id":"5","commitId":"29","createdWithCommitId":"4","updatedTreeWithCommitId":"29","parentMissionId":null,"externalId":null,"questionnaireId":null,"refId":"27","missionStatus":"OPEN","missionPriority":"HIGH","startDate":[2020,5,1],"dueDate":[2020,6,1],"reporterId":"jane.doe@housing.com","description":"The best house ever","title":"my first mission to build a house","completedAt":null,"archivedAt":null,"archivedStatus":null}
   diff: commitId :: 27 -> 29
   diff: updatedTreeWithCommitId :: 27 -> 29
   diff: startDate :: [2020, 5, 1] -> [2020,5,1]
   diff: dueDate :: [2020, 6, 1] -> [2020,6,1]

""", toStaticData(repo.getRepo()));
  }
}
