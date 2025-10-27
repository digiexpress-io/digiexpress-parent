package io.resys.thena.contract.client.entities;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableParty.class)
@JsonDeserialize(as = ImmutableParty.class)
public interface Party extends ContractEntity {
  UUID getId();
  UUID getContractId();

  String getExternalId();
  UUID getCommitId();
  UUID getCreatedCommitId();

  // Transitive data from joins
  @Value.Auxiliary
  @Nullable PartyTransitives getTransitives();

  String getPartyType();
  LocalDate getPartyEffectiveFrom();
  Optional<LocalDate> getPartyEffectiveTo();

  // Business dates (expanded)
  LocalDate getPartyTermStartDate();
  Duration getPartyTermStartDateInterval();
  String getPartyTermStartDateType();

  Optional<LocalDate> getPartyTermEndDate();
  Optional<Duration> getPartyTermEndDateInterval();
  Optional<String> getPartyTermEndDateType();

  Optional<JsonObject> getPartyData();

  @Override 
  default ContractDocType getDocType() { 
    return ContractDocType.PARTY; 
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutablePartyTransitives.class)
  @JsonDeserialize(as = ImmutablePartyTransitives.class)
  interface PartyTransitives {
    OffsetDateTime getCreatedAt();
    OffsetDateTime getUpdatedAt();
  }
}