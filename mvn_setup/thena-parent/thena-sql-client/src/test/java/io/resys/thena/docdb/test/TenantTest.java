package io.resys.thena.docdb.test;

import java.util.Arrays;
import java.util.Map;

/*-
 * #%L
 * thena-sql-client
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

import org.junit.jupiter.api.Test;

import io.resys.thena.api.entities.ImmutableAliasConfig;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.vertx.core.json.JsonObject;

public class TenantTest extends DbTestTemplate {

  @SuppressWarnings("unused")
  @Test
  public void createTenantTest() {
    final var client = getClient();
    
    final var tenant = client.createOneTenant()
      .name("test tenant", StructureType.fs)
      .build().await().atMost(atMost)
      .getRepo();
    
    final var tenant_alias = client.createOneTenant()
        .name("alias for tenant", StructureType.fs)
        .build().await().atMost(atMost)
        .getRepo();
      
    
    final var alias = client.createOneAlias()
        .aliasDesc("super alias")
        .aliasName("group_x")
        .author("sam vimes")
        .refTenantId(tenant.getId())
        .aliasTenantId(tenant_alias.getId())
        .aliasConfig(Arrays.asList(ImmutableAliasConfig.builder()
            .configBody(new JsonObject(Map.of("first", "Sam")))
            .configType("super_types")
            .build()))
        .build()
        .await().atMost(atMost);
    
    final var aliasQuery = client.queryAliases().findAll()
      .collect().asList()
      .await().atMost(atMost);
      
    
    final var member = client.createOneMember()
      .aliasId(alias.getId())
      .externalId("sam vimes")
      .aliasStatus(true)
      .build()
      .await().atMost(atMost);
    
    
    final var member_mod = client
      .modifyOneMember()
      .memberId(member.getId())
      .aliasStatus(true)
      .build()
      .await().atMost(atMost);
  }
  
}
