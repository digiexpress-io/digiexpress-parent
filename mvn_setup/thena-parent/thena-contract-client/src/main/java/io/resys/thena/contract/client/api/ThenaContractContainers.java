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

import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.thena.api.envelope.ThenaContainer;
import io.resys.thena.contract.client.entities.Capability;
import io.resys.thena.contract.client.entities.Command;
import io.resys.thena.contract.client.entities.Contract;
import io.resys.thena.contract.client.entities.DateRule;
import io.resys.thena.contract.client.entities.Coverage;
import io.resys.thena.contract.client.entities.InvPlan;
import io.resys.thena.contract.client.entities.InvPlanAlloc;
import io.resys.thena.contract.client.entities.Note;
import io.resys.thena.contract.client.entities.Party;
import io.resys.thena.contract.client.entities.PaymentPlan;
import io.resys.thena.contract.client.entities.Reference;

/**
 * Container objects that aggregate related contract entities together,
 * similar to GrimMissionContainer in the Grim domain.
 */
public interface ThenaContractContainers {

  @Value.Immutable
  @JsonSerialize(as = ImmutableContractContainer.class)
  @JsonDeserialize(as = ImmutableContractContainer.class)
  interface ContractContainer extends ThenaContainer {
    Contract getContract();
    List<Party> getParties();
    List<Coverage> getCoverages();
    List<Reference> getReferences();
    List<Note> getNotes();
    List<Capability> getCapabilities();
    List<InvPlan> getInvPlans();
    List<PaymentPlan> getPaymentPlans();
    List<DateRule> getDateRules();
    List<Command> getCommands();
    
    // Investment plan allocations grouped by investment plan id
    Map<String, List<InvPlanAlloc>> getInvPlanAllocations();
  }
}