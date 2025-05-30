package io.digiexpress.thena.batch.client.api.entities;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.immutables.value.Value;


//Abstract grouping entity, provides under what to collect existing runs

@Value.Immutable
public interface Batch extends AnyBatchEntity {
  String getId();
  String getBatchName(); // unique id, technical name
  String getAppId();
  Optional<String> getExternalId(); 
  
  OffsetDateTime getCreatedAt();
  String getCreatedBy();
  
  Optional<OffsetDateTime> getUpdatedAt();
  Optional<String> getUpdatedBy();
  BatchStatus getStatus();
  
  String getComment();
  
  @Override 
  default public BatchDocType getDocType() { 
    return BatchDocType.BATCH; 
  }
}
