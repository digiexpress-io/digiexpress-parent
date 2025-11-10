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

import java.time.Period;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewDateRelativity;
import io.resys.thena.contract.client.entities.ContractDateRelativity;
import io.resys.thena.contract.client.entities.ImmutableContractDateRelativity;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.contract.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class NewDateRelativityBuilder implements NewDateRelativity {
  private final ContractCommitBuilder logger;
  private final String contractId;
  private final Map<String, ContractDateRelativity> allDateRelativity;
  private final ImmutableContractDateRelativity.Builder next;
  private final String dateRelativityId;
  private boolean built;
  
  public NewDateRelativityBuilder(
      ContractCommitBuilder logger, 
      String contractId,
      ImmutablePersistenceUnit currentTx,
      @Nullable ContractContainer savedState
  ) {
    
    super();
    this.logger = logger;
    this.contractId = contractId;
    this.dateRelativityId = OidUtils.genUUID();
    this.next = ImmutableContractDateRelativity.builder()
        .id(dateRelativityId)
        .commitId(logger.getCommitId())
        .createdCommitId(logger.getCommitId())
        .contractId(contractId)
        .invPlanId(Optional.empty())
        .coverageId(Optional.empty())
        .partyId(Optional.empty())
        .paymentPlanId(Optional.empty())
        .offsetInterval(Optional.empty())
        .calculationRule(Optional.empty())
        .description(Optional.empty());
    
    final var updates = currentTx.getContractDateRelativityUpdates().stream().map(e -> e.getId()).toList();
    final var deletes = Collections.<String>emptyList(); // Date relativity doesn't support deletes
    
    this.allDateRelativity = Stream.of(
        // from current TX
        currentTx.getContractDateRelativityInserts().stream(),
        currentTx.getContractDateRelativityUpdates().stream(),
        
        // previously saved
        Optional.ofNullable(savedState)
          .map(saved -> saved.getDateRelativity())
          .orElse(Collections.emptyList())
          .stream()
          .filter(saved -> !deletes.contains(saved.getId()))
          .filter(saved -> !updates.contains(saved.getId()))
      )
      .flatMap(e -> e)
      .collect(Collectors.toMap(e -> e.getId(), e -> e));
  }

  @Override
  public NewDateRelativity invPlanId(@Nullable String invPlanId) {
    this.next.invPlanId(Optional.ofNullable(invPlanId));
    return this;
  }

  @Override
  public NewDateRelativity coverageId(@Nullable String coverageId) {
    this.next.coverageId(Optional.ofNullable(coverageId));
    return this;
  }

  @Override
  public NewDateRelativity partyId(@Nullable String partyId) {
    this.next.partyId(Optional.ofNullable(partyId));
    return this;
  }

  @Override
  public NewDateRelativity paymentPlanId(@Nullable String paymentPlanId) {
    this.next.paymentPlanId(Optional.ofNullable(paymentPlanId));
    return this;
  }

  @Override
  public NewDateRelativity entityType(String entityType) {
    this.next.entityType(entityType);
    return this;
  }

  @Override
  public NewDateRelativity fieldName(String fieldName) {
    this.next.fieldName(fieldName);
    return this;
  }

  @Override
  public NewDateRelativity relativeToType(String relativeToType) {
    this.next.relativeToType(relativeToType);
    return this;
  }

  @Override
  public NewDateRelativity offsetInterval(@Nullable Period offsetInterval) {
    this.next.offsetInterval(Optional.ofNullable(offsetInterval));
    return this;
  }

  @Override
  public NewDateRelativity calculationRule(@Nullable String calculationRule) {
    this.next.calculationRule(Optional.ofNullable(calculationRule));
    return this;
  }

  @Override
  public NewDateRelativity description(@Nullable String description) {
    this.next.description(Optional.ofNullable(description));
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public ImmutableContractDateRelativity close() {
    RepoAssert.isTrue(built, () -> "you must call NewDateRelativity.build() to finalize date relativity CREATE!");
    
    final var dateRelativity = next.build();
    
    this.logger.add(dateRelativity);
    
    return dateRelativity;
  }
}