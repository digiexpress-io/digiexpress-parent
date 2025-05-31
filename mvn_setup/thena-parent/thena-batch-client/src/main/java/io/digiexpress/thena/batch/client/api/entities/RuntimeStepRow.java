package io.digiexpress.thena.batch.client.api.entities;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.immutables.value.Value;

import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeExecutionStatus;
import io.vertx.core.json.JsonObject;


@Value.Immutable
public interface RuntimeStepRow extends AnyBatchEntity {
  String getId();
  String getRuntimeId();
  String getStepId();
  
  RuntimeExecutionStatus getExecutionStatus();
  
  OffsetDateTime getCreatedAt();
  Optional<OffsetDateTime> getEndedAt();
  
  Long getRowNumber();
  String getExternalId();
  
  Optional<JsonObject> getInput();
  Optional<JsonObject> getOutput();

  
  @Override 
  default public BatchDocType getDocType() { 
    return BatchDocType.RUNTIME_STEP_ROW; 
  }
}