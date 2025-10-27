package io.resys.thena.contract.client.entities;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableReference.class)
@JsonDeserialize(as = ImmutableReference.class)
public interface Reference extends ContractEntity {
  UUID getId();
  UUID getContractId();

  // Multi-FK relations resolver
  @Nullable ContractOneOfRelations getRelations();

  UUID getCommitId();
  UUID getCreatedCommitId();

  // Transitive data from joins
  @Value.Auxiliary
  @Nullable ReferenceTransitives getTransitives();

  String getReferenceValue();
  String getReferenceType();
  Optional<JsonObject> getReferenceBody();

  @Override 
  default ContractDocType getDocType() { 
    return ContractDocType.REFERENCE; 
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableReferenceTransitives.class)
  @JsonDeserialize(as = ImmutableReferenceTransitives.class)
  interface ReferenceTransitives {
    OffsetDateTime getCreatedAt();
    OffsetDateTime getUpdatedAt();
  }
}