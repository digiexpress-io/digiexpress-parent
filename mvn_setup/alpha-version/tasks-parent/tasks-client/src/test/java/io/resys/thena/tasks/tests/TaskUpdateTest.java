package io.resys.thena.tasks.tests;

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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.tasks.client.api.TaskClient;
import io.resys.thena.tasks.client.api.model.ImmutableArchiveTask;
import io.resys.thena.tasks.client.api.model.ImmutableAssignTask;
import io.resys.thena.tasks.client.api.model.ImmutableAssignTaskParent;
import io.resys.thena.tasks.client.api.model.ImmutableAssignTaskReporter;
import io.resys.thena.tasks.client.api.model.ImmutableAssignTaskRoles;
import io.resys.thena.tasks.client.api.model.ImmutableChangeTaskComment;
import io.resys.thena.tasks.client.api.model.ImmutableChangeTaskDueDate;
import io.resys.thena.tasks.client.api.model.ImmutableChangeTaskExtension;
import io.resys.thena.tasks.client.api.model.ImmutableChangeTaskInfo;
import io.resys.thena.tasks.client.api.model.ImmutableChangeTaskPriority;
import io.resys.thena.tasks.client.api.model.ImmutableChangeTaskStartDate;
import io.resys.thena.tasks.client.api.model.ImmutableChangeTaskStatus;
import io.resys.thena.tasks.client.api.model.ImmutableCommentOnTask;
import io.resys.thena.tasks.client.api.model.ImmutableCreateTask;
import io.resys.thena.tasks.client.api.model.ImmutableCreateTaskExtension;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.Task.Priority;
import io.resys.thena.tasks.tests.config.TaskPgProfile;
import io.resys.thena.tasks.tests.config.TaskTestCase;
import io.vertx.core.json.JsonObject;

@QuarkusTest
@TestProfile(TaskPgProfile.class)
public class TaskUpdateTest extends TaskTestCase {

  private Task createTaskForUpdating(TaskClient client) {
    return client.tasks()
      .createTask()
      .createOne(ImmutableCreateTask.builder()
        .targetDate(getTargetDate())
        .title("very important title no: init")
        .description("first task ever no: init")
        .priority(Priority.LOW)
        .addRoles("admin-users", "view-only-users")
        .userId("user-1")
        .reporterId("reporter-1")
        .assigneeIds(List.of("assignee-1", "assignee-2"))
        .build())
      .await().atMost(atMost);
  }
  
  @org.junit.jupiter.api.Test
  public void updateStatus() {
    final var repoName = TaskUpdateTest.class.getSimpleName() + "UpdateStatus";
    final var client = getClient().tenants().query().repoName(repoName).createIfNot().await().atMost(atMost);
    final var task = createTaskForUpdating(client);
    
    final var updated = client.tasks().updateTask().updateOne(ImmutableChangeTaskStatus.builder()
        .userId("tester-bob")
        .taskId(task.getId())
        .targetDate(
          getTargetDate()
            .plus(1, java.time.temporal.ChronoUnit.DAYS)
            .plus(1, java.time.temporal.ChronoUnit.HOURS)
        )
        .status(Task.Status.IN_PROGRESS)
        .build())
    .await().atMost(atMost);

    assertTaskJson("update-test-cases/updateStatus.json", updated);
    assertTenant(client, "update-test-cases/updateStatus.txt");
  }

  @org.junit.jupiter.api.Test
  public void updatePriority() {
    final var repoName = TaskUpdateTest.class.getSimpleName() + "UpdatePriority";
    final var client = getClient().tenants().query().repoName(repoName).createIfNot().await().atMost(atMost);
    final var task = createTaskForUpdating(client);

    final var updated = client.tasks().updateTask().updateOne(ImmutableChangeTaskPriority.builder()
            .userId("tester-bob")
            .taskId(task.getId())
            .targetDate(
              getTargetDate()
                .plus(1, java.time.temporal.ChronoUnit.DAYS)
                .plus(1, java.time.temporal.ChronoUnit.HOURS)
            )
            .priority(Priority.HIGH)
            .build())
        .await().atMost(atMost);
    
    assertTaskJson("update-test-cases/updatePriority.json", updated);
    assertTenant(client, "update-test-cases/updatePriority.txt");
  }

