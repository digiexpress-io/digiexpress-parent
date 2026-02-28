package io.resys.limaone.spi.program.input;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.resys.limaone.model.Parameter;
import io.resys.limaone.program.ProgramInput;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultProgramInput implements ProgramInput {

  private static final long serialVersionUID = 5418242175019286658L;
  private final List<ParameterResolver> resolvers;

  @Override
  public Serializable getValue(Parameter typeDef) {
    if(typeDef.getBeanType() != null && typeDef.getBeanType().isAssignableFrom(this.getClass())) {
      return this;
    }
    for(final var resolver : resolvers) {
      final var resolved = resolver.getValue(typeDef);
      if(resolved.getSuitable()) {
        return resolved.getValue();
      }
    }
    return null;
  }

  public static ProgramInput of(Map<String, Serializable> inputs) {
    return new DefaultProgramInput(Arrays.asList(
        new Inputs_Type1(null),
        new Inputs_Type2(null),
        Inputs_Type3.of(inputs)
    ));
  }
}
