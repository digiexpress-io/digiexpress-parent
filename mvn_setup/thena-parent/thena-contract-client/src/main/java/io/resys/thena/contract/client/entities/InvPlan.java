package io.resys.thena.contract.client.entities;

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
@JsonSerialize(as = ImmutableInvPlan.class)
@JsonDeserialize(as = ImmutableInvPlan.class)
public interface InvPlan extends ContractEntity {
  UUID getId();
  UUID getContractId();

  String getExternalId();
  UUID getCommitId();
  UUID getCreatedCommitId();

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