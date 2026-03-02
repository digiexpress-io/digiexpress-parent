package io.resys.limaone.spi.program.input;

import io.resys.limaone.model.Parameter;
import io.resys.limaone.program.ProgramInput.ResolvedParameter;

@FunctionalInterface
public interface ParameterResolver {
  ResolvedParameter getValue(Parameter typeDef);
}
