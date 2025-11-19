package io.resys.lp.product.test;

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

import org.junit.jupiter.api.Test;

import io.resys.lp.product.spi.providers.Contract_Provider;
import io.resys.lp.product.test.config.DbTestTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Contract_CRUD_Test extends DbTestTemplate {

  private static String TENANT_ID = "LP_CRUD_Test";  
  
  @Test
  public void genPoliciesTest() {
    final var client = super.createClient(TENANT_ID);
    Contract_Provider.newSavings(client, null, "001").await().atMost(atMost);
    Contract_Provider.newPension(client, null, "002").await().atMost(atMost);
    Contract_Provider.newPS(client, null, "003").await().atMost(atMost);
    Contract_Provider.newNovaVirtus(client, null, "004").await().atMost(atMost);
    
    client.withTenant().find().contractQuery().findAll().await().atMost(atMost);
  }
}
