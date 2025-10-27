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
@JsonSerialize(as = ImmutableNote.class)
@JsonDeserialize(as = ImmutableNote.class)
public interface Note extends ContractEntity {
  UUID getId();
  UUID getContractId();

  // Multi-FK relations resolver
  @Nullable ContractOneOfRelations getRelations();

  UUID getCommitId();
  UUID getCreatedCommitId();

  // Transitive data from joins
  @Value.Auxiliary
  @Nullable NoteTransitives getTransitives();

  String getNoteValue();
  String getNoteType();
  Optional<JsonObject> getNoteBody();

  @Override 
  default ContractDocType getDocType() { 
    return ContractDocType.NOTE; 
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableNoteTransitives.class)
  @JsonDeserialize(as = ImmutableNoteTransitives.class)
  interface NoteTransitives {
    OffsetDateTime getCreatedAt();
    OffsetDateTime getUpdatedAt();
  }
}