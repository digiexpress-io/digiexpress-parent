package io.digiexpress.eveli.client.test.task;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import io.digiexpress.eveli.client.api.ImmutableCreateTaskCommand;
import io.digiexpress.eveli.client.api.ImmutableCreateTaskCommentCommand;
import io.digiexpress.eveli.client.api.ImmutableModifyTaskCommand;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.TaskCommentSource;
import io.digiexpress.eveli.client.api.TaskClient.TaskStatus;
import io.digiexpress.eveli.client.test.BaseEnvir;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class CreateTaskAssignmentTest extends TaskEnvirSetup {
  @Container @ServiceConnection static PostgreSQLContainer<?> CONTAINER = new PostgreSQLContainer<>("postgres:17");
  @BeforeAll static void beforeAll() { start(CONTAINER); }
  @AfterAll static void afterAll() { end(); }
  @Autowired TaskClient taskClient;
  
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
    
    final var comment = taskClient.taskBuilder()
      .userId(user, email)
      .createTaskComment(ImmutableCreateTaskCommentCommand.builder()
        .external(true)
        .commentText(BaseEnvir.FAKER.chuckNorris().fact())
        .taskId(task.getId())
        .source(TaskCommentSource.FRONTDESK)
        .build())
      .await().atMost(atMost);

    final var assignee = taskClient.taskBuilder()
      .userId(user, email)
      .modifyTask(task.getId(), ImmutableModifyTaskCommand.builder()
          .status(TaskStatus.REJECTED)
          .assignedUser("bob")
          .assignedUserEmail("bob@bob")
          .subject(task.getSubject())
          .build())
      .await().atMost(atMost);

    final var assignee_change = taskClient.taskBuilder()
        .userId(user, email)
        .modifyTask(task.getId(), ImmutableModifyTaskCommand.builder()
            .assignedUser("sam")
            .subject(task.getSubject())
            .build())
        .await().atMost(atMost);

    final var group_change_1 = taskClient.taskBuilder()
        .userId(user, email)
        .modifyTask(task.getId(), ImmutableModifyTaskCommand.builder()
            .assignedRoles(Arrays.asList("role-1", "role-2", "role-3"))
            .subject(task.getSubject())
            .build())
        .await().atMost(atMost);
    

    final var group_change_2 = taskClient.taskBuilder()
        .userId(user, email)
        .modifyTask(task.getId(), ImmutableModifyTaskCommand.builder()
            .assignedRoles(Arrays.asList("role-2", "role-3"))
            .subject(task.getSubject())
            .build())
        .await().atMost(atMost);

    
    final var group_change_3 = taskClient.taskBuilder()
        .userId(user, email)
        .modifyTask(task.getId(), ImmutableModifyTaskCommand.builder()
            .assignedRoles(Arrays.asList("role-2", "role-3", "role-1"))
            .subject(task.getSubject())
            .build())
        .await().atMost(atMost);
    
    final var diff_version_4 = taskClient.queryTasks()
        .getOneTaskDiff(task.getId(), group_change_3.getVersion())
        .await().atMost(atMost);
    
    
    System.out.println(diff_version_4);
  }
}
