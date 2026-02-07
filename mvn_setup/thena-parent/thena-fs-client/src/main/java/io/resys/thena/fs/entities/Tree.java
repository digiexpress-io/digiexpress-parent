package io.resys.thena.fs.entities;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.hash.Hashing;

import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableTree.class)
@JsonDeserialize(as = ImmutableTree.class)
public interface Tree extends FileSystemEntity {
  
  String getId();
  List<Node> getTreeNodes();

  @Value.Auxiliary
  @Nullable 
  TreeTransitives getTransitives();

  @Override
  default FileSystemEntityType getDocType() { 
    return FileSystemEntityType.TREE; 
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableTreeTransitives.class)
  @JsonDeserialize(as = ImmutableTreeTransitives.class)
  interface TreeTransitives {
    OffsetDateTime getCreatedAt();
    OffsetDateTime getUpdatedAt();
  }

  // H(tree) = μ(∑ᵢ₌₁ⁿ H(nodeᵢ))
  public static Tree newInstance(List<Node> nodes) {
    final var content = new StringBuilder();
    nodes.stream()
        .sorted((a, b) -> (a.getNodePath() + "/" + a.getNodeName()).compareTo(b.getNodePath() + "/" + b.getNodeName()))
        .forEach(node -> content.append(node.getId()));
    
    final var hash = Hashing.murmur3_128().hashString(content.toString(), StandardCharsets.UTF_8).toString();
    return ImmutableTree.builder()
        .id(hash)
        .treeNodes(nodes)
        .build();
  }
}