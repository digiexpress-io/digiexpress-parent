package io.digiexpress.thena.batch.client.api.executor;

import io.smallrye.mutiny.Uni;

public interface Executor<Entity, ExecutorConfig> {
  ExecutorQuery<Entity, ExecutorConfig> before(ExecutorContext context);
  
  Uni<ExecutorEntity> accept(Entity entity, ExecutorConfig config, ExecutorContext context);
  
  Uni<ExecutorResult> after(ExecutorConfig config, ExecutorContext context);  
}
