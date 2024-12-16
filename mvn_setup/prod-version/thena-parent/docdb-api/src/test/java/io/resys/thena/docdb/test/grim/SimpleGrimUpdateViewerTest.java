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
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.docdb.test.config.DbTestTemplate;
import io.resys.thena.docdb.test.config.PgProfile;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;


@QuarkusTest
@TestProfile(PgProfile.class)
@Slf4j
public class SimpleGrimUpdateViewerTest extends DbTestTemplate {
  
  private GrimMissionContainer createMission(TenantCommitResult repo) {
    final var mission = getClient().grim(repo).commit()
        .createOneMission()
        .commitMessage("batching tests")
        .commitAuthor("jane.doe@morgue.com")
        .mission((NewMission newMission) -> {
          newMission
            .title("my first mission to build a house")
            .description("The best house ever")
            .status("OPEN")
            .priority("HIGH")
            .startDate(LocalDate.of(2020, 05, 01))
            .dueDate(LocalDate.of(2020, 06, 01))
            .reporterId("jane.doe@housing.com")
            .addCommands(Arrays.asList(JsonObject.of("commandType", "CREATE_TASK")))
            .build();
         
        }).build()
        .onItem().transformToUni(resp -> {
          final var missionId = resp.getMission().getId();
          return getClient().grim(repo).find().missionQuery().get(missionId);
        })
        .onItem().transform(resp -> resp.getObjects())
        .await().atMost(Duration.ofMinutes(1)).getMission();
    final var missionId = mission.getId();
    
    
    getClient().grim(repo).commit().modifyOneMission()
    .commitMessage("creating commits for viewer")
    .commitAuthor("jane.doe@morgue.com")
    .missionId(missionId)
    .modifyMission(mergeMission -> mergeMission.addLabels(newLabel -> newLabel.labelType("keyword").labelValue("housing").build()).build())
    .build()
    .await().atMost(Duration.ofMinutes(1)).getMission();
    
    getClient().grim(repo).commit().modifyOneMission()
    .commitMessage("creating commits for viewer")
    .commitAuthor("jane.doe@morgue.com")
    .missionId(missionId)
    .modifyMission(mergeMission -> mergeMission.addLabels(newLabel -> newLabel.labelType("keyword").labelValue("roofing").build()).build())
    .build()
    .await().atMost(Duration.ofMinutes(1)).getMission();
    
    getClient().grim(repo).commit().modifyOneMission()
    .commitMessage("creating commits for viewer")
    .commitAuthor("jane.doe@morgue.com")
    .missionId(missionId)
    .modifyMission(mergeMission -> mergeMission.addAssignees(newAssignee -> newAssignee.assignmentType("worker").assignee("sam-from-the-mill").build()).build())
    .build()
    .await().atMost(Duration.ofMinutes(1)).getMission();
    
    getClient().grim(repo).commit().modifyOneMission()
    .commitMessage("creating commits for viewer")
    .commitAuthor("jane.doe@morgue.com")
    .missionId(missionId)
    .modifyMission(mergeMission -> mergeMission.addAssignees(newAssignee -> newAssignee.assignmentType("worker").assignee("jane-from-the-roofing").build()).build())
    .build()
    .await().atMost(Duration.ofMinutes(1)).getMission();
    
    
    getClient().grim(repo).commit().modifyOneMission()
    .commitMessage("creating commits for viewer")
    .commitAuthor("jane.doe@morgue.com")
    .missionId(missionId)
    .modifyMission(mergeMission -> mergeMission.addLink(newLink -> newLink.linkType("project-plans").linkValue("site.com/plans/1").build()).build())
    .build()
    .await().atMost(Duration.ofMinutes(1)).getMission();
    

    getClient().grim(repo).commit().modifyOneMission()
    .commitMessage("creating commits for viewer")
    .commitAuthor("jane.doe@morgue.com")
    .missionId(missionId)
    .modifyMission(mergeMission -> mergeMission.addLink(newLink -> newLink.linkType("permits").linkValue("site.com/permits/5").build()).build())
    .build()
    .await().atMost(Duration.ofMinutes(1)).getMission();
    
    
    getClient().grim(repo).commit().modifyOneMission()
    .commitMessage("creating commits for viewer")
    .commitAuthor("jane.doe@morgue.com")
    .missionId(missionId)
    .modifyMission(mergeMission -> mergeMission
      .addRemark(newRemark -> newRemark.remarkText("Created main task for building a house!").reporterId("jane.doe").build())
      .addRemark(newRemark -> newRemark.remarkText("Waiting for results already!").reporterId("the.bob.clown").build())
      .build())
    
    .build()
    .await().atMost(Duration.ofMinutes(1)).getMission();
    
    
    getClient().grim(repo).commit().modifyOneMission()
    .commitMessage("creating commits for viewer")
    .commitAuthor("jane.doe@morgue.com")
    .missionId(missionId)
    .modifyMission(mergeMission -> mergeMission.addObjective(newObjective -> newObjective
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
        
        .build()).build()
        
    ).build()
    .await().atMost(Duration.ofMinutes(1)).getMission();
   
    
    return getClient().grim(repo).find().missionQuery().get(missionId).await().atMost(Duration.ofMinutes(1)).getObjects();
  }

  @Test
  public void createAndUpdateMission() {
    // create project
    TenantCommitResult repo = getClient().tenants().commit()                      
        .name("SimpleGrimUpdateViewerTest-1", StructureType.grim)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(CommitStatus.OK, repo.getStatus());
    
    final var newMission = createMission(repo);
    
    // Mark mission viewed
    {
    getClient().grim(repo).commit().modifyManyCommitViewer()
      .commitAuthor("john.doe@morgue.com")
      .commitMessage("viewed")
      .usedFor("tracing")
      .object(newMission.getMission().getCommitId(), GrimDocType.GRIM_MISSION, newMission.getMission().getId())
      .build()
      .await().atMost(Duration.ofMinutes(1));
    
    final var missionWithViewedData = getClient().grim(repo).find().missionQuery()
        .viewer("john.doe@morgue.com", "tracing")
        .get(newMission.getMission().getId())
        .await().atMost(Duration.ofMinutes(1));
    
    Assertions.assertEquals(1, missionWithViewedData.getObjects().getViews().size());
    }
    
    { // try to duplicate
    getClient().grim(repo).find().missionQuery()
        .viewer("john.doe@morgue.com", "tracing")
        .get(newMission.getMission().getId())
        .await().atMost(Duration.ofMinutes(1));
    final var missionWithViewedData = getClient().grim(repo).find().missionQuery()
        .viewer("john.doe@morgue.com", "tracing")
        .get(newMission.getMission().getId())
        .await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals(1, missionWithViewedData.getObjects().getViews().size());
    
    }
  }
}
