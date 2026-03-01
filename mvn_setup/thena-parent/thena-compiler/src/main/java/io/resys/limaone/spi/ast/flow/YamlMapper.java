package io.resys.limaone.spi.ast.flow;

import io.resys.limaone.ast.Flow_CST.Yaml;


public class YamlMapper {


  public static String getStringValue(Yaml node) {
    if (node == null || node.getValue() == null) {
      return null;
    }
    return node.getValue();
  }

  public static boolean getBooleanValue(Yaml node) {
    if (node == null || node.getValue() == null) {
      return false;
    }
    return Boolean.parseBoolean(node.getValue());
  }
}
