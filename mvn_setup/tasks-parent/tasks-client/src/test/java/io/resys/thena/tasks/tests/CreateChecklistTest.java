package io.resys.thena.tasks.tests;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.tasks.client.api.TaskClient;
import io.resys.thena.tasks.client.api.model.ImmutableChangeChecklistTitle;
import io.resys.thena.tasks.client.api.model.ImmutableCreateChecklist;
import io.resys.thena.tasks.client.api.model.ImmutableCreateTask;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.Task.Priority;
import io.resys.thena.tasks.tests.config.TaskPgProfile;
import io.resys.thena.tasks.tests.config.TaskTestCase;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuarkusTest
@TestProfile(TaskPgProfile.class)
public class CreateChecklistTest extends TaskTestCase {

  private Task createTask(TaskClient client) {
    return client.tasks()
      .createTask()
      .createOne(ImmutableCreateTask.builder()
        .targetDate(getTargetDate())
        .title("Creating a task for checklists")
        .description("This is the first task ever!")
        .priority(Priority.LOW)
        .addRoles("admin-users", "view-only-users")
        .userId("user-1")
        .reporterId("reporter-1")
        .build())
      .await().atMost(atMost);
  }
  
  @Test
  public void createChecklist() {
    final var repoName = CreateChecklistTest.class.getSimpleName() + "CreateChecklist";
    final var client = getClient().repo().query().repoName(repoName).createIfNot().await().atMost(atMost);
    final var task = createTask(client);
    
    final var taskWithChecklist = client.tasks().updateTask().updateOne(ImmutableCreateChecklist.builder()
        .title("My first checklist")
        .userId("John smith")
        .taskId(task.getId())
        .targetDate(getTargetDate())
        .build())
    .await().atMost(atMost);
    assertEquals("checklist-test-cases/create-checklist.json", taskWithChecklist);

    
    final var updatedChecklist = client.tasks().updateTask().updateOne(ImmutableChangeChecklistTitle.builder()
        .title("My second checklist")
        .userId("John smith")
        .taskId(task.getId())
        .checklistId("3_TASK")
        .targetDate(getTargetDate())
        .build())
    .await().atMost(atMost);

    assertEquals("checklist-test-cases/change-checklist-title.json", updatedChecklist);
  }

}
