package io.resys.thena.docdb.spi.pgsql;

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

import org.immutables.value.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.docdb.api.actions.CommitActions.CommitResult;
import io.resys.thena.docdb.api.actions.CommitActions.CommitStatus;
import io.resys.thena.docdb.api.actions.RepoActions.RepoResult;
import io.resys.thena.docdb.api.actions.RepoActions.RepoStatus;
import io.resys.thena.docdb.spi.pgsql.config.DbTestTemplate;
import io.resys.thena.docdb.spi.pgsql.config.PgProfile;


@QuarkusTest
@TestProfile(PgProfile.class)
public class SimpleTest extends DbTestTemplate {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleTest.class);
  
  @Value.Immutable
  public interface TestContent extends Serializable {
    String getId();
    String getName();
  }

  @Test
  public void crateRepoAddAndDeleteFile() {
    // create project
    RepoResult repo = getClient().repo().create()
        .name("crateRepoAddAndDeleteFile")
        .build()
        .await().atMost(Duration.ofMinutes(1));
    LOGGER.debug("created repo {}", repo);
    Assertions.assertEquals(RepoStatus.OK, repo.getStatus());
    
    // Create head and first commit
    CommitResult commit_0 = getClient().commit().head()
      .head(repo.getRepo().getName(), "main")
      .append("readme.md", "readme content")
      .author("same vimes")
      .message("first commit!")
      .build()
      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
      .await().atMost(Duration.ofMinutes(1));

    LOGGER.debug("created commit {}", commit_0);
    Assertions.assertEquals(CommitStatus.OK, commit_0.getStatus());
    
    
    // Create head and first commit
    CommitResult commit_1 = getClient().commit().head()
      .head(repo.getRepo().getName(), "main")
      .parent(commit_0.getCommit().getId())
      .remove("readme.md")
      .author("same vimes")
      .message("second commit!")
      .build()
      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
      .await().atMost(Duration.ofMinutes(1));
    
    LOGGER.debug("created commit 1 {}", commit_1);
    Assertions.assertEquals(CommitStatus.OK, commit_1.getStatus());
    super.printRepo(repo.getRepo());
  }
  
  @Test
  public void crateRepoWithOneCommit() {
    // create project
    RepoResult repo = getClient().repo().create()
        .name("project-x")
        .build()
        .await().atMost(Duration.ofMinutes(1));
    LOGGER.debug("created repo {}", repo);
    Assertions.assertEquals(RepoStatus.OK, repo.getStatus());
    
    // Create head and first commit
    CommitResult commit_0 = getClient().commit().head()
      .head("project-x", "main")
      .append("readme.md", "readme content")
      .append("file1.json", "[{}]")
      .append("fileFromObject.txt", ImmutableTestContent.builder().id("10").name("sam vimes").build().toString())
      .author("same vimes")
      .head("project-x", "main")
      .message("first commit!")
      .build()
      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
      .await().atMost(Duration.ofMinutes(1));

    LOGGER.debug("created commit {}", commit_0);
    Assertions.assertEquals(CommitStatus.OK, commit_0.getStatus());
    super.printRepo(repo.getRepo());
  }
  
  
  @Test
  public void createRepoWithTwoCommits() {
    // create project
    RepoResult repo = getClient().repo().create()
        .name("project-xy")
        .build()
        .await().atMost(Duration.ofMinutes(1));
    LOGGER.debug("created repo {}", repo);
    Assertions.assertEquals(RepoStatus.OK, repo.getStatus());
    
    // Create head and first commit
    CommitResult commit_0 = getClient().commit().head()
      .head(repo.getRepo().getName(), "main")
      .append("readme.md", "readme content")
      .append("file1.json", "[{}]")
      .append("fileFromObject.txt", ImmutableTestContent.builder().id("10").name("sam vimes").build().toString())
      .author("same vimes")
      .message("first commit!")
      .build()
      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
      .await().atMost(Duration.ofMinutes(1));

    LOGGER.debug("created commit 0 {}", commit_0);
    Assertions.assertEquals(CommitStatus.OK, commit_0.getStatus());
    
    
    // Create head and first commit
    CommitResult commit_1 = getClient().commit().head()
      .head(repo.getRepo().getName(), "main")
      .parent(commit_0.getCommit().getId())
      .append("readme.md", "readme content")
      .append("file1.json", "[{}, {}]")
      .append("fileFromObject.txt", ImmutableTestContent.builder().id("10").name("sam vimes 1").build().toString())
      .author("same vimes")
      .message("second commit!")
      .build()
      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
      .await().atMost(Duration.ofMinutes(1));
    
    LOGGER.debug("created commit 1 {}", commit_1);
    Assertions.assertEquals(CommitStatus.OK, commit_1.getStatus());
    
    super.printRepo(repo.getRepo());
  }
}
