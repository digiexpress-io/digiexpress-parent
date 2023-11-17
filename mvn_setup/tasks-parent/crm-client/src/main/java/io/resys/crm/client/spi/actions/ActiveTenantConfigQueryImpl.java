package io.resys.crm.client.spi.actions;

/*-
 * #%L
 * thena-tasks-client
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

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import io.resys.crm.client.api.TenantConfigClient.ActiveTenantConfigQuery;
import io.resys.crm.client.api.model.TenantConfig;
import io.resys.crm.client.spi.store.DocumentStore;
import io.resys.crm.client.spi.visitors.DeleteAllTenantsVisitor;
import io.resys.crm.client.spi.visitors.FindAllTenantsVisitor;
import io.resys.crm.client.spi.visitors.GetActiveTenantVisitor;
import io.resys.crm.client.spi.visitors.GetTenantsByIdsVisitor;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ActiveTenantConfigQueryImpl implements ActiveTenantConfigQuery {
  private final DocumentStore ctx;
  
  @Override
  public Uni<TenantConfig> get(String id) {
    return ctx.getConfig().accept(new GetActiveTenantVisitor(id));
  }
  
  @Override
  public Uni<List<TenantConfig>> findAll() {
    return ctx.getConfig().accept(new FindAllTenantsVisitor());
  }

  @Override
  public Uni<List<TenantConfig>> deleteAll(String userId, Instant targetDate) {
    return ctx.getConfig().accept(new DeleteAllTenantsVisitor(userId, targetDate))
        .onItem().transformToUni(unwrap -> unwrap);
  }
  
  @Override
  public Uni<List<TenantConfig>> findByIds(Collection<String> taskIds) {
    return ctx.getConfig().accept(new GetTenantsByIdsVisitor(taskIds));
  }
}
