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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.contract.client.api.ImmutableContractContainer;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractNewObject;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewCapability;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewContract;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewCoverage;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewInvPlan;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewNote;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewParty;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewPaymentPlan;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewReference;
import io.resys.thena.contract.client.entities.ImmutableContract;
import io.resys.thena.contract.client.entities.ImmutableContractTransitives;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.contract.client.tables.ContractDbBuilder.PersistenceUnit;
import io.resys.thena.contract.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;



public class NewContractBuilder implements ThenaContractNewObject.NewContract {
  private final ContractCommitBuilder logger;
  private final ImmutableContract.Builder contract;
  private final String contractId;
  private final String commitId;
  private final OffsetDateTime createdAt;
  
  private ImmutablePersistenceUnit.Builder next;
  private Consumer<ContractContainer> handleNewState;
  private boolean built;
  
  public NewContractBuilder(ContractCommitBuilder logger, String contractNumber) {
    super();
    this.next = ImmutablePersistenceUnit.builder()
        .tenantId(logger.getTenantId())
        .status(BatchStatus.OK)
        .log("");

    this.createdAt = logger.getCreatedAt();
    this.commitId = logger.getCommitId();
    this.contractId = OidUtils.genUUID();
    this.contract = ImmutableContract.builder()
        .id(contractId)
        .commitId(commitId)
        .updatedTreeCommitId(commitId)
        .createdCommitId(commitId)
        .contractNumber(contractNumber)
        .parentContractId(Optional.empty())
        .externalId(Optional.empty())
        .contractMaturityDate(Optional.empty())
        .contractSubStatus(Optional.empty())
        .contractSubType(Optional.empty())
        .contractData(Optional.empty());
        
    this.logger = logger;
  }
  
  @Override
  public NewContract contractNumber(String contractNumber) {
    this.contract.contractNumber(contractNumber);
    return this;
  }

  @Override
  public NewContract parentContractId(@Nullable String parentContractId) {
    this.contract.parentContractId(Optional.ofNullable(parentContractId));
    return this;
  }

  @Override
  public NewContract externalId(@Nullable String externalId) {
    this.contract.externalId(Optional.ofNullable(externalId));
    return this;
  }

  @Override
  public NewContract contractIssueDate(LocalDate contractIssueDate) {
    this.contract.contractIssueDate(contractIssueDate);
    return this;
  }


  @Override
  public NewContract contractStartDate(LocalDate contractStartDate) {
    this.contract.contractStartDate(contractStartDate);
    return this;
  }


  @Override
  public NewContract contractMaturityDate(@Nullable LocalDate contractMaturityDate) {
    this.contract.contractMaturityDate(Optional.ofNullable(contractMaturityDate));
    return this;
  }


  @Override
  public NewContract contractStatus(String contractStatus) {
    this.contract.contractStatus(contractStatus);
    return this;
  }

  @Override
  public NewContract contractSubStatus(@Nullable String contractSubStatus) {
    this.contract.contractSubStatus(Optional.ofNullable(contractSubStatus));
    return this;
  }

  @Override
  public NewContract contractType(String contractType) {
    this.contract.contractType(contractType);
    return this;
  }

  @Override
  public NewContract contractSubType(@Nullable String contractSubType) {
    this.contract.contractSubType(Optional.ofNullable(contractSubType));
    return this;
  }

  @Override
  public NewContract contractData(@Nullable JsonObject contractData) {
    this.contract.contractData(Optional.ofNullable(contractData));
    return this;
  }

  @Override
  public NewContract addParty(Consumer<NewParty> party) {
    final var allParties = this.next.build();
    final var builder = new NewPartyBuilder(logger, contractId, allParties, null);
    party.accept(builder);
    final var built = builder.close();
    this.next.addPartyInserts(built);
    return this;
  }

  @Override
  public NewContract addCoverage(Consumer<NewCoverage> coverage) {
    final var allCoverages = this.next.build();
    final var builder = new NewCoverageBuilder(logger, contractId, allCoverages, null);
    coverage.accept(builder);
    final var built = builder.close();
    this.next.addCoverageInserts(built);
    return this;
  }

  @Override
  public NewContract addReference(Consumer<NewReference> reference) {
    final var allReferences = this.next.build();
    final var builder = new NewReferenceBuilder(logger, contractId, null, allReferences, null);
    reference.accept(builder);
    final var built = builder.close();
    this.next.addReferenceInserts(built);
    return this;
  }

  @Override
  public NewContract addNote(Consumer<NewNote> note) {
    final var allNotes = this.next.build();
    final var builder = new NewNoteBuilder(logger, contractId, null, allNotes, null);
    note.accept(builder);
    final var built = builder.close();
    this.next.addNoteInserts(built);
    return this;
  }

  @Override
  public NewContract addCapability(Consumer<NewCapability> capability) {
    final var allCapabilities = this.next.build();
    final var builder = new NewCapabilityBuilder(logger, contractId, allCapabilities, null);
    capability.accept(builder);
    final var built = builder.close();
    this.next.addCapabilityInserts(built);
    return this;
  }

  @Override
  public NewContract addInvPlan(Consumer<NewInvPlan> invPlan) {
    final var builder = new NewInvPlanBuilder(logger, contractId, this.next.build(), null);
    invPlan.accept(builder);
    final var built = builder.close();
    this.next.from(built);
    return this;
  }

  @Override
  public NewContract addPaymentPlan(Consumer<NewPaymentPlan> paymentPlan) {
    final var allPaymentPlans = this.next.build();
    final var builder = new NewPaymentPlanBuilder(logger, contractId, allPaymentPlans, null);
    paymentPlan.accept(builder);
    final var built = builder.close();
    this.next.addPaymentPlanInserts(built);
    return this;
  }

  @Override
  public NewContract onNewState(Consumer<ContractContainer> handleNewState) {
    this.handleNewState = handleNewState;
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public PersistenceUnit close() {
    RepoAssert.isTrue(built, () -> "you must call NewContract.build() to finalize contract CREATE!");

    final var contract = this.contract
        .transitives(ImmutableContractTransitives.builder()
            .createdAt(createdAt)
            .updatedAt(createdAt)
            .updatedTreeAt(createdAt)
            .build())
        .build();
    
    logger.add(contract);
    
    next.addContractInserts(contract);
    final var batch = next.build();
    
    onNewState(batch);
    
    return batch;
  }
  
  private void onNewState(PersistenceUnit batch) {
    if(handleNewState == null) {
      return;
    }
    final var contract = batch.getContractInserts().iterator().next();
    final var container = ImmutableContractContainer.builder()
        .contract(contract)
        .parties(batch.getPartyInserts())
        .coverages(batch.getCoverageInserts())
        .references(batch.getReferenceInserts())
        .notes(batch.getNoteInserts())
        .capabilities(batch.getCapabilityInserts())
        .invPlans(batch.getInvPlanInserts())
        .paymentPlans(batch.getPaymentPlanInserts())
        .invPlanAllocations(batch.getInvPlanAllocInserts().stream()
            .collect(Collectors.groupingBy(alloc -> alloc.getInvPlanId())))
        .build();
    
    handleNewState.accept(container);
  }
}
