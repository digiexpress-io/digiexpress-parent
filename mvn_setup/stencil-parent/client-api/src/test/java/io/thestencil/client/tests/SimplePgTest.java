package io.thestencil.client.tests;

import java.io.Serializable;
import java.time.Duration;

import org.immutables.value.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/*-
 * #%L
 * stencil-persistence
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

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import io.resys.thena.api.actions.TenantActions.TenantStatus;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.thestencil.client.tests.util.PgProfile;
import io.thestencil.client.tests.util.PgTestTemplate;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@QuarkusTest
@TestProfile(PgProfile.class)
@Slf4j
public class SimplePgTest extends PgTestTemplate {

  @Value.Immutable
  public interface TestContent extends Serializable {
    String getId();
    String getName();
  }
  
  @Test
  public void crateRepoWithOneCommit() {
    // create project
    TenantCommitResult repo = getClient().tenants().commit()
        .name("project-x", StructureType.git)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(TenantStatus.OK, repo.getStatus());
    
    // Create head and first commit
    final var commit_0 = getClient().git("project-x").commit().commitBuilder()
      .branchName("main")
      .append("fileFromObject.txt", JsonObject.mapFrom(ImmutableTestContent.builder().id("10").name("sam vimes").build()))
      .author("same vimes")
      .message("first commit!")
      .build()
      .await().atMost(Duration.ofMinutes(1));

    log.debug("created commit {}", commit_0);
    Assertions.assertEquals(CommitResultStatus.OK, commit_0.getStatus());
    super.printRepo(repo.getRepo());
  }
  
  
  @Test
  public void crateRepoWithTwoCommits() {
    // create project
    TenantCommitResult repo = getClient().tenants().commit()
        .name("project-xy", StructureType.git)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(TenantStatus.OK, repo.getStatus());
    
    // Create head and first commit
    var commit_0 = getClient().git(repo.getRepo().getName()).commit().commitBuilder()
      .branchName("main")
      .append("fileFromObject.txt", JsonObject.mapFrom(ImmutableTestContent.builder().id("10").name("sam vimes").build()))
      .author("same vimes")
      .message("first commit!")
      .build()
      .await().atMost(Duration.ofMinutes(1));

    log.debug("created commit 0 {}", commit_0);
    Assertions.assertEquals(CommitResultStatus.OK, commit_0.getStatus());
    
    
    // Create head and first commit
    var commit_1 = getClient().git(repo.getRepo().getName()).commit().commitBuilder()
      .branchName("main")
      .parent(commit_0.getCommit().getId())
      .append("fileFromObject.txt", JsonObject.mapFrom(ImmutableTestContent.builder().id("10").name("sam vimes 1").build()))
      .author("same vimes")
      .message("second commit!")
      .build()
      .onFailure(Throwable.class).invoke(t -> {
        log.debug(t.getMessage(), t);
      })
      .await().atMost(Duration.ofMinutes(1));
    
    log.debug("created commit 1 {}", commit_1);
    Assertions.assertEquals(CommitResultStatus.OK, commit_1.getStatus());
    
    super.printRepo(repo.getRepo());
  }
}
