package io.resys.thena.contract.client.api;

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
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewCapability;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewCommand;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewCoverage;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewDateRelativity;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewInvPlan;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewInvPlanAlloc;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewNote;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewParty;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewPaymentPlan;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewReference;
import io.resys.thena.contract.client.entities.ContractEntity.ContractOneOfRelations;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

// Generic interfaces for create/update/delete operations 
public interface ThenaContractMergeObject {

  interface MergeContract {
    // State access
    MergeContract onCurrentState(Consumer<ContractContainer> handleCurrentState);
    ContractContainer getCurrentState();
    
    // Main contract fields
    MergeContract contractNumber(String contractNumber);
    MergeContract parentContractId(@Nullable String parentContractId);
    MergeContract externalId(@Nullable String externalId);
    
    // Business dates
    MergeContract contractIssueDate(LocalDate contractIssueDate);
    MergeContract contractStartDate(LocalDate contractStartDate);
    MergeContract contractMaturityDate(@Nullable LocalDate contractMaturityDate);
    
    // Status and type
    MergeContract contractStatus(String contractStatus);
    MergeContract contractSubStatus(@Nullable String contractSubStatus);
    MergeContract contractType(String contractType);
    MergeContract contractSubType(@Nullable String contractSubType);
    MergeContract contractData(@Nullable JsonObject contractData);
    
    // Collection bulk replacement operations
    <T> MergeContract setAllParties(String partyType, List<T> replacements, Function<T, Consumer<NewParty>> party);
    <T> MergeContract setAllCoverages(String coverageType, List<T> replacements, Function<T, Consumer<NewCoverage>> coverage);
    <T> MergeContract setAllReferences(String referenceType, List<T> replacements, Function<T, Consumer<NewReference>> reference);
    <T> MergeContract setAllNotes(String noteType, List<T> replacements, Function<T, Consumer<NewNote>> note);
    <T> MergeContract setAllCapabilities(List<T> replacements, Function<T, Consumer<NewCapability>> capability);
    <T> MergeContract setAllInvPlans(List<T> replacements, Function<T, Consumer<NewInvPlan>> invPlan);
    <T> MergeContract setAllPaymentPlans(List<T> replacements, Function<T, Consumer<NewPaymentPlan>> paymentPlan);
    
    // Add new child entities
    MergeContract addParty(Consumer<NewParty> party);
    MergeContract addCoverage(Consumer<NewCoverage> coverage);
    MergeContract addReference(Consumer<NewReference> reference);
    MergeContract addNote(Consumer<NewNote> note);
    MergeContract addCapability(Consumer<NewCapability> capability);
    MergeContract addInvPlan(Consumer<NewInvPlan> invPlan);
    MergeContract addPaymentPlan(Consumer<NewPaymentPlan> paymentPlan);
    MergeContract addDateRelativity(Consumer<NewDateRelativity> dateRelativity);
    MergeContract addCommand(Consumer<NewCommand> command);
    
    // Modify existing child entities by ID
    MergeContract modifyParty(String partyId, Consumer<MergeParty> party);
    MergeContract modifyCoverage(String coverageId, Consumer<MergeCoverage> coverage);
    MergeContract modifyReference(String referenceId, Consumer<MergeReference> reference);
    MergeContract modifyNote(String noteId, Consumer<MergeNote> note);
    MergeContract modifyCapability(String capabilityId, Consumer<MergeCapability> capability);
    MergeContract modifyInvPlan(String invPlanId, Consumer<MergeInvPlan> invPlan);
    MergeContract modifyPaymentPlan(String paymentPlanId, Consumer<MergePaymentPlan> paymentPlan);
    MergeContract modifyDateRelativity(String dateRelativityId, Consumer<MergeDateRelativity> dateRelativity);
    MergeContract modifyCommand(String commandId, Consumer<MergeCommand> command);
    
    // Remove child entities by ID
    MergeContract removeParty(String partyId);
    MergeContract removeCoverage(String coverageId);
    MergeContract removeReference(String referenceId);
    MergeContract removeNote(String noteId);
    MergeContract removeCapability(String capabilityId);
    MergeContract removeInvPlan(String invPlanId);
    MergeContract removePaymentPlan(String paymentPlanId);
    MergeContract removeDateRelativity(String dateRelativityId);
    MergeContract removeCommand(String commandId);
    
    void build();
  }
  
  // Merge interfaces for child entities
  interface MergeParty {
    MergeParty externalId(String externalId);
    MergeParty partyType(String partyType);
    MergeParty partyEffectiveFrom(LocalDate partyEffectiveFrom);
    MergeParty partyEffectiveTo(@Nullable LocalDate partyEffectiveTo);
    
    // Business dates
    MergeParty partyTermStartDate(LocalDate partyTermStartDate);
    MergeParty partyTermEndDate(@Nullable LocalDate partyTermEndDate);
    
    MergeParty partyData(@Nullable JsonObject partyData);
    
    // Collection operations for party's child entities
    <T> MergeParty setAllReferences(String referenceType, List<T> replacements, Function<T, Consumer<NewReference>> reference);
    <T> MergeParty setAllNotes(String noteType, List<T> replacements, Function<T, Consumer<NewNote>> note);
    
    MergeParty addReference(Consumer<NewReference> reference);
    MergeParty addNote(Consumer<NewNote> note);
    
    void build();
  }
  
