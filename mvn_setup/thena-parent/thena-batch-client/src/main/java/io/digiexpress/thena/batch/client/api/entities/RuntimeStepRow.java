package io.digiexpress.thena.batch.client.api.entities;

import java.beans.Transient;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeExecutionStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeStatus;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;


@Value.Immutable
public interface RuntimeStepRow extends AnyBatchEntity {
  String getId();
  String getStepId();
  
  RuntimeStatus getStatus();
  RuntimeExecutionStatus getExecutionStatus();
  
  OffsetDateTime getCreatedAt();
  Optional<OffsetDateTime> getEndedAt();
  
  Long getRowNumber();
  
  Optional<JsonObject> getInput();
  Optional<JsonObject> getOutput();
  
  @Transient @JsonIgnore
  @Nullable RuntimeInstanceTransitives getTransitives();
  
  @Override 
  default public BatchDocType getDocType() { 
    return BatchDocType.RUNTIME_STEP_ROW; 
  }
}