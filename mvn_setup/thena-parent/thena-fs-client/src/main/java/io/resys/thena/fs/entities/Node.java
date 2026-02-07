package io.resys.thena.fs.entities;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.hash.Hashing;

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
  
  
  // H(node) = μ(node_path ⊕ node_name ⊕ H(blob) ⊕ H(props))
  public static Node newInstance(String path, String name, Optional<String> blobId, Optional<String> propsId) {
    final var content = new StringBuilder();
    content.append(path);
    content.append(name);
    content.append(blobId.orElse(""));
    content.append(propsId.orElse(""));
    
    final var hash = Hashing.murmur3_128().hashString(content.toString(), StandardCharsets.UTF_8).toString();
    return ImmutableNode.builder()
        .id(hash)
        .nodePath(path)
        .nodeName(name)
        .blobId(blobId)
        .propsId(propsId)
        .build();
  }
}