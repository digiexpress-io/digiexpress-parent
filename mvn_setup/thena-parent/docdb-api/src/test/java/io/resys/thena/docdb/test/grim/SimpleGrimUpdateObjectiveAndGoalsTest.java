package io.resys.thena.docdb.test.grim;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;

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
public class SimpleGrimUpdateObjectiveAndGoalsTest extends DbTestTemplate {
  
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
        .name("SimpleGrimUpdateObjectiveAndGoalsTest-1", StructureType.grim)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(CommitStatus.OK, repo.getStatus());
    
    final var newMission = createMission(repo);
    final var remarkToModify = newMission.getRemarks().values().stream().filter(e -> e.getRemarkText().equals("Created main task for building a house!")).findFirst().get();
    final var goalToModify = newMission.getGoals().values().stream().filter(e -> e.getTransitives().getTitle().equals("kitchen")).findFirst().get();
    final var objectiveToModify = newMission.getObjectives().values().stream().filter(e -> e.getTransitives().getTitle().equals("interior design ideas")).findFirst().get();
    
    getClient().grim(repo).commit().modifyManyMissions()
    .commitMessage("forgot to add comments to things")
    .commitAuthor("jane.doe@morgue.com")
    .modifyMission(newMission.getMission().getId(), (modifyMission) -> {
      
      modifyMission
      .addCommands(Arrays.asList(JsonObject.of("commandType", "MODIFY_REMARK")))
      
      .modifyRemark(remarkToModify.getId(), (modifyRemark) -> 
        modifyRemark
        .remarkText("Main task for building customer #C19837 house")
        .build())
      .modifyObjective(objectiveToModify.getId(), modifyObjective -> {
        modifyObjective
        .title("Interior Definition")
        .description("White marble everywhere!")
        .build();
      })
      .modifyGoal(goalToModify.getId(), modifyObjective -> {
        modifyObjective
        .title("Kitchen with white marble")
        .description("Design a very nice kitchen for lots and lots of cooking dramas!")
        .build();
      })
      .build();
      
    })
    .build()
    .await().atMost(Duration.ofMinutes(1));
        
    
    
