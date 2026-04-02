package io.digiexpress.eveli.client.test;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import io.digiexpress.eveli.client.api.ImmutableCreateTaskCommand;
import io.digiexpress.eveli.client.api.ImmutableModifyTaskCommand;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.TaskDiff;
import io.digiexpress.eveli.client.api.TaskClient.TaskDiffValue;
import io.digiexpress.eveli.client.api.TaskClient.TaskStatus;
import io.digiexpress.eveli.client.test.task.TaskEnvirSetup;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesClient.HdesTypesMapper;
import jakarta.json.JsonPatch;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class CreateTaskAssignmentTest extends TaskEnvirSetup {
  @Container @ServiceConnection static PostgreSQLContainer<?> CONTAINER = new PostgreSQLContainer<>("postgres:17");
  @BeforeAll static void beforeAll() { start(CONTAINER); }
  @AfterAll static void afterAll() { end(); }
  @Autowired TaskClient taskClient;
  @Autowired HdesClient hdesClient;
  @Autowired HdesTypesMapper hdesTypesMapper;
  
  private Duration atMost = Duration.ofMinutes(5);


  @Test
  public void createTask() {
    final var user = BaseEnvir.FAKER.starTrek().character();
    final var email = user+"@resys.io";
    
    final var task = taskClient.taskBuilder()
      .userId(user, email)
      .createTask(ImmutableCreateTaskCommand.builder()
      .subject(BaseEnvir.FAKER.book().title())
      .build())
      .await().atMost(atMost);


    taskClient.taskBuilder()
      .userId(user, email)
      .modifyTask(task.getId(), ImmutableModifyTaskCommand.builder()
          .status(TaskStatus.OPEN)
          .assignedRoles(Arrays.asList("role-1", "role-2"))
          .assignedUser("bob")
          .assignedUserEmail("bob@bob")
          .subject(task.getSubject())
          .build())
      .await().atMost(atMost);

    taskClient.taskBuilder()
        .userId(user, email)
        .modifyTask(task.getId(), ImmutableModifyTaskCommand.builder()
            .assignedRoles(Arrays.asList("role-1", "role-2"))
            .assignedUser("sam")
            .assignedUserEmail("sam@sam")
            .subject(task.getSubject())
            .build())
        .await().atMost(atMost);

    taskClient.taskBuilder()
        .userId(user, email)
        .modifyTask(task.getId(), ImmutableModifyTaskCommand.builder()
            .assignedRoles(Arrays.asList("role-1", "role-2"))
            .assignedUser("mike")
            .assignedUserEmail("mike@mike")
            .subject(task.getSubject())
            .build())
        .await().atMost(atMost);
    
    final var assignee_change3 = taskClient.taskBuilder()
        .userId(user, email)
        .modifyTask(task.getId(), ImmutableModifyTaskCommand.builder()
            .assignedRoles(Arrays.asList("role-1"))
            .assignedUser("mike")
            .assignedUserEmail("mike@mike")
            .subject(task.getSubject())
            .build())
        .await().atMost(atMost);
    

    final var diff_version = taskClient.queryTasks()
        .getOneTaskDiff(task.getId(), assignee_change3.getVersion())
        .await().atMost(atMost);
    
    log.info("Diff version: {}", diff_version);
    Assert.assertEquals(6, diff_version.getValues().size());
    List<TaskDiffValue> assigned_diffs = filterTechnicalProperties(diff_version);
    Assert.assertEquals(2, assigned_diffs.size());
    Assert.assertEquals(JsonPatch.Operation.REMOVE, assigned_diffs.get(0).getOp());
    // index of removed role is not determined, can't assert it by index
    Assert.assertTrue(assigned_diffs.get(0).getPath().startsWith("/assignedRoles/"));
    
    
    final var assignee_change4 = taskClient.taskBuilder()
        .userId(user, email)
        .modifyTask(task.getId(), ImmutableModifyTaskCommand.builder()
            .assignedRoles(Arrays.asList("role-1"))
            .assignedUser("sam")
            .assignedUserEmail("sam@sam")
            .subject(task.getSubject())
            .build())
        .await().atMost(atMost);
    
    final var diff_version2 = taskClient.queryTasks()
        .getOneTaskDiff(task.getId(), assignee_change4.getVersion())
        .await().atMost(atMost);
    
    log.info("Diff version 2: {}",diff_version2);
    Assert.assertEquals(8, diff_version2.getValues().size());
    List<TaskDiffValue> assigned_diffs2 = filterTechnicalProperties(diff_version2);
    Assert.assertEquals(4, assigned_diffs2.size());
    Assert.assertEquals(JsonPatch.Operation.REPLACE, assigned_diffs2.get(0).getOp());
    Assert.assertEquals("/assignedUser", assigned_diffs2.get(0).getPath());
    Assert.assertEquals("sam", assigned_diffs2.get(0).getValue());
  }
  
  private List<TaskDiffValue> filterTechnicalProperties(final TaskDiff diff_version) {
    return diff_version.getValues().stream()
    .filter(v->!(v.getPath().startsWith("/updated") || v.getPath().startsWith("/version")))
    .toList();
  }
}
