package io.resys.hdes.docdb.tests;

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

import io.resys.hdes.docdb.tests.config.MongoDbConfig;
import io.resys.thena.docdb.api.actions.CommitActions.CommitResult;
import io.resys.thena.docdb.api.actions.CommitActions.CommitStatus;
import io.resys.thena.docdb.api.actions.RepoActions.RepoResult;
import io.resys.thena.docdb.api.actions.RepoActions.RepoStatus;
import io.resys.thena.docdb.api.actions.TagActions.TagResult;
import io.resys.thena.docdb.api.actions.TagActions.TagStatus;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.Objects.Commit;
import io.resys.thena.docdb.api.models.Objects.Tag;


public class HistoryTest extends MongoDbConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(HistoryTest.class);
  
  @Value.Immutable
  public interface TestContent extends Serializable {
    String getId();
    String getName();
  }

  @Test
  public void diffTest() {
    final var repo = createRepo();
    Commit main_0 = createCommits(repo, "main");
    Commit main_1 = createCommits(repo, "main", main_0.getId());
    Commit main_2 = createCommits(repo, "main", main_1.getId());
        
    Tag tag0 = createTag(repo, main_0);
    Tag tag1 = createTag(repo, main_1);
    Tag tag2 = createTag(repo, main_2);

    final var result = getClient().diff().head()
      .repo(repo.getId())
      .left("main")
      .right(main_0.getId())
      .build().await().indefinitely();
    Assertions.assertEquals(10, result.getObjects().getDivergences().get(0).getMain().getCommits());
    super.printDiff(result.getObjects());
    super.printRepo(repo);
  }

  
  private Tag createTag(Repo repo, Commit commit) {
    TagResult result = getClient().tag().create()
      .repo(repo.getName(), commit.getId())
      .author("sam vimes").message("diff-test")
      .tagName(commit.getId() + "_tag")
      .build().await().indefinitely();
    
    Assertions.assertEquals(TagStatus.OK, result.getStatus());
    
    return result.getTag();
  }

  private Repo createRepo() {
    RepoResult repo = getClient().repo().create()
        .name("diff-test-project")
        .build()
        .await().atMost(Duration.ofMinutes(1));
    LOGGER.debug("created repo {}", repo);
    Assertions.assertEquals(RepoStatus.OK, repo.getStatus());
    return repo.getRepo();
  }
  
  private Commit createCommits(Repo repo, String head) {    
    CommitResult previous = null;
    for(int index = 0; index < 5; index++) {
      previous = getClient().commit().head()
          .head(repo.getName(), head)
          .parent(previous == null ? null : previous.getCommit().getId())
          .append("file1.json", "content" + index + head)
          .author("same vimes")
          .message("diff test!")
          .build()
          .await().atMost(Duration.ofMinutes(1));
      
      if(previous.getStatus() != CommitStatus.OK) {
        printRepo(repo);
      }
      Assertions.assertEquals(CommitStatus.OK, previous.getStatus());
    }
    return previous.getCommit();
  }
  
  
  private Commit createCommits(Repo repo, String head, String parent) {    
    CommitResult previous = null;
    for(int index = 0; index < 5; index++) {
      previous = getClient().commit().head()
          .head(repo.getName(), head)
          .parent(previous == null ? parent : previous.getCommit().getId())
          .append("file1.json", "content" + index + head)
          .author("same vimes")
          .message("diff test!")
          .build()
          .await().atMost(Duration.ofMinutes(1));
      
      if(previous.getStatus() != CommitStatus.OK) {
        System.err.println(previous);
        printRepo(repo);
      }
      Assertions.assertEquals(CommitStatus.OK, previous.getStatus());
    }
    return previous.getCommit();
  }
}
