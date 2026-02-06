package io.resys.thena.fs.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface FileSystemEntity {
  
  String getId();
  
  @JsonIgnore
  FileSystemEntityType getDocType();
  
  
  enum FileSystemEntityType {
    BLOB,
    TREE,
    PROPS,
    COMMIT,
    REF,
    TAG,
    NODE
  }
}