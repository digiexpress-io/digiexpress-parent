package io.resys.thena.docdb.test;

/*-
 * #%L
 * thena-docdb-mongo
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.immutables.value.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.docdb.api.actions.CommitActions.CommitResultEnvelope;
import io.resys.thena.docdb.api.actions.RepoActions.RepoResult;
import io.resys.thena.docdb.api.actions.RepoActions.RepoStatus;
import io.resys.thena.docdb.api.models.Repo.RepoType;
import io.resys.thena.docdb.test.config.DbTestTemplate;
import io.resys.thena.docdb.test.config.PgProfile;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;


@QuarkusTest
@TestProfile(PgProfile.class)
public class ConcurrectModificationDBTest extends DbTestTemplate {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrectModificationDBTest.class);
  
  @JsonSerialize(as = ImmutableUseTasks.class) @JsonDeserialize(as = ImmutableUseTasks.class)
  @Value.Immutable
  public interface UseTasks extends Serializable {
    String getId();
    String getUserName();
    List<Integer> getTasks();
  }
  
  private AtomicInteger index = new AtomicInteger(0);

  @Test
  public void crateRepoWithOneCommit() {
    // create project
    RepoResult repo = getClient().repo().projectBuilder()
        .name("user-tasks", RepoType.git)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    LOGGER.debug("created repo {}", repo);
    Assertions.assertEquals(RepoStatus.OK, repo.getStatus());
    
    // Create head and first commit
    getClient().git().commit().commitBuilder()
        .head(repo.getRepo().getName(), "main")
        .append("user-1", JsonObject.mapFrom(ImmutableUseTasks.builder().id("user-1").userName("sam vimes 1").addTasks(0).build()))
        .author("same vimes")
        .message("init user with one task")
        .build()
      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
      .await().atMost(Duration.ofMinutes(1));

    
    runInserts(repo, 100);
    
    final var state = getClient().git().branch().branchQuery()
      .projectName(repo.getRepo().getName()).branchName("main")
      .docsIncluded(true)
      .get().await().atMost(Duration.ofMinutes(1));
    
    final var blobId = state.getObjects().getTree().getValues().get("user-1").getBlob();
    final var result = state.getObjects().getBlobs().get(blobId).getValue().mapTo(UseTasks.class);
    
    Assertions.assertEquals(101, result.getTasks().size());
    for(var runningNumber = 0; runningNumber < 100; runningNumber++) {
      Assertions.assertTrue(result.getTasks().contains(runningNumber));
    }
    
    super.printRepo(repo.getRepo());
  }
  
  
  
  private void runInserts(RepoResult repo, int total) {
    final var commands = new ArrayList<Uni<CommitResultEnvelope>>();
    for(int index = 0; index < total; index++) {
      // Create head and first commit
      Uni<CommitResultEnvelope> commit_0 = getClient().git().commit().commitBuilder()
        .head(repo.getRepo().getName(), "main")
        .latestCommit()
        .merge("user-1", (previous) -> {
          final var next = ImmutableUseTasks.builder().from(previous.mapTo(UseTasks.class)).addTasks(this.index.incrementAndGet()).build();
          return JsonObject.mapFrom(next);
        })
        .author("same vimes")
        .message("add task")
        .build();
      
      commands.add(commit_0);

    }
    
    final var completed = Multi.createFrom().items(commands.stream())
      .onItem().transformToUni(command -> command)
      .merge(5)
      .collect().asList()
      .await().atMost(Duration.ofMinutes(1));
    
  }
}
