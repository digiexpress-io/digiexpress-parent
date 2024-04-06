package io.resys.thena.tasks.tests;

import java.time.LocalDate;
import java.time.ZoneId;

/*-
 * #%L
 * thena-tasks-client
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import org.json.JSONException;
import org.junit.jupiter.api.Assertions;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.tasks.client.api.model.ImmutableCreateTask;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.Task.Priority;
import io.resys.thena.tasks.tests.config.TaskPgProfile;
import io.resys.thena.tasks.tests.config.TaskTestCase;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuarkusTest
@TestProfile(TaskPgProfile.class)
public class TaskArchiveTest extends TaskTestCase {
  
  @org.junit.jupiter.api.Test
  public void createTaskAndUpdate() throws JsonProcessingException, JSONException {
    final var repoName = TaskArchiveTest.class.getSimpleName() + "CreateTaskAndUpdate";
    final var client = getClient().repo().query().repoName(repoName).createIfNot().await().atMost(atMost);
    
    // first commit
    @SuppressWarnings("unused")
    Task createdTask_1 = client.tasks().createTask().createOne(ImmutableCreateTask.builder()
        .targetDate(getTargetDate())
        .title("very important title no: init")
        .description("first task ever no: init")
        .priority(Priority.LOW)
        .addRoles("admin-users", "view-only-users")
        .userId("user-1")
        .reporterId("reporter-1")
        .build())
    .await().atMost(atMost);

    @SuppressWarnings("unused")
    Task createdTask_2 = client.tasks().createTask().createOne(ImmutableCreateTask.builder()
        .targetDate(getTargetDate())
        .title("very important title no: init")
        .description("second task ever no: init")
        .priority(Priority.LOW)
        .addRoles("admin-users", "view-only-users")
        .userId("user-1")
        .reporterId("reporter-1")
        .build())
    .await().atMost(atMost);
    
    
    final var deletedTasks = client.tasks().queryActiveTasks().deleteAll("sam vimes", getTargetDate()).await().atMost(atMost);
    Assertions.assertEquals(2, deletedTasks.size());
    
    final var activeTasks = client.tasks().queryActiveTasks().findAll().await().atMost(atMost);
    Assertions.assertEquals(0, activeTasks.size());
    
    @SuppressWarnings("unused")
    Task createdTask_3 = client.tasks().createTask().createOne(ImmutableCreateTask.builder()
        .targetDate(getTargetDate())
        .title("very important title no: init")
        .description("second task ever no: init")
        .priority(Priority.LOW)
        .addRoles("admin-users", "view-only-users")
        .userId("user-1")
        .reporterId("reporter-3")
        .build())
    .await().atMost(atMost);
    

    final var archivedTasks = client.tasks().queryArchivedTasks().findAll(LocalDate.ofInstant(getTargetDate(), ZoneId.of("UTC"))).await().atMost(atMost);
    Assertions.assertEquals(2, archivedTasks.size());
  }
}
