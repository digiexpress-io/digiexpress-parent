package io.resys.thena.contract.client.entities;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableCapability.class)
@JsonDeserialize(as = ImmutableCapability.class)
public interface Capability extends ContractEntity {
  UUID getId();
  UUID getContractId();

  Optional<String> getExternalId();
  UUID getCommitId();
  UUID getCreatedCommitId();

  // Transitive data from joins
  @Value.Auxiliary
  @Nullable CapabilityTransitives getTransitives();

  String getCapabilityCode();
  String getCapabilityName();
  String getCapabilityType();
  Boolean getCapabilityEnabled();

  @Override 
  default ContractDocType getDocType() { 
    return ContractDocType.CAPABILITY; 
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableCapabilityTransitives.class)
  @JsonDeserialize(as = ImmutableCapabilityTransitives.class)
  interface CapabilityTransitives {
    OffsetDateTime getCreatedAt();
    OffsetDateTime getUpdatedAt();
  }
}