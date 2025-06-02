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
import io.digiexpress.eveli.permission.client.api.model.ImmutableChangePermissionName;
import io.digiexpress.eveli.permission.client.api.model.ImmutableCreatePermission;
import io.digiexpress.eveli.permission.client.api.model.Principal.Permission;
import io.digiexpress.eveli.permission.client.tests.config.DbTestTemplate;
import io.digiexpress.eveli.permission.client.tests.config.OrgPgProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(OrgPgProfile.class)
public class PermissionUpdateTest extends DbTestTemplate {
  
  public Permission createPermissionForUpdate(PermissionClient client) {
    return client.createPermission().createOne(ImmutableCreatePermission.builder()
        .comment("New permission for update")
        .name("DB-write")
        .description("For admins only!")
        .build()).await().atMost(Duration.ofMinutes(1));
  }

  @Test
  public void getPermissionAndUpdateName() {
    final PermissionClient client = getClient().tenantQuery()
      .repoName("PermissionUpdateTest-1")
      .create()
      .await().atMost(Duration.ofMinutes(1));

    final var createdPermission = createPermissionForUpdate(client);
    
    final var updatedPermission = client.updatePermission().updateOne(ImmutableChangePermissionName.builder()
      .id(createdPermission.getId())
      .name("SUPER USER AND MANAGER")
      .comment("Changed permission name for reasons")
      .build())
    .await().atMost(Duration.ofMinutes(5));
    
 
    Assertions.assertEquals("SUPER USER AND MANAGER", client.permissionQuery().get(updatedPermission.getId()).await().atMost(Duration.ofMinutes(1)).getName());
  }
}
