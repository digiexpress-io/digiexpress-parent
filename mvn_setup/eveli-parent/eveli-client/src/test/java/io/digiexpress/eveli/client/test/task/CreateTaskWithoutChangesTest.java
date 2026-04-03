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
import io.digiexpress.eveli.client.api.TaskClient.TaskStatus;
import io.digiexpress.eveli.client.test.BaseEnvir;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class CreateTaskWithoutChangesTest extends TaskEnvirSetup {
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


    final var statusChange = taskClient.taskBuilder()
      .userId(user, email)
      .modifyTask(task.getId(), ImmutableModifyTaskCommand.builder()
          .status(TaskStatus.REJECTED)
          .subject(task.getSubject())
          .build())
      .await().atMost(atMost);

    final var noChange = taskClient.taskBuilder()
        .userId(user, email)
        .modifyTask(task.getId(), ImmutableModifyTaskCommand.builder()
            .status(TaskStatus.REJECTED)
            .subject(task.getSubject())
            .build())
        .await().atMost(atMost);
    
  }
  
}
