package io.digiexpress.thena.batch.client.spi.createoneruntimeinstance;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.event.Level;

import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.RuntimeParams;
import io.digiexpress.thena.batch.client.spi.loggers.CommitLogger;
import io.vertx.core.json.JsonObject;
import lombok.Getter;


public class CORI_Logger extends CommitLogger {

  @Getter
  public static enum CRI_LogEventType {
    RUNTIME_INSTANCE_CREATED("RUNTIME_INSTANCE_CREATED", Level.INFO),
    RUNTIME_INSTANCE_PARAMS_CREATED("RUNTIME_INSTANCE_PARAMS_CREATED", Level.INFO),
    BATCH_NOT_FOUND("BATCH_NOT_FOUND", Level.ERROR),
    UNKNOWN_ERROR("UNKNOWN_ERROR", Level.ERROR),
    ;
    
    private final Level level; 
    private final String name;
    
    private CRI_LogEventType(String name, Level level) {
      this.name = name;
      this.level = level;
    }
  }
  
  @SuppressWarnings("unchecked")
  public void append(CRI_LogEventType type, JsonObject args) {
    final var props = new HashMap<String, String>();
    try {
      props.putAll(args.mapTo(Map.class));
    } catch(Exception e) {
      // log failed
    }
    super.append(type.getName(), props, type.getLevel());
  }
  
  public void append(RuntimeInstance instance, CRI_LogEventType type) {
    super.append(type.getName(), Map.of(
        "batchId", instance.getBatchId(),
        "instanceName", instance.getName()
      ), 
      type.getLevel());
  }
  
  public void append(RuntimeParams instance, CRI_LogEventType type) {
    super.append(type.getName(), Map.of(
        "paramName", instance.getName(),
        "paramBody", instance.getBody().encode()
      ), 
      type.getLevel());
  }
}
