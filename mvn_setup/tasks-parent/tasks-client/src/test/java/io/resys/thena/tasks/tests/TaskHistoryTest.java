package io.resys.thena.tasks.tests;

import org.json.JSONException;

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


import com.fasterxml.jackson.core.JsonProcessingException;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.tasks.client.api.model.ImmutableAssignTaskReporter;
import io.resys.thena.tasks.client.api.model.ImmutableChangeTaskPriority;
import io.resys.thena.tasks.client.api.model.ImmutableChangeTaskStatus;
import io.resys.thena.tasks.client.api.model.ImmutableCreateTask;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.tests.config.TaskPgProfile;
import io.resys.thena.tasks.tests.config.TaskTestCase;

@QuarkusTest
@TestProfile(TaskPgProfile.class)
public class TaskHistoryTest extends TaskTestCase {

  private static final String repoName = TaskHistoryTest.class.getSimpleName();


  @SuppressWarnings("unused")
  @org.junit.jupiter.api.Test
  public void createTaskAndUpdateAndGetHistory() throws JsonProcessingException, JSONException {
    final var client = getClient().repo().query().repoName(repoName).createIfNot().await().atMost(atMost);

    Task createdTask_1 = client.tasks().createTask().createOne(ImmutableCreateTask.builder()
            .targetDate(getTargetDate())
            .title("very important title no: init")
            .description("first task ever no: init")
            .priority(Task.Priority.LOW)
            .addRoles("admin-users", "view-only-users")
            .userId("user-1")
            .reporterId("reporter-1")
            .build())
        .await().atMost(atMost);

    Task createdTask_2 = client.tasks().createTask().createOne(ImmutableCreateTask.builder()
            .targetDate(getTargetDate())
            .title("very important title no: init")
            .description("second task ever no: init")
            .priority(Task.Priority.LOW)
            .addRoles("admin-users", "view-only-users")
            .userId("user-1")
            .reporterId("reporter-1")
            .build())
        .await().atMost(atMost);


    client.tasks().updateTask().updateOne(ImmutableChangeTaskStatus.builder()
            .userId("tester-bob")
            .taskId(createdTask_1.getId())
            .targetDate(
              getTargetDate()
                .plus(1, java.time.temporal.ChronoUnit.DAYS)
                .plus(1, java.time.temporal.ChronoUnit.HOURS)
            )
            .status(Task.Status.IN_PROGRESS)
            .build())
        .await().atMost(atMost);

    client.tasks().updateTask().updateOne(ImmutableChangeTaskPriority.builder()
            .userId("tester-bob")
            .taskId(createdTask_1.getId())
            .targetDate(
              getTargetDate()
                .plus(1, java.time.temporal.ChronoUnit.DAYS)
                .plus(1, java.time.temporal.ChronoUnit.HOURS)
            )
            .priority(Task.Priority.HIGH)
            .build())
        .await().atMost(atMost);

    client.tasks().updateTask().updateOne(ImmutableAssignTaskReporter.builder()
            .userId("tester-bob")
            .taskId(createdTask_1.getId())
            .targetDate(
              getTargetDate()
                .plus(1, java.time.temporal.ChronoUnit.DAYS)
                .plus(1, java.time.temporal.ChronoUnit.HOURS)
            )
            .reporterId("citizen jane")
            .build())
        .await().atMost(atMost);
  }

}
