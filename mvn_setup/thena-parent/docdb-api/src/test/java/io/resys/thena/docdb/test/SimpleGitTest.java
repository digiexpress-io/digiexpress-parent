package io.resys.thena.docdb.test;

import java.io.Serializable;
import java.time.Duration;
import java.util.Map;

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

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.api.actions.CommitActions.CommitResultEnvelope;
import io.resys.thena.api.actions.TenantActions.RepoResult;
import io.resys.thena.api.actions.TenantActions.RepoStatus;
import io.resys.thena.api.models.Repo;
import io.resys.thena.api.models.Repo.RepoType;
import io.resys.thena.docdb.test.config.DbTestTemplate;
import io.resys.thena.docdb.test.config.PgProfile;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;


@QuarkusTest
@TestProfile(PgProfile.class)
@Slf4j
public class SimpleGitTest extends DbTestTemplate {


  @Value.Immutable
  public interface TestContent extends Serializable {
    String getId();
    String getName();
  }

  @Test
  public void crateRepoAddAndDeleteFile() {
    // create project
    RepoResult repo = getClient().tenants().commit()
        .name("crateRepoAddAndDeleteFile", RepoType.git)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(RepoStatus.OK, repo.getStatus());
    
    // Create head and first commit
    CommitResultEnvelope commit_0 = getClient().git(repo).commit().commitBuilder()
      .branchName("main")
      .append("readme.md", new JsonObject(Map.of(
          "type", "person",
          "name", "sam", 
          "lastName", "vimes")))
      .author("same vimes")
      .message("first commit!")
      .build()
      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
      .await().atMost(Duration.ofMinutes(1));

    log.debug("created commit {}", commit_0);
    Assertions.assertEquals(Repo.CommitResultStatus.OK, commit_0.getStatus());
    
    
    // Create head and first commit
    CommitResultEnvelope commit_1 = getClient().git(repo).commit().commitBuilder()
      .branchName( "main")
      .parent(commit_0.getCommit().getId())
      .remove("readme.md")
      .author("same vimes")
      .message("second commit!")
      .build()
      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
      .await().atMost(Duration.ofMinutes(1));
    
    log.debug("created commit 1 {}", commit_1);
    Assertions.assertEquals(Repo.CommitResultStatus.OK, commit_1.getStatus());
    super.printRepo(repo.getRepo());
  }
  
  @Test
  public void crateRepoWithOneCommit() {
    // create project
    RepoResult repo = getClient().tenants().commit()
        .name("project-x", RepoType.git)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(RepoStatus.OK, repo.getStatus());
    
    // Create head and first commit
    CommitResultEnvelope commit_0 = getClient().git(repo).commit().commitBuilder()
      .branchName("main")
      .append("readme.md", JsonObject.of("doc", "readme content"))
      .append("file1.json", JsonObject.of())
      .append("fileFromObject.txt", JsonObject.mapFrom(ImmutableTestContent.builder().id("10").name("sam vimes").build()))
      .author("same vimes")
      .message("first commit!")
      .build()
      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
      .await().atMost(Duration.ofMinutes(1));

    log.debug("created commit {}", commit_0);
    Assertions.assertEquals(Repo.CommitResultStatus.OK, commit_0.getStatus());
    super.printRepo(repo.getRepo());
  }
  
  
  @Test
  public void createRepoWithTwoCommits() {
    // create project
    RepoResult repo = getClient().tenants().commit()
        .name("project-xy", RepoType.git)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(RepoStatus.OK, repo.getStatus());
    
    // Create head and first commit
    CommitResultEnvelope commit_0 = getClient().git(repo).commit().commitBuilder()
      .branchName("main")
      .append("readme.md", JsonObject.of("doc", "readme content"))
      .append("file1.json", JsonObject.of())
      .append("fileFromObject.txt", JsonObject.mapFrom(ImmutableTestContent.builder().id("10").name("sam vimes").build()))
      .author("same vimes")
      .message("first commit!")
      .build()
      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
      .await().atMost(Duration.ofMinutes(1));

    log.debug("created commit 0 {}", commit_0);
    Assertions.assertEquals(Repo.CommitResultStatus.OK, commit_0.getStatus());
    
    
    // Create head and first commit
    CommitResultEnvelope commit_1 = getClient().git(repo).commit().commitBuilder()
      .branchName("main")
      .parent(commit_0.getCommit().getId())
      .append("readme.md", JsonObject.of("doc", "readme content"))
      .append("file1.json", JsonObject.of())
      .append("fileFromObject.txt", JsonObject.mapFrom(ImmutableTestContent.builder().id("10").name("sam vimes 1").build()))
      .author("same vimes")
      .message("second commit!")
      .build()
      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
      .await().atMost(Duration.ofMinutes(1));
    
    log.debug("created commit 1 {}", commit_1);
    Assertions.assertEquals(Repo.CommitResultStatus.OK, commit_1.getStatus());
    
    super.printRepo(repo.getRepo());
  }
}
