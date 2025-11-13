package io.digiexpress.eveli.client.spi.batch.contract_db;

/*-
 * #%L
 * eveli-client
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

import io.digiexpress.eveli.client.spi.batch.contract_db.BatchJob_DROP_DB_Contract.DropDbConfig;
import io.digiexpress.thena.batch.client.api.executor.Executor;
import io.digiexpress.thena.batch.client.api.executor.ExecutorConfig;
import io.digiexpress.thena.batch.client.api.executor.ExecutorContext;
import io.digiexpress.thena.batch.client.api.executor.ExecutorEntity;
import io.digiexpress.thena.batch.client.api.executor.ExecutorQuery;
import io.digiexpress.thena.batch.client.api.executor.ExecutorResult;
import io.digiexpress.thena.batch.client.api.executor.ImmutableExecutorEntity;
import io.digiexpress.thena.batch.client.api.executor.ImmutableExecutorResult;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.contract.client.api.ContractClient;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchJob_DROP_DB_Contract implements Executor<Tenant, DropDbConfig> {

  private final ContractClient contractClient;

  @Override
  public ExecutorQuery<Tenant, DropDbConfig> before(ExecutorContext context) {
    return new ExecutorQuery<Tenant, DropDbConfig>() {
      @Override
      public DropDbConfig getConfig() {
        return new DropDbConfig();
      }
      @Override
      public Multi<Tenant> findAll() {
        final var tenantId = contractClient.withTenant().getTenantId();
        return contractClient.tenants().find().id(tenantId).findAll();
      }
    };
  }

  @Override
  public Uni<ExecutorEntity> accept(Tenant tenant, DropDbConfig config, ExecutorContext context) {
    if(tenant.getType() != StructureType.contract) {
      return Uni.createFrom().item(ImmutableExecutorEntity.builder()
              .status(ExecutorEntity.ExecutorEntityStatus.SKIP)
              .entityId(tenant.getName())
              .build());
    }
    return contractClient.tenants().delete()
        .onItem().transform(resp -> ImmutableExecutorEntity.builder()
            .status(ExecutorEntity.ExecutorEntityStatus.OK)
            .entityId(tenant.getName())
            .build());
  }

  @Override
  public Uni<ExecutorResult> after(DropDbConfig config, ExecutorContext context) {
    return Uni.createFrom().item(ImmutableExecutorResult.builder()
        .status(ExecutorResult.ExecutorStatus.OK)
        .build());
  }

  @RequiredArgsConstructor
  @Data
  public static class DropDbConfig implements ExecutorConfig {
    private static final long serialVersionUID = 7079554536966522627L;
  }
}
