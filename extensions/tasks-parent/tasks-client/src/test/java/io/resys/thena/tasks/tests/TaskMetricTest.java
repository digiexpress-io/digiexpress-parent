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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.tasks.client.api.TasksClient;
import io.resys.thena.tasks.client.api.model.ImmutableCreateTask;
import io.resys.thena.tasks.client.api.model.Task.Priority;
import io.resys.thena.tasks.client.api.model.TaskAction.CreateTask;
import io.resys.thena.tasks.tests.config.TaskPgProfile;
import io.resys.thena.tasks.tests.config.TaskTestCase;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuarkusTest
@TestProfile(TaskPgProfile.class)
public class TaskMetricTest extends TaskTestCase {
  private final Duration atMost = Duration.ofSeconds(2);
  
  //@org.junit.jupiter.api.Test
  
  public void createAndReadTheTask() throws JsonProcessingException, JSONException {
    final var client = getClient().repo().repoName("init-test").create().await().atMost(atMost);
    
    runInserts(client, 1000);
    select(client);
  }
  
  
  private void select(TasksClient client) {
    final var start = System.currentTimeMillis();
    
    final var blobs = client.actions().queryActiveTasks().findAll().collect().asList().await().atMost(Duration.ofMinutes(1));
    final var end = System.currentTimeMillis();
    
    log.debug("total time for selecting: {} entries is: {} millis", blobs.size(), end-start);
  }
  
  
  
  private void runInserts(TasksClient client, int total) {
    var start = System.currentTimeMillis();
    
    List<CreateTask> bulk = new ArrayList<>();
    for(int index = 0; index < total; index++) {
      final var newTask = ImmutableCreateTask.builder()
      .targetDate(getTargetDate())
      .title("very important title no: " + index)
      .description("first task ever no: "  + index)
      .priority(Priority.LOW)
      .addRoles("admin-users", "view-only-users")
      .userId("user-1")
      .reporterId("reporter-1")
      .build();
      bulk.add(newTask);
    }
    var end = System.currentTimeMillis();
    final var loopTime = end - start;
    
    start = System.currentTimeMillis();
    
    client.actions().createTask().createMany(bulk).await().atMost(atMost);
    end = System.currentTimeMillis();
    log.debug("total time for inserting: {} entries is: {} millis, loop time: {}", total, end-start, loopTime);
  }
}
