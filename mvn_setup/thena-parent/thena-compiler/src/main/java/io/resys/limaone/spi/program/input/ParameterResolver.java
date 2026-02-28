package io.resys.limaone.spi.program.input;

import java.io.Serializable;

import org.immutables.value.Value;

import io.resys.limaone.model.Parameter;
import jakarta.annotation.Nullable;

@FunctionalInterface
public interface ParameterResolver {
  ResolvedParameter getValue(Parameter typeDef);
  
  @Value.Immutable
  interface ResolvedParameter {
    boolean getSuitable();
    @Nullable Serializable getValue();
  } 
}
