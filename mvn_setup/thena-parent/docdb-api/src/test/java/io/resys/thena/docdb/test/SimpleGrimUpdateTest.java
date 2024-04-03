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
        .name("SimpleGrimTest-1", StructureType.grim)
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
    name: SimpleGrimTest-1, prefix: SIMPLEGRIM10_, type: grim
Mission: 4
  - 7::GRIM_ASSIGNMENT
  - 8::GRIM_ASSIGNMENT
  - 9::GRIM_ASSIGNMENT
  - 10::GRIM_ASSIGNMENT
  - 11::GRIM_ASSIGNMENT
  - 12::GRIM_ASSIGNMENT
  - 13::GRIM_MISSION_DATA
  - 14::GRIM_MISSION_DATA
  - 15::GRIM_MISSION_DATA
  - 16::GRIM_MISSION_DATA
  - 17::GRIM_MISSION_LABEL
  - 18::GRIM_MISSION_LABEL
  - 19::GRIM_MISSION_LINKS
  - 20::GRIM_MISSION_LINKS
  - 21::GRIM_OBJECTIVE
  - 22::GRIM_OBJECTIVE_GOAL
  - 23::GRIM_OBJECTIVE_GOAL
  - 24::GRIM_REMARK
  - 25::GRIM_REMARK
  - 26::GRIM_REMARK
  - 27::GRIM_REMARK
  - 28::GRIM_COMMANDS
  - 29::GRIM_COMMANDS
  - 30::GRIM_COMMANDS
  - 31::GRIM_COMMANDS

