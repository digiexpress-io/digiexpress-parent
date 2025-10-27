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

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableInvPlan.class)
@JsonDeserialize(as = ImmutableInvPlan.class)
public interface InvPlan extends ContractEntity {
  String getId();
  String getContractId();

  String getExternalId();
  String getCommitId();
  String getCreatedCommitId();

  // Transitive data from joins
  @Value.Auxiliary
  @Nullable InvPlanTransitives getTransitives();

  String getInvPlanStatus();
  String getInvPlanCode();
  String getInvPlanName();

  // Business dates (expanded)
  LocalDate getInvPlanStartDate();
  Duration getInvPlanStartDateInterval();
  String getInvPlanStartDateType();

  Optional<LocalDate> getInvPlanEndDate();
  Optional<Duration> getInvPlanEndDateInterval();
  Optional<String> getInvPlanEndDateType();

  @Override 
  default ContractDocType getDocType() { 
    return ContractDocType.INV_PLAN; 
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableInvPlanTransitives.class)
  @JsonDeserialize(as = ImmutableInvPlanTransitives.class)
  interface InvPlanTransitives {
    OffsetDateTime getCreatedAt();
    OffsetDateTime getUpdatedAt();
  }
}
