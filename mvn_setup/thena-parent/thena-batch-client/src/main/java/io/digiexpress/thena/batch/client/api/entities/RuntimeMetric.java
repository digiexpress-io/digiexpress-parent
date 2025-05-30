package io.digiexpress.thena.batch.client.api.entities;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.immutables.value.Value;

import io.vertx.core.json.JsonObject;


@Value.Immutable
public interface RuntimeMetric extends AnyBatchEntity {
  String getId();
  String getRuntimeId();
  Optional<String> getStepId();
  
  String getName();
  OffsetDateTime getCreatedAt();
  Optional<OffsetDateTime> getUpdatedAt();
  
  Optional<Long> getValueCounter();
  Optional<JsonObject> getValueStructured();
  
  
  @Override 
  default public BatchDocType getDocType() { 
    return BatchDocType.RUNTIME_METRIC; 
  }
}
