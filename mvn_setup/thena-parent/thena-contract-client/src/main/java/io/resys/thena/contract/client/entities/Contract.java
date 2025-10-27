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
@JsonSerialize(as = ImmutableContract.class)
@JsonDeserialize(as = ImmutableContract.class)
public interface Contract extends ContractEntity {
  UUID getId();
  Optional<UUID> getParentContractId();
  String getContractNumber();

  Optional<String> getExternalId();
  UUID getCommitId();
  UUID getCreatedCommitId();
  UUID getUpdatedTreeCommitId();

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
