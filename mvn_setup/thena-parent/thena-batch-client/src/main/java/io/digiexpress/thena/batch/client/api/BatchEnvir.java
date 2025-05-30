package io.digiexpress.thena.batch.client.api;

import java.util.List;

import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.smallrye.mutiny.Uni;



public interface BatchEnvir {
  BatchEnvirExecuteBuilder executor();
  BatchEnvirKillBuilder kill();
  
  
  interface BatchEnvirExecuteBuilder {
    BatchEnvirExecuteBuilder commitMessage(String commitMessage);
    BatchEnvirExecuteBuilder commitAuthor(String commitAuthor);
    Uni<RuntimeInstance> execute(RuntimeInstance runtime);
  }
  
  interface BatchEnvirKillBuilder {
    BatchEnvirKillBuilder commitMessage(String commitMessage);
    BatchEnvirKillBuilder commitAuthor(String commitAuthor);
    
    Uni<List<RuntimeInstance>> killAll();
    Uni<RuntimeInstance> killInstance(String runtimeIdOrName);    
  }
}