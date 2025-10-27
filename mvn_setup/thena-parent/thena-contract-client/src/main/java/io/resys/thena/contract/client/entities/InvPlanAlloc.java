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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableInvPlanAlloc.class)
@JsonDeserialize(as = ImmutableInvPlanAlloc.class)
public interface InvPlanAlloc extends ContractEntity {
  UUID getId();
  UUID getInvPlanId();

  UUID getCommitId();
  UUID getCreatedCommitId();

  // Transitive data from joins
  @Value.Auxiliary
  @Nullable InvPlanAllocTransitives getTransitives();

  String getInvPlanAllocCode();
  String getInvPlanAllocName();
  BigDecimal getInvPlanAllocPercentage();
  String getInvPlanAllocStatus();

  @Override 
  default ContractDocType getDocType() { 
    return ContractDocType.INV_PLAN_ALLOC; 
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableInvPlanAllocTransitives.class)
  @JsonDeserialize(as = ImmutableInvPlanAllocTransitives.class)
  interface InvPlanAllocTransitives {
    OffsetDateTime getCreatedAt();
    OffsetDateTime getUpdatedAt();
    // Virtual field from parent table
    UUID getContractId();
  }
}
