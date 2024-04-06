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
import org.junit.jupiter.api.Disabled;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.tasks.client.api.TaskClient;
import io.resys.thena.tasks.client.api.model.ImmutableCreateTask;
import io.resys.thena.tasks.client.api.model.Task.Priority;
import io.resys.thena.tasks.client.api.model.TaskCommand.CreateTask;
import io.resys.thena.tasks.tests.config.TaskPgProfile;
import io.resys.thena.tasks.tests.config.TaskTestCase;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuarkusTest
@TestProfile(TaskPgProfile.class)
public class TaskMetricTest extends TaskTestCase {
  private final Duration atMost = Duration.ofMinutes(10);
  // SELECT pg_size_pretty( pg_total_relation_size('init_test10_grim_commit') )
  // VACUUM (VERBOSE, ANALYZE) init_test10_grim_commit;
  // vacuum full init_test10_grim_commit;
  // SELECT pg_size_pretty( pg_total_relation_size('init_test10_grim_commit') )
  
  
  @Disabled
  @org.junit.jupiter.api.Test
  public void createAndReadTheTask() throws JsonProcessingException, JSONException {
    final var client = getClient().repo().query().repoName("init-test").create().await().atMost(atMost);
    
    runInserts(client, 10000);
    select(client);
  }
  

  private void select(TaskClient client) {
    final var start = System.currentTimeMillis();
    
    final var blobs = client.tasks().queryActiveTasks().findAll().await().atMost(atMost);
    final var end = System.currentTimeMillis();
    
    log.debug("total time for selecting: {} entries is: {} millis", blobs.size(), end-start);
  }
  
  
  
  private void runInserts(TaskClient client, int total) {
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
    
    client.tasks().createTask().createMany(bulk).await().atMost(atMost);
    end = System.currentTimeMillis();
    log.debug("total time for inserting: {} entries is: {} millis, loop time: {}", total, end-start, loopTime);
  }
}
