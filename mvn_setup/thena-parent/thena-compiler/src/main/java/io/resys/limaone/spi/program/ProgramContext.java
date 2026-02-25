package io.resys.limaone.spi.program;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.limaone.model.Parameter;
import io.resys.limaone.program.DecisionProgram;
import io.resys.limaone.program.FlowProgram;
import io.resys.limaone.program.Program.ProgramLog;
import io.resys.limaone.program.FlowTaskProgram;
import jakarta.annotation.Nullable;

interface ProgramContext extends Serializable {
  ProgramContextNamedValue getValueWithMeta(String typeDefName);
  Serializable getValue(Parameter typeDef);
  
  Map<String, Serializable> toMap(Object input);
  // Throws exception if not found
  Serializable getValue(String typeDefName);
  Optional<Serializable> findValue(String typeDefName);
  
  <T> T getBean(Class<T> type);
  
  
  FlowProgram getFlow(String name);
  DecisionProgram getDecision(String name);
  FlowTaskProgram getService(String name);
  
  ProgramLog getLog();
  ExecutorBuilder executor();
  
  
  @Value.Immutable
  interface ProgramContextNamedValue {
    Boolean getFound();
    @Nullable
    Serializable getValue();
  }
}
