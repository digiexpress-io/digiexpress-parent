package io.resys.thena.docdb.test;

/*-
 * #%L
 * thena-doc-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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
import java.util.Arrays;
import java.util.Collections;

import org.immutables.value.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.thena.api.actions.TenantActions.TenantOperationStatus;
import io.resys.thena.api.actions.TenantActions.CreatedTenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.envelope.CommitResultStatus;
import io.resys.thena.doc.api.DocQueryActions.IncludeInQuery;
import io.resys.thena.docdb.test.config.DbTestTemplate;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;



@Slf4j
public class BatchDocWithoutCommitTreeTest extends DbTestTemplate {
  private final static String tenantId = "BatchDocWithoutCommitTreeTest-1";

  @Value.Immutable
  public interface TestContent extends Serializable {
    String getId();
    String getName();
  }

  @Test
  public void batch10Docs() {
    
    // 1 doc 1 commit
    final var branchCreated = createOnDocAndOneBranch();
    
    final var initCommits = getClient().doc(tenantId).find()
      .docQuery().include(IncludeInQuery.COMMITS).findAll()
      .onItem().transform(e -> e.getObjects().getCommits().values())
      .await().atMost(atMost);
    
    Assertions.assertEquals(1, initCommits.size());
    
    
    // update dev branch with new data
    {
      final var modifyBranch = getClient().doc(tenantId).commit().modifyManyBranches()
        .commitAuthor("jane.doe@morgue.com")
        .commitMessage("edit dev branch")
        .commitLogExcludesBranchBody()
        .commitTreeEnabled(false)
        .item()
          .branchName("main")
          .docId(branchCreated.getDocId())
          .merge(old -> old.copy().put("added new field", "super cool field"))
          .next();
      
      final var modified = modifyBranch.build().await().atMost(atMost);
      Assertions.assertEquals(CommitResultStatus.OK, modified.getStatus());
      
      final var modifiedCommits = getClient().doc(tenantId).find()
          .docQuery().include(IncludeInQuery.COMMITS).findAll()
          .onItem().transform(e -> e.getObjects().getCommits().values())
          .await().atMost(atMost);
        
      Assertions.assertEquals(1, modifiedCommits.size());
    }

    // update branch for the second time... previous commit will be deleted and replaced with the new one
    {
      final var modifyManyDocs = getClient().doc(tenantId).commit().modifyManyDocs()
          .commitAuthor("jane.doe@morgue.com")
          .commitMessage("edit dev branch")
          .commitLogExcludesBranchBody()
          .commitTreeEnabled(false)
          .item()
            .docId(branchCreated.getDocId())
            .meta(JsonObject.of("meta data", "some cool to add to meta"))
            .commands(Collections.emptyList())
            .next();
      
      final var modified = modifyManyDocs.build().await().atMost(Duration.ofMinutes(1));
      Assertions.assertEquals(CommitResultStatus.OK, modified.getStatus());
      
      final var modifiedCommits = getClient().doc(tenantId).find()
          .docQuery().include(IncludeInQuery.COMMITS).findAll()
          .onItem().transform(e -> e.getObjects().getCommits().values())
          .await().atMost(atMost);
        
      Assertions.assertEquals(1, modifiedCommits.size());
    }
    
    
    // fail safe test, query everything... see if smth blows up
    printRepo(getClient().tenants().queryTenants().id(tenantId).getOne().await().atMost(atMost));
  }
  
  
  
  private DocBranch createOnDocAndOneBranch() {
    // create project
    // with main branch, commit log na doc id from json
    CreatedTenant repo = getClient().tenants().createOneTenant()
        .name(tenantId, StructureType.doc)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(TenantOperationStatus.OK, repo.getStatus());
    
    final var createdDoc = getClient().doc(repo).commit()
      .createManyDocs()
      .commitMessage("batching tests")
      .commitAuthor("jane.doe@morgue.com")
      .commitLogExcludesBranchBody()
      .commitTreeEnabled(false);
    
    createdDoc.item()
      .externalId("bobs-ssn-id")      
      .branchContent(JsonObject.of("id", "id-1", "first_name", "bob", "last_name", "flop"))
      .commands(Arrays.asList(JsonObject.of("some_cool_command", "create_customer")))
      .branchName("main")
      .docType("customer-data")
      .next();
      
    final var inserted = createdDoc.build().await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals(CommitResultStatus.OK, inserted.getStatus());
    
    final var findAllDocs = getClient().doc(repo).find().docQuery().findAll()
        .await().atMost(Duration.ofMinutes(1));
    
    Assertions.assertEquals(1, findAllDocs.getObjects().getDocs().size());
    Assertions.assertEquals(1, findAllDocs.getObjects().getBranches().size());
    
    
    return inserted.getBranch().iterator().next();
  }
}
