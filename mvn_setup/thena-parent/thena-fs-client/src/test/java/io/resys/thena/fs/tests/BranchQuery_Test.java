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
import java.util.stream.Collectors;

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
public class BranchQuery_Test extends DbTestTemplate {
  private final String tenant = "BranchQuery_Test";  


  @Test
  public void createBranchesAndQueryThem() {
    // creates main branch with one commit
    final var mainCommit = setup();
    
    // create dev from main branch
    final var devCommit = addCommit("dev");

    // create issue-1 branch
    getClient().withTenant(tenant)
        .commitBuilder()
        .branchLock(devCommit.getId())
        .branchName("issue-1")
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
    
    
    // create issue-2 branch
    getClient().withTenant(tenant)
        .commitBuilder()
        .branchLock(devCommit.getId())
        .branchName("issue-2")
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
    
    final var branches = getClient().withTenant(tenant)
        .branchQuery().findAll()
        .collect().asList().await().atMost(atMost)
        .stream().collect(Collectors.toMap(e -> e.getRefName(), e -> e));
    
    Assertions.assertEquals(4, branches.size());
    
    final var main = branches.get("main");
    final var dev = branches.get("dev");
    final var issue_1 = branches.get("issue-1");
    final var issue_2 = branches.get("issue-2");
    
    Assertions.assertEquals(main.getCommitId(), dev.getTransitives().getCommit().getParentId().get());
    Assertions.assertEquals(dev.getCommitId(), issue_1.getCommitId());
    Assertions.assertEquals(dev.getCommitId(), issue_2.getCommitId());
    
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
  
  
  private Ref createBranch() {
    
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
