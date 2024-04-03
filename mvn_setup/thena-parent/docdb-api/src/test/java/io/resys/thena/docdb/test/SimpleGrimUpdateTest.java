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
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewMission;
import io.resys.thena.docdb.test.config.DbTestTemplate;
import io.resys.thena.docdb.test.config.PgProfile;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;


@QuarkusTest
@TestProfile(PgProfile.class)
@Slf4j
public class SimpleGrimUpdateTest extends DbTestTemplate {

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
        .name("SimpleGrimUpdateTest-1", StructureType.grim)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(CommitStatus.OK, repo.getStatus());
    
    final var newMission = createMission(repo).getMissions().values().iterator().next();
    
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
    name: SimpleGrimUpdateTest-1, prefix: 3, type: grim
Mission: 5
  - 8::GRIM_ASSIGNMENT
  - 9::GRIM_ASSIGNMENT
  - 10::GRIM_ASSIGNMENT
  - 11::GRIM_ASSIGNMENT
  - 12::GRIM_ASSIGNMENT
  - 13::GRIM_ASSIGNMENT
  - 14::GRIM_MISSION_DATA
  - 15::GRIM_MISSION_DATA
  - 16::GRIM_MISSION_DATA
  - 17::GRIM_MISSION_DATA
  - 18::GRIM_MISSION_LABEL
  - 19::GRIM_MISSION_LABEL
  - 20::GRIM_MISSION_LINKS
  - 21::GRIM_MISSION_LINKS
  - 22::GRIM_OBJECTIVE
  - 23::GRIM_OBJECTIVE_GOAL
  - 24::GRIM_OBJECTIVE_GOAL
  - 25::GRIM_REMARK
  - 26::GRIM_REMARK
  - 27::GRIM_REMARK
  - 28::GRIM_REMARK
  - 29::GRIM_COMMANDS
  - 30::GRIM_COMMANDS
  - 31::GRIM_COMMANDS
  - 32::GRIM_COMMANDS

commit: 4, tenant: 1
 | created
  + added new: 20 entries
  + 18::GRIM_MISSION_LABEL
    {"id":"18","commitId":"4","labelType":"keyword","labelValue":"housing","labelBody":null,"missionId":"5","relation":null}
  + 19::GRIM_MISSION_LABEL
    {"id":"19","commitId":"4","labelType":"keyword","labelValue":"roofing","labelBody":null,"missionId":"5","relation":null}
  + 13::GRIM_ASSIGNMENT
    {"id":"13","commitId":"4","missionId":"5","assignee":"sam-from-the-mill","assignmentType":"worker","relation":null}
  + 12::GRIM_ASSIGNMENT
    {"id":"12","commitId":"4","missionId":"5","assignee":"jane-from-the-roofing","assignmentType":"worker","relation":null}
  + 21::GRIM_MISSION_LINKS
    {"id":"21","commitId":"4","missionId":"5","externalId":"site.com/plans/1","linkType":"project-plans","linkBody":null,"relation":null}
  + 20::GRIM_MISSION_LINKS
    {"id":"20","commitId":"4","missionId":"5","externalId":"site.com/permits/5","linkType":"permits","linkBody":null,"relation":null}
  + 25::GRIM_REMARK
    {"id":"25","commitId":"4","missionId":"5","remarkText":"Created main task for building a house!","remarkStatus":null,"reporterId":"jane.doe","relation":null}
  + 26::GRIM_REMARK
    {"id":"26","commitId":"4","missionId":"5","remarkText":"Waiting for results already!","remarkStatus":null,"reporterId":"the.bob.clown","relation":null}
  + 10::GRIM_ASSIGNMENT
    {"id":"10","commitId":"4","missionId":"5","assignee":"no-name-worker-1","assignmentType":"objective-worker","relation":{"objectiveId":"22","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}
  + 11::GRIM_ASSIGNMENT
    {"id":"11","commitId":"4","missionId":"5","assignee":"no-name-worker-2","assignmentType":"objective-worker","relation":{"objectiveId":"22","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}
  + 8::GRIM_ASSIGNMENT
    {"id":"8","commitId":"4","missionId":"5","assignee":"no-name-worker-3","assignmentType":"goal-worker","relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"23","relationType":"GOAL"}}
  + 9::GRIM_ASSIGNMENT
    {"id":"9","commitId":"4","missionId":"5","assignee":"no-name-worker-4","assignmentType":"goal-worker","relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"23","relationType":"GOAL"}}
  + 23::GRIM_OBJECTIVE_GOAL
    {"id":"23","commitId":"4","objectiveId":"22","missionId":"5","goalStatus":null,"startDate":[2023,1,2],"dueDate":[2023,2,1]}
  + 16::GRIM_MISSION_DATA
    {"id":"16","commitId":"4","missionId":"5","title":"kitchen","description":"kitcher plan goes here!","dataExtension":null,"relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"23","relationType":"GOAL"}}
  + 24::GRIM_OBJECTIVE_GOAL
    {"id":"24","commitId":"4","objectiveId":"22","missionId":"5","goalStatus":null,"startDate":null,"dueDate":null}
  + 14::GRIM_MISSION_DATA
    {"id":"14","commitId":"4","missionId":"5","title":"bathroom","description":"kitcher plan goes here!","dataExtension":null,"relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"24","relationType":"GOAL"}}
  + 22::GRIM_OBJECTIVE
    {"id":"22","commitId":"4","missionId":"5","objectiveStatus":null,"startDate":[2023,1,1],"dueDate":[2024,1,1]}
  + 15::GRIM_MISSION_DATA
    {"id":"15","commitId":"4","missionId":"5","title":"interior design ideas","description":"all ideas are welcome how we should design kitchen and bathroom!","dataExtension":null,"relation":{"objectiveId":"22","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}
  + 5::GRIM_MISSION
    {"id":"5","commitId":"4","parentMissionId":null,"externalId":null,"missionStatus":"OPEN","missionPriority":"HIGH","reporterId":"jane.doe@housing.com","startDate":[2020,5,1],"dueDate":[2020,6,1],"archivedDate":null,"archivedStatus":null}
  + 17::GRIM_MISSION_DATA
    {"id":"17","commitId":"4","missionId":"5","title":"The best house ever","description":"","dataExtension":null,"relation":null}

 | deleted
  - deleted: 0 entries

 | merged
  +- merged: 0 entries


commit: 6, tenant: 1
 | created
  + added new: 2 entries
  + 27::GRIM_REMARK
    {"id":"27","commitId":"6","missionId":"5","remarkText":"Not to self, give feedback to architects","remarkStatus":null,"reporterId":"jane.doe@morgue.com","relation":null}
  + 28::GRIM_REMARK
    {"id":"28","commitId":"6","missionId":"5","remarkText":"Note to self, compliment works on after job well done!","remarkStatus":null,"reporterId":"jane.doe@morgue.com","relation":null}

 | deleted
  - deleted: 0 entries

 | merged
  +- merged: 0 entries


commit: 7, tenant: 1
 | created
  + added new: 0 entries

 | deleted
  - deleted: 0 entries

 | merged
  +- merged: 2 entries
  +- 17::GRIM_MISSION_DATA
   -  {"id":"17","commitId":"4","missionId":"5","title":"The best house ever","description":"","dataExtension":null,"relation":null}
   +  {"id":"17","commitId":"7","missionId":"5","title":"House plans for customer #198CC","description":"Basic house plans for customer","dataExtension":null,"relation":null}
  +- 5::GRIM_MISSION
   -  {"id":"5","commitId":"4","parentMissionId":null,"externalId":null,"missionStatus":"OPEN","missionPriority":"HIGH","reporterId":"jane.doe@housing.com","startDate":[2020,5,1],"dueDate":[2020,6,1],"archivedDate":null,"archivedStatus":null}
   +  {"id":"5","commitId":"7","parentMissionId":null,"externalId":null,"missionStatus":"OPEN","missionPriority":"HIGH","reporterId":"jane.doe@morgue.com","startDate":null,"dueDate":null,"archivedDate":null,"archivedStatus":null}

""", toStaticData(repo.getRepo()));
  }
}
