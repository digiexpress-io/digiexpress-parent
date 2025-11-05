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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.digiexpress.eveli.permission.client.api.PermissionClient;
import io.digiexpress.eveli.permission.client.api.model.ImmutableChangeRoleName;
import io.digiexpress.eveli.permission.client.api.model.ImmutableCreateRole;
import io.digiexpress.eveli.permission.client.api.model.Principal.Role;
import io.digiexpress.eveli.permission.client.tests.config.DbTestTemplate;



public class RoleUpdateTest extends DbTestTemplate {
  
  public Role createRoleForTest(PermissionClient client) {
    return client.createRole().createOne(ImmutableCreateRole.builder()
        .comment("New role needed")
        .name("front-office-trainee")
        .description("temporary for 3 weeks")
        .build()).await().atMost(Duration.ofMinutes(1));
  }

  @Test
  public void getRoleAndUpdateName() {
 
    final PermissionClient client = getClient().tenantQuery()
      .repoName("RoleUpdateTest-1")
      .create()
      .await().atMost(Duration.ofMinutes(1));

    final var createdRole = createRoleForTest(client);
    
    final var updatedRole = client.updateRole().updateOne(ImmutableChangeRoleName.builder()
      .id(createdRole.getId())
      .name("The cool kids")
      .comment("This role is only for awesome people now")
      .build())
    .await().atMost(Duration.ofMinutes(1));
    
   Assertions.assertEquals("The cool kids", updatedRole.getName());
   Assertions.assertEquals("The cool kids", client.roleQuery().get(updatedRole.getId()).await().atMost(Duration.ofMinutes(1)).getName());

  }
}
