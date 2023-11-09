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

import org.immutables.value.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.docdb.api.actions.RepoActions.RepoResult;
import io.resys.thena.docdb.api.actions.RepoActions.RepoStatus;
import io.resys.thena.docdb.api.models.Repo.RepoType;
import io.resys.thena.docdb.test.config.DbTestTemplate;
import io.resys.thena.docdb.test.config.PgProfile;
import io.vertx.core.json.JsonObject;


@QuarkusTest
@TestProfile(PgProfile.class)
public class SimpleDocTest extends DbTestTemplate {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleDocTest.class);
  
  @Value.Immutable
  public interface TestContent extends Serializable {
    String getId();
    String getName();
  }

  @Test
  public void crateRepoAddAndDeleteFile() {
    // create project
    RepoResult repo = getClient().repo().projectBuilder()
        .name("SimpleDocTest-1", RepoType.doc)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    LOGGER.debug("created repo {}", repo);
    Assertions.assertEquals(RepoStatus.OK, repo.getStatus());
    
    // branch 1
    final var createdDoc = getClient().doc().commit()
      .createOneDoc()
      .docType("customer-data")
      .repoId(repo.getRepo().getId())
      .externalId("bobs-ssn-id")
      .branchName("main")
      .append(JsonObject.of("first_name", "bob", "last_name", "flop"))
      .message("created first entry")
      .log(JsonObject.of("some_cool_command", "create_customer"))
      .author("jane.doe@morgue.com")
    .build().await().atMost(Duration.ofMinutes(1));

    // branch 2
    final var branchDoc = getClient().doc().commit()
      .branchOneDoc()
      .repoId(repo.getRepo().getId())
      .branchFrom(createdDoc.getBranch().getId())
      .branchName("dev")
      .append(JsonObject.of("first_name", "bob", "last_name", "flop-2"))
      .message("created branch entry")
      .log(JsonObject.of("created-branch-command", "branch the customer for some reason"))
      .author("jane.doe@morgue.com")
    .build().await().atMost(Duration.ofMinutes(1));
    
    // meta update, 1 commit into each branch
    getClient().doc().commit()
      .modifyOneDoc()
      .repoId(repo.getRepo().getId())
      .docId(createdDoc.getDoc().getId())
      .meta(JsonObject.of("super cool field 1", "cool meta about the document"))
      .author("jane.doe@morgue.com")
      .message("changed meta for doc")
    .build().await().atMost(Duration.ofMinutes(1));

    
    // update dev branch with new data
    getClient().doc().commit().modifyOneBranch()
      .branchId(branchDoc.getBranch().getId())
      .append(JsonObject.of("branch new content", "something in here", "last_name", "used to be -> flop-2"))
      .repoId(repo.getRepo().getId())
      .author("jane.doe@morgue.com")
      .message("edited dev branch")
    .build().await().atMost(Duration.ofMinutes(1));

    assertRepo(repo.getRepo(), "doc-db-test-cases/crud-test-1.txt");
    
    
    final var findAllDocs = getClient().doc().find().docQuery()
        .repoId(repo.getRepo().getId())
        .findAll()
    .await().atMost(Duration.ofMinutes(1));
    
    Assertions.assertEquals(1, findAllDocs.getObjects().getDocs().size());
    Assertions.assertEquals(2, findAllDocs.getObjects().getCommits().size());
    
    // one document, 2 branches
    Assertions.assertEquals(1, findAllDocs.getObjects().getBranches().size());
    Assertions.assertEquals(2, findAllDocs.getObjects().getBranches().get(createdDoc.getDoc().getId()).size());
    
    final var findAllMainBranchDocs = getClient().doc().find().docQuery()
        .repoId(repo.getRepo().getId())
        .branchName("main")
        .docType("customer-data")
        .findAll()
    .await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals(1, findAllMainBranchDocs.getObjects().getDocs().size());
    Assertions.assertEquals(1, findAllMainBranchDocs.getObjects().getBranches().values().stream().flatMap(e -> e.stream()).count());
    
    printRepo(repo.getRepo());
  }
}
