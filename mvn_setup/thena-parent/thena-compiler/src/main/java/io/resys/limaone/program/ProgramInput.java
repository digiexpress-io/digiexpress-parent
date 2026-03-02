package io.resys.limaone.program;

import java.io.Serializable;
import java.util.Map;

import org.immutables.value.Value;

import io.resys.limaone.model.Parameter;
import jakarta.annotation.Nullable;

public interface ProgramInput extends Serializable {
  Serializable getValue(Parameter parameter);
  ResolvedParameter getValueWithMeta(String name);
  ProgramInput withInputs(Map<String, Serializable> nextInputs);
  
  
  @Value.Immutable
  interface ResolvedParameter {
    boolean getFound();
    @Nullable Serializable getValue();
  }
}