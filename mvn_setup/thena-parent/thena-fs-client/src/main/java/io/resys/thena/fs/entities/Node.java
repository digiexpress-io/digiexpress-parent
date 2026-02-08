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
  
  // the actual hash calculated based on the contents
  String getId();
  
  // convenience junk data, the json content object id thats connected to json in the blob, its excluded from hash calculation
  String getNodeId();
  
  // path to whatever we have part of hash calc
  String getNodePath();
  
  // last path fragment to whatever we have part of hash calc, never empty for files
  String getNodeName();
  
  // for folders this is not present, for actual files its always present, part of has calculation
  Optional<String> getBlobId();
  
  // extra comments, permissions, docs for everything in file or folder... meta content, part of has calculation
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
  public static ImmutableNode.Builder newInstance(String path, String nodeId, String name, Optional<String> blobId, Optional<String> propsId) {
    final var content = new StringBuilder();
    content.append(path);
    content.append(name);
    content.append(blobId.orElse(""));
    content.append(propsId.orElse(""));
    
    final var hash = Hashing.murmur3_128().hashString(content.toString(), StandardCharsets.UTF_8).toString();
    return ImmutableNode.builder()
        .id(hash)
        .nodeId(nodeId)
        .nodePath(path)
        .nodeName(name)
        .blobId(blobId)
        .propsId(propsId);
  }
}