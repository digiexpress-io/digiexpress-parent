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
import io.resys.thena.api.actions.GrimQueryActions.MissionOrderByType;
import io.resys.thena.api.actions.TenantActions.CommitStatus;
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import io.resys.thena.api.entities.ImmutablePageQuery;
import io.resys.thena.api.entities.ImmutablePageSorting;
import io.resys.thena.api.entities.ImmutablePageSortingOrder;
import io.resys.thena.api.entities.PageQuery.PageSortDirection;
import io.resys.thena.api.entities.PageQuery.PageSorting;
import io.resys.thena.api.entities.PageQuery.PageSortingOrder;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewMission;
import io.resys.thena.docdb.test.config.DbTestTemplate;
import io.resys.thena.docdb.test.config.PgProfile;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;


@QuarkusTest
@TestProfile(PgProfile.class)
@Slf4j
public class PaginateMissionsTest extends DbTestTemplate {

  @Value.Immutable
  public interface TestContent extends Serializable {
    String getId();
    String getName();
  }

  @Test
  public void createAndUpdateMission() {
    // create project
    TenantCommitResult repo = getClient().tenants().commit()
        .name("SearchMissions-1", StructureType.grim)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(CommitStatus.OK, repo.getStatus());
   
    createTestData(repo);
    
    
    // 100 results per page
    {
      final var pagination =getClient().grim(repo).find().missionQuery().paginate(ImmutablePageQuery.<MissionOrderByType>builder()
          .offset(0)
          .pageSize(100)
          .pageNumber(0)
          .sort(sort(
              sortBy(PageSortDirection.ASC, MissionOrderByType.MISSION_ID)
          ))
          .build())
      .await().atMost(Duration.ofMinutes(5));
      Assertions.assertEquals(30, pagination.getTotalObjectsOnPages());
      Assertions.assertEquals(30, pagination.getCurrentPageObjects().size());
      Assertions.assertEquals(1, pagination.getTotalPages());
      Assertions.assertEquals(0, pagination.getCurrentPageNumber());
    }
    
    // 30 results per page
    {
      final var pagination =getClient().grim(repo).find().missionQuery().paginate(ImmutablePageQuery.<MissionOrderByType>builder()
          .offset(0)
          .pageSize(30)
          .pageNumber(0)
          .sort(sort(
              sortBy(PageSortDirection.ASC, MissionOrderByType.MISSION_ID)
          ))
          .build())
      .await().atMost(Duration.ofMinutes(5));
      Assertions.assertEquals(30, pagination.getTotalObjectsOnPages());
      Assertions.assertEquals(30, pagination.getCurrentPageObjects().size());
      Assertions.assertEquals(1, pagination.getTotalPages());
      Assertions.assertEquals(0, pagination.getCurrentPageNumber());
    }
    
    
    // 15 results per page - on page 1
    {
      final var pagination =getClient().grim(repo).find().missionQuery().paginate(ImmutablePageQuery.<MissionOrderByType>builder()
          .offset(0)
          .pageSize(15)
          .pageNumber(0)
          .sort(sort(
              sortBy(PageSortDirection.ASC, MissionOrderByType.MISSION_ID)
          ))
          .build())
      .await().atMost(Duration.ofMinutes(5));
      Assertions.assertEquals(30, pagination.getTotalObjectsOnPages());
      Assertions.assertEquals(15, pagination.getCurrentPageObjects().size());
      Assertions.assertEquals(2, pagination.getTotalPages());
      Assertions.assertEquals(0, pagination.getCurrentPageNumber());
    }
    
    
    // 15 results per page - on page 2
    {
      final var pagination =getClient().grim(repo).find().missionQuery().paginate(ImmutablePageQuery.<MissionOrderByType>builder()
          .offset(15)
          .pageSize(15)
          .pageNumber(0)
          .sort(sort(
              sortBy(PageSortDirection.ASC, MissionOrderByType.MISSION_ID)
          ))
          .build())
      .await().atMost(Duration.ofMinutes(5));
      Assertions.assertEquals(30, pagination.getTotalObjectsOnPages());
      Assertions.assertEquals(15, pagination.getCurrentPageObjects().size());
      Assertions.assertEquals(2, pagination.getTotalPages());
      Assertions.assertEquals(1, pagination.getCurrentPageNumber());
    }
  }
  
  @SafeVarargs
  public final PageSorting<MissionOrderByType> sort(PageSortingOrder<MissionOrderByType>...types) {
    return ImmutablePageSorting.<MissionOrderByType>builder()
        .orders(Arrays.asList(types))
        .build();
  }
  
  public ImmutablePageSortingOrder<MissionOrderByType> sortBy(PageSortDirection direction, MissionOrderByType type) {
    return ImmutablePageSortingOrder.<MissionOrderByType>builder()
        .direction(direction)
        .property(type)
        .build();
  }
  
  private void createTestData(TenantCommitResult repo) {
    
    final var builder = getClient().grim(repo).commit()
        .createManyMissions()
        .commitMessage("batching tests")
        .commitAuthor("jane.doe@morgue.com");
    
    for(int index = 0; index < 30; index++) {
        builder.addMission((NewMission newMission) -> {
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

        });
    }
    builder.build().await().atMost(Duration.ofMinutes(1));

  }
}