  @org.junit.jupiter.api.Test
  public void updateReporter() {
    final var repoName = TaskUpdateTest.class.getSimpleName() + "UpdateReporter";
    final var client = getClient().tenants().query().repoName(repoName).createIfNot().await().atMost(atMost);
    final var task = createTaskForUpdating(client);

    final var updated = client.tasks().updateTask().updateOne(ImmutableAssignTaskReporter.builder()
            .userId("tester-bob")
            .taskId(task.getId())
            .targetDate(
              getTargetDate()
                .plus(1, java.time.temporal.ChronoUnit.DAYS)
                .plus(1, java.time.temporal.ChronoUnit.HOURS)
            )
            .reporterId("reporter-bob")
            .build())
        .await().atMost(atMost);

    assertTaskJson("update-test-cases/updateReporter.json", updated);
    assertTenant(client, "update-test-cases/updateReporter.txt");
  }

  @org.junit.jupiter.api.Test
  public void archiveTaskViaUpdate() {
    final var repoName = TaskUpdateTest.class.getSimpleName() + "ArchiveTaskViaUpdate";
    final var client = getClient().tenants().query().repoName(repoName).createIfNot().await().atMost(atMost);
    final var task = createTaskForUpdating(client);

    final var updated = client.tasks().updateTask().updateOne(ImmutableArchiveTask.builder()
            .userId("tester-bob")
            .taskId(task.getId())
            .targetDate(
              getTargetDate()
                .plus(1, java.time.temporal.ChronoUnit.DAYS)
                .plus(1, java.time.temporal.ChronoUnit.HOURS)
            )
            .build())
        .await().atMost(atMost);

    assertTaskJson("update-test-cases/archiveTaskViaUpdate.json", updated);
    assertTenant(client, "update-test-cases/archiveTaskViaUpdate.txt");
  }

  @org.junit.jupiter.api.Test
  public void addComments() {
    final var repoName = TaskUpdateTest.class.getSimpleName() + "AddComments";
    final var client = getClient().tenants().query().repoName(repoName).createIfNot().await().atMost(atMost);
    final var task = createTaskForUpdating(client);

    final var addedComment = client.tasks().updateTask().updateOne(ImmutableCommentOnTask.builder()
            .userId("tester-bob")
            .taskId(task.getId())
            .targetDate(
              getTargetDate()
                .plus(1, java.time.temporal.ChronoUnit.DAYS)
                .plus(1, java.time.temporal.ChronoUnit.HOURS)
            )
            .commentText("comment-1-text")
            .build())
        .await().atMost(atMost)
        .getComments().stream().findFirst().get();

    final var updated = client.tasks().updateTask().updateOne(ImmutableCommentOnTask.builder()
            .userId("tester-bob")
            .taskId(task.getId())
            .targetDate(
              getTargetDate()
                .plus(1, java.time.temporal.ChronoUnit.DAYS)
                .plus(2, java.time.temporal.ChronoUnit.HOURS)
            )
            .commentText("comment-2-text")
            .replyToCommentId(addedComment.getId())
            .build())
        .await().atMost(atMost);

    assertTaskJson("update-test-cases/addComments.json", updated);
    assertTenant(client, "update-test-cases/addComments.txt");
  }

  @org.junit.jupiter.api.Test
  public void updateComment() {
    final var repoName = TaskUpdateTest.class.getSimpleName() + "UpdateComment";
    final var client = getClient().tenants().query().repoName(repoName).createIfNot().await().atMost(atMost);
    final var task = createTaskForUpdating(client);

    final var addedComment = client.tasks().updateTask().updateOne(ImmutableCommentOnTask.builder()
            .userId("tester-bob")
            .taskId(task.getId())
            .targetDate(
              getTargetDate()
                .plus(1, java.time.temporal.ChronoUnit.DAYS)
                .plus(1, java.time.temporal.ChronoUnit.HOURS)
            )
            .commentText("comment-1-text")
            .build())
        .await().atMost(atMost)
        .getComments().stream().findFirst().get();

    final var updated = client.tasks().updateTask().updateOne(ImmutableChangeTaskComment.builder()
            .userId("tester-bob")
            .taskId(task.getId())
            .targetDate(
              getTargetDate()
                .plus(1, java.time.temporal.ChronoUnit.DAYS)
                .plus(2, java.time.temporal.ChronoUnit.HOURS)
            )
            .commentId(addedComment.getId())
            .commentText("new-comment-text")
            .build())
        .await().atMost(atMost);

    assertTaskJson("update-test-cases/updateComment.json", updated);
    assertTenant(client, "update-test-cases/updateComment.txt");
  }

