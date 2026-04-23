package io.resys.limaone.persistence.fs;

import lombok.Value;

@Value
public class NodePathAndName {
  
  String path;
  String name;
  
  public static NodePathAndName of(String path, String name) {
    return new NodePathAndName(path, name);
  }
}
