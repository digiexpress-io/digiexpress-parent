package io.digiexpress.thena.batch.client.spi;

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

import io.digiexpress.thena.batch.client.api.BatchClient;
import io.digiexpress.thena.batch.client.api.persistence.BatchDb;
import io.digiexpress.thena.batch.client.spi.createbatchconfig.CreateBatchConfigImpl;
import io.digiexpress.thena.batch.client.spi.createoneruntimeinstance.CreateOneRuntimeInstanceImpl;
import io.resys.thena.api.actions.TenantActions;
import io.resys.thena.api.entities.ImmutableTenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.spi.TenantActionsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class BatchClientImpl implements BatchClient {
  private final BatchDb batchDb;
  
  @Override
  public TenantActions manageTenants() {
    return new TenantActionsImpl(batchDb, StructureType.batch);
  }
  
  @Override
  public BatchQuery queryBatches() {
    return new BatchQueryImpl(batchDb);
  }
  
  @Override
  public CreateBatchConfig createBatchConfig() {
    return new CreateBatchConfigImpl(batchDb);
  }

  @Override
  public BatchClient withTenant(String tenantId) {
    // create tenant that will be loaded later
    return new BatchClientImpl(batchDb.withTenant(ImmutableTenant.builder()
        .type(StructureType.batch)
        .name(tenantId)
        .id("")
        .rev("")
        .prefix("")
        .externalId(null)
        .build()));
  }

  @Override
  public CreateOneRuntimeInstance createOneRuntimeInstance() {
    return new CreateOneRuntimeInstanceImpl(batchDb);
  }
  @Override
  public CreateBatchEnvir createBatchEnvir() {
    return new CreateBatchEnvirImpl(batchDb);
  }
  @Override
  public RuntimeInstanceQuery queryRuntimeInstances() {
    return new RuntimeInstanceQueryImpl(batchDb);
  }

  @Override
  public RuntimeStepQuery queryRuntimeSteps() {
    return new RuntimeStepQueryImpl(batchDb);
  }
}
