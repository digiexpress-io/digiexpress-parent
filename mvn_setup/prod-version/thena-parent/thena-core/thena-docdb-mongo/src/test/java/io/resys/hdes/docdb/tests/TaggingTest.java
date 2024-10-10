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

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

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


public class TaggingTest extends MongoDbConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(TaggingTest.class);

  
  @Test
  public void duplicateTag() {
    // create project
    RepoResult repo = getClient().repo().create()
        .name("project-xyz")
        .build()
        .await().atMost(Duration.ofMinutes(1));
    LOGGER.debug("created repo {}", repo);
    Assertions.assertEquals(RepoStatus.OK, repo.getStatus());
    
    // Create head and first commit
    CommitResult commit_0 = getClient().commit().head()
      .head(repo.getRepo().getName(), "main")
      .append("readme.md", "readme content")
      .append("file1.json", "[{}]")
      .author("same vimes")
      .message("first commit!")
      .build()
      .await().atMost(Duration.ofMinutes(1));

    LOGGER.debug("created commit 0 {}", commit_0);
    Assertions.assertEquals(CommitStatus.OK, commit_0.getStatus());
    
    TagResult tag_0 = getClient().tag().create()
        .tagName("super tag on commit 1")
        .author("same vimes")
        .message("first commit tag")
        .repo(repo.getRepo().getName(), commit_0.getCommit().getId())
        .build()
        .await().atMost(Duration.ofMinutes(1));
      Assertions.assertEquals(TagStatus.OK, tag_0.getStatus());

    TagResult tag_1 = getClient().tag().create()
        .tagName("super tag on commit 1")
        .author("same vimes")
        .message("second commit tag")
        .repo(repo.getRepo().getName(), commit_0.getCommit().getId())
        .build()
        .await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals(TagStatus.ERROR, tag_1.getStatus());
    
    
    getClient().tag().query()
    .tagName("super tag on commit 1")
    .repo(repo.getRepo().getName())
    .delete()
    .await().atMost(Duration.ofMinutes(1));
    
    tag_1 = getClient().tag().create()
        .tagName("super tag on commit 1")
        .author("same vimes")
        .message("second commit tag")
        .repo(repo.getRepo().getName(), commit_0.getCommit().getId())
        .build()
        .await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals(TagStatus.ERROR, tag_1.getStatus());
  }
  
  
  @Test
  public void crateRepoWithTwoCommitsAndTagBothCommits() {
    // create project
    RepoResult repo = getClient().repo().create()
        .name("project-xyz")
        .build()
        .await().atMost(Duration.ofMinutes(1));
    LOGGER.debug("created repo {}", repo);
    Assertions.assertEquals(RepoStatus.OK, repo.getStatus());
    
    // Create head and first commit
    CommitResult commit_0 = getClient().commit().head()
      .head(repo.getRepo().getName(), "main")
      .append("readme.md", "readme content")
      .append("file1.json", "[{}]")
      .author("same vimes")
      .message("first commit!")
      .build()
      .await().atMost(Duration.ofMinutes(1));

    LOGGER.debug("created commit 0 {}", commit_0);
    Assertions.assertEquals(CommitStatus.OK, commit_0.getStatus());
    
    
    // Create head and first commit
    CommitResult commit_1 = getClient().commit().head()
      .head(repo.getRepo().getName(), "main")
      .parent(commit_0.getCommit().getId())
      .append("readme.md", "readme content")
      .append("file1.json", "[{}, {}]")
      .author("same vimes")
      .message("second commit!")
      .build()
      .await().atMost(Duration.ofMinutes(1));
    
    LOGGER.debug("created commit 1 {}", commit_1);
    Assertions.assertEquals(CommitStatus.OK, commit_1.getStatus());
    
    
    TagResult tag_0 = getClient().tag().create()
      .tagName("super tag on commit 1")
      .author("same vimes")
      .message("first commit tag")
      .repo(repo.getRepo().getName(), commit_0.getCommit().getId())
      .build()
      .await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals(TagStatus.OK, tag_0.getStatus());

    TagResult tag_1 = getClient().tag().create()
        .tagName("Tagging again")
        .author("same vimes")
        .message("second commit tag")
        .repo(repo.getRepo().getName(), commit_1.getCommit().getId())
        .build()
        .await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals(TagStatus.OK, tag_1.getStatus());
    
    List<String> tagNames = getClient().tag().query().repo(repo.getRepo().getName()).find().collectItems().asList()
    .await().indefinitely().stream().map(t -> t.getName()).collect(Collectors.toList());
    
    Assertions.assertTrue(tagNames.contains(tag_0.getTag().getName()));
    Assertions.assertTrue(tagNames.contains(tag_1.getTag().getName()));
    
    super.printRepo(repo.getRepo());
  }
}