    Assertions.assertEquals(
"""

Repo
  - id: 1, rev: 2
    name: SimpleGrimUpdateObjectiveAndGoalsTest-1, prefix: 3, type: grim
Mission: 5
  - 9::GRIM_OBJECTIVE
  - 10::GRIM_OBJECTIVE_GOAL
  - 11::GRIM_OBJECTIVE_GOAL
  - 12::GRIM_REMARK
  - 13::GRIM_REMARK
  - 14::GRIM_COMMANDS
  - 15::GRIM_COMMANDS
  - 16::GRIM_ASSIGNMENT
  - 17::GRIM_ASSIGNMENT
  - 18::GRIM_ASSIGNMENT
  - 19::GRIM_ASSIGNMENT
  - 20::GRIM_ASSIGNMENT
  - 21::GRIM_ASSIGNMENT
  - 22::GRIM_MISSION_DATA
  - 23::GRIM_MISSION_DATA
  - 24::GRIM_MISSION_DATA
  - 25::GRIM_MISSION_DATA
  - 26::GRIM_MISSION_LABEL
  - 27::GRIM_MISSION_LABEL
  - 28::GRIM_MISSION_LINKS
  - 29::GRIM_MISSION_LINKS

commit: 4, tenant: 1
author: jane.doe@morgue.com, message: batching tests
 | created
  + added new: 20 entries
  + 26::GRIM_MISSION_LABEL
    {"id":"26","commitId":"4","labelType":"keyword","labelValue":"housing","labelBody":null,"missionId":"5","relation":null}
  + 27::GRIM_MISSION_LABEL
    {"id":"27","commitId":"4","labelType":"keyword","labelValue":"roofing","labelBody":null,"missionId":"5","relation":null}
  + 21::GRIM_ASSIGNMENT
    {"id":"21","commitId":"4","missionId":"5","assignee":"sam-from-the-mill","assignmentType":"worker","relation":null}
  + 20::GRIM_ASSIGNMENT
    {"id":"20","commitId":"4","missionId":"5","assignee":"jane-from-the-roofing","assignmentType":"worker","relation":null}
  + 29::GRIM_MISSION_LINKS
    {"id":"29","commitId":"4","missionId":"5","externalId":"site.com/plans/1","linkType":"project-plans","linkBody":null,"relation":null}
  + 28::GRIM_MISSION_LINKS
    {"id":"28","commitId":"4","missionId":"5","externalId":"site.com/permits/5","linkType":"permits","linkBody":null,"relation":null}
  + 13::GRIM_REMARK
    {"id":"13","commitId":"4","createdWithCommitId":"4","missionId":"5","createdAt":null,"updatedAt":null,"remarkText":"Created main task for building a house!","remarkStatus":null,"reporterId":"jane.doe","relation":null}
  + 12::GRIM_REMARK
    {"id":"12","commitId":"4","createdWithCommitId":"4","missionId":"5","createdAt":null,"updatedAt":null,"remarkText":"Waiting for results already!","remarkStatus":null,"reporterId":"the.bob.clown","relation":null}
  + 18::GRIM_ASSIGNMENT
    {"id":"18","commitId":"4","missionId":"5","assignee":"no-name-worker-1","assignmentType":"objective-worker","relation":{"objectiveId":"9","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}
  + 19::GRIM_ASSIGNMENT
    {"id":"19","commitId":"4","missionId":"5","assignee":"no-name-worker-2","assignmentType":"objective-worker","relation":{"objectiveId":"9","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}
  + 16::GRIM_ASSIGNMENT
    {"id":"16","commitId":"4","missionId":"5","assignee":"no-name-worker-3","assignmentType":"goal-worker","relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"10","relationType":"GOAL"}}
  + 17::GRIM_ASSIGNMENT
    {"id":"17","commitId":"4","missionId":"5","assignee":"no-name-worker-4","assignmentType":"goal-worker","relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"10","relationType":"GOAL"}}
  + 10::GRIM_OBJECTIVE_GOAL
    {"id":"10","commitId":"4","createdWithCommitId":"4","objectiveId":"9","goalStatus":null,"startDate":[2023,1,2],"dueDate":[2023,2,1]}
  + 25::GRIM_MISSION_DATA
    {"id":"25","commitId":"4","createdWithCommitId":"4","missionId":"5","createdAt":null,"updatedAt":null,"title":"kitchen","description":"kitcher plan goes here!","dataExtension":null,"relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"10","relationType":"GOAL"}}
  + 11::GRIM_OBJECTIVE_GOAL
    {"id":"11","commitId":"4","createdWithCommitId":"4","objectiveId":"9","goalStatus":null,"startDate":null,"dueDate":null}
  + 23::GRIM_MISSION_DATA
    {"id":"23","commitId":"4","createdWithCommitId":"4","missionId":"5","createdAt":null,"updatedAt":null,"title":"bathroom","description":"kitcher plan goes here!","dataExtension":null,"relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"11","relationType":"GOAL"}}
  + 9::GRIM_OBJECTIVE
    {"id":"9","commitId":"4","createdWithCommitId":"4","missionId":"5","objectiveStatus":null,"startDate":[2023,1,1],"dueDate":[2024,1,1]}
  + 24::GRIM_MISSION_DATA
    {"id":"24","commitId":"4","createdWithCommitId":"4","missionId":"5","createdAt":null,"updatedAt":null,"title":"interior design ideas","description":"all ideas are welcome how we should design kitchen and bathroom!","dataExtension":null,"relation":{"objectiveId":"9","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}
  + 5::GRIM_MISSION
    {"id":"5","commitId":"4","createdWithCommitId":"4","updatedTreeWithCommitId":"4","parentMissionId":null,"externalId":null,"missionStatus":"OPEN","missionPriority":"HIGH","reporterId":"jane.doe@housing.com","startDate":[2020,5,1],"dueDate":[2020,6,1],"archivedDate":null,"archivedStatus":null}
  + 22::GRIM_MISSION_DATA
    {"id":"22","commitId":"4","createdWithCommitId":"4","missionId":"5","createdAt":null,"updatedAt":null,"title":"The best house ever","description":"","dataExtension":null,"relation":null}

 | deleted
  - deleted: 0 entries

 | merged
  +- merged: 0 entries


commit: 7, tenant: 1
author: jane.doe@morgue.com, message: forgot to add comments to things
 | created
  + added new: 0 entries

 | deleted
  - deleted: 0 entries

 | merged
  +- merged: 3 entries
  +- 13::GRIM_REMARK
   -  {"id":"13","commitId":"4","createdWithCommitId":"4","missionId":"5","createdAt":"OffsetDateTime.now()","updatedAt":"OffsetDateTime.now()","remarkText":"Created main task for building a house!","remarkStatus":null,"reporterId":"jane.doe","relation":null}
   +  {"id":"13","commitId":"7","createdWithCommitId":"4","missionId":"5","createdAt":"OffsetDateTime.now()","updatedAt":"OffsetDateTime.now()","remarkText":"Main task for building customer #C19837 house","remarkStatus":null,"reporterId":"jane.doe","relation":null}
   diff: commitId :: 4 -> 7
   diff: remarkText :: Created main task for building a house! -> Main task for building customer #C19837 house
  +- 24::GRIM_MISSION_DATA
   -  {"id":"24","commitId":"4","createdWithCommitId":"4","missionId":"5","createdAt":"OffsetDateTime.now()","updatedAt":"OffsetDateTime.now()","title":"interior design ideas","description":"all ideas are welcome how we should design kitchen and bathroom!","dataExtension":null,"relation":{"objectiveId":"9","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}
   +  {"id":"24","commitId":"7","createdWithCommitId":"4","missionId":"5","createdAt":"OffsetDateTime.now()","updatedAt":"OffsetDateTime.now()","title":"Interior Definition","description":"White marble everywhere!","dataExtension":null,"relation":{"objectiveId":"9","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}
   diff: commitId :: 4 -> 7
   diff: title :: interior design ideas -> Interior Definition
   diff: description :: all ideas are welcome how we should design kitchen and bathroom! -> White marble everywhere!
   diff: relation :: {objectiveId=9, remarkId=null, objectiveGoalId=null, relationType=OBJECTIVE} -> {"objectiveId":"9","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}
  +- 25::GRIM_MISSION_DATA
   -  {"id":"25","commitId":"4","createdWithCommitId":"4","missionId":"5","createdAt":"OffsetDateTime.now()","updatedAt":"OffsetDateTime.now()","title":"kitchen","description":"kitcher plan goes here!","dataExtension":null,"relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"10","relationType":"GOAL"}}
   +  {"id":"25","commitId":"7","createdWithCommitId":"4","missionId":"5","createdAt":"OffsetDateTime.now()","updatedAt":"OffsetDateTime.now()","title":"Kitchen with white marble","description":"Design a very nice kitchen for lots and lots of cooking dramas!","dataExtension":null,"relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"10","relationType":"GOAL"}}
   diff: commitId :: 4 -> 7
   diff: title :: kitchen -> Kitchen with white marble
   diff: description :: kitcher plan goes here! -> Design a very nice kitchen for lots and lots of cooking dramas!
   diff: relation :: {objectiveId=null, remarkId=null, objectiveGoalId=10, relationType=GOAL} -> {"objectiveId":null,"remarkId":null,"objectiveGoalId":"10","relationType":"GOAL"}

""", toStaticData(repo.getRepo()));
  }
}
