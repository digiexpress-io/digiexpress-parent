package io.resys.thena.docdb.test.grim;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;

import org.immutables.value.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.api.actions.TenantActions.CommitStatus;
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewMission;
import io.resys.thena.docdb.test.config.DbTestTemplate;
import io.resys.thena.docdb.test.config.PgProfile;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;


@QuarkusTest
@TestProfile(PgProfile.class)
@Slf4j
public class CreateMissionWithGoalsAndAssignees extends DbTestTemplate {

  @Value.Immutable
  public interface TestContent extends Serializable {
    String getId();
    String getName();
  }

  @Test
  public void createAndUpdateMission() {
    // create project
    TenantCommitResult repo = getClient().tenants().commit()
        .name("CreateMissionWithGoalsAndAssignees-1", StructureType.grim)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(CommitStatus.OK, repo.getStatus());
    
    
    final var createdTask = getClient().grim(repo).commit()
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
            .addRemark(newRemark -> newRemark
                .remarkText("Waiting for results already!").reporterId("the.bob.clown")
                .addAssignees(newAssignee -> newAssignee.assignmentType("worker").assignee("sam-from-the-mill").build())
                .addAssignees(newAssignee -> newAssignee.assignmentType("worker").assignee("jane-from-the-roofing").build())
                .build())
            
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
        .await().atMost(Duration.ofMinutes(1));
    
    final var newMission = createdTask.getMissions().iterator().next();
    
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
    name: CreateMissionWithGoalsAndAssignees-1, prefix: 3, type: grim
Mission: 5
  - 11::GRIM_OBJECTIVE
  - 12::GRIM_OBJECTIVE_GOAL
  - 13::GRIM_OBJECTIVE_GOAL
  - 14::GRIM_REMARK
  - 15::GRIM_REMARK
  - 16::GRIM_REMARK
  - 17::GRIM_REMARK
  - 18::GRIM_COMMANDS
  - 19::GRIM_COMMANDS
  - 20::GRIM_COMMANDS
  - 21::GRIM_COMMANDS
  - 22::GRIM_ASSIGNMENT
  - 23::GRIM_ASSIGNMENT
  - 24::GRIM_ASSIGNMENT
  - 25::GRIM_ASSIGNMENT
  - 26::GRIM_ASSIGNMENT
  - 27::GRIM_ASSIGNMENT
  - 28::GRIM_ASSIGNMENT
  - 29::GRIM_ASSIGNMENT
  - 30::GRIM_MISSION_DATA
  - 31::GRIM_MISSION_DATA
  - 32::GRIM_MISSION_DATA
  - 33::GRIM_MISSION_DATA
  - 34::GRIM_MISSION_LABEL
  - 35::GRIM_MISSION_LABEL
  - 36::GRIM_MISSION_LINKS
  - 37::GRIM_MISSION_LINKS

commit: 4, tenant: 1
author: jane.doe@morgue.com, message: batching tests
 | created
  + added new: 22 entries
  + 34::GRIM_MISSION_LABEL
    {"id":"34","commitId":"4","labelType":"keyword","labelValue":"housing","labelBody":null,"missionId":"5","relation":null}
  + 35::GRIM_MISSION_LABEL
    {"id":"35","commitId":"4","labelType":"keyword","labelValue":"roofing","labelBody":null,"missionId":"5","relation":null}
  + 28::GRIM_ASSIGNMENT
    {"id":"28","commitId":"4","missionId":"5","assignee":"sam-from-the-mill","assignmentType":"worker","relation":null}
  + 26::GRIM_ASSIGNMENT
    {"id":"26","commitId":"4","missionId":"5","assignee":"jane-from-the-roofing","assignmentType":"worker","relation":null}
  + 37::GRIM_MISSION_LINKS
    {"id":"37","commitId":"4","missionId":"5","externalId":"site.com/plans/1","linkType":"project-plans","linkBody":null,"relation":null}
  + 36::GRIM_MISSION_LINKS
    {"id":"36","commitId":"4","missionId":"5","externalId":"site.com/permits/5","linkType":"permits","linkBody":null,"relation":null}
  + 14::GRIM_REMARK
    {"id":"14","commitId":"4","createdWithCommitId":"4","missionId":"5","createdAt":null,"updatedAt":null,"remarkText":"Created main task for building a house!","remarkStatus":null,"reporterId":"jane.doe","relation":null}
  + 29::GRIM_ASSIGNMENT
    {"id":"29","commitId":"4","missionId":"5","assignee":"sam-from-the-mill","assignmentType":"worker","relation":{"objectiveId":null,"remarkId":"15","objectiveGoalId":null,"relationType":"REMARK"}}
  + 27::GRIM_ASSIGNMENT
    {"id":"27","commitId":"4","missionId":"5","assignee":"jane-from-the-roofing","assignmentType":"worker","relation":{"objectiveId":null,"remarkId":"15","objectiveGoalId":null,"relationType":"REMARK"}}
  + 15::GRIM_REMARK
    {"id":"15","commitId":"4","createdWithCommitId":"4","missionId":"5","createdAt":null,"updatedAt":null,"remarkText":"Waiting for results already!","remarkStatus":null,"reporterId":"the.bob.clown","relation":null}
  + 24::GRIM_ASSIGNMENT
    {"id":"24","commitId":"4","missionId":"5","assignee":"no-name-worker-1","assignmentType":"objective-worker","relation":{"objectiveId":"11","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}
  + 25::GRIM_ASSIGNMENT
    {"id":"25","commitId":"4","missionId":"5","assignee":"no-name-worker-2","assignmentType":"objective-worker","relation":{"objectiveId":"11","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}
  + 22::GRIM_ASSIGNMENT
    {"id":"22","commitId":"4","missionId":"5","assignee":"no-name-worker-3","assignmentType":"goal-worker","relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"12","relationType":"GOAL"}}
  + 23::GRIM_ASSIGNMENT
    {"id":"23","commitId":"4","missionId":"5","assignee":"no-name-worker-4","assignmentType":"goal-worker","relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"12","relationType":"GOAL"}}
  + 12::GRIM_OBJECTIVE_GOAL
    {"id":"12","commitId":"4","createdWithCommitId":"4","objectiveId":"11","goalStatus":null,"startDate":[2023,1,2],"dueDate":[2023,2,1]}
  + 32::GRIM_MISSION_DATA
    {"id":"32","commitId":"4","createdWithCommitId":"4","missionId":"5","createdAt":null,"updatedAt":null,"title":"kitchen","description":"kitcher plan goes here!","dataExtension":null,"relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"12","relationType":"GOAL"}}
  + 13::GRIM_OBJECTIVE_GOAL
    {"id":"13","commitId":"4","createdWithCommitId":"4","objectiveId":"11","goalStatus":null,"startDate":null,"dueDate":null}
  + 30::GRIM_MISSION_DATA
    {"id":"30","commitId":"4","createdWithCommitId":"4","missionId":"5","createdAt":null,"updatedAt":null,"title":"bathroom","description":"kitcher plan goes here!","dataExtension":null,"relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"13","relationType":"GOAL"}}
  + 11::GRIM_OBJECTIVE
    {"id":"11","commitId":"4","createdWithCommitId":"4","missionId":"5","objectiveStatus":null,"startDate":[2023,1,1],"dueDate":[2024,1,1]}
  + 31::GRIM_MISSION_DATA
    {"id":"31","commitId":"4","createdWithCommitId":"4","missionId":"5","createdAt":null,"updatedAt":null,"title":"interior design ideas","description":"all ideas are welcome how we should design kitchen and bathroom!","dataExtension":null,"relation":{"objectiveId":"11","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}
  + 5::GRIM_MISSION
    {"id":"5","commitId":"4","createdWithCommitId":"4","updatedTreeWithCommitId":"4","parentMissionId":null,"externalId":null,"missionStatus":"OPEN","missionPriority":"HIGH","reporterId":"jane.doe@housing.com","startDate":[2020,5,1],"dueDate":[2020,6,1],"archivedDate":null,"archivedStatus":null}
  + 33::GRIM_MISSION_DATA
    {"id":"33","commitId":"4","createdWithCommitId":"4","missionId":"5","createdAt":null,"updatedAt":null,"title":"The best house ever","description":"","dataExtension":null,"relation":null}

 | deleted
  - deleted: 0 entries

 | merged
  +- merged: 0 entries


commit: 7, tenant: 1
author: jane.doe@morgue.com, message: forgot to add comments to things
 | created
  + added new: 2 entries
  + 16::GRIM_REMARK
    {"id":"16","commitId":"7","createdWithCommitId":"7","missionId":"5","createdAt":null,"updatedAt":null,"remarkText":"Not to self, give feedback to architects","remarkStatus":null,"reporterId":"jane.doe@morgue.com","relation":null}
  + 17::GRIM_REMARK
    {"id":"17","commitId":"7","createdWithCommitId":"7","missionId":"5","createdAt":null,"updatedAt":null,"remarkText":"Note to self, compliment works on after job well done!","remarkStatus":null,"reporterId":"jane.doe@morgue.com","relation":null}

 | deleted
  - deleted: 0 entries

 | merged
  +- merged: 0 entries


commit: 9, tenant: 1
author: jane.doe@morgue.com, message: changed the title
 | created
  + added new: 0 entries

 | deleted
  - deleted: 0 entries

 | merged
  +- merged: 2 entries
  +- 33::GRIM_MISSION_DATA
   -  {"id":"33","commitId":"4","createdWithCommitId":"4","missionId":"5","createdAt":"OffsetDateTime.now()","updatedAt":"OffsetDateTime.now()","title":"The best house ever","description":"","dataExtension":null,"relation":null}
   +  {"id":"33","commitId":"9","createdWithCommitId":"4","missionId":"5","createdAt":"OffsetDateTime.now()","updatedAt":"OffsetDateTime.now()","title":"House plans for customer #198CC","description":"Basic house plans for customer","dataExtension":null,"relation":null}
   diff: commitId :: 4 -> 9
   diff: title :: The best house ever -> House plans for customer #198CC
   diff: description ::  -> Basic house plans for customer
  +- 5::GRIM_MISSION
   -  {"id":"5","commitId":"4","createdWithCommitId":"4","updatedTreeWithCommitId":"4","parentMissionId":null,"externalId":null,"missionStatus":"OPEN","missionPriority":"HIGH","reporterId":"jane.doe@housing.com","startDate":[2020,5,1],"dueDate":[2020,6,1],"archivedDate":null,"archivedStatus":null}
   +  {"id":"5","commitId":"9","createdWithCommitId":"4","updatedTreeWithCommitId":"9","parentMissionId":null,"externalId":null,"missionStatus":"OPEN","missionPriority":"HIGH","reporterId":"jane.doe@morgue.com","startDate":null,"dueDate":null,"archivedDate":null,"archivedStatus":null}
   diff: commitId :: 4 -> 9
   diff: updatedTreeWithCommitId :: 4 -> 9
   diff: reporterId :: jane.doe@housing.com -> jane.doe@morgue.com
   diff: startDate :: [2020, 5, 1] -> null
   diff: dueDate :: [2020, 6, 1] -> null

""", toStaticData(repo.getRepo()));
  }
}
