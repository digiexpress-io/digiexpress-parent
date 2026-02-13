package io.resys.thena.fs.entities;

import java.time.OffsetDateTime;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableObjectIndex.class)
@JsonDeserialize(as = ImmutableObjectIndex.class)
public interface ObjectIndex extends FileSystemEntity {
  String getObjectId();
  String getCreatedBy();
  String getUpdatedBy();
  OffsetDateTime getCreatedAt();
  OffsetDateTime getUpdatedAt();
  
  @Override
  default String getId() {
    return getObjectId();
  }
  
  @Override
  default FileSystemEntityType getDocType() { 
    return FileSystemEntityType.INDEX; 
  }
}
