package io.digiexpress.thena.batch.client.api.executor;

import io.smallrye.mutiny.Multi;


public interface ExecutorQuery<Entity, ExecutorConfig> {   
  ExecutorConfig getConfig();
  Multi<Entity> findAll();
  
}
