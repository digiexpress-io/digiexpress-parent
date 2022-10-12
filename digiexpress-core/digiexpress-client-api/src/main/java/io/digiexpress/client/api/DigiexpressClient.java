package io.digiexpress.client.api;

import java.io.Serializable;

import io.digiexpress.client.api.model.ExecutionState;
import io.digiexpress.client.api.model.ServiceDefEnvir;
import io.digiexpress.client.api.model.ServiceDefEnvir.ServiceDefEnvirBuilder;


public interface DigiexpressClient {
  ExecutorBuilder executor(ServiceDefEnvir envir);
  ServiceDefEnvirBuilder envir();


  
  interface ExecutorBuilder {
    ExecutorBuilder serviceId(String id);
    StartActivityExecutor activity(String id);
    CreateFillExecutor fill(String id);
    RestoreFillExecutor restore(String id);
    
  }
  interface StartActivityExecutor {
    StartActivityExecutor contextValue(String name, Serializable value);
    ExecutionState build();
  }
  
  interface CreateFillExecutor {
    
  }
  
  interface RestoreFillExecutor {
    
  }
}
