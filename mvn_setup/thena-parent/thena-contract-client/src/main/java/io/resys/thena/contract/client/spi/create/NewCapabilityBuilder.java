package io.resys.thena.contract.client.spi.create;

import java.util.Collections;

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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewCapability;
import io.resys.thena.contract.client.entities.Capability;
import io.resys.thena.contract.client.entities.ImmutableCapability;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.contract.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class NewCapabilityBuilder implements NewCapability {
  private final ContractCommitBuilder logger;
  private final String contractId;
  private final Map<String, Capability> allCapabilities;
  private final ImmutableCapability.Builder next;
  private boolean built;
  
  public NewCapabilityBuilder(
      ContractCommitBuilder logger, 
      String contractId, 
      
      ImmutablePersistenceUnit currentTx,
      @Nullable ContractContainer savedState) {
    
    super();
    this.logger = logger;
    this.contractId = contractId;
    this.next = ImmutableCapability.builder()
        .id(OidUtils.gen())
        .commitId(logger.getCommitId())
        .createdCommitId(logger.getCommitId())
        .contractId(contractId)
        .externalId(Optional.empty());
    
    final var updates = currentTx.getCapabilityUpdates().stream().map(e -> e.getId()).toList();
    final var deletes = currentTx.getCapabilityDeletes().stream().map(e -> e.getId()).toList();
    
    this.allCapabilities = Stream.of(
        
        // from current TX
        currentTx.getCapabilityInserts().stream(),
        currentTx.getCapabilityUpdates().stream(),
        
        // previously saved
        Optional.ofNullable(savedState)
          .map(saved -> saved.getCapabilities())
          .orElse(Collections.emptyList())
          .stream()
          .filter(saved -> !updates.contains(saved.getId()))
          .filter(saved -> !deletes.contains(saved.getId()))
      )
      .flatMap(e -> e)
      .collect(Collectors.toMap(e -> e.getId(), e -> e));
  }

  @Override
  public NewCapability externalId(@Nullable String externalId) {
    this.next.externalId(Optional.ofNullable(externalId));
    return this;
  }

  @Override
  public NewCapability capabilityCode(String capabilityCode) {
    this.next.capabilityCode(capabilityCode);
    return this;
  }

  @Override
  public NewCapability capabilityName(String capabilityName) {
    this.next.capabilityName(capabilityName);
    return this;
  }

  @Override
  public NewCapability capabilityType(String capabilityType) {
    this.next.capabilityType(capabilityType);
    return this;
  }

  @Override
  public NewCapability capabilityEnabled(Boolean capabilityEnabled) {
    this.next.capabilityEnabled(capabilityEnabled);
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public ImmutableCapability close() {
    RepoAssert.isTrue(built, () -> "you must call NewCapability.build() to finalize capability CREATE!");
    
    final var built = next.build();
    
    // Validate uniqueness - no duplicate capabilities with same code
    RepoAssert.isTrue(
        this.allCapabilities.values().stream()
        .filter(c -> c.getCapabilityCode().equals(built.getCapabilityCode()))
        .count() == 0
        , () -> "can't have duplicate capabilities with same code!");

    this.logger.add(built);
    return built;
  }
}