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

import io.resys.thena.api.actions.TenantActions.TenantOperationStatus;
import io.resys.thena.api.actions.TenantActions.CreatedTenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewMission;
import io.resys.thena.grim.test.config.DbTestTemplate;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class CreateAndDeleteMissionTest extends DbTestTemplate {

  private static String tenantId = "CreateAndDeleteMissionTest";  
  
  @Value.Immutable
  public interface TestContent extends Serializable {
    String getId();
    String getName();
  }

  @Test
  public void deleteMissions() {
    createTestData();
    
    final var mission = getClient().grim(tenantId).find().missionQuery()
      .findAll()
      .await().atMost(atMost);
    
    Assertions.assertEquals(1, mission.getObjects().size());
    
    
    getClient().grim(tenantId).find().missionDeleteQuery()
      .commitAuthor("mis. delete")
      .commitMessage("lets wipe the DB >_<")
      .missionId(mission.getObjects().stream().map(e -> e.getMission().getId()).toList())
      .deleteAll()
      .collect().asList()
      .await().atMost(atMost);
      
    
    // data wiped
    Assertions.assertEquals(
"""

Repo
  - id: 1, rev: 2
    name: CreateAndDeleteMissionTest, prefix: 3, type: grim
""", toStaticData(getClient().tenants().queryTenants().id(tenantId).getOne().await().atMost(atMost)));
    
    
    // check for audit commits
    final var commits = getClient().grim(tenantId).find().commitQuery().findAllCommits().await().atMost(atMost);
    
    Assertions.assertEquals(1, commits.getObjects().getCommits().size());
    Assertions.assertEquals(1, commits.getObjects().getCommitTrees().size());
  }
  
  
  public void createTestData() {
    // create project
    CreatedTenant repo = getClient().tenants().createOneTenant()
        .name(tenantId, StructureType.grim)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(TenantOperationStatus.OK, repo.getStatus());
    
    
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
  }
}
