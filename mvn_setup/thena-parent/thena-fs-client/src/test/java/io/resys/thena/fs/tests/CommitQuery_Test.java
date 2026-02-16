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
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.thena.api.actions.TenantActions.CreatedTenant;
import io.resys.thena.api.actions.TenantActions.TenantOperationStatus;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.envelope.CommitResultStatus;
import io.resys.thena.fs.entities.Commit;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.fs.tests.config.DbTestTemplate;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class CommitQuery_Test extends DbTestTemplate {
  private final String tenant = "CommitQuery_Test";  


  @Test
  public void create4branchesAndQueryCommits() {
    // creates main branch with one commit
    setup();
    
    // create dev from main branch
    final var devCommit = createBranch("dev", "main").getTransitives().getCommit();
    
    createBranch("issue-1", "dev").getTransitives().getCommit();
    createBranch("issue-2", "dev").getTransitives().getCommit();
    
    // create issue-1 branch    
    getClient().withTenant(tenant)
        .commitBuilder()
        .branchLock(devCommit.getId())
        .branchName("issue-1")
        .commitAuthor("john smith")
        .commitMessage("create main branch with some content")
        .newFile((newFile) -> newFile
            .fileName("xxx2.txt")
            .filePath("root/xyz")
            .fileType("text")
            .fileValue(JsonObject.of("firstName", "Sam", "lastName", "Vimes", "uuid", UUID.randomUUID().toString()))
            .fileProps(props -> props.propsComments(JsonObject.of("comment 1", "very very good first file")).build())
            .build())
        .build()
        .await().atMost(atMost);
    
    
    // create issue-2 branch
    getClient().withTenant(tenant)
        .commitBuilder()
        .branchLock(devCommit.getId())
        .branchName("issue-2")
        .commitAuthor("john smith")
        .commitMessage("added 2 files")
        .newFile((newFile) -> newFile
            .fileName("xxx2.txt")
            .filePath("root/xyz")
            .fileType("text")
            .fileValue(JsonObject.of("firstName", "Sam", "lastName", "Vimes", "uuid", UUID.randomUUID().toString()))
            .fileProps(props -> props.propsComments(JsonObject.of("comment 1", "very very good first file")).build())
            .build())
        .build()
        .await().atMost(atMost);
    
    final var commits = getClient().withTenant(tenant)
        .commitQuery()
        .branchName("issue-2")
        //.fileOrFolderId("root/xyz/xxx2.txt")
        .findAll()
        .collect().asList()
        .await().atMost(atMost);
    
    Assertions.assertEquals(4, commits.size());
    

    
  }
  
  

  
  public Commit setup() {
    final CreatedTenant repo = getClient().tenants()
        .createOneTenant()
        .name(tenant, StructureType.fs)
        .buildOnlyIfNotCreated()
        .await().atMost(Duration.ofMinutes(1)).getItem2();
    
    log.debug("created repo {}", repo);
    Assertions.assertEquals(TenantOperationStatus.OK, repo.getStatus());
    wipeRepo(repo.getRepo());
    
    return addCommit("main");
  }
  
  
  private Ref createBranch(String newBranchName, String createFrom) {
    final var result = getClient()
        .withTenant(tenant)
        .createBranch()
          .branchAuthor("john doe")
          .commitIdOrBranchName(createFrom)
          .newBranch(branch -> branch.branchName(newBranchName).build())
        .build()
        .await().atMost(Duration.ofMinutes(1));
    
    return result.getBranch();
  }
  
  private Commit addCommit(String branchName) {
    // Generate unique commit
    final var fs = getClient().withTenant(tenant);
    final var commit_1 = fs
      .commitBuilder()
      .branchName(branchName)
      .commitAuthor("john smith")
      .commitMessage("create main branch with some content")
      .newFile((newFile) -> newFile
          .fileName("xxx.txt")
          .filePath("root/xyz")
          .fileType("text")
          .fileValue(JsonObject.of("firstName", "Sam", "lastName", "Vimes", "uuid", UUID.randomUUID().toString()))
          .fileProps(props -> props.propsComments(JsonObject.of("comment 1", "very very good first file")).build())
          .build())
      .build()
      .await().atMost(atMost);
    Assertions.assertEquals(CommitResultStatus.OK, commit_1.getStatus());
    return commit_1.getCommit();
  }
}
