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
import java.util.function.Consumer;

import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public interface ThenaContractNewObject {

  interface NewContract {
    NewContract contractNumber(String contractNumber);
    NewContract parentContractId(@Nullable String parentContractId);
    NewContract externalId(@Nullable String externalId);
    
    // Business dates
    NewContract contractIssueDate(LocalDate contractIssueDate);
    NewContract contractIssueDateInterval(@Nullable Duration contractIssueDateInterval);
    NewContract contractIssueDateType(@Nullable String contractIssueDateType);
    
    NewContract contractStartDate(LocalDate contractStartDate);
    NewContract contractStartDateInterval(@Nullable Duration contractStartDateInterval);
    NewContract contractStartDateType(@Nullable String contractStartDateType);
    
    NewContract contractMaturityDate(@Nullable LocalDate contractMaturityDate);
    NewContract contractMaturityDateInterval(@Nullable Duration contractMaturityDateInterval);
    NewContract contractMaturityDateType(@Nullable String contractMaturityDateType);
    
    // Status and type
    NewContract contractStatus(String contractStatus);
    NewContract contractSubStatus(@Nullable String contractSubStatus);
    NewContract contractType(String contractType);
    NewContract contractSubType(@Nullable String contractSubType);
    NewContract contractData(@Nullable JsonObject contractData);
    
    // nested builders for related entities
    NewContract addParty(Consumer<NewParty> party);
    NewContract addCoverage(Consumer<NewCoverage> coverage);
    NewContract addReference(Consumer<NewReference> reference);
    NewContract addNote(Consumer<NewNote> note);
    NewContract addCapability(Consumer<NewCapability> capability);
    NewContract addInvPlan(Consumer<NewInvPlan> invPlan);
    NewContract addPaymentPlan(Consumer<NewPaymentPlan> paymentPlan);
    
    // state handling
    NewContract onNewState(Consumer<ContractContainer> handleNewState);
    void build();
  }
  
  // support interface for party creation
  interface NewParty {
    NewParty externalId(String externalId);
    NewParty partyType(String partyType);
    NewParty partyEffectiveFrom(LocalDate partyEffectiveFrom);
    NewParty partyEffectiveTo(@Nullable LocalDate partyEffectiveTo);
    
    // Business dates
    NewParty partyTermStartDate(LocalDate partyTermStartDate);
    NewParty partyTermStartDateInterval(@Nullable Duration partyTermStartDateInterval);
    NewParty partyTermStartDateType(@Nullable String partyTermStartDateType);
    
    NewParty partyTermEndDate(@Nullable LocalDate partyTermEndDate);
    NewParty partyTermEndDateInterval(@Nullable Duration partyTermEndDateInterval);
    NewParty partyTermEndDateType(@Nullable String partyTermEndDateType);
    
    NewParty partyData(@Nullable JsonObject partyData);
    
    // nested entities
    NewParty addNote(Consumer<NewNote> note);
    NewParty addReference(Consumer<NewReference> reference);
    void build();
  }
  
  // support interface for coverage creation
  interface NewCoverage {
    NewCoverage externalId(@Nullable String externalId);
    NewCoverage coverageType(String coverageType);
    NewCoverage coverageSubType(@Nullable String coverageSubType);
    NewCoverage coverageStatus(String coverageStatus);
    
    // Business dates
    NewCoverage coverageStartDate(LocalDate coverageStartDate);
    NewCoverage coverageStartDateInterval(@Nullable Duration coverageStartDateInterval);
    NewCoverage coverageStartDateType(@Nullable String coverageStartDateType);
    
    NewCoverage coverageEndDate(@Nullable LocalDate coverageEndDate);
    NewCoverage coverageEndDateInterval(@Nullable Duration coverageEndDateInterval);
    NewCoverage coverageEndDateType(@Nullable String coverageEndDateType);
    
    NewCoverage coverageAmount(@Nullable String coverageAmount);
    NewCoverage coverageData(@Nullable JsonObject coverageData);
    
    // nested entities
    NewCoverage addNote(Consumer<NewNote> note);
    NewCoverage addReference(Consumer<NewReference> reference);
    void build();
  }
  
  // support interface for reference creation
  interface NewReference {
    NewReference referenceType(String referenceType);
    NewReference referenceValue(String referenceValue);
    NewReference referenceData(@Nullable JsonObject referenceData);
    void build();
  }
  
  // support interface for note creation
  interface NewNote {
    NewNote noteType(String noteType);
    NewNote noteText(String noteText);
    NewNote noteData(@Nullable JsonObject noteData);
    String build(); // returns generated note id
  }
  
  // support interface for capability creation
  interface NewCapability {
    NewCapability capabilityType(String capabilityType);
    NewCapability capabilityStatus(String capabilityStatus);
    NewCapability capabilityData(@Nullable JsonObject capabilityData);
    void build();
  }
  
  // support interface for investment plan creation
  interface NewInvPlan {
    NewInvPlan externalId(@Nullable String externalId);
    NewInvPlan invPlanType(String invPlanType);
    NewInvPlan invPlanStatus(String invPlanStatus);
    NewInvPlan invPlanData(@Nullable JsonObject invPlanData);
    
    // nested allocation
    NewInvPlan addAllocation(Consumer<NewInvPlanAlloc> allocation);
    void build();
  }
  
  // support interface for investment plan allocation
  interface NewInvPlanAlloc {
    NewInvPlanAlloc allocType(String allocType);
    NewInvPlanAlloc allocAmount(String allocAmount);
    NewInvPlanAlloc allocPercentage(@Nullable String allocPercentage);
    NewInvPlanAlloc allocData(@Nullable JsonObject allocData);
    void build();
  }
  
  // support interface for payment plan creation
  interface NewPaymentPlan {
    NewPaymentPlan externalId(@Nullable String externalId);
    NewPaymentPlan paymentType(String paymentType);
    NewPaymentPlan paymentAmount(String paymentAmount);
    NewPaymentPlan paymentFrequency(String paymentFrequency);
    NewPaymentPlan paymentStartDate(LocalDate paymentStartDate);
    NewPaymentPlan paymentEndDate(@Nullable LocalDate paymentEndDate);
    NewPaymentPlan paymentData(@Nullable JsonObject paymentData);
    void build();
  }
  
  // support interface for commit viewer creation (similar to Grim)
  interface NewContractCommitViewer {
    NewContractCommitViewer userId(String userId);
    NewContractCommitViewer usedFor(String usedFor);
    NewContractCommitViewer commitId(String commitId);
    NewContractCommitViewer currentTxCommit(); // ongoing tx commit
    NewContractCommitViewer currentTreeCommit(); // whatever is last tree updated commit 
    NewContractCommitViewer skipViewer(); // cancel out of viewer, skips the object 
    String getCurrentTreeCommit();
    void build(); 
  }
}