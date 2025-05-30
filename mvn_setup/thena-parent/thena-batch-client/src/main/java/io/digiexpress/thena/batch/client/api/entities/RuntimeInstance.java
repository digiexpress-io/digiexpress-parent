package io.digiexpress.thena.batch.client.api.entities;

import java.beans.Transient;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.annotation.Nullable;



@Value.Immutable
public interface RuntimeInstance extends AnyBatchEntity {
  String getId();
  String getBatchId();
  
  OffsetDateTime getCreatedAt();
  Optional<OffsetDateTime> getEndedAt();
  
  String getName();
  RuntimeStatus getStatus();
  RuntimeExecutionStatus getExecutionStatus();
  
  String getComment();
    
  @Transient @JsonIgnore
  @Nullable RuntimeInstanceTransitives getTransitives();
  
  @Override 
  default public BatchDocType getDocType() { 
    return BatchDocType.RUNTIME_INSTANCE; 
  }

  
  enum RuntimeExecutionStatus {
    OK, ERROR
  }
  
  enum RuntimeStatus { 
    CREATED, EXECUTING, SKIPPED, COMPLETED, CANCELLED
  }
}
