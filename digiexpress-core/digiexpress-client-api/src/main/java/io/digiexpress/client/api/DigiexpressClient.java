package io.digiexpress.client.api;

import java.io.Serializable;

import io.digiexpress.client.api.model.ActivityState;
import io.digiexpress.client.api.model.ServiceDefEnvir;


public interface DigiexpressClient {
  ExecutorBuilder executor(ServiceDefEnvir envir);
  EnvirBuilder envir();
  
  interface EnvirBuilder {
    EnvirBuilder from(ServiceDefEnvir envir);
    EnvirCommandFormatBuilder addCommand();
    ServiceDefEnvir build();
  }
  
  interface EnvirCommandFormatBuilder {
    EnvirCommandFormatBuilder id(String externalId);
    EnvirCommandFormatBuilder cachless(); 
    EnvirBuilder build();
  }
  
  interface ExecutorBuilder {
    StartActivityExecutor activity(String id);
    StartFillExecutor fill(String id);
    RestoreFillExecutor restore(String id);
  }
  
  
  interface StartActivityExecutor {
    StartActivityExecutor contextValue(String name, Serializable value);
    ActivityState build();
  }
  
  interface StartFillExecutor {
    
  }
  
  interface RestoreFillExecutor {
    
  }
}
