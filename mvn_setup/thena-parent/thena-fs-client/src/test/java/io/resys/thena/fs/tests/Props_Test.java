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
public class Props_Test extends DbTestTemplate {
  
  @Test
  public void create2FileWith2Commits() {

    final var tenant = "Props_Test";
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
          .fileProps(props -> props.propsDescription("very good description").build())
          .build())
      .build()
      .await().atMost(atMost);
  
    Assertions.assertEquals(CommitResultStatus.OK, commit_1.getStatus());
    
    final var branch = fs.branchQuery().findOne().await().atMost(atMost).get();
    final var props = branch.getTransitives().getPropsById().values().stream().findFirst().get();
    Assertions.assertEquals("very good description", props.getPropsDescription().get());
  }
}
