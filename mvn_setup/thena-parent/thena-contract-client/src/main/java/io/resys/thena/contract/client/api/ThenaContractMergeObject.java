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

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import jakarta.annotation.Nullable;

import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewCapability;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewCoverage;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewInvPlan;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewInvPlanAlloc;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewNote;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewParty;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewPaymentPlan;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewReference;
import io.vertx.core.json.JsonObject;

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
    MergeContract contractIssueDateInterval(@Nullable Duration contractIssueDateInterval);
    MergeContract contractIssueDateType(@Nullable String contractIssueDateType);
    
    MergeContract contractStartDate(LocalDate contractStartDate);
    MergeContract contractStartDateInterval(@Nullable Duration contractStartDateInterval);
    MergeContract contractStartDateType(@Nullable String contractStartDateType);
    
    MergeContract contractMaturityDate(@Nullable LocalDate contractMaturityDate);
    MergeContract contractMaturityDateInterval(@Nullable Duration contractMaturityDateInterval);
    MergeContract contractMaturityDateType(@Nullable String contractMaturityDateType);
    
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
    <T> MergeContract setAllCapabilities(String capabilityType, List<T> replacements, Function<T, Consumer<NewCapability>> capability);
    <T> MergeContract setAllInvPlans(String invPlanType, List<T> replacements, Function<T, Consumer<NewInvPlan>> invPlan);
    <T> MergeContract setAllPaymentPlans(String paymentType, List<T> replacements, Function<T, Consumer<NewPaymentPlan>> paymentPlan);
    
    // Add new child entities
    MergeContract addParty(Consumer<NewParty> party);
    MergeContract addCoverage(Consumer<NewCoverage> coverage);
    MergeContract addReference(Consumer<NewReference> reference);
    MergeContract addNote(Consumer<NewNote> note);
    MergeContract addCapability(Consumer<NewCapability> capability);
    MergeContract addInvPlan(Consumer<NewInvPlan> invPlan);
    MergeContract addPaymentPlan(Consumer<NewPaymentPlan> paymentPlan);
    
    // Modify existing child entities by ID
    MergeContract modifyParty(String partyId, Consumer<MergeParty> party);
    MergeContract modifyCoverage(String coverageId, Consumer<MergeCoverage> coverage);
    MergeContract modifyReference(String referenceId, Consumer<MergeReference> reference);
    MergeContract modifyNote(String noteId, Consumer<MergeNote> note);
    MergeContract modifyCapability(String capabilityId, Consumer<MergeCapability> capability);
    MergeContract modifyInvPlan(String invPlanId, Consumer<MergeInvPlan> invPlan);
    MergeContract modifyPaymentPlan(String paymentPlanId, Consumer<MergePaymentPlan> paymentPlan);
    
    // Remove child entities by ID
    MergeContract removeParty(String partyId);
    MergeContract removeCoverage(String coverageId);
    MergeContract removeReference(String referenceId);
    MergeContract removeNote(String noteId);
    MergeContract removeCapability(String capabilityId);
    MergeContract removeInvPlan(String invPlanId);
    MergeContract removePaymentPlan(String paymentPlanId);
    
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
    MergeParty partyTermStartDateInterval(@Nullable Duration partyTermStartDateInterval);
    MergeParty partyTermStartDateType(@Nullable String partyTermStartDateType);
    
    MergeParty partyTermEndDate(@Nullable LocalDate partyTermEndDate);
    MergeParty partyTermEndDateInterval(@Nullable Duration partyTermEndDateInterval);
    MergeParty partyTermEndDateType(@Nullable String partyTermEndDateType);
    
    MergeParty partyData(@Nullable JsonObject partyData);
    
    // Collection operations for party's child entities
    <T> MergeParty setAllReferences(String referenceType, List<T> replacements, Function<T, Consumer<NewReference>> reference);
    <T> MergeParty setAllNotes(String noteType, List<T> replacements, Function<T, Consumer<NewNote>> note);
    
    MergeParty addReference(Consumer<NewReference> reference);
    MergeParty addNote(Consumer<NewNote> note);
    
    void build();
  }
  
  interface MergeCoverage {
    MergeCoverage externalId(@Nullable String externalId);
    MergeCoverage coverageType(String coverageType);
    MergeCoverage coverageSubType(@Nullable String coverageSubType);
    MergeCoverage coverageStatus(String coverageStatus);
    
    // Business dates
    MergeCoverage coverageStartDate(LocalDate coverageStartDate);
    MergeCoverage coverageStartDateInterval(@Nullable Duration coverageStartDateInterval);
    MergeCoverage coverageStartDateType(@Nullable String coverageStartDateType);
    
    MergeCoverage coverageEndDate(@Nullable LocalDate coverageEndDate);
    MergeCoverage coverageEndDateInterval(@Nullable Duration coverageEndDateInterval);
    MergeCoverage coverageEndDateType(@Nullable String coverageEndDateType);
    
    MergeCoverage coverageAmount(@Nullable String coverageAmount);
    MergeCoverage coverageData(@Nullable JsonObject coverageData);
    
    // Collection operations for coverage's child entities
    <T> MergeCoverage setAllReferences(String referenceType, List<T> replacements, Function<T, Consumer<NewReference>> reference);
    <T> MergeCoverage setAllNotes(String noteType, List<T> replacements, Function<T, Consumer<NewNote>> note);
    
    MergeCoverage addReference(Consumer<NewReference> reference);
    MergeCoverage addNote(Consumer<NewNote> note);
    
    void build();
  }
  
  interface MergeReference {
    MergeReference referenceType(String referenceType);
    MergeReference referenceValue(String referenceValue);
    MergeReference referenceData(@Nullable JsonObject referenceData);
    void build();
  }
  
  interface MergeNote {
    MergeNote noteType(String noteType);
    MergeNote noteText(String noteText);
    MergeNote noteData(@Nullable JsonObject noteData);
    void build();
  }
  
  interface MergeCapability {
    MergeCapability capabilityType(String capabilityType);
    MergeCapability capabilityStatus(String capabilityStatus);
    MergeCapability capabilityData(@Nullable JsonObject capabilityData);
    void build();
  }
  
  interface MergeInvPlan {
    MergeInvPlan externalId(@Nullable String externalId);
    MergeInvPlan invPlanType(String invPlanType);
    MergeInvPlan invPlanStatus(String invPlanStatus);
    MergeInvPlan invPlanData(@Nullable JsonObject invPlanData);
    
    // Collection operations for investment plan allocations
    <T> MergeInvPlan setAllAllocations(String allocType, List<T> replacements, Function<T, Consumer<NewInvPlanAlloc>> allocation);
    MergeInvPlan addAllocation(Consumer<NewInvPlanAlloc> allocation);
    MergeInvPlan modifyAllocation(String allocId, Consumer<MergeInvPlanAlloc> allocation);
    MergeInvPlan removeAllocation(String allocId);
    
    void build();
  }
  
  interface MergeInvPlanAlloc {
    MergeInvPlanAlloc allocType(String allocType);
    MergeInvPlanAlloc allocAmount(String allocAmount);
    MergeInvPlanAlloc allocPercentage(@Nullable String allocPercentage);
    MergeInvPlanAlloc allocData(@Nullable JsonObject allocData);
    void build();
  }
  
  interface MergePaymentPlan {
    MergePaymentPlan externalId(@Nullable String externalId);
    MergePaymentPlan paymentType(String paymentType);
    MergePaymentPlan paymentAmount(String paymentAmount);
    MergePaymentPlan paymentFrequency(String paymentFrequency);
    MergePaymentPlan paymentStartDate(LocalDate paymentStartDate);
    MergePaymentPlan paymentEndDate(@Nullable LocalDate paymentEndDate);
    MergePaymentPlan paymentData(@Nullable JsonObject paymentData);
    void build();
  }
}