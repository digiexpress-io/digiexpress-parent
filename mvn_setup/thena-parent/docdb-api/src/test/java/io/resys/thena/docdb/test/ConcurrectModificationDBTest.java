package io.resys.thena.docdb.test;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.immutables.value.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.api.actions.GitCommitActions.CommitResultEnvelope;
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import io.resys.thena.api.actions.TenantActions.CommitStatus;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.docdb.test.config.DbTestTemplate;
import io.resys.thena.docdb.test.config.PgProfile;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;


@QuarkusTest
@TestProfile(PgProfile.class)
@Slf4j
public class ConcurrectModificationDBTest extends DbTestTemplate {

  @JsonSerialize(as = ImmutableUseTasks.class) @JsonDeserialize(as = ImmutableUseTasks.class)
  @Value.Immutable
  public interface UseTasks extends Serializable {
    String getId();
    String getUserName();
    List<Integer> getTasks();
  }
  
  private AtomicInteger index = new AtomicInteger(0);
  private Collection<Integer> rejected = Collections.synchronizedCollection(new ArrayList<Integer>());

  @Test
  public void crateRepoWithOneCommit() {
    // create project
    TenantCommitResult repo = getClient().tenants().commit()
        .name("user-tasks", StructureType.git)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(CommitStatus.OK, repo.getStatus());
    
    // Create head and first commit
    getClient().git(repo).commit().commitBuilder()
        .branchName("main")
        .append("user-1", JsonObject.mapFrom(ImmutableUseTasks.builder().id("user-1").userName("sam vimes 1").addTasks(0).build()))
        .author("same vimes")
        .message("init user with one task")
        .build()
      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
      .await().atMost(Duration.ofMinutes(1));

    
    runInserts(repo, 100);
    
    final var state = getClient().git(repo).branch().branchQuery()
      .branchName("main")
      .docsIncluded(true)
      .get().await().atMost(Duration.ofMinutes(1));
    
    final var blobId = state.getObjects().getTree().getValues().get("user-1").getBlob();
    final var result = state.getObjects().getBlobs().get(blobId).getValue().mapTo(UseTasks.class);
    
    Assertions.assertEquals(101 - rejected.size(), result.getTasks().size());
    /*
    for(var runningNumber = 0; runningNumber < 100; runningNumber++) {
      if(rejected.contains(runningNumber)) {
        continue;
      }
      final var session = runningNumber;
      Assertions.assertTrue(result.getTasks().contains(runningNumber), () -> "Running number: " + session + "/" + rejected );
    }
    */
    super.printRepo(repo.getRepo());
  }

  
  private void runInserts(TenantCommitResult repo, int total) {
    final var commands = new ArrayList<Uni<CommitResultEnvelope>>();
    for(int index = 0; index < total; index++) {
      // Create head and first commit
      final var session = index;
      Uni<CommitResultEnvelope> commit_0 = getClient().git(repo).commit().commitBuilder()
        .branchName("main")
        .latestCommit()
        .merge("user-1", (previous) -> {
          final var next = ImmutableUseTasks.builder().from(previous.mapTo(UseTasks.class)).addTasks(this.index.incrementAndGet()).build();
          return JsonObject.mapFrom(next);
        })
        .author("same vimes")
        .message("add task")
        .build()
        .onItem().transform(resp -> {
          if(resp.getStatus() != CommitResultStatus.OK) {
            resp.getMessages().forEach(msg -> {
              // race condition, somebody managed to already modify the data, while getting the lock 
              log.error(msg.getText(), msg.getException());
              rejected.add(session);
            });
            
          }
          return resp;
        });
      
      commands.add(commit_0);

    }
    
    Multi.createFrom().items(commands.stream())
      .onItem().transformToUni(command -> command)
      .merge(50)
      .collect().asList()
      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
      .await().atMost(Duration.ofMinutes(1));
    
  }
}
