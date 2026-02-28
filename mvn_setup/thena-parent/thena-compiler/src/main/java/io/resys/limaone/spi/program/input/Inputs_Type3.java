package io.resys.limaone.spi.program.input;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import io.resys.limaone.model.Parameter;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Inputs_Type3 implements ParameterResolver {

  // generic data sources that will be used for init of genericData
  private final List<Supplier<Map<String, Serializable>>> suppliers;
  
  // generic data to transform to target
  private Map<String, Serializable> genericData;
  
  public ResolvedParameter getValue(Parameter typeDef) {
    if(genericData == null) {
      genericData = new HashMap<>();
      suppliers.forEach(e -> genericData.putAll(e.get()));
    }
    if(typeDef.getData() && typeDef.getBeanType() != null) {
      final var value = (Serializable) JsonObject.mapFrom(genericData).mapTo(typeDef.getBeanType());
      return ImmutableResolvedParameter.builder().suitable(true).value(value).build();
    }
    
    final var value = (Serializable) genericData.get(typeDef.getName());
    return ImmutableResolvedParameter.builder().suitable(true).value(value).build();
  }
  
  
  public static Inputs_Type3 of(Map<String, Serializable> any) {
    return new Inputs_Type3(Arrays.asList(() -> any));
  }
}