  interface MergeCoverage {
    MergeCoverage insuredId(String insuredId);
    MergeCoverage externalId(String externalId);
    MergeCoverage coverageType(String coverageType);
    MergeCoverage coverageCode(String coverageCode);
    MergeCoverage coverageSumInsured(@Nullable BigDecimal coverageSumInsured);
    MergeCoverage coverageRate(@Nullable BigDecimal coverageRate);
    MergeCoverage coverageRateType(@Nullable String coverageRateType);
    MergeCoverage coverageStatus(String coverageStatus);
    MergeCoverage coverageEffectiveFrom(LocalDate coverageEffectiveFrom);
    MergeCoverage coverageEffectiveTo(@Nullable LocalDate coverageEffectiveTo);
    
    // Business term dates
    MergeCoverage coverageTermStartDate(LocalDate coverageTermStartDate);
    MergeCoverage coverageTermEndDate(@Nullable LocalDate coverageTermEndDate);
    
    // Collection operations for coverage's child entities
    <T> MergeCoverage setAllReferences(String referenceType, List<T> replacements, Function<T, Consumer<NewReference>> reference);
    <T> MergeCoverage setAllNotes(String noteType, List<T> replacements, Function<T, Consumer<NewNote>> note);
    
    MergeCoverage addReference(Consumer<NewReference> reference);
    MergeCoverage addNote(Consumer<NewNote> note);
    
    void build();
  }
  
  interface MergeReference {
    MergeReference relations(@Nullable ContractOneOfRelations relations);
    MergeReference referenceType(String referenceType);
    MergeReference referenceValue(String referenceValue);
    MergeReference referenceBody(@Nullable JsonObject referenceBody);
    void build();
  }
  
  interface MergeNote {
    MergeNote relations(@Nullable ContractOneOfRelations relations);
    MergeNote noteType(String noteType);
    MergeNote noteValue(String noteValue);
    MergeNote noteBody(@Nullable JsonObject noteBody);
    void build();
  }
  
  interface MergeCapability {
    MergeCapability externalId(@Nullable String externalId);
    MergeCapability capabilityCode(String capabilityCode);
    MergeCapability capabilityName(String capabilityName);
    MergeCapability capabilityType(String capabilityType);
    MergeCapability capabilityEnabled(Boolean capabilityEnabled);
    void build();
  }
  
  interface MergeInvPlan {
    MergeInvPlan externalId(String externalId);
    MergeInvPlan invPlanCode(String invPlanCode);
    MergeInvPlan invPlanName(String invPlanName);
    MergeInvPlan invPlanStatus(String invPlanStatus);
    
    // Business dates
    MergeInvPlan invPlanStartDate(LocalDate invPlanStartDate);
    MergeInvPlan invPlanEndDate(@Nullable LocalDate invPlanEndDate);
    
    // Collection operations for investment plan allocations
    <T> MergeInvPlan setAllAllocations(List<T> replacements, Function<T, Consumer<NewInvPlanAlloc>> allocation);
    MergeInvPlan addAllocation(Consumer<NewInvPlanAlloc> allocation);
    MergeInvPlan modifyAllocation(String allocId, Consumer<MergeInvPlanAlloc> allocation);
    MergeInvPlan removeAllocation(String allocId);
    
    <T> MergeInvPlan setAllNotes(String noteType, List<T> replacements, Function<T, Consumer<NewNote>> note);
    <T> MergeInvPlan setAllReferences(String referenceType, List<T> replacements, Function<T, Consumer<NewReference>> reference);
    
    MergeInvPlan addReference(Consumer<NewReference> reference);
    MergeInvPlan addNote(Consumer<NewNote> note);
    
    void build();
  }
  
  interface MergeInvPlanAlloc {
    MergeInvPlanAlloc invPlanAllocCode(String invPlanAllocCode);
    MergeInvPlanAlloc invPlanAllocName(String invPlanAllocName);
    MergeInvPlanAlloc invPlanAllocPercentage(BigDecimal invPlanAllocPercentage);
    MergeInvPlanAlloc invPlanAllocStatus(String invPlanAllocStatus);
    void build();
  }
  
  interface MergePaymentPlan {
    MergePaymentPlan partyId(@Nullable String partyId);
    MergePaymentPlan paymentPlanStatus(String paymentPlanStatus);
    MergePaymentPlan paymentPlanFrequency(String paymentPlanFrequency);
    MergePaymentPlan paymentPlanAmount(BigDecimal paymentPlanAmount);
    
    // Business dates
    MergePaymentPlan paymentPlanStartDate(LocalDate paymentPlanStartDate);
    MergePaymentPlan paymentPlanEndDate(@Nullable LocalDate paymentPlanEndDate);
    void build();
  }
  
  interface MergeDateRelativity {
    MergeDateRelativity invPlanId(@Nullable String invPlanId);
    MergeDateRelativity coverageId(@Nullable String coverageId);
    MergeDateRelativity partyId(@Nullable String partyId);
    MergeDateRelativity paymentPlanId(@Nullable String paymentPlanId);
    
    MergeDateRelativity entityType(String entityType);
    MergeDateRelativity fieldName(String fieldName);
    MergeDateRelativity relativeToType(String relativeToType);
    MergeDateRelativity offsetInterval(@Nullable Period offsetInterval);
    MergeDateRelativity calculationRule(@Nullable String calculationRule);
    MergeDateRelativity description(@Nullable String description);
    void build();
  }
  
  interface MergeCommand {
    MergeCommand externalId(@Nullable String externalId);
    MergeCommand commandBody(JsonObject commandBody);
    MergeCommand commandStatus(String commandStatus);
    MergeCommand commandType(String commandType);
    MergeCommand commandTargetDate(@Nullable LocalDate commandTargetDate);
    MergeCommand commandDescription(@Nullable String commandDescription);
    MergeCommand commandError(@Nullable JsonObject commandError);
    void build();
  }
}