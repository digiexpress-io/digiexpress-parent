package io.digiexpress.thena.batch.client.api.persistence;

import java.util.List;
import java.util.Optional;

import io.digiexpress.thena.batch.client.api.entities.Batch;
import io.digiexpress.thena.batch.client.api.entities.BatchConsumer;
import io.digiexpress.thena.batch.client.api.entities.BatchContainers.BatchTenantContainer;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStep;
import io.smallrye.mutiny.Uni;

public interface BatchDbQuery {
  Uni<BatchTenantContainer> findAll();
  
  BatchDbBatchQuery queryBatches();
  BatchDbBatchConsumerQuery queryBatchConsumers();
  
  BatchDbInstanceQuery queryInstances();
  
  BatchDbStepQuery querySteps();
  
  
  interface BatchDbBatchQuery {
    Uni<List<Batch>> findAllByAppId(String appId, boolean lockForUpdate);
    Uni<Optional<Batch>> findOneByAppIdAndName(String appId, String batchName);     
  }
  
  interface BatchDbInstanceQuery {
    Uni<RuntimeInstance> getById(String id, boolean lockForUpdate);
    Uni<List<RuntimeInstance>> findAllByStatus(List<RuntimeStatus> status);
    Uni<Long> nextSequence();
  }
  
  interface BatchDbStepQuery {
    Uni<RuntimeStep> getById(String id, boolean lockForUpdate);
    Uni<List<RuntimeStep>> findAllByInstanceId(String instanceId);
    Uni<List<RuntimeStep>> findAllByInstanceStatus(List<RuntimeStatus> status);
  }
  
  
  interface BatchDbBatchConsumerQuery {
    Uni<List<BatchConsumer>> findAllByAppId(String appId, boolean lockForUpdate);
  }
}