  @org.junit.jupiter.api.Test
  public void updateRoles() {
    final var repoName = TaskUpdateTest.class.getSimpleName() + "UpdateRoles";
    final var client = getClient().tenants().query().repoName(repoName).createIfNot().await().atMost(atMost);
    final var task = createTaskForUpdating(client);

    // add to static data
    toStaticData(client);
    
    
    final var updated = client.tasks().updateTask().updateOne(ImmutableAssignTaskRoles.builder()
            .userId("tester-bob")
            .taskId(task.getId())
            .targetDate(
              getTargetDate()
                .plus(1, java.time.temporal.ChronoUnit.DAYS)
                .plus(1, java.time.temporal.ChronoUnit.HOURS)
            )
            .roles(List.of("new-role"))
            .build())
        .await().atMost(atMost);

    assertTaskJson("update-test-cases/updateRoles.json", updated);
    assertTenant(client, "update-test-cases/updateRoles.txt");
  }

  @org.junit.jupiter.api.Test
  public void updateAssignees() {
    final var repoName = TaskUpdateTest.class.getSimpleName() + "UpdateAssignees";
    final var client = getClient().tenants().query().repoName(repoName).createIfNot().await().atMost(atMost);
    final var task = createTaskForUpdating(client);
    
    // add to static data
    toStaticData(client);
    
    
    final var updated = client.tasks().updateTask().updateOne(ImmutableAssignTask.builder()
            .userId("tester-bob")
            .taskId(task.getId())
            .targetDate(
              getTargetDate()
                .plus(1, java.time.temporal.ChronoUnit.DAYS)
                .plus(1, java.time.temporal.ChronoUnit.HOURS)
            )
            .assigneeIds(List.of("new-assignee"))
            .build())
        .await().atMost(atMost);

    assertTaskJson("update-test-cases/updateAssignees.json", updated);
    assertTenant(client, "update-test-cases/updateAssignees.txt");
  }

  @org.junit.jupiter.api.Test
  public void updateStartDate() {
    final var repoName = TaskUpdateTest.class.getSimpleName() + "UpdateStartDate";
    final var client = getClient().tenants().query().repoName(repoName).createIfNot().await().atMost(atMost);
    final var task = createTaskForUpdating(client);

    final var updated = client.tasks().updateTask().updateOne(ImmutableChangeTaskStartDate.builder()
            .userId("tester-bob")
            .taskId(task.getId())
            .targetDate(
              getTargetDate()
                .plus(1, java.time.temporal.ChronoUnit.DAYS)
                .plus(1, java.time.temporal.ChronoUnit.HOURS)
            )
            .startDate(
              LocalDate.ofInstant(
                getTargetDate()
                  .plus(1, java.time.temporal.ChronoUnit.DAYS),
                ZoneId.of("UTC")
              )
            )
            .build())
        .await().atMost(atMost);

    assertTaskJson("update-test-cases/updateStartDate.json", updated);
    assertTenant(client, "update-test-cases/updateStartDate.txt");
  }

  @org.junit.jupiter.api.Test
  public void updateDueDate() {
    final var repoName = TaskUpdateTest.class.getSimpleName() + "UpdateDueDate";
    final var client = getClient().tenants().query().repoName(repoName).createIfNot().await().atMost(atMost);
    final var task = createTaskForUpdating(client);

    final var updated = client.tasks().updateTask().updateOne(ImmutableChangeTaskDueDate.builder()
            .userId("tester-bob")
            .taskId(task.getId())
            .targetDate(
              getTargetDate()
                .plus(1, java.time.temporal.ChronoUnit.DAYS)
                .plus(1, java.time.temporal.ChronoUnit.HOURS)
            )
            .dueDate(
              LocalDate.ofInstant(
                getTargetDate()
                  .plus(5, java.time.temporal.ChronoUnit.DAYS),
                ZoneId.of("UTC")
              )
            )
            .build())
        .await().atMost(atMost);

    assertTaskJson("update-test-cases/updateDueDate.json", updated);
    assertTenant(client, "update-test-cases/updateDueDate.txt");
  }

