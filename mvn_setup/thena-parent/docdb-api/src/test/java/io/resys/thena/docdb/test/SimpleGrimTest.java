package io.resys.thena.docdb.test;

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
public class SimpleGrimTest extends DbTestTemplate {

  @Value.Immutable
  public interface TestContent extends Serializable {
    String getId();
    String getName();
  }

  @Test
  public void createAndUpdateMission() {
    // create project
    TenantCommitResult repo = getClient().tenants().commit()
        .name("SimpleGrimTest-1", StructureType.grim)
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
        .await().atMost(Duration.ofMinutes(1));
    
    final var newMission = createdTask.getMissions().iterator().next();
    
    // add comments
    getClient().grim(repo).commit().modifyManyMission()
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
    getClient().grim(repo).commit().modifyManyMission()
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
    name: SimpleGrimTest-1, prefix: 3, type: grim
Mission: 5
  - 11::GRIM_ASSIGNMENT
  - 12::GRIM_ASSIGNMENT
  - 13::GRIM_ASSIGNMENT
  - 14::GRIM_ASSIGNMENT
  - 15::GRIM_ASSIGNMENT
  - 16::GRIM_ASSIGNMENT
  - 17::GRIM_MISSION_DATA
  - 18::GRIM_MISSION_DATA
  - 19::GRIM_MISSION_DATA
  - 20::GRIM_MISSION_DATA
  - 21::GRIM_MISSION_LABEL
  - 22::GRIM_MISSION_LABEL
  - 23::GRIM_MISSION_LINKS
  - 24::GRIM_MISSION_LINKS
  - 25::GRIM_OBJECTIVE
  - 26::GRIM_OBJECTIVE_GOAL
  - 27::GRIM_OBJECTIVE_GOAL
  - 28::GRIM_REMARK
  - 29::GRIM_REMARK
  - 30::GRIM_REMARK
  - 31::GRIM_REMARK
  - 32::GRIM_COMMANDS
  - 33::GRIM_COMMANDS
  - 34::GRIM_COMMANDS
  - 35::GRIM_COMMANDS

commit: 4, tenant: 1
 | created
  + added new: 20 entries
  + 21::GRIM_MISSION_LABEL
    {"id":"21","commitId":"4","labelType":"keyword","labelValue":"housing","labelBody":null,"missionId":"5","relation":null}
  + 22::GRIM_MISSION_LABEL
    {"id":"22","commitId":"4","labelType":"keyword","labelValue":"roofing","labelBody":null,"missionId":"5","relation":null}
  + 16::GRIM_ASSIGNMENT
    {"id":"16","commitId":"4","missionId":"5","assignee":"sam-from-the-mill","assignmentType":"worker","relation":null}
  + 15::GRIM_ASSIGNMENT
    {"id":"15","commitId":"4","missionId":"5","assignee":"jane-from-the-roofing","assignmentType":"worker","relation":null}
  + 24::GRIM_MISSION_LINKS
    {"id":"24","commitId":"4","missionId":"5","externalId":"site.com/plans/1","linkType":"project-plans","linkBody":null,"relation":null}
  + 23::GRIM_MISSION_LINKS
    {"id":"23","commitId":"4","missionId":"5","externalId":"site.com/permits/5","linkType":"permits","linkBody":null,"relation":null}
  + 28::GRIM_REMARK
    {"id":"28","commitId":"4","missionId":"5","remarkText":"Created main task for building a house!","remarkStatus":null,"reporterId":"jane.doe","relation":null}
  + 29::GRIM_REMARK
    {"id":"29","commitId":"4","missionId":"5","remarkText":"Waiting for results already!","remarkStatus":null,"reporterId":"the.bob.clown","relation":null}
  + 13::GRIM_ASSIGNMENT
    {"id":"13","commitId":"4","missionId":"5","assignee":"no-name-worker-1","assignmentType":"objective-worker","relation":{"objectiveId":"25","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}
  + 14::GRIM_ASSIGNMENT
    {"id":"14","commitId":"4","missionId":"5","assignee":"no-name-worker-2","assignmentType":"objective-worker","relation":{"objectiveId":"25","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}
  + 11::GRIM_ASSIGNMENT
    {"id":"11","commitId":"4","missionId":"5","assignee":"no-name-worker-3","assignmentType":"goal-worker","relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"26","relationType":"GOAL"}}
  + 12::GRIM_ASSIGNMENT
    {"id":"12","commitId":"4","missionId":"5","assignee":"no-name-worker-4","assignmentType":"goal-worker","relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"26","relationType":"GOAL"}}
  + 26::GRIM_OBJECTIVE_GOAL
    {"id":"26","commitId":"4","objectiveId":"25","missionId":"5","goalStatus":null,"startDate":[2023,1,2],"dueDate":[2023,2,1]}
  + 19::GRIM_MISSION_DATA
    {"id":"19","commitId":"4","missionId":"5","title":"kitchen","description":"kitcher plan goes here!","dataExtension":null,"relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"26","relationType":"GOAL"}}
  + 27::GRIM_OBJECTIVE_GOAL
    {"id":"27","commitId":"4","objectiveId":"25","missionId":"5","goalStatus":null,"startDate":null,"dueDate":null}
  + 17::GRIM_MISSION_DATA
    {"id":"17","commitId":"4","missionId":"5","title":"bathroom","description":"kitcher plan goes here!","dataExtension":null,"relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"27","relationType":"GOAL"}}
  + 25::GRIM_OBJECTIVE
    {"id":"25","commitId":"4","missionId":"5","objectiveStatus":null,"startDate":[2023,1,1],"dueDate":[2024,1,1]}
  + 18::GRIM_MISSION_DATA
    {"id":"18","commitId":"4","missionId":"5","title":"interior design ideas","description":"all ideas are welcome how we should design kitchen and bathroom!","dataExtension":null,"relation":{"objectiveId":"25","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}
  + 5::GRIM_MISSION
    {"id":"5","commitId":"4","createdWithCommitId":"4","updatedTreeWithCommitId":"4","createdAt":null,"updatedAt":null,"treeUpdatedAt":null,"parentMissionId":null,"externalId":null,"missionStatus":"OPEN","missionPriority":"HIGH","reporterId":"jane.doe@housing.com","startDate":[2020,5,1],"dueDate":[2020,6,1],"archivedDate":null,"archivedStatus":null}
  + 20::GRIM_MISSION_DATA
    {"id":"20","commitId":"4","missionId":"5","title":"The best house ever","description":"","dataExtension":null,"relation":null}

 | deleted
  - deleted: 0 entries

 | merged
  +- merged: 0 entries


commit: 7, tenant: 1
 | created
  + added new: 2 entries
  + 30::GRIM_REMARK
    {"id":"30","commitId":"7","missionId":"5","remarkText":"Not to self, give feedback to architects","remarkStatus":null,"reporterId":"jane.doe@morgue.com","relation":null}
  + 31::GRIM_REMARK
    {"id":"31","commitId":"7","missionId":"5","remarkText":"Note to self, compliment works on after job well done!","remarkStatus":null,"reporterId":"jane.doe@morgue.com","relation":null}

 | deleted
  - deleted: 0 entries

 | merged
  +- merged: 0 entries


commit: 9, tenant: 1
 | created
  + added new: 0 entries

 | deleted
  - deleted: 0 entries

 | merged
  +- merged: 2 entries
  +- 20::GRIM_MISSION_DATA
   -  {"id":"20","commitId":"4","missionId":"5","title":"The best house ever","description":"","dataExtension":null,"relation":null}
   +  {"id":"20","commitId":"9","missionId":"5","title":"House plans for customer #198CC","description":"Basic house plans for customer","dataExtension":null,"relation":null}
  +- 5::GRIM_MISSION
   -  {"id":"5","commitId":"4","createdWithCommitId":"4","updatedTreeWithCommitId":"4","createdAt":"OffsetDateTime.now()","updatedAt":"OffsetDateTime.now()","treeUpdatedAt":"OffsetDateTime.now()","parentMissionId":null,"externalId":null,"missionStatus":"OPEN","missionPriority":"HIGH","reporterId":"jane.doe@housing.com","startDate":[2020,5,1],"dueDate":[2020,6,1],"archivedDate":null,"archivedStatus":null}
   +  {"id":"5","commitId":"9","createdWithCommitId":"9","updatedTreeWithCommitId":"4","createdAt":"OffsetDateTime.now()","updatedAt":"OffsetDateTime.now()","treeUpdatedAt":"OffsetDateTime.now()","parentMissionId":null,"externalId":null,"missionStatus":"OPEN","missionPriority":"HIGH","reporterId":"jane.doe@morgue.com","startDate":null,"dueDate":null,"archivedDate":null,"archivedStatus":null}

""", toStaticData(repo.getRepo()));
  }
}
