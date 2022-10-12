package io.digiexpress.client.api;

import java.io.Serializable;

import io.digiexpress.client.api.model.ExecutionState;

public interface DigiexpressExecutor {

  
  interface Builder {
    Builder serviceId(String id);
    StartActivityExecutor activity(String id);
    CreateFillExecutor fill(String id);
    RestoreFillExecutor restore(String id);
    
  }
  interface StartActivityExecutor extends DigiexpressExecutor {
    StartActivityExecutor contextValue(String name, Serializable value);
    ExecutionState build();
  }
  
  interface CreateFillExecutor extends DigiexpressExecutor {
    
  }
  
  interface RestoreFillExecutor extends DigiexpressExecutor {
    
  }

}
