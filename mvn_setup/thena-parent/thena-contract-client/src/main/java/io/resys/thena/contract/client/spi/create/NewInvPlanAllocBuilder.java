package io.resys.thena.contract.client.spi.create;

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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewInvPlanAlloc;
import io.resys.thena.contract.client.entities.ImmutableInvPlanAlloc;
import io.resys.thena.contract.client.entities.InvPlanAlloc;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.contract.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class NewInvPlanAllocBuilder implements NewInvPlanAlloc {
  private final ContractCommitBuilder logger;
  private final String invPlanId;
  private final Map<String, InvPlanAlloc> allAllocations;
  private final ImmutableInvPlanAlloc.Builder next;
  private boolean built;
  
  public NewInvPlanAllocBuilder(
      ContractCommitBuilder logger, 
      String invPlanId,
      ImmutablePersistenceUnit currentTx,
      @Nullable ContractContainer savedState) {
    
    super();
    this.logger = logger;
    this.invPlanId = invPlanId;
    
    this.next = ImmutableInvPlanAlloc.builder()
        .id(OidUtils.genUUID())
        .commitId(logger.getCommitId())
        .createdCommitId(logger.getCommitId())
        .invPlanId(invPlanId);
    
    
    final var updates = currentTx.getInvPlanAllocUpdates().stream().map(e -> e.getId()).toList();
    final var deletes = currentTx.getInvPlanAllocDeletes().stream().map(e -> e.getId()).toList();
    
    this.allAllocations = Stream.of(
        // from current TX
        currentTx.getInvPlanAllocInserts().stream(),
        currentTx.getInvPlanAllocUpdates().stream(),
        
        // previously saved
        Optional.ofNullable(savedState)
          .map(saved -> saved.getInvPlanAllocations().get(invPlanId))
          .orElse(Collections.emptyList())
          .stream()
      )
      .flatMap(e -> e)
      .filter(saved -> invPlanId.equals(saved.getInvPlanId()))
      .filter(saved -> !deletes.contains(saved.getId()))
      .filter(saved -> !updates.contains(saved.getId()))
      .collect(Collectors.toMap(e -> e.getId(), e -> e));
  }

  @Override
  public NewInvPlanAlloc invPlanAllocCode(String invPlanAllocCode) {
    this.next.invPlanAllocCode(invPlanAllocCode);
    return this;
  }

  @Override
  public NewInvPlanAlloc invPlanAllocName(String invPlanAllocName) {
    this.next.invPlanAllocName(invPlanAllocName);
    return this;
  }

  @Override
  public NewInvPlanAlloc invPlanAllocPercentage(BigDecimal invPlanAllocPercentage) {
    this.next.invPlanAllocPercentage(invPlanAllocPercentage);
    return this;
  }

  @Override
  public NewInvPlanAlloc invPlanAllocStatus(String invPlanAllocStatus) {
    this.next.invPlanAllocStatus(invPlanAllocStatus);
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public ImmutableInvPlanAlloc close() {
    RepoAssert.isTrue(built, () -> "you must call NewInvPlanAlloc.build() to finalize allocation CREATE!");
    
    final var built = next.build();
    
    // Validate uniqueness - no duplicate allocations with same code for same investment plan
    RepoAssert.isTrue(
        this.allAllocations.values().stream()
        .filter(a -> a.getInvPlanId().equals(invPlanId))
        .filter(a -> a.getInvPlanAllocCode().equals(built.getInvPlanAllocCode()))
        .count() == 0
        , () -> "can't have duplicate allocations with same code for same investment plan!");

    this.logger.add(built);
    return built;
  }
}