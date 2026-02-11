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
public class MergeFolder_Test extends DbTestTemplate {
  

  
  @Test
  public void mergeOneFile() {

    final var tenant = "MergeF_Test";
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
      .newFolder((newFolder) -> newFolder
          .folderName("xxx")
          .folderPath("root/xyz")
          .folderProps(props -> props.propsComments(JsonObject.of("comment 1", "very very good first file")).build())
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
        .mergeFolder("root/xyz/xxx", (prev, mergeFolder) -> mergeFolder
            .folderPath("super good path/files")
            .folderName("new folder name")
            .build())
        .build()
        .await().atMost(atMost);
      
      Assertions.assertEquals(CommitResultStatus.OK, commit_2.getStatus());
      TestAsserts.assertLog(commit_2, 
      """
Tree changes:
A    super good path/files/new folder name
D    root/xyz/xxx
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
  
        
        Assertions.assertEquals(1, result.getTransitives().getTree().getTreeNodes().size());
        Assertions.assertEquals(0, result.getTransitives().getBlobsById().size());
      }
    }
    
    
    
    // commit 3, change path and name
    {
      final var commit_2 = fs
        .commitBuilder()
        .commitAuthor("john smith")
        .commitMessage("file rename")
        .mergeFolder("super good path/files/new folder name", (prev, mergeFolder) -> mergeFolder
            .folderPath("new_super_folder/xyz")
            .folderName("changed_filename")
            .build())
        .build()
        .await().atMost(atMost);
      
      Assertions.assertEquals(CommitResultStatus.OK, commit_2.getStatus());
      TestAsserts.assertLog(commit_2, 
      """
Tree changes:
A    new_super_folder/xyz/changed_filename
D    super good path/files/new folder name
commit commit01
Author: john smith
Date: 2024-01-01 12:00:00 UTC

    file rename

Branch updated: main commit02..commit01
      """);
      
      // pull all files
      final var result = fs
          .branchQuery().getOne()
          .await().atMost(atMost);

      Assertions.assertEquals(1, result.getTransitives().getTree().getTreeNodes().size());
      Assertions.assertEquals(0, result.getTransitives().getBlobsById().size());
      Assertions.assertEquals(1, result.getTransitives().getPropsById().size());
      
      
      final var props = result.getTransitives().getPropsById().values().iterator().next();
    
      Assertions.assertEquals(1, props.getPropsComments().get().size());
      Assertions.assertEquals("very very good first file", props.getPropsComments().get().getString("comment 1"));
      
      
      
      final var wholeDb = createState().withTenant(tenant)
          .onItem().transformToUni(t -> t.query().findAll())
          .await().atMost(atMost);
      Assertions.assertEquals(0, wholeDb.getBlob().size());
      Assertions.assertEquals(1, wholeDb.getProps().size());
      Assertions.assertEquals(3, wholeDb.getTree().size());
    }
  }
}
