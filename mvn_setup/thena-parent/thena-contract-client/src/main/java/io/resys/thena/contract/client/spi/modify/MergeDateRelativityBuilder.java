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

import java.time.Period;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractMergeObject.MergeDateRelativity;
import io.resys.thena.contract.client.entities.ContractDateRelativity;
import io.resys.thena.contract.client.entities.ImmutableContractDateRelativity;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.contract.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class MergeDateRelativityBuilder implements MergeDateRelativity {

  private final ContractCommitBuilder logger;
  private final ImmutablePersistenceUnit.Builder batch;
  private final ContractDateRelativity currentDateRelativity; 
  private final ImmutableContractDateRelativity.Builder nextDateRelativity;
  private final Map<String, ContractDateRelativity> allDateRelativity;
  private boolean built;

  public MergeDateRelativityBuilder(
      ContractContainer container, ContractCommitBuilder logger, 
      String contractId, String dateRelativityId, 
      ImmutablePersistenceUnit currentTx,
      @Nullable ContractContainer savedState) {
    super();
    this.logger = logger;
    this.batch = ImmutablePersistenceUnit.builder().tenantId(logger.getTenantId()).log("").status(BatchStatus.OK);
    this.currentDateRelativity = container.getDateRelativity().stream()
        .filter(dr -> dr.getId().equals(dateRelativityId))
        .findFirst()
        .orElse(null);
    RepoAssert.notNull(currentDateRelativity, () -> "Can't find date relativity with id: '" + dateRelativityId + "' for contract: '" + contractId + "'!");
    this.nextDateRelativity = ImmutableContractDateRelativity.builder().from(currentDateRelativity);

    
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
  public MergeDateRelativity invPlanId(@Nullable String invPlanId) {
    this.nextDateRelativity.invPlanId(Optional.ofNullable(invPlanId));
    return this;
  }

  @Override
  public MergeDateRelativity coverageId(@Nullable String coverageId) {
    this.nextDateRelativity.coverageId(Optional.ofNullable(coverageId));
    return this;
  }

  @Override
  public MergeDateRelativity partyId(@Nullable String partyId) {
    this.nextDateRelativity.partyId(Optional.ofNullable(partyId));
    return this;
  }

  @Override
  public MergeDateRelativity paymentPlanId(@Nullable String paymentPlanId) {
    this.nextDateRelativity.paymentPlanId(Optional.ofNullable(paymentPlanId));
    return this;
  }

  @Override
  public MergeDateRelativity entityType(String entityType) {
    this.nextDateRelativity.entityType(entityType);
    return this;
  }

  @Override
  public MergeDateRelativity fieldName(String fieldName) {
    this.nextDateRelativity.fieldName(fieldName);
    return this;
  }

  @Override
  public MergeDateRelativity relativeToType(String relativeToType) {
    this.nextDateRelativity.relativeToType(relativeToType);
    return this;
  }

  @Override
  public MergeDateRelativity offsetInterval(@Nullable Period offsetInterval) {
    this.nextDateRelativity.offsetInterval(Optional.ofNullable(offsetInterval));
    return this;
  }

  @Override
  public MergeDateRelativity calculationRule(@Nullable String calculationRule) {
    this.nextDateRelativity.calculationRule(Optional.ofNullable(calculationRule));
    return this;
  }

  @Override
  public MergeDateRelativity description(@Nullable String description) {
    this.nextDateRelativity.description(Optional.ofNullable(description));
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public ImmutablePersistenceUnit close() {
    RepoAssert.isTrue(built, () -> "you must call MergeDateRelativity.build() to finalize date relativity MERGE!");
    
    var nextDateRelativity = this.nextDateRelativity.build();
    final var isModified = !nextDateRelativity.equals(currentDateRelativity);
    if(isModified) {
      nextDateRelativity = ImmutableContractDateRelativity.builder()
          .from(nextDateRelativity)
          .commitId(this.logger.getCommitId())
          .build();
      logger.merge(currentDateRelativity, nextDateRelativity);
      batch.addContractDateRelativityUpdates(nextDateRelativity);
    }
    return batch.build();
  }
}