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
import java.util.ArrayList;

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
public class CreateTag_Test extends DbTestTemplate {
  
  @Test
  public void createTag() {

    final var tenant = "CreateTag_Test";
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
          .fileProps(props -> props.propsComments(JsonObject.of("comment 1", "very very good first file")).build())
          .build())
      .build()
      .await().atMost(atMost);
    Assertions.assertEquals(CommitResultStatus.OK, commit_1.getStatus());
  
  
    
    // commit 2, change first name
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
  
  
    // commit 3, change path and name
    final var commit_3 = fs
      .commitBuilder()
      .commitAuthor("john smith")
      .commitMessage("file rename")
      .mergeFile("root/xyz/xxx.txt", (prev, newFile) -> newFile
          .filePath("new_super_folder/xyz")
          .fileName("changed_filename.txt")
          .build())
      .build()
      .await().atMost(atMost);
    
    Assertions.assertEquals(CommitResultStatus.OK, commit_3.getStatus());

    
    {
      final var tag_0 = fs.createTag()
        .commitId(commit_1.getCommit().getId().toString())
        .tagAuthor("john doe")
        .newTag(newTag -> newTag.tagName("super-tag-0").build())
        .build()
        .await().atMost(atMost);
      
      Assertions.assertEquals(commit_1.getCommit().getId(), tag_0.getTag().getCommitId());
    }
    
    
    {
      final var tag_1 = fs.createTag()
        .commitId("main")
        .tagAuthor("john doe")
        .newTag(newTag -> newTag.tagName("super-tag-1").build())
        .build()
        .await().atMost(atMost);
      
      Assertions.assertEquals(commit_3.getCommit().getId(), tag_1.getTag().getCommitId());
    }
    
    {
      final var tag_2 = fs.createTag()
        .commitId(commit_2.getCommit().getId())
        .tagAuthor("john doe")
        .newTag(newTag -> newTag.tagName("super-tag-2").build())
        .build()
        .await().atMost(atMost);
      Assertions.assertEquals(commit_2.getCommit().getId(), tag_2.getTag().getCommitId());
    }
    
    
    {
      final var tags = fs.tagQuery().findAll().collect().asList().await().atMost(atMost);
      Assertions.assertEquals(3, tags.size());
    }
    
    
    
    // QUERY tag and validate blobs
    {
      final var tag_1 = fs.tagQuery()
          .tagName(name -> name.equals("super-tag-0"))
          .getOne()
          .await().atMost(atMost);
      
      
      Assertions.assertNotNull(tag_1.getTransitives().getCommit());
      Assertions.assertNotNull(tag_1.getTransitives().getTree());
      
      Assertions.assertEquals(commit_1.getCommit().getId(), tag_1.getTransitives().getCommit().getId());
      Assertions.assertEquals(1, tag_1.getTransitives().getTree().getTreeNodes().size());
      Assertions.assertEquals("Sam", new ArrayList<>(tag_1.getTransitives().getTree().getTreeNodes()).getFirst().getTransitives().getBlob().getBlobValue().getString("firstName")); 
    }
    
    
    
    // QUERY tag and validate blobs
    {
      final var tag_1 = fs.tagQuery()
          .tagName(name -> name.equals("super-tag-1"))
          .getOne()
          .await().atMost(atMost);
      
      
      Assertions.assertNotNull(tag_1.getTransitives().getCommit());
      Assertions.assertNotNull(tag_1.getTransitives().getTree());
      
      Assertions.assertEquals(commit_3.getCommit().getId(), tag_1.getTransitives().getCommit().getId());
      Assertions.assertEquals(1, tag_1.getTransitives().getTree().getTreeNodes().size());
      Assertions.assertEquals("Lady Sybil", new ArrayList<>(tag_1.getTransitives().getTree().getTreeNodes()).getFirst().getTransitives().getBlob().getBlobValue().getString("firstName")); 
    }
  }
}
