package io.resys.thena.fs.entities;

import java.time.OffsetDateTime;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableBlob.class)
@JsonDeserialize(as = ImmutableBlob.class)
public interface Blob extends FileSystemEntity {
  
  String getId();
  String getBlobType();
  JsonObject getBlobValue();

  @Value.Auxiliary
  @Nullable 
  BlobTransitives getTransitives();

  @Override
  default FileSystemEntityType getDocType() { 
    return FileSystemEntityType.BLOB; 
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableBlobTransitives.class)
  @JsonDeserialize(as = ImmutableBlobTransitives.class)
  interface BlobTransitives {
    OffsetDateTime getCreatedAt();
    OffsetDateTime getUpdatedAt();
  }
}