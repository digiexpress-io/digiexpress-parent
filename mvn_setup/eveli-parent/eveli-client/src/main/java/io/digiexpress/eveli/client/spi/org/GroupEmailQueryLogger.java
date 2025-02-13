package io.digiexpress.eveli.client.spi.org;

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

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GroupEmailQueryLogger {
  private final List<LogEvent> events = new ArrayList<>();
  private final long start = System.currentTimeMillis();
  
  @Data @Builder
  private static class LogEvent {
    private final String id;
    private final String text;
    private final LogEventType type;
  }
  
  private static enum LogEventType {
    QUERY_FOR_EMAILS_BY_GROUP_NAME,
    REST_API_SERVICE_URL,
    REST_API_RESPONSE,
    GROUP_EMAILS_FOUND
  }
  
  
  public GroupEmailQueryLogger groupName(String groupName) {
    events.add(LogEvent.builder()
      .type(LogEventType.QUERY_FOR_EMAILS_BY_GROUP_NAME)
      .id("group name")
      .text(groupName)
      .build());
    return this;
  }
  
  public GroupEmailQueryLogger serviceUrl(String serviceUrl) {
    events.add(LogEvent.builder()
      .type(LogEventType.REST_API_SERVICE_URL)
      .id("service url")
      .text(serviceUrl)
      .build());
    return this;
  }

  public GroupEmailQueryLogger response(ResponseEntity<String[]> response) {
    events.add(LogEvent.builder()
      .type(LogEventType.REST_API_RESPONSE)
      .id("service rsp")
      .text(String.valueOf(response.getStatusCode().value()))
      .build());
    return this; 
  }
  
  public GroupEmailQueryLogger result(List<String> result) {
    events.add(LogEvent.builder()
        .type(LogEventType.GROUP_EMAILS_FOUND)
        .id("emails")
        .text(String.join(", ", result))
        .build());
    return this;
  }
  
  public void info() {
    if(events.isEmpty()) {
      return;
    }
    final var result = new StringBuilder("events:").append(System.lineSeparator());
    for(final var event : events) {
      result.append("  ").append(event.getId()).append(": ").append(event.getText()).append(System.lineSeparator());
    }
    final var cost = System.currentTimeMillis() - start;
    log.info("Queried org. group emails, cost: {} millis, \r\n{}", cost, result.toString());
  }
  
  public void error(Exception e) {
    if(events.isEmpty()) {
      return;
    }
    final var result = new StringBuilder("events:").append(System.lineSeparator());
    for(final var event : events) {
      result.append("  ").append(event.getId()).append(": ").append(event.getText()).append(System.lineSeparator());
    }
    result.append("  error message: ").append(e.getMessage());
    
    final var cost = System.currentTimeMillis() - start;
    log.error("Failed to queried org. group emails, cost: {} millis, \r\n{}", cost, result.toString(), e);
  }
}
