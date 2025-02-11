package io.digiexpress.eveli.envir.spi.actions;

/*-
 * #%L
 * eveli-envir
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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliRuntime;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class EveliRuntimeLogger {
  private final List<LogEvent> events = new ArrayList<>();
  private final long start = System.currentTimeMillis();
  
  @Data @Builder
  private static class LogEvent {
    private final Optional<String> id;
    private final Optional<String> name;
    private final Optional<OffsetDateTime> startsAt;
    private final LogEventType type;
  }
  
  private static enum LogEventType {
    RUNTIME_PUT_TO_CACHE,
    RUNTIME_GET_FROM_CACHE,
    DEPLOYMENT_PUT_LAST_TO_CACHE,
    DEPLOYMENT_GET_LAST_FROM_CACHE,
    DEPLOYMENT_GET_LAST_FROM_EXTERNAL,
    DEPLOYMENT_GET_LAST_FROM_DB,
  }
  
  public EveliRuntimeLogger cachingRuntime(EveliRuntime runtime) {
    events.add(LogEvent.builder()
        .id(Optional.ofNullable(runtime.getDeploymentId()))
        .name(Optional.ofNullable(runtime.getName()))
        .startsAt(Optional.ofNullable(runtime.getStartsAt()))
        .type(LogEventType.RUNTIME_PUT_TO_CACHE)
        .build());
    return this;
  }

  public EveliRuntimeLogger cachedRuntime(Optional<EveliRuntime> runtime) {
    events.add(LogEvent.builder()
        .id(runtime.map(e -> e.getDeploymentId()))
        .name(runtime.map(e -> e.getName()))
        .startsAt(runtime.map(e -> e.getStartsAt()))
        .type(LogEventType.RUNTIME_GET_FROM_CACHE)
        .build());
    return this;
  }
  
  
  public EveliRuntimeLogger lastCachedDeployment(Optional<EveliDeployment> deployment) {
    events.add(LogEvent.builder()
        .id(deployment.map(e -> e.getId()))
        .name(deployment.map(e -> e.getName()))
        .startsAt(deployment.map(e -> e.getStartsAt()))
        .type(LogEventType.DEPLOYMENT_GET_LAST_FROM_CACHE)
        .build());
    return this;
  }
  
  
  public EveliRuntimeLogger lastExternalDeployment(Optional<EveliDeployment> deployment) {
    events.add(LogEvent.builder()
        .id(deployment.map(e -> e.getId()))
        .name(deployment.map(e -> e.getName()))
        .startsAt(deployment.map(e -> e.getStartsAt()))
        .type(LogEventType.DEPLOYMENT_GET_LAST_FROM_EXTERNAL)
        .build());
    return this;
  }
  
  public EveliRuntimeLogger lastQueriedDeployment(Optional<EveliDeployment> deployment) {
    events.add(LogEvent.builder()
        .id(deployment.map(e -> e.getId()))
        .name(deployment.map(e -> e.getName()))
        .startsAt(deployment.map(e -> e.getStartsAt()))
        .type(LogEventType.DEPLOYMENT_GET_LAST_FROM_DB)
        .build());
    return this;
  }
  
  
  public EveliRuntimeLogger cachingDeployment(Optional<EveliDeployment> deployment) {
    events.add(LogEvent.builder()
        .id(deployment.map(e -> e.getId()))
        .name(deployment.map(e -> e.getName()))
        .startsAt(deployment.map(e -> e.getStartsAt()))
        .type(LogEventType.DEPLOYMENT_PUT_LAST_TO_CACHE)
        .build());
    return this;
  }
  
  public void info() {
    final var result = new StringBuilder("events:").append(System.lineSeparator());
    for(final var event : events) {
      result
        .append("  - ").append(event.getType()).append(":").append(System.lineSeparator())
        .append("    id: ").append(event.getId()).append(":").append(System.lineSeparator())
        .append("    name: ").append(event.getName()).append(":").append(System.lineSeparator())
        .append("    starts at: ").append(event.getStartsAt()).append(":").append(System.lineSeparator())
      ;
    }
    final var cost = System.currentTimeMillis() - start;
    log.info("Resolving runtime, cost: {} millis, \r\n{}", cost, result.toString());
  }
  
  public void error() {
    final var result = new StringBuilder("events:").append(System.lineSeparator());
    for(final var event : events) {
      result
        .append("  - ").append(event.getType()).append(":").append(System.lineSeparator())
        .append("    id: ").append(event.getId()).append(":").append(System.lineSeparator())
        .append("    name: ").append(event.getName()).append(":").append(System.lineSeparator())
        .append("    starts at: ").append(event.getStartsAt()).append(":").append(System.lineSeparator())
      ;
    }
    final var cost = System.currentTimeMillis() - start;
    log.error("Failed to resolve runtime, cost: {} millis, \r\n{}", cost, result.toString());
  }
}
