package io.digiexpress.thena.batch.client.spi.createoneruntimeinstance;

/*-
 * #%L
 * thena-batch-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
