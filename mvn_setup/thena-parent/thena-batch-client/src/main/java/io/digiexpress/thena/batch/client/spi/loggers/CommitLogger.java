package io.digiexpress.thena.batch.client.spi.loggers;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.event.Level;

import io.digiexpress.thena.batch.client.api.BatchLogConstants;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = BatchLogConstants.SHOW_COMMIT_CREATE_BATCH_ENVIR)
public abstract class CommitLogger {

  private final List<CommitLoggerEvent> events = new ArrayList<>();
  private final long start = System.currentTimeMillis();
  
  @Data @Builder
  private static class CommitLoggerEvent {
    private final Map<String, String> props;
    private final String type;
    private final Level level;
    @Nullable private final Throwable throwable;
  }

  public void append(String type, Level level) {
    this.append(type, Map.of(), level);
  }
  
  public void append(String type, Map<String, String> props, Level level) {
    if(isEventDisabled(type, level)) {
      return;
    }
    final var mergedProps = new HashMap<String, String>();
    mergedProps.put("code", type);
    
    try {
      mergedProps.putAll(props);
    } catch(Exception e) {
      // log failed
    }
    
    events.add(CommitLoggerEvent.builder()
        .props(mergedProps)
        .type(type)
        .build()); 
  }
  public void fail(Throwable throwable) {
    events.add(CommitLoggerEvent.builder()
      .props(Map.of())
      .type("UNKNOWN_ERROR")
      .throwable(throwable)
      .build());
  }
  
  protected boolean isEventDisabled(String type, Level level) {
    return !log.isEnabledForLevel(level);
  }
  
  
  protected Level getLevel() {
    final var isError = events.stream()
      .filter(e -> e.getLevel() == Level.ERROR)
      .findFirst().isPresent();
    if(isError) {
      return Level.ERROR;
    } 
    
    final var isWarn = events.stream()
      .filter(e -> e.getLevel() == Level.WARN)
      .findFirst()
      .isPresent();
    if(isWarn) {
      return Level.WARN;
    }
    
    final var isDebug = events.stream()
        .filter(e -> e.getLevel() == Level.DEBUG)
        .findFirst()
        .isPresent();
    if(isDebug) {
      return Level.DEBUG;
    }
    
    return Level.INFO;
  }
  
  
  protected Optional<Throwable> getCause() {
    final var errors = this.events.stream()
        .filter(f -> f.getThrowable() != null)
        .collect(Collectors.toList());
    
    if(errors.isEmpty()) {
      return Optional.empty();
    }
    
    if(errors.size() == 1) {
      return Optional.ofNullable(errors.iterator().next().getThrowable());
    }
    final var throwable = new Throwable("There where multiple exception see the causes!");
    errors.forEach(e -> throwable.addSuppressed(e.getThrowable()));
    return Optional.of(throwable);
  }
  
  protected String getMessage(Level level) {
    switch (level) {
      case ERROR: {
        return "Failed to create batch envir, cost: {} millis!{}";
      }
      case WARN: {
        return "Created batch envir but there where some warnings, cost: {} millis!{}";
      }
      default: {
        return "Batch envir created, cost: {} millis.{}";
      }
    }
  }

  protected String getEventMessage(Level level) {
  
    final var result = new StringBuilder(System.lineSeparator())
        .append("Batch envir events:").append(System.lineSeparator());
    final var formatEvents = new LogMessageFormatter();
    
    for(final var event : events) {
      formatEvents.append(event.getType(), event.getProps(), event.getLevel(), event.getThrowable());
      
      // log down stack
      if(event.getThrowable() != null) {
        log.error(event.getThrowable().getMessage(), event.getThrowable());
      }
    }
    result.append(formatEvents.build());
    return result.toString();
  }
  
  
  public void close() {
    if(this.events.isEmpty()) {
      return;
    }
    
    try {
      final var cost = System.currentTimeMillis() - start;
      final var level = getLevel();
      
      final var builder = log
          .atLevel(level)
          .setMessage(getMessage(level))
          .addArgument(cost)
          .addArgument(getEventMessage(level));
      getCause().ifPresent(cause -> builder.setCause(cause));
  
      builder.log();
    } catch(Exception e) {
      log.error("FAILED TO CREATE COMMIT LOG.... {}", e.getMessage(), e);
    }
  }
}
