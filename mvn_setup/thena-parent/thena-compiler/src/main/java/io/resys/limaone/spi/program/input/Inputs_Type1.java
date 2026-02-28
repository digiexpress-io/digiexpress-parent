package io.resys.limaone.spi.program.input;

import java.io.Serializable;
import java.util.function.Function;

import io.resys.limaone.model.Parameter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Inputs_Type1 implements ParameterResolver {
  private final Function<Parameter, Object> callbackThatWillSupplyAllData;

  @Override
  public ResolvedParameter getValue(Parameter typeDef) {
    if(callbackThatWillSupplyAllData == null) {
      return ImmutableResolvedParameter.builder().suitable(false).build();
    }
    final Serializable target = (Serializable) callbackThatWillSupplyAllData.apply(typeDef);
    if(target != null) {
      return ImmutableResolvedParameter.builder().suitable(true).value(target).build();
    }
    return ImmutableResolvedParameter.builder().suitable(false).build();
  }
}