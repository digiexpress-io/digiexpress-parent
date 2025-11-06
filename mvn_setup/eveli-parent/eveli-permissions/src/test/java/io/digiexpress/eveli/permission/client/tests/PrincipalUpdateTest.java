package io.digiexpress.eveli.permission.client.tests;

/*-
 * #%L
 * eveli-permissions
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
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.digiexpress.eveli.permission.client.api.PermissionClient;
import io.digiexpress.eveli.permission.client.api.model.ChangeType;
import io.digiexpress.eveli.permission.client.api.model.ImmutableChangePrincipalRoles;
import io.digiexpress.eveli.permission.client.tests.config.DbTestTemplate;
import io.digiexpress.eveli.permission.client.tests.config.GenerateTestData;
import io.resys.thena.api.entities.Tenant;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class PrincipalUpdateTest extends DbTestTemplate {

  @Disabled
  @Test
  public void getPrincipalAndAddRole() {
   
    final PermissionClient client = getClient().tenantQuery()
    .repoName("PrincipalUpdateTest-1")
    .create()
    .await().atMost(Duration.ofMinutes(1));

    final Tenant repo = client.getTenant().await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    new GenerateTestData(getDocDb()).populate(repo);
    
    List<String> roles = Arrays.asList("testitalo", "kakkulaari", "Lapiotehdas");
      
    final var updated = client.updatePrincipal().updateOne(ImmutableChangePrincipalRoles.builder()
      .id(null)
      .roles(roles)
      .changeType(ChangeType.ADD)
      .comment("New roles needed to access ABC")
      .build())
    .await().atMost(Duration.ofMinutes(5));
    
    log.debug("Updated principal: {}", updated);
    
  }
}
   

