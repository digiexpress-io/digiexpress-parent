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
import java.util.function.Consumer;

import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.entities.ContractEntity.ContractOneOfRelations;
import io.resys.thena.contract.client.entities.Party;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public interface ThenaContractNewObject {

  interface NewContract {
    NewContract contractNumber(String contractNumber);
    NewContract parentContractId(@Nullable String parentContractId);
    NewContract externalId(@Nullable String externalId);
    
    // Business dates
    NewContract contractIssueDate(LocalDate contractIssueDate);
    NewContract contractIssueDateInterval(@Nullable Period contractIssueDateInterval);
    NewContract contractIssueDateType(@Nullable String contractIssueDateType);
    
    NewContract contractStartDate(LocalDate contractStartDate);
    NewContract contractStartDateInterval(@Nullable Period contractStartDateInterval);
    NewContract contractStartDateType(@Nullable String contractStartDateType);
    
    NewContract contractMaturityDate(@Nullable LocalDate contractMaturityDate);
    NewContract contractMaturityDateInterval(@Nullable Period contractMaturityDateInterval);
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
    NewParty partyTermStartDateInterval(@Nullable Period partyTermStartDateInterval);
    NewParty partyTermStartDateType(@Nullable String partyTermStartDateType);
    
    NewParty partyTermEndDate(@Nullable LocalDate partyTermEndDate);
    NewParty partyTermEndDateInterval(@Nullable Period partyTermEndDateInterval);
    NewParty partyTermEndDateType(@Nullable String partyTermEndDateType);
    
    NewParty partyData(@Nullable JsonObject partyData);
    
    // nested entities
    NewParty addNote(Consumer<NewNote> note);
    NewParty addReference(Consumer<NewReference> reference);
    Party build();
  }
  
  // support interface for coverage creation
  interface NewCoverage {
    NewCoverage insuredId(String insuredId);
    NewCoverage externalId(String externalId);
    NewCoverage coverageType(String coverageType);
    NewCoverage coverageCode(String coverageCode);
    NewCoverage coverageSumInsured(@Nullable BigDecimal coverageSumInsured);
    NewCoverage coverageRate(@Nullable BigDecimal coverageRate);
    NewCoverage coverageRateType(@Nullable String coverageRateType);
    NewCoverage coverageStatus(String coverageStatus);
    NewCoverage coverageEffectiveFrom(LocalDate coverageEffectiveFrom);
    NewCoverage coverageEffectiveTo(@Nullable LocalDate coverageEffectiveTo);
    
    // Business term dates
    NewCoverage coverageTermStartDate(LocalDate coverageTermStartDate);
    NewCoverage coverageTermStartDateInterval(@Nullable Period coverageTermStartDateInterval);
    NewCoverage coverageTermStartDateType(@Nullable String coverageTermStartDateType);
    
    NewCoverage coverageTermEndDate(@Nullable LocalDate coverageTermEndDate);
    NewCoverage coverageTermEndDateInterval(@Nullable Period coverageTermEndDateInterval);
    NewCoverage coverageTermEndDateType(@Nullable String coverageTermEndDateType);
    
    // nested entities
    NewCoverage addNote(Consumer<NewNote> note);
    NewCoverage addReference(Consumer<NewReference> reference);
    void build();
  }
  
  // support interface for reference creation
  interface NewReference {
    NewReference relations(@Nullable ContractOneOfRelations relations);
    NewReference referenceType(String referenceType);
    NewReference referenceValue(String referenceValue);
    NewReference referenceBody(@Nullable JsonObject referenceBody);
    void build();
  }
  
  // support interface for note creation
  interface NewNote {
    NewNote relations(@Nullable ContractOneOfRelations relations);
    NewNote noteType(String noteType);
    NewNote noteValue(String noteValue);
    NewNote noteBody(@Nullable JsonObject noteBody);
    String build(); // returns generated note id
  }
  
  // support interface for capability creation
  interface NewCapability {
    NewCapability externalId(@Nullable String externalId);
    NewCapability capabilityCode(String capabilityCode);
    NewCapability capabilityName(String capabilityName);
    NewCapability capabilityType(String capabilityType);
    NewCapability capabilityEnabled(Boolean capabilityEnabled);
    void build();
  }
  
  // support interface for investment plan creation
  interface NewInvPlan {
    NewInvPlan externalId(String externalId);
    NewInvPlan invPlanCode(String invPlanCode);
    NewInvPlan invPlanName(String invPlanName);
    NewInvPlan invPlanStatus(String invPlanStatus);
    
    // Business dates
    NewInvPlan invPlanStartDate(LocalDate invPlanStartDate);
    NewInvPlan invPlanStartDateInterval(@Nullable Period invPlanStartDateInterval);
    NewInvPlan invPlanStartDateType(@Nullable String invPlanStartDateType);
    
    NewInvPlan invPlanEndDate(@Nullable LocalDate invPlanEndDate);
    NewInvPlan invPlanEndDateInterval(@Nullable Period invPlanEndDateInterval);
    NewInvPlan invPlanEndDateType(@Nullable String invPlanEndDateType);
    
    // nested allocation
    NewInvPlan addAllocation(Consumer<NewInvPlanAlloc> allocation);
    void build();
  }
  
  // support interface for investment plan allocation
  interface NewInvPlanAlloc {
    NewInvPlanAlloc invPlanAllocCode(String invPlanAllocCode);
    NewInvPlanAlloc invPlanAllocName(String invPlanAllocName);
    NewInvPlanAlloc invPlanAllocPercentage(BigDecimal invPlanAllocPercentage);
    NewInvPlanAlloc invPlanAllocStatus(String invPlanAllocStatus);
    void build();
  }
  
  // support interface for payment plan creation
  interface NewPaymentPlan {
    NewPaymentPlan partyId(@Nullable String partyId);
    NewPaymentPlan paymentPlanStatus(String paymentPlanStatus);
    NewPaymentPlan paymentPlanFrequency(String paymentPlanFrequency);
    NewPaymentPlan paymentPlanAmount(BigDecimal paymentPlanAmount);
    
    // Business dates
    NewPaymentPlan paymentPlanStartDate(LocalDate paymentPlanStartDate);
    NewPaymentPlan paymentPlanStartDateInterval(@Nullable Period paymentPlanStartDateInterval);
    NewPaymentPlan paymentPlanStartDateType(@Nullable String paymentPlanStartDateType);
    
    NewPaymentPlan paymentPlanEndDate(@Nullable LocalDate paymentPlanEndDate);
    NewPaymentPlan paymentPlanEndDateInterval(@Nullable Period paymentPlanEndDateInterval);
    NewPaymentPlan paymentPlanEndDateType(@Nullable String paymentPlanEndDateType);
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