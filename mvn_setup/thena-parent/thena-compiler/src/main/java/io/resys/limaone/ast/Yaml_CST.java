package io.resys.limaone.ast;

import java.io.Serializable;
import java.util.Map;

public interface Yaml_CST {

  interface Yaml extends Yaml_CST, Comparable<Yaml>, Serializable {
    Yaml getParent();
    String getKeyword();
    Map<String, Yaml> getChildren();
    Yaml get(String name);
    String getValue();
    String getSyntax();
    boolean hasNonNull(String name);
    int getStart();
    int getEnd();
  }
}