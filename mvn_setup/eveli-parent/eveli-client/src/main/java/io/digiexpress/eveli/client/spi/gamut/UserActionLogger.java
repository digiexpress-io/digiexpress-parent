package io.digiexpress.eveli.client.spi.gamut;

/*-
 * #%L
 * eveli-client
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import io.digiexpress.eveli.client.api.GamutClient.UserActionMeta;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliRuntime;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserActionLogger {

  private final long start = System.currentTimeMillis();
  private final Map<EventType, LogEvent> entries = new LinkedHashMap<>();
  
  private enum EventType {
    EVELI_RUNTIME,
    STENCIL_SERVICE,
    AUTH,
    WRENCH_ALLOWED_ROLES,
    PROCESS_INSTANCE,
    FORM_ID,
    FORM_TAG,
    FORM_CREATE
  }
  
  @Data @Getter @Setter @Builder
  private static class LogEvent {
    private final EventType type;
    private final Long start;
    private Long end;
  }
  
  
  // FIND ASSETS FROM CACHE
  public void startRuntime() {
    createEvent(EventType.EVELI_RUNTIME);
  }
  public void endRuntime(EveliRuntime runtime) {
    endEvent(EventType.EVELI_RUNTIME);
  }
  
  
  // FIND STENCIL SERVICE FROM RUNTIME
  public void startStencilService() {
    createEvent(EventType.STENCIL_SERVICE);
  }
  public void endStencilService(UserActionMeta meta) {
    endEvent(EventType.STENCIL_SERVICE);
  }
  
  
  // QUERY FOR USER PRINCIPAL
  public void startAuth() {
    createEvent(EventType.AUTH);
  }
  public void endAuth() {
    endEvent(EventType.AUTH);
  }
  
  
  // QUERY FOR WRENCH DT and RUN TO SEE ALLOWED ROLES AGAINS AUTH
  public void startWrenchAllowedRoles() {
    createEvent(EventType.WRENCH_ALLOWED_ROLES);
  }
  public void endWrenchAllowedRoles() {
    endEvent(EventType.WRENCH_ALLOWED_ROLES);
  }
  
  
  // CREATE PROCESS INSTANCE
  public void startProcessInstance() {
    createEvent(EventType.PROCESS_INSTANCE);
  }
  public void endProcessInstance() {
    endEvent(EventType.PROCESS_INSTANCE);
  }
  
  
  // QUERY FORM BY ID
  public void startFormId() {
    createEvent(EventType.FORM_ID);
  }
  public void endFormId() {
    endEvent(EventType.FORM_ID);
  }
  
  
  // QUERY FORM TAG FOR FINDING ID
  public void startFormTag() {
    createEvent(EventType.FORM_TAG);
  }
  public void endFormTag() {
    endEvent(EventType.FORM_TAG);
  }
  
  // CREATE QUESTIONNAIRE
  public void startFormCreate() {
    createEvent(EventType.FORM_CREATE);
  }
  
  public void endFormCreate() {
    endEvent(EventType.FORM_CREATE);
  }
  
  
  private void createEvent(EventType type) {
    final var event = LogEvent.builder().type(type).start(System.currentTimeMillis()).build();
    entries.put(event.getType(), event);
  }
  
  private void endEvent(EventType type) {
    Optional.ofNullable(entries.get(type)).ifPresent(event -> event.setEnd(System.currentTimeMillis()));
  }
  
  public void close() {
    final long end = System.currentTimeMillis();
    
    final var builder = new StringBuilder("GAMUT create action(total cost: ").append(end-start).append(" ms) timetable: ").append(System.lineSeparator());
    
    for(final var entry : this.entries.values()) {
      if(entry.getEnd() == null) {
        continue;
      }
      builder.append("  - cost: ").append(entry.getEnd() - entry.getStart()).append(" ms, event type: ").append(entry.getType()).append(System.lineSeparator());
    }
    
    log.info(builder.toString());
  }
}
