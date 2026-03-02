package io.resys.limaone.spi.program.input;

import java.io.Serializable;

import io.resys.limaone.model.FlowTask.ServiceData;
import io.resys.limaone.model.Parameter;
import io.resys.limaone.program.ImmutableResolvedParameter;
import io.resys.limaone.program.ProgramInput.ResolvedParameter;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Inputs_Type2 implements ParameterResolver {
  //data object that should be directly transformed to target
  private final Object serviceData;
  private boolean isBuilt;
  private boolean isSuitable;
  
  public ResolvedParameter getValue(Parameter typeDef) {
    if(serviceData == null) {
      return ImmutableResolvedParameter.builder().found(false).build();
    }
    if(!isBuilt) {
      isBuilt = true;
      isSuitable = serviceData.getClass().isAnnotationPresent(ServiceData.class);
    }
    
    if(!isSuitable) {
      return ImmutableResolvedParameter.builder().found(false).build();
    }
    
    if(Boolean.TRUE.equals(typeDef.getData())) {
      final var value = (Serializable) JsonObject.mapFrom(serviceData).mapTo(typeDef.getBeanType());
      return ImmutableResolvedParameter.builder().found(true).value(value).build();
    }
    return ImmutableResolvedParameter.builder().found(false).build();
  }
}
