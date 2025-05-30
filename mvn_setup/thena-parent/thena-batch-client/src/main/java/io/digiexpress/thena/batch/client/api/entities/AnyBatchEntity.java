package io.digiexpress.thena.batch.client.api.entities;

import io.resys.thena.api.entities.TenantEntity;

// marker interface
public interface AnyBatchEntity extends TenantEntity {
  String getId(); 
  BatchDocType getDocType();

  
  enum BatchDocType {
    RUNTIME_INSTANCE,
    RUNTIME_STEP,
    RUNTIME_STEP_ROW,
    RUNTIME_LOG,

    
    RUNTIME_METRIC,
    RUNTIME_PARAMS,
    
    BATCH,
    BATCH_CONSUMER,
  }
}
