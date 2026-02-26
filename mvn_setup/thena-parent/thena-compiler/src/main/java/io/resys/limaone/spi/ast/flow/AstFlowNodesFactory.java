package io.resys.limaone.spi.ast.flow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import io.resys.limaone.ast.Flow_AST.FlowInputNode;
import io.resys.limaone.ast.Flow_AST.AnyFlowNode;
import io.resys.limaone.ast.Flow_AST.FlowRoot;
import io.resys.limaone.model.Parameter;
import io.resys.limaone.model.Parameter.Direction;
import io.resys.limaone.model.Parameter.ValueType;
import io.resys.limaone.spi.parameter.Parameter_Factory;
import io.resys.limaone.ast.ImmutableHeaders_AST;
import io.resys.limaone.ast.ImmutableMessageRange_AST;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AstFlowNodesFactory {

  public static HeadersBuilder headers() {
    return new HeadersBuilder();
  }
  public static RangeBuilder range() {
    return new RangeBuilder();
  }
  
  public static class RangeBuilder {
    
    public ImmutableMessageRange_AST build(int start, int end, Boolean insert) {
      return ImmutableMessageRange_AST.builder().start(start).end(end).insert(insert).build();
    }

    public ImmutableMessageRange_AST build(int start, int end, Boolean insert, Integer column) {
      return ImmutableMessageRange_AST.builder().start(start).end(end).insert(insert).column(column).build();
    }

    public ImmutableMessageRange_AST build(int start, int end) {
      return ImmutableMessageRange_AST.builder().start(start).end(end).build();
    }

    public ImmutableMessageRange_AST build(int start) {
      return ImmutableMessageRange_AST.builder().start(start).end(start).build();
    }
  }
  
  public static String getStringValue(AnyFlowNode node) {
    if (node == null || node.getValue() == null) {
      return null;
    }
    return node.getValue();
  }

  public static boolean getBooleanValue(AnyFlowNode node) {
    if (node == null || node.getValue() == null) {
      return false;
    }
    return Boolean.parseBoolean(node.getValue());
  }
  
  public static class HeadersBuilder {
    
    public ImmutableHeaders_AST build(FlowRoot data) {
      Map<String, FlowInputNode> inputs = data.getInputs();

      int index = 0;
      Collection<Parameter> result = new ArrayList<>();
      for (Map.Entry<String, FlowInputNode> entry : inputs.entrySet()) {
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
