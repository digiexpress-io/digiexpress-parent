package io.resys.limaone.spi.ast.flow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import io.resys.limaone.ast.Flow_CST.Yaml;
import io.resys.limaone.ast.Flow_CST.YamlInput;
import io.resys.limaone.ast.Flow_CST.YamlParseTree;
import io.resys.limaone.ast.ImmutableHeaders_AST;
import io.resys.limaone.model.Parameter;
import io.resys.limaone.model.Parameter.Direction;
import io.resys.limaone.model.Parameter.ValueType;
import io.resys.limaone.spi.parameter.Parameter_Factory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class YamlMapper {

  public static HeadersBuilder headers() {
    return new HeadersBuilder();
  }
    
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
  
  public static class HeadersBuilder {
    
    public ImmutableHeaders_AST build(YamlParseTree data) {
      Map<String, YamlInput> inputs = data.getInputs();

      int index = 0;
      Collection<Parameter> result = new ArrayList<>();
      for (Map.Entry<String, YamlInput> entry : inputs.entrySet()) {
        if (entry.getValue().getType() == null) {
          continue;
        }
        
        final var required = getBooleanValue(entry.getValue().getRequired());
        try {
          ValueType valueType = ValueType.valueOf(entry.getValue().getType().getValue());
          
          result.add(Parameter_Factory.newParam()
              .id(entry.getValue().getStart() + "")
              .order(index++)
              .name(entry.getKey())
              .valueType(valueType)
              .direction(Direction.IN)
              .required(required)
              .values(getStringValue(entry.getValue().getDebugValue()))
              .build());
          
        } catch (Exception e) {
          final String msg = String.format("Failed to convert data type from: %s, error: %s", entry.getValue().getType().getValue(), e.getMessage());
          log.error(msg);
          result.add(Parameter_Factory.newParam()
              .id(entry.getValue().getStart() + "")
              .order(index++)
              .name(entry.getKey())
              .valueType(ValueType.STRING) // fake it 
              .direction(Direction.IN)
              .required(required)
              .values(getStringValue(entry.getValue().getDebugValue()))
              .build());
        }
      }
      return ImmutableHeaders_AST.builder().acceptDefs(result).build();
    } 
  }

}
