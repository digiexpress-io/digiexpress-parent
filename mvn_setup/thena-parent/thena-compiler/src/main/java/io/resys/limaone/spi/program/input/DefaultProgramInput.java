package io.resys.limaone.spi.program.input;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import io.resys.limaone.model.Parameter;
import io.resys.limaone.program.ProgramInput;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@SuppressWarnings("unused")
public class DefaultProgramInput implements ProgramInput {

  private static final long serialVersionUID = 5418242175019286658L;
  private final io.resys.limaone.program.Runtime runtime;
  private final List<ParameterResolver> resolvers;
  private final Optional<ProgramInput> parent;

  @Override
  public Serializable getValue(Parameter typeDef) {
    if(typeDef.getBeanType() != null && typeDef.getBeanType().isAssignableFrom(this.getClass())) {
      return this;
    }
    if(typeDef.getBeanType() != null && typeDef.getBeanType().isAssignableFrom(runtime.getClass())) {
      return runtime;
    }
    for(final var resolver : resolvers) {
      final var resolved = resolver.getValue(typeDef);
      if(resolved.getSuitable()) {
        return resolved.getValue();
      }
    }
    return null;
  }

  public static ProgramInput of(Map<String, Serializable> inputs, io.resys.limaone.program.Runtime runtime) {
    return new DefaultProgramInput(runtime, Arrays.asList(
        new Inputs_Type1(null),
        new Inputs_Type2(null),
        Inputs_Type3.of(inputs)
    ), Optional.empty());
  }
  
  public ProgramInput withInputs(Map<String, Serializable> nextInputs) {
    return new DefaultProgramInput(runtime, Stream.concat(
        Arrays.asList(Inputs_Type3.of(nextInputs)).stream(), 
        resolvers.stream()
      ).toList(), Optional.of(this));
  }
}
