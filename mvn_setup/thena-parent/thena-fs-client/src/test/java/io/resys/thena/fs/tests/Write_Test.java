package io.resys.thena.fs.tests;

/*-
 * #%L
 * thena-fs-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.thena.api.actions.TenantActions.CreatedTenant;
import io.resys.thena.api.actions.TenantActions.TenantOperationStatus;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.envelope.CommitResultStatus;
import io.resys.thena.fs.tests.config.DbTestTemplate;
import io.resys.thena.fs.tests.config.TestAsserts;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Write_Test extends DbTestTemplate {
  
  @Test
  public void create2FileWith2Commits() {

    final var tenant = "Write";
    final CreatedTenant repo = getClient().tenants()
        .createOneTenant()
        
        .name(tenant, StructureType.fs)
        .buildOnlyIfNotCreated()
        .await().atMost(Duration.ofMinutes(1)).getItem2();
    
    log.debug("created repo {}", repo);
    Assertions.assertEquals(TenantOperationStatus.OK, repo.getStatus());
    
    final var fs = getClient().withTenant(tenant);
    final var objectId = "my-super-file";
    
     // commit 1
    final var commit_1 = fs
      .commitBuilder()
      .commitAuthor("john smith")
      .commitMessage("create main branch with some content")
      .newFile((newFile) -> newFile
          .fileName("xxx.txt")
          .filePath("root/xyz")
          .fileType("text")
          .fileId(objectId)
          .fileValue(JsonObject.of("firstName", "Sam", "lastName", "Vimes"))
          .build())
      .build()
      .await().atMost(atMost);
  
    Assertions.assertEquals(CommitResultStatus.OK, commit_1.getStatus());
  
  

    // commit 2 
    final var commit_2 = fs
      .commitBuilder()
      .commitAuthor("john smith")
      .commitMessage("create main branch with some content")
      .newFile((newFile) -> newFile
          .fileId(objectId)
          .fileType("text")
          .fileValue(JsonObject.of("firstName", "Sam", "lastName", "Vimes"))
          .build())
      .build()
      .await().atMost(atMost);
    
    TestAsserts.assertLog(commit_2, 
    """
Tree changes:
M    root/xyz/xxx.txt
commit commit01
Author: john smith
Date: 2024-01-01 12:00:00 UTC

    create main branch with some content

Branch updated: main commit02..commit01
    """);
    Assertions.assertEquals(CommitResultStatus.OK, commit_2.getStatus());
  
    
    
    { // pull 2 files
      final var result = fs
          .branchQuery().getOne()
          .await().atMost(atMost);
      
      Assertions.assertNotNull(result.getTransitives(), "transitives must be loaded!");
      Assertions.assertNotNull(result.getTransitives().getTree(), "transitives.tree must be loaded!");
      Assertions.assertNotNull(result.getTransitives().getCommit(), "transitives.commit must be loaded!");
      Assertions.assertEquals(result.getTransitives().getBlobsById().size(), 1);
      Assertions.assertEquals(result.getTransitives().getTree().getTreeNodes().size(), 1);
      
    
      // same content
      final var blob = result.getTransitives().getBlobsById().values().iterator().next();
      Assertions.assertEquals(blob.getBlobValue().size(), 2);
      Assertions.assertEquals(blob.getBlobValue().getString("firstName"), "Sam");
      Assertions.assertEquals(blob.getBlobValue().getString("lastName"), "Vimes");
      
      final var node_1 = result.getTransitives().getTree().getOneNode("root/xyz/xxx.txt");
      final var node_1Created = node_1.getTransitives().getObjectIndex().getCreatedAt();
      Assertions.assertEquals(commit_1.getCommit().getCommitCreatedAt().toEpochSecond(), node_1Created.toEpochSecond());
    }
  }
}
