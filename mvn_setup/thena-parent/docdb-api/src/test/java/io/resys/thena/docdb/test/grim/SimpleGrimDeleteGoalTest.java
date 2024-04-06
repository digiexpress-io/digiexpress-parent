package io.resys.thena.docdb.test.grim;

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
          .filter(e -> e.getTransitives().getTitle().equals("kitchen"))
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
  - 35::GRIM_COMMANDS
  - 36::GRIM_COMMANDS
  - 37::GRIM_COMMANDS
  - 38::GRIM_COMMANDS
  - 39::GRIM_COMMANDS
  - 19::GRIM_MISSION_DATA

commit: 4, tenant: 1
author: jane.doe@morgue.com, message: batching tests
 | created
  + added new: 20 entries
  + 23::GRIM_MISSION_LABEL
    {"id":"23","commitId":"4","labelType":"keyword","labelValue":"housing","labelBody":null,"missionId":"5","relation":null}
  + 24::GRIM_MISSION_LABEL
    {"id":"24","commitId":"4","labelType":"keyword","labelValue":"roofing","labelBody":null,"missionId":"5","relation":null}
  + 18::GRIM_ASSIGNMENT
    {"id":"18","commitId":"4","missionId":"5","assignee":"sam-from-the-mill","assignmentType":"worker","relation":null}
  + 17::GRIM_ASSIGNMENT
    {"id":"17","commitId":"4","missionId":"5","assignee":"jane-from-the-roofing","assignmentType":"worker","relation":null}
  + 26::GRIM_MISSION_LINKS
    {"id":"26","commitId":"4","missionId":"5","externalId":"site.com/plans/1","linkType":"project-plans","linkBody":null,"relation":null}
  + 25::GRIM_MISSION_LINKS
    {"id":"25","commitId":"4","missionId":"5","externalId":"site.com/permits/5","linkType":"permits","linkBody":null,"relation":null}
  + 10::GRIM_REMARK
    {"id":"10","commitId":"4","createdWithCommitId":"4","missionId":"5","createdAt":null,"updatedAt":null,"remarkText":"Created main task for building a house!","remarkStatus":null,"reporterId":"jane.doe","relation":null}
  + 11::GRIM_REMARK
    {"id":"11","commitId":"4","createdWithCommitId":"4","missionId":"5","createdAt":null,"updatedAt":null,"remarkText":"Waiting for results already!","remarkStatus":null,"reporterId":"the.bob.clown","relation":null}
  + 15::GRIM_ASSIGNMENT
    {"id":"15","commitId":"4","missionId":"5","assignee":"no-name-worker-1","assignmentType":"objective-worker","relation":{"objectiveId":"7","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}
  + 16::GRIM_ASSIGNMENT
    {"id":"16","commitId":"4","missionId":"5","assignee":"no-name-worker-2","assignmentType":"objective-worker","relation":{"objectiveId":"7","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}
  + 13::GRIM_ASSIGNMENT
    {"id":"13","commitId":"4","missionId":"5","assignee":"no-name-worker-3","assignmentType":"goal-worker","relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"8","relationType":"GOAL"}}
  + 14::GRIM_ASSIGNMENT
    {"id":"14","commitId":"4","missionId":"5","assignee":"no-name-worker-4","assignmentType":"goal-worker","relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"8","relationType":"GOAL"}}
  + 8::GRIM_OBJECTIVE_GOAL
    {"id":"8","commitId":"4","createdWithCommitId":"4","objectiveId":"7","goalStatus":null,"startDate":[2023,1,2],"dueDate":[2023,2,1]}
  + 22::GRIM_MISSION_DATA
    {"id":"22","commitId":"4","createdWithCommitId":"4","missionId":"5","createdAt":null,"updatedAt":null,"title":"kitchen","description":"kitcher plan goes here!","dataExtension":null,"relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"8","relationType":"GOAL"}}
  + 9::GRIM_OBJECTIVE_GOAL
    {"id":"9","commitId":"4","createdWithCommitId":"4","objectiveId":"7","goalStatus":null,"startDate":null,"dueDate":null}
  + 20::GRIM_MISSION_DATA
    {"id":"20","commitId":"4","createdWithCommitId":"4","missionId":"5","createdAt":null,"updatedAt":null,"title":"bathroom","description":"kitcher plan goes here!","dataExtension":null,"relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"9","relationType":"GOAL"}}
  + 7::GRIM_OBJECTIVE
    {"id":"7","commitId":"4","createdWithCommitId":"4","missionId":"5","objectiveStatus":null,"startDate":[2023,1,1],"dueDate":[2024,1,1]}
  + 21::GRIM_MISSION_DATA
    {"id":"21","commitId":"4","createdWithCommitId":"4","missionId":"5","createdAt":null,"updatedAt":null,"title":"interior design ideas","description":"all ideas are welcome how we should design kitchen and bathroom!","dataExtension":null,"relation":{"objectiveId":"7","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}
  + 5::GRIM_MISSION
    {"id":"5","commitId":"4","createdWithCommitId":"4","updatedTreeWithCommitId":"4","parentMissionId":null,"externalId":null,"missionStatus":"OPEN","missionPriority":"HIGH","reporterId":"jane.doe@housing.com","startDate":[2020,5,1],"dueDate":[2020,6,1],"archivedDate":null,"archivedStatus":null}
  + 19::GRIM_MISSION_DATA
    {"id":"19","commitId":"4","createdWithCommitId":"4","missionId":"5","createdAt":null,"updatedAt":null,"title":"The best house ever","description":"","dataExtension":null,"relation":null}

 | deleted
  - deleted: 0 entries

 | merged
  +- merged: 0 entries


commit: 27, tenant: 1
author: jane.doe@morgue.com, message: remove remark #1
 | created
  + added new: 0 entries

 | deleted
  - deleted: 1 entries
  - 10::GRIM_REMARK
    {"id":"10","commitId":"4","createdWithCommitId":"4","missionId":"5","createdAt":"OffsetDateTime.now()","updatedAt":"OffsetDateTime.now()","remarkText":"Created main task for building a house!","remarkStatus":null,"reporterId":"jane.doe","relation":null}

 | merged
  +- merged: 0 entries


commit: 29, tenant: 1
author: jane.doe@morgue.com, message: remove remark #2
 | created
  + added new: 0 entries

 | deleted
  - deleted: 1 entries
  - 11::GRIM_REMARK
    {"id":"11","commitId":"4","createdWithCommitId":"4","missionId":"5","createdAt":"OffsetDateTime.now()","updatedAt":"OffsetDateTime.now()","remarkText":"Waiting for results already!","remarkStatus":null,"reporterId":"the.bob.clown","relation":null}

 | merged
  +- merged: 0 entries


commit: 31, tenant: 1
author: jane.doe@morgue.com, message: remove remark #2
 | created
  + added new: 0 entries

 | deleted
  - deleted: 4 entries
  - 8::GRIM_OBJECTIVE_GOAL
    {"id":"8","commitId":"4","createdWithCommitId":"4","objectiveId":"7","goalStatus":null,"startDate":[2023,1,2],"dueDate":[2023,2,1]}
  - 13::GRIM_ASSIGNMENT
    {"id":"13","commitId":"4","missionId":"5","assignee":"no-name-worker-3","assignmentType":"goal-worker","relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"8","relationType":"GOAL"}}
  - 14::GRIM_ASSIGNMENT
    {"id":"14","commitId":"4","missionId":"5","assignee":"no-name-worker-4","assignmentType":"goal-worker","relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"8","relationType":"GOAL"}}
  - 22::GRIM_MISSION_DATA
    {"id":"22","commitId":"4","createdWithCommitId":"4","missionId":"5","createdAt":"OffsetDateTime.now()","updatedAt":"OffsetDateTime.now()","title":"kitchen","description":"kitcher plan goes here!","dataExtension":null,"relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"8","relationType":"GOAL"}}

 | merged
  +- merged: 0 entries


commit: 33, tenant: 1
author: jane.doe@morgue.com, message: remove remark #2
 | created
  + added new: 0 entries

 | deleted
  - deleted: 6 entries
  - 7::GRIM_OBJECTIVE
    {"id":"7","commitId":"4","createdWithCommitId":"4","missionId":"5","objectiveStatus":null,"startDate":[2023,1,1],"dueDate":[2024,1,1]}
  - 9::GRIM_OBJECTIVE_GOAL
    {"id":"9","commitId":"4","createdWithCommitId":"4","objectiveId":"7","goalStatus":null,"startDate":null,"dueDate":null}
  - 15::GRIM_ASSIGNMENT
    {"id":"15","commitId":"4","missionId":"5","assignee":"no-name-worker-1","assignmentType":"objective-worker","relation":{"objectiveId":"7","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}
  - 16::GRIM_ASSIGNMENT
    {"id":"16","commitId":"4","missionId":"5","assignee":"no-name-worker-2","assignmentType":"objective-worker","relation":{"objectiveId":"7","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}
  - 20::GRIM_MISSION_DATA
    {"id":"20","commitId":"4","createdWithCommitId":"4","missionId":"5","createdAt":"OffsetDateTime.now()","updatedAt":"OffsetDateTime.now()","title":"bathroom","description":"kitcher plan goes here!","dataExtension":null,"relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"9","relationType":"GOAL"}}
  - 21::GRIM_MISSION_DATA
    {"id":"21","commitId":"4","createdWithCommitId":"4","missionId":"5","createdAt":"OffsetDateTime.now()","updatedAt":"OffsetDateTime.now()","title":"interior design ideas","description":"all ideas are welcome how we should design kitchen and bathroom!","dataExtension":null,"relation":{"objectiveId":"7","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}

 | merged
  +- merged: 0 entries

""", toStaticData(repo.getRepo()));
  }
}
