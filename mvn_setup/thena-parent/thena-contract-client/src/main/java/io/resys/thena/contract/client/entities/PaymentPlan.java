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
@JsonSerialize(as = ImmutablePaymentPlan.class)
@JsonDeserialize(as = ImmutablePaymentPlan.class)
public interface PaymentPlan extends ContractEntity {
  UUID getId();
  UUID getContractId();

  Optional<UUID> getPartyId();
  UUID getCommitId();
  UUID getCreatedCommitId();

  // Transitive data from joins
  @Value.Auxiliary
  @Nullable PaymentPlanTransitives getTransitives();

  String getPaymentPlanStatus();
  String getPaymentPlanFrequency();
  BigDecimal getPaymentPlanAmount();

  // Business dates (expanded)
  LocalDate getPaymentPlanStartDate();
  Duration getPaymentPlanStartDateInterval();
  String getPaymentPlanStartDateType();

  Optional<LocalDate> getPaymentPlanEndDate();
  Optional<Duration> getPaymentPlanEndDateInterval();
  Optional<String> getPaymentPlanEndDateType();

  @Override 
  default ContractDocType getDocType() { 
    return ContractDocType.PAYMENT_PLAN; 
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutablePaymentPlanTransitives.class)
  @JsonDeserialize(as = ImmutablePaymentPlanTransitives.class)
  interface PaymentPlanTransitives {
    OffsetDateTime getCreatedAt();
    OffsetDateTime getUpdatedAt();
  }
}