commit: 3, tenant: 1
 | created
  + added new: 0 entries
  + 17::GRIM_MISSION_LABEL
    {"id":"17","commitId":"3","labelType":"keyword","labelValue":"housing","labelBody":null,"missionId":"4","relation":null}
  + 18::GRIM_MISSION_LABEL
    {"id":"18","commitId":"3","labelType":"keyword","labelValue":"roofing","labelBody":null,"missionId":"4","relation":null}
  + 12::GRIM_ASSIGNMENT
    {"id":"12","commitId":"3","missionId":"4","assignee":"sam-from-the-mill","assignmentType":"worker","relation":null}
  + 11::GRIM_ASSIGNMENT
    {"id":"11","commitId":"3","missionId":"4","assignee":"jane-from-the-roofing","assignmentType":"worker","relation":null}
  + 20::GRIM_MISSION_LINKS
    {"id":"20","commitId":"3","missionId":"4","externalId":"site.com/plans/1","linkType":"project-plans","linkBody":null,"relation":null}
  + 19::GRIM_MISSION_LINKS
    {"id":"19","commitId":"3","missionId":"4","externalId":"site.com/permits/5","linkType":"permits","linkBody":null,"relation":null}
  + 24::GRIM_REMARK
    {"id":"24","commitId":"3","missionId":"4","remarkText":"Created main task for building a house!","remarkStatus":null,"reporterId":"jane.doe","relation":null}
  + 25::GRIM_REMARK
    {"id":"25","commitId":"3","missionId":"4","remarkText":"Waiting for results already!","remarkStatus":null,"reporterId":"the.bob.clown","relation":null}
  + 9::GRIM_ASSIGNMENT
    {"id":"9","commitId":"3","missionId":"4","assignee":"no-name-worker-1","assignmentType":"objective-worker","relation":{"objectiveId":"21","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}
  + 10::GRIM_ASSIGNMENT
    {"id":"10","commitId":"3","missionId":"4","assignee":"no-name-worker-2","assignmentType":"objective-worker","relation":{"objectiveId":"21","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}
  + 7::GRIM_ASSIGNMENT
    {"id":"7","commitId":"3","missionId":"4","assignee":"no-name-worker-3","assignmentType":"goal-worker","relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"22","relationType":"GOAL"}}
  + 8::GRIM_ASSIGNMENT
    {"id":"8","commitId":"3","missionId":"4","assignee":"no-name-worker-4","assignmentType":"goal-worker","relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"22","relationType":"GOAL"}}
  + 22::GRIM_OBJECTIVE_GOAL
    {"id":"22","commitId":"3","objectiveId":"21","missionId":"4","goalStatus":null,"startDate":[2023,1,2],"dueDate":[2023,2,1]}
  + 15::GRIM_MISSION_DATA
    {"id":"15","commitId":"3","missionId":"4","title":"kitchen","description":"kitcher plan goes here!","dataExtension":null,"relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"22","relationType":"GOAL"}}
  + 23::GRIM_OBJECTIVE_GOAL
    {"id":"23","commitId":"3","objectiveId":"21","missionId":"4","goalStatus":null,"startDate":null,"dueDate":null}
  + 13::GRIM_MISSION_DATA
    {"id":"13","commitId":"3","missionId":"4","title":"bathroom","description":"kitcher plan goes here!","dataExtension":null,"relation":{"objectiveId":null,"remarkId":null,"objectiveGoalId":"23","relationType":"GOAL"}}
  + 21::GRIM_OBJECTIVE
    {"id":"21","commitId":"3","missionId":"4","objectiveStatus":null,"startDate":[2023,1,1],"dueDate":[2024,1,1]}
  + 14::GRIM_MISSION_DATA
    {"id":"14","commitId":"3","missionId":"4","title":"interior design ideas","description":"all ideas are welcome how we should design kitchen and bathroom!","dataExtension":null,"relation":{"objectiveId":"21","remarkId":null,"objectiveGoalId":null,"relationType":"OBJECTIVE"}}
  + 4::GRIM_MISSION
    {"id":"4","commitId":"3","parentMissionId":null,"externalId":null,"missionStatus":"OPEN","missionPriority":"HIGH","reporterId":"jane.doe@housing.com","startDate":[2020,5,1],"dueDate":[2020,6,1],"archivedDate":null,"archivedStatus":null}
  + 16::GRIM_MISSION_DATA
    {"id":"16","commitId":"3","missionId":"4","title":"The best house ever","description":"","dataExtension":null,"relation":null}

 | deleted
  - deleted: 0 entries

 | merged
  +- merged: 0 entries


commit: 5, tenant: 1
 | created
  + added new: 0 entries
  + 26::GRIM_REMARK
    {"id":"26","commitId":"5","missionId":"4","remarkText":"Not to self, give feedback to architects","remarkStatus":null,"reporterId":"jane.doe@morgue.com","relation":null}
  + 27::GRIM_REMARK
    {"id":"27","commitId":"5","missionId":"4","remarkText":"Note to self, compliment works on after job well done!","remarkStatus":null,"reporterId":"jane.doe@morgue.com","relation":null}

 | deleted
  - deleted: 0 entries

 | merged
  +- merged: 0 entries


commit: 6, tenant: 1
 | created
  + added new: 0 entries

 | deleted
  - deleted: 0 entries

 | merged
  +- merged: 0 entries
  +- 16::GRIM_MISSION_DATA
   -  {"id":"16","commitId":"3","missionId":"4","title":"The best house ever","description":"","dataExtension":null,"relation":null}
   +  {"id":"16","commitId":"6","missionId":"4","title":"House plans for customer #198CC","description":"Basic house plans for customer","dataExtension":null,"relation":null}
  +- 4::GRIM_MISSION
   -  {"id":"4","commitId":"3","parentMissionId":null,"externalId":null,"missionStatus":"OPEN","missionPriority":"HIGH","reporterId":"jane.doe@housing.com","startDate":[2020,5,1],"dueDate":[2020,6,1],"archivedDate":null,"archivedStatus":null}
   +  {"id":"4","commitId":"6","parentMissionId":null,"externalId":null,"missionStatus":"OPEN","missionPriority":"HIGH","reporterId":"jane.doe@morgue.com","startDate":null,"dueDate":null,"archivedDate":null,"archivedStatus":null}

""", toStaticData(repo.getRepo()));
  }
}
