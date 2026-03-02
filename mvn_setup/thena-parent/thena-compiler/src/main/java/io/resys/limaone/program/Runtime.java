package io.resys.limaone.program;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.limaone.model.Parameter;
import io.resys.limaone.program.Compiler.Bundle;
import jakarta.annotation.Nullable;

public interface Runtime extends Serializable {
  Heap getHeap();
  EnvironmentProperties getProperties();
  ProgramParameters getParameters();
  Bundle getBundle();
  
  <T> T getBean(Class<T> type); 
  

  // dump everything from runtime
  interface Heap extends Serializable {
    
  }
  
  interface RuntimeUser {
    String getId();
    boolean isAuthenticated();
    <T> T unwrap(Class<T> implType);
  }
  
  interface EnvironmentProperties {
    boolean isDev();
    OffsetDateTime getTargetDate();
    RuntimeUser getUser();
  }
  
  interface ProgramParameters extends Serializable {

    // Throws exception if not found
    Serializable getValue(String typeDefName);
    Serializable getValue(Parameter typeDef);
    
    Optional<Serializable> findValue(String typeDefName);
    ProgramParameterWithMeta findValueWithMeta(String typeDefName);
  }
  
  @Value.Immutable
  interface ProgramParameterWithMeta {
    Boolean getFound(); // parameter is defined in path, but it can be defined as null
    @Nullable Serializable getValue();
  }
}
