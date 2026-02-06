package io.resys.thena.fs.entities;

import java.time.OffsetDateTime;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

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
}