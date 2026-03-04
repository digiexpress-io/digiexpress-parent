package io.resys.limaone.yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.resys.limaone.ast.Yaml_CST.Yaml;


public class YamlMapper {
  private final ObjectMapper yaml = new ObjectMapper(new YAMLFactory());

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
  
  public ObjectMapper unwrap() {
    return yaml;
  }
}
