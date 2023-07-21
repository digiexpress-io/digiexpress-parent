package io.resys.hdes.client.spi;

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

import java.io.Serializable;
import java.time.Duration;
import java.util.Map;

import org.immutables.value.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.hdes.client.spi.config.PgProfile;
import io.resys.hdes.client.spi.config.PgTestTemplate;
import io.resys.thena.docdb.api.actions.CommitActions.CommitResultStatus;
import io.resys.thena.docdb.api.actions.ProjectActions.RepoResult;
import io.resys.thena.docdb.api.actions.ProjectActions.RepoStatus;
import io.vertx.core.json.JsonObject;

@QuarkusTest
@TestProfile(PgProfile.class)
public class ThenaPgTest extends PgTestTemplate {

  private static final Logger LOGGER = LoggerFactory.getLogger(ThenaPgTest.class);
  
  @Value.Immutable
  public interface TestContent extends Serializable {
    String getId();
    String getName();
  }
  
  @Test
  public void crateRepoWithOneCommit() {
    // create project
    RepoResult repo = getThena().project().projectBuilder()
        .name("project-x")
        .build()
        .await().atMost(Duration.ofMinutes(1));
    LOGGER.debug("created repo {}", repo);
    Assertions.assertEquals(RepoStatus.OK, repo.getStatus());
    
    // Create head and first commit
    var commit_0 = getThena().commit().commitBuilder()
      .head("project-x", "main")
      .append("readme.md", JsonObject.mapFrom(Map.of("content", "readme content")))
      .append("file1.json", new JsonObject("{}"))
      .append("fileFromObject.txt", JsonObject.mapFrom(ImmutableTestContent.builder().id("10").name("sam vimes").build()))
      .author("same vimes")
      .head("project-x", "main")
      .message("first commit!")
      .build()
      .await().atMost(Duration.ofMinutes(1));

    LOGGER.debug("created commit {}", commit_0);
    Assertions.assertEquals(CommitResultStatus.OK, commit_0.getStatus());
    super.printRepo(repo.getRepo());
  }
  
  
  @Test
  public void crateRepoWithTwoCommits() {
    // create project
    RepoResult repo = getThena().project().projectBuilder()
        .name("project-xy")
        .build()
        .await().atMost(Duration.ofMinutes(1));
    LOGGER.debug("created repo {}", repo);
    Assertions.assertEquals(RepoStatus.OK, repo.getStatus());
    
    // Create head and first commit
    var commit_0 = getThena().commit().commitBuilder()
      .head(repo.getRepo().getName(), "main")
      .append("readme.md", JsonObject.mapFrom(Map.of("content", "readme content")))
      .append("file1.json", new JsonObject("{}"))
      .append("fileFromObject.txt", JsonObject.mapFrom(ImmutableTestContent.builder().id("10").name("sam vimes").build()))
      .author("same vimes")
      .message("first commit!")
      .build()
      .await().atMost(Duration.ofMinutes(1));

    LOGGER.debug("created commit 0 {}", commit_0);
    Assertions.assertEquals(CommitResultStatus.OK, commit_0.getStatus());
    
    
    // Create head and first commit
    var commit_1 = getThena().commit().commitBuilder()
      .head(repo.getRepo().getName(), "main")
      .parent(commit_0.getCommit().getId())
      .append("readme.md", JsonObject.mapFrom(Map.of("content", "readme content")))
      .append("file1.json", new JsonObject("{}"))
      .append("fileFromObject.txt", JsonObject.mapFrom(ImmutableTestContent.builder().id("10").name("sam vimes 1").build()))
      .author("same vimes")
      .message("second commit!")
      .build()
      .onFailure(Throwable.class).invoke(t -> {
        LOGGER.debug(t.getMessage(), t);
      })
      .await().atMost(Duration.ofMinutes(1));
    
    LOGGER.debug("created commit 1 {}", commit_1);
    Assertions.assertEquals(CommitResultStatus.OK, commit_1.getStatus());
    
    super.printRepo(repo.getRepo());
  }
}
