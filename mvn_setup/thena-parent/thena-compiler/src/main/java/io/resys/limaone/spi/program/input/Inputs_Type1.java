package io.resys.limaone.spi.program.input;

import java.io.Serializable;
import java.util.function.Function;

import io.resys.limaone.model.Parameter;
import io.resys.limaone.program.ImmutableResolvedParameter;
import io.resys.limaone.program.ProgramInput.ResolvedParameter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Inputs_Type1 implements ParameterResolver {
  private final Function<Parameter, Object> callbackThatWillSupplyAllData;

  @Override
  public ResolvedParameter getValue(Parameter typeDef) {
    if(callbackThatWillSupplyAllData == null) {
      return ImmutableResolvedParameter.builder().found(false).build();
    }
    final Serializable target = (Serializable) callbackThatWillSupplyAllData.apply(typeDef);
    if(target != null) {
      return ImmutableResolvedParameter.builder().found(true).value(target).build();
    }
    return ImmutableResolvedParameter.builder().found(false).build();
  }
}