  @org.junit.jupiter.api.Test
  public void updateTaskInfo() {
    final var repoName = TaskUpdateTest.class.getSimpleName() + "UpdateTaskInfo";
    final var client = getClient().tenants().query().repoName(repoName).createIfNot().await().atMost(atMost);
    final var task = createTaskForUpdating(client);

    final var updated = client.tasks().updateTask().updateOne(ImmutableChangeTaskInfo.builder()
            .userId("tester-bob")
            .taskId(task.getId())
            .targetDate(
              getTargetDate()
                .plus(1, java.time.temporal.ChronoUnit.DAYS)
                .plus(1, java.time.temporal.ChronoUnit.HOURS)
            )
            .title("new-title")
            .description("new-description")
            .build())
        .await().atMost(atMost);

    assertTaskJson("update-test-cases/updateTaskInfo.json", updated);
    assertTenant(client, "update-test-cases/updateTaskInfo.txt");
  }

  @org.junit.jupiter.api.Test
  public void createTaskExtension() {
    final var repoName = TaskUpdateTest.class.getSimpleName() + "CreateTaskExtension";
    final var client = getClient().tenants().query().repoName(repoName).createIfNot().await().atMost(atMost);
    final var task = createTaskForUpdating(client);

    final var updated = client.tasks().updateTask().updateOne(ImmutableCreateTaskExtension.builder()
            .userId("tester-bob")
            .taskId(task.getId())
            .targetDate(
              getTargetDate()
                .plus(1, java.time.temporal.ChronoUnit.DAYS)
                .plus(1, java.time.temporal.ChronoUnit.HOURS)
            )
            .type("attachment")
            .externalId("attachment-1")
            .body(JsonObject.of("x", "attachment-body"))
            .build())
        .await().atMost(atMost);

    assertTaskJson("update-test-cases/createTaskExtension.json", updated);
    assertTenant(client, "update-test-cases/createTaskExtension.txt");
  }

  @org.junit.jupiter.api.Test
  public void updateTaskExtension() {
    final var repoName = TaskUpdateTest.class.getSimpleName() + "UpdateTaskExtension";
    final var client = getClient().tenants().query().repoName(repoName).createIfNot().await().atMost(atMost);
    final var task = createTaskForUpdating(client);

    final var createdExt = client.tasks().updateTask().updateOne(ImmutableCreateTaskExtension.builder()
            .userId("tester-bob")
            .taskId(task.getId())
            .targetDate(
              getTargetDate()
                .plus(1, java.time.temporal.ChronoUnit.DAYS)
                .plus(1, java.time.temporal.ChronoUnit.HOURS)
            )
            .type("attachment")
            .externalId("attachment-1")
            .body(JsonObject.of("x", "attachment-body"))
            .build())
        .await().atMost(atMost)
        .getExtensions().stream().findFirst().get();

    final var updated = client.tasks().updateTask().updateOne(ImmutableChangeTaskExtension.builder()
            .userId("tester-bob")
            .taskId(task.getId())
            .targetDate(
              getTargetDate()
                .plus(1, java.time.temporal.ChronoUnit.DAYS)
                .plus(2, java.time.temporal.ChronoUnit.HOURS)
            )
            .id(createdExt.getId())
            .type("attachment")
            .externalId("attachment-1")
            .body(JsonObject.of("x", "attachment-body"))
            .build())
        .await().atMost(atMost);

    assertTaskJson("update-test-cases/updateTaskExtension.json", updated);
    assertTenant(client, "update-test-cases/updateTaskExtension.txt");
  }

  @org.junit.jupiter.api.Test
  public void updateParentTask() {
    final var repoName = TaskUpdateTest.class.getSimpleName() + "UpdateParentTask";
    final var client = getClient().tenants().query().repoName(repoName).createIfNot().await().atMost(atMost);
    final var task1 = createTaskForUpdating(client);

    final var task2 = client.tasks()
        .createTask()
        .createOne(ImmutableCreateTask.builder()
            .targetDate(getTargetDate())
            .title("sub task")
            .description("sub task description")
            .priority(Priority.LOW)
            .addRoles("admin-users")
            .userId("user-1")
            .reporterId("reporter-1")
            .build())
        .await().atMost(atMost);

    final var updated = client.tasks().updateTask().updateOne(ImmutableAssignTaskParent.builder()
            .userId("tester-bob")
            .taskId(task2.getId())
            .targetDate(
              getTargetDate()
                .plus(1, java.time.temporal.ChronoUnit.DAYS)
                .plus(1, java.time.temporal.ChronoUnit.HOURS)
            )
            .parentId(task1.getId())
            .build())
        .await().atMost(atMost);
    
    assertTaskJson("update-test-cases/updateParentTask.json", updated);
    assertTenant(client, "update-test-cases/updateParentTask.txt");
  }


}
