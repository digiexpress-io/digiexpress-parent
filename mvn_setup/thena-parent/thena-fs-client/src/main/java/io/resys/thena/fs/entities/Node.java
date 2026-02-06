package io.resys.thena.fs.entities;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableNode.class)
@JsonDeserialize(as = ImmutableNode.class)
public interface Node extends FileSystemEntity {
  
  String getId();
  String getNodePath();
  String getNodeName();
  Optional<String> getBlobId();
  Optional<String> getPropsId();

  @Value.Auxiliary
  @Nullable 
  NodeTransitives getTransitives();

  @Override
  default FileSystemEntityType getDocType() { 
    return FileSystemEntityType.NODE; 
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableNodeTransitives.class)
  @JsonDeserialize(as = ImmutableNodeTransitives.class)
  interface NodeTransitives {
    OffsetDateTime getCreatedAt();
    OffsetDateTime getUpdatedAt();
  }
}