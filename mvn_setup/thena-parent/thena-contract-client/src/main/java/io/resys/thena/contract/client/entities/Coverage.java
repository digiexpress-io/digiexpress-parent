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
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableCoverage.class)
@JsonDeserialize(as = ImmutableCoverage.class)
public interface Coverage extends ContractEntity {
  String getId();
  String getContractId();
  String getInsuredId();

  String getExternalId();
  String getCommitId();
  String getCreatedCommitId();

  // Transitive data from joins
  @Value.Auxiliary
  @Nullable CoverageTransitives getTransitives();

  String getCoverageType();
  String getCoverageCode();
  Optional<BigDecimal> getCoverageSumInsured();
  Optional<BigDecimal> getCoverageRate();
  Optional<String> getCoverageRateType();
  String getCoverageStatus();
  LocalDate getCoverageEffectiveFrom();
  Optional<LocalDate> getCoverageEffectiveTo();

  // Business dates (expanded)
  LocalDate getCoverageTermStartDate();
  Duration getCoverageTermStartDateInterval();
  String getCoverageTermStartDateType();

  Optional<LocalDate> getCoverageTermEndDate();
  Optional<Duration> getCoverageTermEndDateInterval();
  Optional<String> getCoverageTermEndDateType();

  @Override 
  default ContractDocType getDocType() { 
    return ContractDocType.COVERAGE; 
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableCoverageTransitives.class)
  @JsonDeserialize(as = ImmutableCoverageTransitives.class)
  interface CoverageTransitives {
    OffsetDateTime getCreatedAt();
    OffsetDateTime getUpdatedAt();
  }
}
