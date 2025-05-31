package io.digiexpress.thena.batch.client.spi.batchenvir;

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
import java.util.Optional;

import org.slf4j.event.Level;

import io.digiexpress.thena.batch.client.api.BatchLogConstants;
import io.digiexpress.thena.batch.client.api.entities.BatchConfig.BatchConfigWithExecutor;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStep;
import io.digiexpress.thena.batch.client.api.executor.ExecutorContext;
import io.digiexpress.thena.batch.client.api.executor.ExecutorResult;
import io.digiexpress.thena.batch.client.spi.loggers.LogMessageFormatter;
import io.resys.thena.support.RepoAssert;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = BatchLogConstants.SHOW_COMMIT_CREATE_BATCH_ENVIR)
@Getter
public enum BatchEnvirLogger {
  
  INSTANCE_CREATED("INSTANCE_CREATED", Level.INFO),
  INSTANCE_STARTED("INSTANCE_STARTED", Level.INFO),
  INSTANCE_EXECUTING("INSTANCE_EXECUTING", Level.INFO),
  INSTANCE_COMPLETED("INSTANCE_COMPLETED", Level.INFO),
  INSTANCE_ERROR("INSTANCE_EXECUTION_ERROR", Level.ERROR),
  
  STEP_CREATED("STEP_CREATED", Level.INFO),
  STEP_STARTED("STEP_STARTED", Level.INFO),
  STEP_EXECUTING("STEP_EXECUTING", Level.INFO),
  STEP_COMPLETED("STEP_COMPLETED", Level.INFO),
  STEP_SKIP("STEP_SKIP", Level.WARN),
  
  STEP_ERROR("STEP_ERROR", Level.ERROR),
  STEP_ENTITY("STEP_ENTITY", Level.DEBUG),
  STEP_ENTITY_ERROR("STEP_ENTITY_ERROR", Level.ERROR),
  
  METRIC_ERROR("METRIC_ERROR", Level.ERROR),
  
  UNKNOWN_ERROR("UNKNOWN_ERROR", Level.ERROR),
  ;
  private final Level level; 
  private final String name;
  
  private BatchEnvirLogger(String name, Level level) {
    this.name = name;
    this.level = level;
  }
  
  public BatchEnvirLoggerBuilder withContext(ExecutorContext ctx) {
    /*
    private boolean isEventDisabled(LoggerEvent type) {
      return !log.isEnabledForLevel(type.getLevel());
    }*/
    
    return new BatchEnvirLoggerBuilder(ctx, this.getLevel(), this.getName());
  }

  @RequiredArgsConstructor
  public static class BatchEnvirLoggerBuilder {
    private final ExecutorContext ctx;
    private final Level initLevel; 
    private final String name;
    private Level level; 
    
    private Throwable cause;
    private BatchConfigWithExecutor config;
    private ExecutorResult executorResult;
    private RuntimeStep step;
    public BatchEnvirLoggerBuilder level(Level level) {
      RepoAssert.notNull(level, () -> "Level must be defined");
      this.level = level;
      return this;
    }
    public BatchEnvirLoggerBuilder cause(Throwable cause) {
      if(this.cause != null) {
        this.cause.addSuppressed(cause);
      } else {
        this.cause = cause;        
      }

      return this;
    }
    public BatchEnvirLoggerBuilder addProps(BatchConfigWithExecutor config) {
      this.config = config;
      return this;
    }
    public BatchEnvirLoggerBuilder addProps(RuntimeStep step) {
      this.step = step;
      return this;
    }
    public BatchEnvirLoggerBuilder addProps(ExecutorResult executorResult) {
      this.executorResult = executorResult;
      return this;
    }
    public void append(String message, Object ...args) {
      final var builder = log.atLevel(Optional.ofNullable(level).orElse(initLevel));
      if(cause != null) {
        builder.setCause(cause);
      }
      if(args == null) {
        builder.log(formatMessage(message));
      } else {
        builder.log(formatMessage(message), args);
      }
    }

    public void append(String message) {
      final var builder = log.atLevel(Optional.ofNullable(level).orElse(initLevel));
      if(cause != null) {
        builder.setCause(cause);
      }
      builder.log(formatMessage(message));
    }
    
    private String formatMessage(String userMessage) {
      
      final var props = new HashMap<String, String>();
      
      props.putAll(Map.of(
        "batch", ctx.getBatch().getBatchName(),
        "instance", ctx.getInstance().getName(),
        "status", ctx.getInstance().getStatus().name(),
        "user", ctx.getScope().getCommitAuthor(),
        "message", userMessage
      ));
      
      if(config != null) {
        props.putAll(Map.of(
            "consumerName", config.getBatchConsumer().getConsumerName(),
            "consumerQualifiedJavaName", config.getBatchConsumer().getQualifiedJavaName()
          ));
      }
      if(executorResult != null) {
        props.putAll(Map.of(
            "stepExecutorStatus", executorResult.getStatus().name()
          ));
      }
      if(step != null) {
        props.putAll(Map.of(
            "stepName", step.getName(),
            "stepCreatedAt", step.getCreatedAt().toString(),
            "stepExcecutionStatus", step.getExecutionStatus().name()
          ));
      }
      
      final var formatEvents = new LogMessageFormatter().append(name, props, level, cause);
      return System.lineSeparator() + formatEvents.build().toString();
    }
  }
}
