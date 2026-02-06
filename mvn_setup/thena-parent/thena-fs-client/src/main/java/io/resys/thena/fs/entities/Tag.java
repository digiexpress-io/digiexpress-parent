package io.resys.thena.fs.entities;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableTag.class)
@JsonDeserialize(as = ImmutableTag.class)
public interface Tag extends FileSystemEntity {
  
  String getId();
  String getTagName();
  Optional<String> getTagDescription();
  String getCommitId();
  OffsetDateTime getTagCreatedAt();
  String getTagAuthor();
  Optional<JsonObject> getTagExtension();
  JsonObject getTagErrors();
  Optional<String> getExternalId();
  Optional<String> getExternalTenantId();
  Optional<OffsetDateTime> getTagStartsAt();
  Optional<JsonObject> getTagReport();

  @Value.Auxiliary
  @Nullable 
  TagTransitives getTransitives();

  @Override
  default FileSystemEntityType getDocType() { 
    return FileSystemEntityType.TAG; 
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableTagTransitives.class)
  @JsonDeserialize(as = ImmutableTagTransitives.class)
  interface TagTransitives {
    // might not be loaded
    @Nullable Commit getCommit();
    
    // might not be loaded
    @Nullable Tree getTree();

    // might not be loaded
    @Nullable Map<String, Blob> getBlobsById();  
  }
}