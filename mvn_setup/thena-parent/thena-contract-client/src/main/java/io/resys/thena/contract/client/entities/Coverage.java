package io.resys.thena.contract.client.entities;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableCoverage.class)
@JsonDeserialize(as = ImmutableCoverage.class)
public interface Coverage extends ContractEntity {
  UUID getId();
  UUID getContractId();
  UUID getInsuredId();

  String getExternalId();
  UUID getCommitId();
  UUID getCreatedCommitId();

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