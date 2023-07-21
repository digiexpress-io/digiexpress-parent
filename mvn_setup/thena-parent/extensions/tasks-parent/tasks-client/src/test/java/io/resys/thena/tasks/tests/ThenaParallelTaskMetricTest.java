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
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.tasks.client.api.TaskClient;
import io.resys.thena.tasks.client.api.model.ImmutableCreateTask;
import io.resys.thena.tasks.client.api.model.Task.Priority;
import io.resys.thena.tasks.client.api.model.TaskCommand.CreateTask;
import io.resys.thena.tasks.client.spi.store.MainBranch;
import io.resys.thena.tasks.tests.config.TaskPgProfile;
import io.resys.thena.tasks.tests.config.TaskTestCase;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuarkusTest
@TestProfile(TaskPgProfile.class)
public class ThenaParallelTaskMetricTest extends TaskTestCase {
  private AtomicInteger index = new AtomicInteger(0);
  private AtomicInteger fails = new AtomicInteger(0);
  private String repoName = "metric-test";
  
  @org.junit.jupiter.api.Test
  public void createAndReadTheTask() throws JsonProcessingException, JSONException {
    final var client = getClient().repo().query().repoName(repoName).create().await().atMost(atMost);
    
    // first commit
    client.tasks().createTask().createOne(ImmutableCreateTask.builder()
        .targetDate(getTargetDate())
        .title("very important title no: init")
        .description("first task ever no: init")
        .priority(Priority.LOW)
        .addRoles("admin-users", "view-only-users")
        .userId("user-1")
        .reporterId("reporter-1")
        .build())
    .await().atMost(atMost);
    
    
    runInserts(client, 49);
    runSelect(client);    
    
    
    // Assert that there is no data loss from parallel processing
    final var config = getStore().getConfig();
    final var blobState = config.getClient().branch().branchQuery()
        .projectName(repoName)
        .branchName(MainBranch.HEAD_NAME)
        .docsIncluded(true)
        .get().await().atMost(atMost);
    
    JsonArray blobs = new JsonArray(blobState.getObjects().getBlobs().values().stream()
        .map(b -> b.getValue())
        .toList());
    
    Assertions.assertEquals(index.get() - fails.get() +1, blobs.size());
    log.debug(blobs.encodePrettily());
    
    JsonArray expected = new JsonArray();
    expected.add(JsonObject.of("title", "very important title no: init"));
    for(int index = 0; index < 49; index++) {
      expected.add(JsonObject.of("title", "very important title no: " + index));
    }
    JSONAssert.assertEquals(expected.encodePrettily(), blobs.encodePrettily(), false);
  }
  

  private void runSelect(TaskClient client) {
    final var start = System.currentTimeMillis();
    final var blobs = client.tasks().queryActiveTasks().findAll().await().atMost(Duration.ofMinutes(1));
    final var end = System.currentTimeMillis();
    
    log.debug("total time for selecting: {} entries is: {} millis", blobs.size(), end-start);
  }
  
  private void runInserts(TaskClient client, int total) {
    
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
    
    final var insertStart = System.currentTimeMillis();
    
    Multi.createFrom().items(bulk.stream()).onItem().transformToUni(item -> {
      final var commit = client.tasks().createTask().createOne(item)
      .onItem().transform(c -> {
        log.debug("Record stored: {}", index.getAndIncrement());
        return c;
      })
      .onFailure().recoverWithItem((error) -> {
        log.debug("Record failed: {}, error {}", fails.getAndIncrement(), error.getMessage());
        return null;
      });
      return commit;
    })
    .merge(5)
    .collect().asList()
    .onItem().transformToUni(e -> {
      final var insertEnd = System.currentTimeMillis();
      final var start = System.currentTimeMillis();
      return client.tasks().queryActiveTasks().findAll().onItem().transform(blobs -> {
        // log select time
        final var end = System.currentTimeMillis();
        log.debug("total time for inserting: {} entries is: {} millis", blobs.size(), insertEnd-insertStart);
        log.debug("total time for selecting: {} entries is: {} millis", blobs.size(), end-start);
        return Uni.createFrom().nullItem();
      
      }).onItem().transformToUni(junk -> {
        // log commits
        final var config = getStore().getConfig();
        return config.getClient().commit().findAllCommits(repoName)
        .onItem().transformToUni(commits -> {          
          log.debug("Total commits: {}, fails: {}, items: {}", commits.size(), fails.get(), bulk.size());
          return Uni.createFrom().nullItem();
        });
      }).onItem().transformToUni(junk -> {
        // log trees
        final var config = getStore().getConfig();
        return config.getClient().commit().findAllCommitTrees(repoName)
        .onItem().transformToUni(trees -> {          
          log.debug("Total Trees: {}", trees.size());
          return Uni.createFrom().nullItem();
        });
        
      });
    })
    .await().atMost(atMost);
    
    
  }
}
