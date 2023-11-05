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

import java.io.Serializable;
import java.time.Duration;

import org.immutables.value.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.docdb.api.actions.RepoActions.RepoResult;
import io.resys.thena.docdb.api.actions.RepoActions.RepoStatus;
import io.resys.thena.docdb.api.models.Repo.RepoType;
import io.resys.thena.docdb.test.config.DbTestTemplate;
import io.resys.thena.docdb.test.config.PgProfile;
import io.vertx.core.json.JsonObject;


@QuarkusTest
@TestProfile(PgProfile.class)
public class SimpleDocTest extends DbTestTemplate {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleDocTest.class);
  
  @Value.Immutable
  public interface TestContent extends Serializable {
    String getId();
    String getName();
  }

  @Test
  public void crateRepoAddAndDeleteFile() {
    // create project
    RepoResult repo = getClient().repo().projectBuilder()
        .name("SimpleDocTest-1", RepoType.doc)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    LOGGER.debug("created repo {}", repo);
    Assertions.assertEquals(RepoStatus.OK, repo.getStatus());
    
    getClient().doc().append()
      .appendBuilder()
      .repoId(repo.getRepo().getId())
      .externalId("bobs-ssn-id")
      .branchName("main")
      .append(JsonObject.of("first_name", "bob", "last_name", "flop"))
      .message("created first entry")
      .log(JsonObject.of("some_cool_command", "create_customer"))
      .author("jane.doe@morgue.com")
    .build().await().atMost(Duration.ofMinutes(1));
    
  }
}
