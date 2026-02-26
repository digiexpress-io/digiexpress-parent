package io.resys.limaone.spi.program.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import io.vertx.core.json.JsonArray;

public class OperationString {

  public static Operation<String> build(String value, Consumer<String> constants) {
    try {
      if(value.indexOf("[") > -1) {
        
        final List<String> values = new ArrayList<>();
        new JsonArray(value.substring(value.indexOf("["))).forEach(item -> {
          final String stringValue = (String) item;
          values.add(stringValue);
          constants.accept(stringValue);
        });
        values.forEach(constants);
        
        boolean patternMatching = value.startsWith("qin") ? true : false;
        if(patternMatching) {
          return xin(values);
        }
        
        boolean contains = value.startsWith("in") ? true : false;
        return contains ? in(values) : notIn(values);
      } else {
        constants.accept(value);
        return in(Arrays.asList(value));
      }
    } catch(Exception e) {
      throw new ExpressionException("Incorrect string expression: " + value + "!", e);
    }
  }
  private static Operation<String> xin(Collection<String> constant) {
    return (String parameter) -> {
      
      for(final var value : constant) {
        if(Router.builderWithSlash().queueName(parameter).routingKey(value).isMatch()) {
          return true;
        }
      }
      
      return false;
    };
  }    
  private static Operation<String> in(Collection<String> constant) {
    return (String parameter) -> {
      
      return constant.contains(parameter);
    };
  }
  private static Operation<String> notIn(Collection<String> constant) {
    return (String parameter) -> !constant.contains(parameter);
  }
   
}
