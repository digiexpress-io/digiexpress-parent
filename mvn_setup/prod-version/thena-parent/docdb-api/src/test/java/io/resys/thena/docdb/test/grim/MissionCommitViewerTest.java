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
public class MissionCommitViewerTest extends DbTestTemplate {

  @Value.Immutable
  public interface TestContent extends Serializable {
    String getId();
    String getName();
  }

  @Test
  public void queryByViewer() {
    // create project
    TenantCommitResult repo = getClient().tenants().commit()
        .name("SearchMissions-1", StructureType.grim)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(CommitStatus.OK, repo.getStatus());
   
    final var missionId = createTestData(repo);

    // find tasks that nobody IN CUSTOMER APP has seen
    Assertions.assertEquals(0, getClient().grim(repo).find().missionQuery()
        .notViewed("CUSTOMER")
        .findAll()
        .await().atMost(Duration.ofMinutes(1))
        .getObjects().size());
    
    
    // find tasks that nobody IN WORKER APP has seen
    Assertions.assertEquals(1, getClient().grim(repo).find().missionQuery()
        .notViewed("WORKER")
        .findAll()
        .await().atMost(Duration.ofMinutes(1))
        .getObjects().size());
    
    
    
    // mark task viewed by user X
    getClient().grim(repo).commit().modifyOneMission()
    .commitMessage("creating commits for viewer")
    .commitAuthor("jane.doe@morgue.com")
    .missionId(missionId)
    .modifyMission(mergeMission -> mergeMission
      .addViewer(v -> v.usedFor("WORKER").userId("X").build())
      .addRemark(newRemark -> newRemark.remarkType("external").remarkText("Created main task for building a house!").reporterId("jane.doe").build())
      .addRemark(newRemark -> newRemark.remarkText("Waiting for results already!").reporterId("the.bob.clown").build())
      .build())
    .build()
    .await().atMost(Duration.ofMinutes(1)).getMission();
    
    // worker X has seen this
    Assertions.assertEquals(0, getClient().grim(repo).find().missionQuery()
        .notViewed("WORKER")
        .findAll()
        .await().atMost(Duration.ofMinutes(1))
        .getObjects().size());
    
    
    //find tasks that only given user has not seen
    Assertions.assertEquals(1, getClient().grim(repo).find().missionQuery()
        .notViewed("WORKER", "worker-Y")
        .findAll()
        .await().atMost(Duration.ofMinutes(1))
        .getObjects().size());
    
    //find tasks that have external comments and given user has not seen
    Assertions.assertEquals(0, getClient().grim(repo).find().missionQuery()
        .notViewed("WORKER", "worker-Y")
        .atLeastOneRemarkWithType("no-remarks-of-this-type")
        .findAll()
        .await().atMost(Duration.ofMinutes(1))
        .getObjects().size());
    
    Assertions.assertEquals(1, getClient().grim(repo).find().missionQuery()
        .notViewed("WORKER", "worker-Y")
        .atLeastOneRemarkWithType("external")
        .findAll()
        .await().atMost(Duration.ofMinutes(1))
        .getObjects().size());
    
  }
  

  private String createTestData(TenantCommitResult repo) {
    
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
            .addViewer(v -> v.userId("jane.doe@housing.com").usedFor("CUSTOMER").build())
            
            .addLabels(newLabel -> newLabel.labelType("keyword").labelValue("housing").build())
            .addLabels(newLabel -> newLabel.labelType("keyword").labelValue("roofing").build())

            .addAssignees(newAssignee -> newAssignee.assignmentType("role").assignee("admin").build())
            .addAssignees(newAssignee -> newAssignee.assignmentType("role").assignee("tenant-admin").build())
            
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
    
    return createdTask.getMissions().iterator().next().getId();
  }
}
