package io.digiexpress.thena.batch.client.test;

/*-
 * #%L
 * thena-batch-client
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.digiexpress.thena.batch.client.test.config.DbTestTemplate;
import io.resys.thena.api.actions.TenantActions.TenantOperationStatus;
import io.resys.thena.api.actions.TenantActions.CreatedTenant;
import lombok.extern.slf4j.Slf4j;



@Slf4j
public class CreateDropDbTest extends DbTestTemplate {


  @Test
  public void createDropDb() {
    
    CreatedTenant repo = getClient().manageTenants().createOneTenant()
        .name("my-batch-tenant")
        .build()
        .await().atMost(atMost);
    log.debug("created batch tenant {}", repo);
    
    
    getClient().manageTenants().queryTenants().id(repo.getRepo().getId()).deleteOne().await().atMost(atMost);
    Assertions.assertEquals(TenantOperationStatus.OK, repo.getStatus());
  }
}
