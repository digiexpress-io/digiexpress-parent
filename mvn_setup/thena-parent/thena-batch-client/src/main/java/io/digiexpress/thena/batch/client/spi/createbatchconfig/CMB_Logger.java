package io.digiexpress.thena.batch.client.spi.createbatchconfig;

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

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.event.Level;

import io.digiexpress.thena.batch.client.api.entities.Batch;
import io.digiexpress.thena.batch.client.api.entities.BatchConsumer;
import io.digiexpress.thena.batch.client.spi.loggers.CommitLogger;
import lombok.Getter;


public class CMB_Logger extends CommitLogger {

  private final AtomicInteger batchCount = new AtomicInteger(0);
  private final AtomicInteger batchConsumerCount = new AtomicInteger(0);
  

  @Getter
  public static enum CBE_LogEventType {
    BATCH_BUILDER_BUILD_METHOD_NOT_CALLED("BATCH_BUILDER_BUILD_METHOD_NOT_CALLED", Level.ERROR),
    BATCH_CONSUMER_BUILDER_BUILD_METHOD_NOT_CALLED("BATCH_CONSUMER_BUILDER_BUILD_METHOD_NOT_CALLED", Level.ERROR),
    
    BATCH_CONSUMER_UPDATED("BATCH_CONSUMER_UPDATED", Level.INFO),
    BATCH_CONSUMER_CREATED("BATCH_CONSUMER_CREATED", Level.INFO),
    
    BATCH_UPDATED("BATCH_UPDATED", Level.INFO),
    BATCH_CREATED("BATCH_CREATED", Level.INFO),
    BATCH_SUMMARY("BATCH_SUMMARY", Level.INFO),
    
    
    UNKNOWN_ERROR("UNKNOWN_ERROR", Level.ERROR),
    ;
    private final Level level; 
    private final String name;
    
    private CBE_LogEventType(String name, Level level) {
      this.name = name;
      this.level = level;
    }
  }
  
  public void append(CBE_LogEventType type) {
    super.append(type.getName(), Map.of(), type.getLevel());
  }
  
  public void append(CBE_LogEventType type, BatchConsumer consumer) {
    if(type == CBE_LogEventType.BATCH_CONSUMER_UPDATED || type == CBE_LogEventType.BATCH_CONSUMER_CREATED) {
      batchConsumerCount.incrementAndGet();
    }
    
    super.append(type.getName(), Map.of(
      "code", type.getName(),
      "consumerId", consumer.getId(),
      "appId", consumer.getAppId(),
      "qualifiedJavaName", consumer.getQualifiedJavaName(),
      "batchName", consumer.getBatchName()
    ), 
    type.getLevel());
  }
  public void append(CBE_LogEventType type, Batch batch) {
    if(type == CBE_LogEventType.BATCH_CREATED || type == CBE_LogEventType.BATCH_UPDATED) {
      batchCount.incrementAndGet();
    }
    super.append(type.getName(), Map.of(
      "code", type.getName(),
      "batchId", batch.getId(),
      "appId", batch.getAppId(),
      "batchName", batch.getBatchName()
    ), 
    type.getLevel()); 
  }

  public void close() {
    super.append(
        CBE_LogEventType.BATCH_SUMMARY.getName(),
        Map.of(
            "batchCount", String.valueOf(batchCount.get()),
            "batchConsumerCount", String.valueOf(batchConsumerCount.get())
        ),
        CBE_LogEventType.BATCH_SUMMARY.getLevel());
    super.close();
  }
  
}
