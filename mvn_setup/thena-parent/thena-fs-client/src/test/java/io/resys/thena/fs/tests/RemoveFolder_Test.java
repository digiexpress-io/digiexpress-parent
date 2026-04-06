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
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class RemoveFolder_Test extends DbTestTemplate {
  

  
  @Test
  public void removeFolder() {

    final var tenant = "RMFolder_Test";
    final CreatedTenant repo = getClient().tenants()
        .createOneTenant()
        .name(tenant, StructureType.fs)
        .buildOnlyIfNotCreated()
        .await().atMost(Duration.ofMinutes(1)).getItem2();
    
    log.debug("created repo {}", repo);
    Assertions.assertEquals(TenantOperationStatus.OK, repo.getStatus());
    wipeRepo(repo.getRepo());

    
    final var fs = getClient().withTenant(tenant);


     // create folder
    var commit = fs
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
    Assertions.assertEquals(CommitResultStatus.OK, commit.getStatus());
    
    var wholeDb = createState().withTenant(tenant)
        .onItem().transformToUni(t -> t.query().findAll())
        .await().atMost(atMost);
    Assertions.assertEquals(0, wholeDb.getBlob().size());
    Assertions.assertEquals(1, wholeDb.getProps().size());
    Assertions.assertEquals(1, wholeDb.getTree().size());
    
    // add 3 files
    commit = fs
      .commitBuilder()
      .commitAuthor("john smith")
      .commitMessage("add 3 files")
      
      .newFile((newFile) -> newFile
          .fileName("file1.txt")
          .filePath("root/xyz/xxx")
          .fileType("text")
          .fileValue(JsonObject.of("firstName", "Sam", "lastName", "Vimes"))
          .build())
      .newFile((newFile) -> newFile
          .fileName("file2.txt")
          .filePath("root/xyz/xxx")
          .fileType("text")
          .fileValue(JsonObject.of("firstName", "Sam", "lastName", "Vimes"))
          .build())
      .newFile((newFile) -> newFile
          .fileName("file3.txt")
          .filePath("root/xyz/xxx")
          .fileType("text")
          .fileValue(JsonObject.of("firstName", "Sam", "lastName", "Vimes"))
          .build())
      .build()
      .await().atMost(atMost);
    Assertions.assertEquals(CommitResultStatus.OK, commit.getStatus());
    
    wholeDb = createState().withTenant(tenant)
        .onItem().transformToUni(t -> t.query().findAll())
        .await().atMost(atMost);
    Assertions.assertEquals(1, wholeDb.getBlob().size());
    Assertions.assertEquals(1, wholeDb.getProps().size());
    Assertions.assertEquals(2, wholeDb.getTree().size());
    
    
    commit = fs
      .commitBuilder()
      .commitAuthor("john smith")
      .commitMessage("remove folder")
      .remove("root/xyz/xxx")
      .build()
      .await().atMost(atMost);
    Assertions.assertEquals(CommitResultStatus.OK, commit.getStatus());
  
    wholeDb = createState().withTenant(tenant)
        .onItem().transformToUni(t -> t.query().findAll())
        .await().atMost(atMost);
    Assertions.assertEquals(1, wholeDb.getBlob().size());
    Assertions.assertEquals(1, wholeDb.getProps().size());
    Assertions.assertEquals(3, wholeDb.getTree().size());
    
    final var lastCommit = wholeDb.getCommit().values().stream()
        .sorted((b, a) -> a.getCommitCreatedAt().compareTo(b.getCommitCreatedAt()))
        .findFirst().get();
    
    final var lastTree = wholeDb.getTree().get(lastCommit.getTreeId().toString());
    Assertions.assertEquals(
        "",
        String.join("\n", lastTree.getTreeNodes().stream().map(e -> e.getFullPath()).toList())
    );
  }
}
