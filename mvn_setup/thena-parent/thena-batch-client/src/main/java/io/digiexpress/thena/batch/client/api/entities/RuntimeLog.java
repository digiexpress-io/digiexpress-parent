package io.digiexpress.thena.batch.client.api.entities;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.immutables.value.Value;

import io.vertx.core.json.JsonObject;



@Value.Immutable
public interface RuntimeLog extends AnyBatchEntity {
  String getRuntimeId();
  OffsetDateTime getCreatedAt();

  Optional<String> getStepId();
  Optional<String> getRowId();
  Optional<String> getExternalId();
  
  String getFormat();                   // string
  String getFormatType();               // user given id for error
  ExecutionLogLevel getLevel();
  
  Optional<String> getStack();
  Optional<JsonObject> getExtra();

  @Override 
  default public BatchDocType getDocType() { 
    return BatchDocType.RUNTIME_LOG; 
  }
  enum ExecutionLogLevel { ERROR, WARN, DEBUG, INFO }
}
