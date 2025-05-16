package io.digiexpress.eveli.client.api;

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
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.digiexpress.eveli.client.api.AuthClient.UserPrincipal;
import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthApiLogger {
  private final List<LogEvent> events = new ArrayList<>();
  @Data @Builder
  private static class LogEvent {
    private final Optional<JsonObject> body;
    private final Optional<Exception> error;
    private final LogEventType type;
  }
  
  private static enum LogEventType {
    USER_CHECK,
    ENTITY_CHECK,
    ACCESS_GRANTED
  }
  
  public void add(UserPrincipal user) {
    events.add(LogEvent.builder()
        .error(Optional.empty())
        .body(Optional.ofNullable(JsonObject.of(
            "username", user.getUsername(),
            "roles", user.getRoles() 
          )))
        .type(LogEventType.USER_CHECK)
        .build());
  }
  
  public void accessForEntityWithRoles(Collection<String> entityRoles) {
    events.add(LogEvent.builder()
        .error(Optional.empty())
        .body(Optional.ofNullable(JsonObject.of(
            "entityRoles", entityRoles
          )))
        .type(LogEventType.ENTITY_CHECK)
        .build());
  }
  
  public void isAccessGranted(boolean isAccessGranted) {
    events.add(LogEvent.builder()
        .error(Optional.empty())
        .body(Optional.ofNullable(JsonObject.of(
            "isAccessGranted", isAccessGranted 
          )))
        .type(LogEventType.ACCESS_GRANTED)
        .build());
  }
  
  public void close() {
    if(events.isEmpty()) {
      return;
    }
    final var result = new StringBuilder("events:").append(System.lineSeparator());
    for(final var event : events) {
      final var body = event.getBody().orElse(new JsonObject());
      result
        .append("  - ").append(event.getType()).append(":").append(System.lineSeparator())
        .append("    ").append(body.encode()).append(System.lineSeparator());
    }
 
    log.info("{}", result.toString());
  }
  
  
}
