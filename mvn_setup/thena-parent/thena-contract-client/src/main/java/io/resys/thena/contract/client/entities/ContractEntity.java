package io.resys.thena.contract.client.entities;

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

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;

public interface ContractEntity {
  
  String getId();
  
  @JsonIgnore
  ContractDocType getDocType();

  enum ContractRelationType {
    INV_PLAN_ALLOC,
    INV_PLAN,
    COVERAGE,
    PARTY
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableContractOneOfRelations.class)
  @JsonDeserialize(as = ImmutableContractOneOfRelations.class)
  interface ContractOneOfRelations {
    @Nullable String getInvPlanAllocId();
    @Nullable String getInvPlanId();
    @Nullable String getCoverageId();
    @Nullable String getPartyId();
    ContractRelationType getRelationType();
    
    @JsonIgnore 
    default String getTargetId() {
      switch (getRelationType()) {
        case INV_PLAN_ALLOC: return getInvPlanAllocId();
        case INV_PLAN: return getInvPlanId();
        case COVERAGE: return getCoverageId();
        case PARTY: return getPartyId();
        default: throw new IllegalArgumentException("Unexpected value: " + getRelationType());
      }
    }
  }
}
