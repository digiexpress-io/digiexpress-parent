package io.resys.thena.contract.test;

/*-
 * #%L
 * thena-contract-client
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

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.api.actions.TenantActions.CommitStatus;
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import io.resys.thena.contract.test.config.DbTestTemplate;
import io.resys.thena.contract.test.config.PgProfile;
import lombok.extern.slf4j.Slf4j;

@QuarkusTest
@TestProfile(PgProfile.class)
@Slf4j
public class Contract_CRUD_Test extends DbTestTemplate {

  
  private static String TENANT_ID = "Contract_CRUD_Test";  
  
  
  
  
  @Test
  public void createTestData() {
    // create project
    TenantCommitResult repo = getClient().tenants().commit()
        .name(TENANT_ID)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(CommitStatus.OK, repo.getStatus());
  }
}
