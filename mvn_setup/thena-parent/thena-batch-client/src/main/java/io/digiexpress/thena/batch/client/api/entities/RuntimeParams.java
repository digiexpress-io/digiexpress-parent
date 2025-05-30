package io.digiexpress.thena.batch.client.api.entities;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.immutables.value.Value;

import io.vertx.core.json.JsonObject;



@Value.Immutable
public interface RuntimeParams extends AnyBatchEntity {
  String getId();
  String getRuntimeId();
  Optional<String> getStepId();
  Optional<String> getRowId();
  
  String getName();
  String getComment();
  OffsetDateTime getCreatedAt();
  JsonObject getBody();
  
  @Override 
  default public BatchDocType getDocType() { 
    return BatchDocType.RUNTIME_PARAMS; 
  }

}
