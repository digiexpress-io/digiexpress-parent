package io.resys.thena.fs.entities;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.hash.Hashing;

import io.resys.thena.support.RepoAssert;
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
  
  @Value.Check
  default void check() {
    RepoAssert.isTrue(!getBlobValue().isEmpty(), () -> "blobValue cannot be empty");
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableBlobTransitives.class)
  @JsonDeserialize(as = ImmutableBlobTransitives.class)
  interface BlobTransitives {
    OffsetDateTime getCreatedAt();
    OffsetDateTime getUpdatedAt();
  }
  
  
  // H(blob) = μ(blob_value)
  public static ImmutableBlob.Builder newInstance(JsonObject content, String type) {
    final var hash = Hashing.murmur3_128().hashString(content.encode(), StandardCharsets.UTF_8).toString();
    return ImmutableBlob.builder()
        .id(hash)
        .blobType(type)
        .blobValue(content);
  }
}