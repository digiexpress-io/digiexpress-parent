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

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.docdb.api.actions.TenantModel.RepoResult;
import io.resys.thena.docdb.api.actions.TenantModel.RepoStatus;
import io.resys.thena.docdb.api.models.Repo.RepoType;
import io.resys.thena.docdb.test.config.DbTestTemplate;
import io.resys.thena.docdb.test.config.PgProfile;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.immutables.value.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.time.Duration;


@QuarkusTest
@TestProfile(PgProfile.class)
@Slf4j
public class BatchDocTest extends DbTestTemplate {

  @Value.Immutable
  public interface TestContent extends Serializable {
    String getId();
    String getName();
  }

  @Test
  public void batch10Docs() {
    // create project
    // with main branch, commit log na doc id from json
    RepoResult repo = getClient().tenants().commit()
        .name("BatchDocTest-1", RepoType.doc)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(RepoStatus.OK, repo.getStatus());
    
    final var createdDoc = getClient().doc(repo).commit()
        .createManyDocs()
        .branchName("main")
        .docType("customer-data")
        .message("batching tests")
        .author("jane.doe@morgue.com");
    
    for(int index = 0; index < 10; index++) {
      createdDoc.item()
        .externalId("bobs-ssn-id-" + index)      
        .append(JsonObject.of("id", "id-" + index, "first_name", "bob", "last_name", "flop"))
        .log(JsonObject.of("some_cool_command", "create_customer"))
        .next();
    }
    final var inserted = createdDoc.build().await().atMost(Duration.ofMinutes(1));

    
    final var findAllDocs = getClient().doc(repo).find().docQuery().findAll()
    .await().atMost(Duration.ofMinutes(1));
    
    Assertions.assertEquals(10, findAllDocs.getObjects().getDocs().size());
    Assertions.assertEquals(10, findAllDocs.getObjects().getCommits().size());
    
    
    // update branches
    // update dev branch with new data
    final var modifyBranch = getClient().doc(repo).commit().modifyManyBranches()
      .author("jane.doe@morgue.com")
      .message("edit dev branch")
      .branchName("main");
    
    for(final var createdBranch : inserted.getBranch()) {
      modifyBranch.item()
        .docId(createdBranch.getDocId())
        .merge(old -> old.copy().put("added new field", "super cool field"))
        .next();
    }
    modifyBranch.build().await().atMost(Duration.ofMinutes(1));
    
    
    // Modify all doc-s meta data
    final var modifyManyDocs = getClient().doc(repo).commit().modifyManyDocs()
        .author("jane.doe@morgue.com")
        .message("edit dev branch");
      
      for(final var createdBranch : inserted.getBranch()) {
        modifyManyDocs.item()
          .docId(createdBranch.getDocId())
          .meta(JsonObject.of("meta data", "some cool to add to meta"))
          .log(JsonObject.of("logging", "added meta"))
          .next();
      }
      modifyManyDocs.build().await().atMost(Duration.ofMinutes(1));
    
    printRepo(repo.getRepo());
    
    assertRepo(repo.getRepo(), "doc-db-test-cases/batch-crud-test-1.txt");
  }
}
