package io.resys.thena.docdb.test;

/*-
 * #%L
 * thena-docdb-pgsql
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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
import java.time.LocalDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.api.actions.CommitActions.CommitResultEnvelope;
import io.resys.thena.api.actions.TenantActions.RepoResult;
import io.resys.thena.api.actions.TenantActions.RepoStatus;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.RepoType;
import io.resys.thena.docdb.test.config.DbTestTemplate;
import io.resys.thena.docdb.test.config.PgProfile;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

//-Djava.util.logging.manager=org.jboss.logmanager.LogManager

@Disabled
@Slf4j
@QuarkusTest
@TestProfile(PgProfile.class)
@SuppressWarnings("unused")
public class MetricsDBtest extends DbTestTemplate {

  //@org.junit.jupiter.api.Test
  public void metrics() {
    RepoResult repo = getClient().tenants().commit()
        .name("create repo for metrics", RepoType.git)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(RepoStatus.OK, repo.getStatus());
    
    // ~ millis 37282
    runInserts(repo, 500000);
    //runInserts(repo, 1000);
    //runInserts(repo, 1000);
    //runInserts(repo, 1000);
    
    // ~ 2858
    select(repo);
    select(repo);
    select(repo);
    select(repo);
  }
  
  private void select(RepoResult repo) {
    final var start = System.currentTimeMillis();
    final var repoState = getClient().tenants().find().id(repo.getRepo().getId()).get().await().atMost(Duration.ofMinutes(1));
    
    final var blobs = getClient().git(repoState).branch()
            .branchQuery()
            .branchName("main")
            .docsIncluded()
            .get()
        
        .await().atMost(Duration.ofMinutes(1));
    final var end = System.currentTimeMillis();
    
    log.debug("total time for selecting: {} entries is: {} millis", blobs.getObjects().getBlobs().size(), end-start);
  }
  
  
  private void runInserts(RepoResult repo, int total) {
    
    final var builder = getClient().git(repo).commit().commitBuilder().latestCommit()
      .branchName("main")
      .author("same vimes")
      .message("Commiting!");

    var start = System.currentTimeMillis();
    final var now = LocalDateTime.now().toString();
    for(int index = 0; index < total; index++) {
      builder.append(now + "-" + index + "-readme.md", JsonObject.of("value", now + "-" + index + "readme content"));
    }
    var end = System.currentTimeMillis();
    final var loopTime = end - start;
    
    start = System.currentTimeMillis();
    CommitResultEnvelope commit_0 = builder.build()
      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
      .await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals(Tenant.CommitResultStatus.OK, commit_0.getStatus());
    end = System.currentTimeMillis();
    log.debug("total time for inserting: {} entries is: {} millis, loop time: {}", total, end-start, loopTime);
  }
}
