package io.resys.hdes.docdb.tests;

import java.io.Serializable;
import java.time.Duration;

import org.immutables.value.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.resys.hdes.docdb.api.actions.CommitActions.CommitResult;
import io.resys.hdes.docdb.api.actions.CommitActions.CommitStatus;
import io.resys.hdes.docdb.api.actions.RepoActions.RepoResult;
import io.resys.hdes.docdb.api.actions.RepoActions.RepoStatus;
import io.resys.hdes.docdb.tests.config.MongoDbConfig;


public class ErrorTest extends MongoDbConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(ErrorTest.class);
  
  @Value.Immutable
  public interface TestContent extends Serializable {
    String getId();
    String getName();
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
      .parent("unknown smth!")
      .append("readme.md", "readme content")
      .append("file1.json", "[{}]")
      .append("fileFromObject.txt", ImmutableTestContent.builder().id("10").name("sam vimes").build().toString())
      .author("same vimes")
      .head("project-x", "main")
      .message("first commit!")
      .build()
      .await().atMost(Duration.ofMinutes(1));

    LOGGER.debug("created commit {}", commit_0);
    Assertions.assertEquals(CommitStatus.ERROR, commit_0.getStatus());
  }
  
  
  @Test
  public void crateRepoWithTwoCommits() {
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
      .await().atMost(Duration.ofMinutes(1));

    LOGGER.debug("created commit 0 {}", commit_0);
    Assertions.assertEquals(CommitStatus.OK, commit_0.getStatus());
    
    
    // Create head and first commit
    CommitResult commit_1 = getClient().commit().head()
      .head(repo.getRepo().getName(), "main")
      // no parent .parent(commit_0.getCommit().getId())
      .append("readme.md", "readme content")
      .append("file1.json", "[{}, {}]")
      .append("fileFromObject.txt", ImmutableTestContent.builder().id("10").name("sam vimes 1").build().toString())
      .author("same vimes")
      .message("second commit!")
      .build()
      .await().atMost(Duration.ofMinutes(1));
    
    LOGGER.debug("created commit 1 {}", commit_1);
    Assertions.assertEquals(CommitStatus.ERROR, commit_1.getStatus());
    
    super.printRepo(repo.getRepo());
  }
}
