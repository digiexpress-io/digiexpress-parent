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

import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableContract.class)
@JsonDeserialize(as = ImmutableContract.class)
public interface Contract extends ContractEntity {
  String getId();
  Optional<String> getParentContractId();
  String getContractNumber();

  Optional<String> getExternalId();
  String getCommitId();
  String getCreatedCommitId();
  String getUpdatedTreeCommitId();

  // Transitive data from joins
  @Value.Auxiliary
  @Nullable ContractTransitives getTransitives();

  // Business dates (expanded)
  LocalDate getContractIssueDate();
  Duration getContractIssueDateInterval();
  String getContractIssueDateType();
  
  LocalDate getContractStartDate();
  Duration getContractStartDateInterval();
  String getContractStartDateType();
  
  Optional<LocalDate> getContractMaturityDate();
  Optional<Duration> getContractMaturityDateInterval();
  Optional<String> getContractMaturityDateType();
  
  String getContractStatus();
  Optional<String> getContractSubStatus();
  String getContractType();
  Optional<String> getContractSubType();
  Optional<JsonObject> getContractData();

  @Override 
  default ContractDocType getDocType() { 
    return ContractDocType.CONTRACT; 
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableContractTransitives.class)
  @JsonDeserialize(as = ImmutableContractTransitives.class)
  interface ContractTransitives {
    OffsetDateTime getCreatedAt();
    OffsetDateTime getUpdatedAt();
    OffsetDateTime getUpdatedTreeAt();
  }
}
