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
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import io.resys.thena.api.actions.TenantActions.CommitStatus;
import io.resys.thena.api.entities.Tenant.StructureType;
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
public class SimpleDocTest extends DbTestTemplate {

  @Value.Immutable
  public interface TestContent extends Serializable {
    String getId();
    String getName();
  }

  @Test
  public void crateRepoAddAndDeleteFile() {
    // create project
    TenantCommitResult repo = getClient().tenants().commit()
        .name("SimpleDocTest-1", StructureType.doc)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(CommitStatus.OK, repo.getStatus());
    
    // branch 1
    final var createdDoc = getClient().doc(repo).commit()
      .createOneDoc()
      .docType("customer-data")
      .externalId("bobs-ssn-id")
      .branchName("main")
      .append(JsonObject.of("first_name", "bob", "last_name", "flop"))
      .message("created first entry")
      .log(JsonObject.of("some_cool_command", "create_customer"))
      .author("jane.doe@morgue.com")
    .build().await().atMost(Duration.ofMinutes(1));

    // branch 2
    final var branchDoc = getClient().doc(repo).commit()
      .branchOneDoc()
      .docId(createdDoc.getDoc().getId())
      .branchFrom(createdDoc.getBranch().getBranchName())
      .branchName("dev")
      .append(JsonObject.of("first_name", "bob", "last_name", "flop-2"))
      .message("created branch entry")
      .log(JsonObject.of("created-branch-command", "branch the customer for some reason"))
      .author("jane.doe@morgue.com")
    .build().await().atMost(Duration.ofMinutes(1));
    
    // meta update, 1 commit into each branch
    getClient().doc(repo).commit()
      .modifyOneDoc()
      .docId(createdDoc.getDoc().getId())
      .meta(JsonObject.of("super cool field 1", "cool meta about the document"))
      .author("jane.doe@morgue.com")
      .message("changed meta for doc")
    .build().await().atMost(Duration.ofMinutes(1));

    
    // update dev branch with new data
    getClient().doc(repo).commit().modifyOneBranch()
      .docId(branchDoc.getDoc().getId())
      .branchName(branchDoc.getBranch().getBranchName())
      .append(JsonObject.of("branch new content", "something in here", "last_name", "used to be -> flop-2"))

      .author("jane.doe@morgue.com")
      .message("edited dev branch")
    .build().await().atMost(Duration.ofMinutes(1));

    assertRepo(repo.getRepo(), "doc-db-test-cases/crud-test-1.txt");
    
    
    final var findAllDocs = getClient().doc(repo).find().docQuery()
        .children(true)
        .findAll()
    .await().atMost(Duration.ofMinutes(1));
    
    Assertions.assertEquals(1, findAllDocs.getObjects().getDocs().size());
    Assertions.assertEquals(2, findAllDocs.getObjects().getCommits().size());
    
    // one document, 2 branches
    Assertions.assertEquals(1, findAllDocs.getObjects().getBranches().size());
    Assertions.assertEquals(2, findAllDocs.getObjects().getBranches().get(createdDoc.getDoc().getId()).size());
    
    final var findAllMainBranchDocs = getClient().doc(repo).find().docQuery()
        .branchName("main")
        .docType("customer-data")
        .findAll()
    .await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals(1, findAllMainBranchDocs.getObjects().getDocs().size());
    Assertions.assertEquals(1, findAllMainBranchDocs.getObjects().getBranches().values().stream().flatMap(e -> e.stream()).count());
    
    printRepo(repo.getRepo());
  }
  
  

  @Test
  public void parentChild() {
    // create project
    TenantCommitResult repo = getClient().tenants().commit()
        .name("SimpleDocTest-parent-child", StructureType.doc)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(CommitStatus.OK, repo.getStatus());
    
    // doc 1
    final var parentDoc = getClient().doc(repo).commit()
      .createOneDoc()
      .docType("customer-data")
      .externalId("bobs-ssn-id")
      .branchName("main")
      .append(JsonObject.of("first_name", "bob", "last_name", "flop"))
      .message("created first entry")
      .log(JsonObject.of("some_cool_command", "create_customer"))
      .author("jane.doe@morgue.com")
    .build().await().atMost(Duration.ofMinutes(1));

    // doc 1 child
    final var childDoc = getClient().doc(repo).commit()
        .createOneDoc()
        .parentDocId(parentDoc.getDoc().getId())
        .docType("customer-data")
        .externalId("bobs-child-ssn-id")
        .branchName("main")
        .append(JsonObject.of("first_name", "bob_child", "last_name", "flop"))
        .message("created child entry")
        .log(JsonObject.of("some_cool_command", "create_customer"))
        .author("jane.doe@morgue.com")
    .build().await().atMost(Duration.ofMinutes(1));
    

    final var findAllDocs = getClient().doc(repo).find().docQuery()
        .children(true)
        .findAll()
    .await().atMost(Duration.ofMinutes(1));
    
    Assertions.assertEquals(2, findAllDocs.getObjects().getDocs().size());
    Assertions.assertEquals(2, findAllDocs.getObjects().getCommits().size());
    
    // 1 parent and 1 child document, 2 branches
    Assertions.assertEquals(2, findAllDocs.getObjects().getBranches().size());
    
    
    // find parent document
    final var findParent = getClient().doc(repo).find().docQuery()
        .branchName("main")
        .docType("customer-data")
        .matchId("bobs-ssn-id")
        .findAll()
    .await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals(1, findParent.getObjects().getDocs().size());
    Assertions.assertEquals(1, findParent.getObjects().getBranches().values().stream().flatMap(e -> e.stream()).count());
    
    

    // find parent with child document
    final var findParentWithChild = getClient().doc(repo).find().docQuery()
        .branchName("main")
        .docType("customer-data")
        .matchId("bobs-ssn-id")
        .children(true)
        .findAll()
    .await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals(2, findParentWithChild.getObjects().getDocs().size());
    
    final var documents = String.join(",", findParentWithChild.getObjects().getDocs().stream().map(d -> d.getExternalId()).sorted().toList());
    Assertions.assertEquals("bobs-child-ssn-id,bobs-ssn-id", documents);
    
    
    // delete documents
    getClient().doc(repo).commit().modifyManyDocs()
      .message("deleting docs")
      .author("jane.doe@morgue.com")
      .item().docId(parentDoc.getDoc().getId()).remove().next()
      .item().docId(childDoc.getDoc().getId()).remove().next()
      .build()
    .await().atMost(Duration.ofMinutes(1));
    
    final var findAllDocsAfterDelete = getClient().doc(repo).find().docQuery().findAll()
    .await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals(0, findAllDocsAfterDelete.getObjects().getDocs().size());
    
    
    printRepo(repo.getRepo());
  }
}
