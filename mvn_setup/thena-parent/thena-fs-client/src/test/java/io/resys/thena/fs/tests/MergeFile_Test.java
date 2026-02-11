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
public class MergeFile_Test extends DbTestTemplate {
  

  
  @Test
  public void mergeOneFile() {

    final var tenant = "Merge_Test";
    final CreatedTenant repo = getClient().tenants()
        .createOneTenant()
        .name(tenant, StructureType.fs)
        .buildOnlyIfNotCreated()
        .await().atMost(Duration.ofMinutes(1)).getItem2();
    
    log.debug("created repo {}", repo);
    Assertions.assertEquals(TenantOperationStatus.OK, repo.getStatus());
    wipeRepo(repo.getRepo());

    
    final var fs = getClient().withTenant(tenant);


     // commit 1
    final var commit_1 = fs
      .commitBuilder()
      .commitAuthor("john smith")
      .commitMessage("create main branch with some content")
      .newFile((newFile) -> newFile
          .fileName("xxx.txt")
          .filePath("root/xyz")
          .fileType("text")
          .fileValue(JsonObject.of("firstName", "Sam", "lastName", "Vimes"))
          .build())
      .build()
      .await().atMost(atMost);
    Assertions.assertEquals(CommitResultStatus.OK, commit_1.getStatus());
  
  
    
    // commit 2, change first name
    {
      final var commit_2 = fs
        .commitBuilder()
        .commitAuthor("john smith")
        .commitMessage("changed last name")
        .mergeFile("root/xyz/xxx.txt", (prev, newFile) -> newFile
            .fileValue(JsonObject.of("firstName", "Lady Sybil", "lastName", prev.getTransitives().getBlob().getBlobValue().getString("lastName")))
            .build())
        .build()
        .await().atMost(atMost);
      
      Assertions.assertEquals(CommitResultStatus.OK, commit_2.getStatus());
      TestAsserts.assertLog(commit_2, 
      """
  Tree changes:
  M    root/xyz/xxx.txt
  commit commit01
  Author: john smith
  Date: 2024-01-01 12:00:00 UTC
  
      changed last name
  
  Branch updated: main commit02..commit01
      """);
      
      { // pull all files
        final var result = fs
            .branchQuery().getOne()
            .await().atMost(atMost);
  
        final var blob = result.getTransitives().getBlobsById().values().iterator().next();
        Assertions.assertEquals(1, result.getTransitives().getTree().getTreeNodes().size());
        Assertions.assertEquals(1, result.getTransitives().getBlobsById().size());
        Assertions.assertEquals(2, blob.getBlobValue().size());
        Assertions.assertEquals("Lady Sybil", blob.getBlobValue().getString("firstName"));
        Assertions.assertEquals("Vimes", blob.getBlobValue().getString("lastName"));
      }
    }
    
    
    
    // commit 3, change path and name
    {
      final var commit_2 = fs
        .commitBuilder()
        .commitAuthor("john smith")
        .commitMessage("file rename")
        .mergeFile("root/xyz/xxx.txt", (prev, newFile) -> newFile
            .filePath("new_super_folder/xyz")
            .fileName("changed_filename.txt")
            .build())
        .build()
        .await().atMost(atMost);
      
      Assertions.assertEquals(CommitResultStatus.OK, commit_2.getStatus());
      TestAsserts.assertLog(commit_2, 
      """
Tree changes:
A    new_super_folder/xyz/changed_filename.txt
D    root/xyz/xxx.txt
commit commit01
Author: john smith
Date: 2024-01-01 12:00:00 UTC

    file rename

Branch updated: main commit02..commit01
      """);
      
      { // pull all files
        final var result = fs
            .branchQuery().getOne()
            .await().atMost(atMost);
  
        final var blob = result.getTransitives().getBlobsById().values().iterator().next();
        Assertions.assertEquals(1, result.getTransitives().getTree().getTreeNodes().size());
        Assertions.assertEquals(1, result.getTransitives().getBlobsById().size());
        Assertions.assertEquals(2, blob.getBlobValue().size());
        Assertions.assertEquals("Lady Sybil", blob.getBlobValue().getString("firstName"));
        Assertions.assertEquals("Vimes", blob.getBlobValue().getString("lastName"));
        
        final var wholeDb = createState().withTenant(tenant)
            .onItem().transformToUni(t -> t.query().findAll())
            .await().atMost(atMost);
        Assertions.assertEquals(2, wholeDb.getBlob().size());
      }
    }
  }
}
