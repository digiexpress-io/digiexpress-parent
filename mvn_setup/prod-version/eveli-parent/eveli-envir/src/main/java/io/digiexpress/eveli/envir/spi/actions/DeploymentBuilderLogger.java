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

import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class DeploymentBuilderLogger {
  private final List<LogEvent> events = new ArrayList<>();
  private final long start = System.currentTimeMillis();
  
  @Data @Builder
  private static class LogEvent {
    private final String id;
    private final String name;
    private final OffsetDateTime startsAt;
    private final LogEventType type;
  }
  
  private static enum LogEventType {
    DEPLOYMENT_IN_ERROR_SKIPPING,
    DEPLOYMENT_SET_READY,
    DEPLOYMENT_SET_DEPLOYED,

  }
  public DeploymentBuilderLogger setDeployed(EveliDeployment deployment) {
    events.add(LogEvent.builder()
        .id(deployment.getId())
        .name(deployment.getName())
        .startsAt(deployment.getStartsAt())
        .type(LogEventType.DEPLOYMENT_SET_DEPLOYED)
        .build());
    return this;
  }
  
  public DeploymentBuilderLogger setReady(EveliDeployment deployment) {
    events.add(LogEvent.builder()
        .id(deployment.getId())
        .name(deployment.getName())
        .startsAt(deployment.getStartsAt())
        .type(LogEventType.DEPLOYMENT_SET_READY)
        .build());
    return this;
  }
  
  public DeploymentBuilderLogger setSkipping(EveliDeployment deployment) {
    events.add(LogEvent.builder()
        .id(deployment.getId())
        .name(deployment.getName())
        .startsAt(deployment.getStartsAt())
        .type(LogEventType.DEPLOYMENT_IN_ERROR_SKIPPING)
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
    log.info("New version deployed, cost: {} millis, \r\n", cost, result.toString());
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
    log.error("Failed to deploy new version, cost: {} millis, \r\n{}", cost, result.toString());
  }
}
