package io.resys.limaone.program;

import java.io.Serializable;
import java.time.OffsetDateTime;

import io.resys.limaone.program.Compiler.Bundle;

public interface Runtime extends Serializable {
  Heap getHeap();
  EnvironmentProperties getProperties();
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
}
