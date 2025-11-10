package io.resys.thena.contract.client.entities;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableParty.class)
@JsonDeserialize(as = ImmutableParty.class)
public interface Party extends ContractEntity {
  String getId();
  String getContractId();

  String getExternalId();
  String getCommitId();
  String getCreatedCommitId();

  // Transitive data from joins
  @Value.Auxiliary
  @Nullable PartyTransitives getTransitives();

  String getPartyType();
  LocalDate getPartyEffectiveFrom();
  Optional<LocalDate> getPartyEffectiveTo();

  // Business dates
  LocalDate getPartyTermStartDate();
  Optional<LocalDate> getPartyTermEndDate();

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
