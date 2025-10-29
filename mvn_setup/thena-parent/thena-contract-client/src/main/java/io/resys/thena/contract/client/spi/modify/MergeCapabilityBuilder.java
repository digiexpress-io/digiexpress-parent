package io.resys.thena.contract.client.spi.modify;

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

import java.util.Map;

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractMergeObject.MergeCapability;
import io.resys.thena.contract.client.entities.Capability;
import io.resys.thena.contract.client.entities.ImmutableCapability;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.contract.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class MergeCapabilityBuilder implements MergeCapability {

  private final ContractCommitBuilder logger;
  private final ImmutablePersistenceUnit.Builder batch;
  private final Capability currentCapability; 
  private final ImmutableCapability.Builder nextCapability;
  private final Map<String, Capability> allCapabilities;
  private boolean built;

  public MergeCapabilityBuilder(
      ContractContainer container, 
      ContractCommitBuilder logger, 
      String contractId, 
      String capabilityId,
      ImmutablePersistenceUnit currentTx,
      @Nullable ContractContainer savedState) {
    super();
    this.logger = logger;
    this.batch = ImmutablePersistenceUnit.builder().tenantId(logger.getTenantId()).log("").status(BatchStatus.OK);
    this.currentCapability = container.getCapabilities().stream()
        .filter(c -> c.getId().equals(capabilityId))
        .findFirst()
        .orElse(null);
    RepoAssert.notNull(currentCapability, () -> "Can't find capability with id: '" + capabilityId + "' for contract: '" + contractId + "'!");
    this.nextCapability = ImmutableCapability.builder().from(currentCapability);
    this.allCapabilities = allCapabilities;
  }

  @Override
  public MergeCapability externalId(String externalId) {
    this.nextCapability.externalId(externalId);
    return this;
  }

  @Override
  public MergeCapability capabilityCode(String capabilityCode) {
    this.nextCapability.capabilityCode(capabilityCode);
    return this;
  }

  @Override
  public MergeCapability capabilityName(String capabilityName) {
    this.nextCapability.capabilityName(capabilityName);
    return this;
  }

  @Override
  public MergeCapability capabilityType(String capabilityType) {
    this.nextCapability.capabilityType(capabilityType);
    return this;
  }

  @Override
  public MergeCapability capabilityEnabled(Boolean capabilityEnabled) {
    this.nextCapability.capabilityEnabled(capabilityEnabled);
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public ImmutablePersistenceUnit close() {
    RepoAssert.isTrue(built, () -> "you must call MergeCapability.build() to finalize capability MERGE!");
    
    var nextCapability = this.nextCapability.build();
    final var isModified = !nextCapability.equals(currentCapability);
    if(isModified) {
      nextCapability = ImmutableCapability.builder()
          .from(nextCapability)
          .commitId(this.logger.getCommitId())
          .build();
      logger.merge(currentCapability, nextCapability);
      batch.addCapabilityInserts(nextCapability);
    }
    return batch.build();
  }
}