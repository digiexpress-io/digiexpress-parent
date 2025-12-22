package io.resys.lp.batches.create_db;

/*-
 * #%L
 * lp-batches
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

import io.digiexpress.thena.batch.client.api.executor.Executor;
import io.digiexpress.thena.batch.client.api.executor.ExecutorConfig;
import io.digiexpress.thena.batch.client.api.executor.ExecutorContext;
import io.digiexpress.thena.batch.client.api.executor.ExecutorEntity;
import io.digiexpress.thena.batch.client.api.executor.ExecutorQuery;
import io.digiexpress.thena.batch.client.api.executor.ExecutorResult;
import io.digiexpress.thena.batch.client.api.executor.ImmutableExecutorEntity;
import io.digiexpress.thena.batch.client.api.executor.ImmutableExecutorResult;
import io.resys.lp.batches.create_db.BatchJob_CREATE_DB_Ledger.CreateDbConfig;
import io.resys.thena.ledger.client.api.LedgerClient;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchJob_CREATE_DB_Ledger implements Executor<String, CreateDbConfig> {

  private final LedgerClient ledgerClient;

  @Override
  public ExecutorQuery<String, CreateDbConfig> before(ExecutorContext context) {
    return new ExecutorQuery<String, CreateDbConfig>() {
      @Override
      public CreateDbConfig getConfig() {
        return new CreateDbConfig();
      }
      @Override
      public Multi<String> findAll() {
        return Multi.createFrom().item(ledgerClient.withTenant().getTenantId());
      }
    };
  }

  @Override
  public Uni<ExecutorEntity> accept(String tenantName, CreateDbConfig config, ExecutorContext context) {
    return ledgerClient
        .tenants().createOneTenant()
        .name(tenantName)
        .build()
        .onItem().transform(resp -> ImmutableExecutorEntity.builder()
            .status(ExecutorEntity.ExecutorEntityStatus.OK)
            .entityId(tenantName)
            .build());
        
  }

  @Override
  public Uni<ExecutorResult> after(CreateDbConfig config, ExecutorContext context) {
    return Uni.createFrom().item(ImmutableExecutorResult.builder()
        .status(ExecutorResult.ExecutorStatus.OK)
        .build());
  }

  @RequiredArgsConstructor
  @Data
  public static class CreateDbConfig implements ExecutorConfig {
    private static final long serialVersionUID = 7079554536966522627L;
  }
}
