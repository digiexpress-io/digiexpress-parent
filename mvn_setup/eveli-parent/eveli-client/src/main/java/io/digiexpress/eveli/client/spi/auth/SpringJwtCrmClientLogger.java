package io.digiexpress.eveli.client.spi.auth;

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
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import io.digiexpress.eveli.client.api.GamutAuthClient.CustomerPrincipal;
import io.digiexpress.eveli.client.spi.auth.CustomerRoleVisitor.UserRoles;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class SpringJwtCrmClientLogger {
  private final List<LogEvent> events = new ArrayList<>();
  
  @Data @Builder
  private static class LogEvent {
    private final Optional<JsonObject> body;
    private final Optional<Exception> error;
    private final LogEventType type;
  }
  
  private static enum LogEventType {
    GET_UNAUTHENTICATED,
    
    GET_JWT_TOKEN,
    GET_JWT_ERROR,
    
    GET_CUSTOMER,
    GET_CUSTOMER_TYPE,
    
    GET_REP_PERSON,
    GET_REP_COMPANY,
    
    GET_REP_ROLE_FOR,
    GET_REP_ROLE_RESP,
    
    GET_REP_ROLES_OK,
    
    GET_REP_PERSON_ROLES,
    GET_REP_COMPANY_ROLES,
  }
  
  public SpringJwtCrmClientLogger unauth() {
    events.add(LogEvent.builder()
        .error(Optional.empty())
        .body(Optional.empty())
        .type(LogEventType.GET_UNAUTHENTICATED)
        .build());
    return this;
  }
  

  public SpringJwtCrmClientLogger jwtError(Exception e) {
    events.add(LogEvent.builder()
        .error(Optional.ofNullable(e))
        .body(Optional.empty())
        .type(LogEventType.GET_JWT_ERROR)
        .build());
    return this;
  }
  
  public SpringJwtCrmClientLogger jwtOk(Jwt token) {
    final var body = JsonObject.mapFrom(token.getClaims());
    events.add(LogEvent.builder()
        .error(Optional.empty())
        .body(Optional.ofNullable(body))
        .type(LogEventType.GET_JWT_TOKEN)
        .build());
    return this;
  }
  
  public SpringJwtCrmClientLogger customerOk(CustomerPrincipal token) {
    final var body = JsonObject.mapFrom(token);
    events.add(LogEvent.builder()
        .error(Optional.empty())
        .body(Optional.ofNullable(body))
        .type(LogEventType.GET_CUSTOMER)
        .build());
    return this;
  }

  public SpringJwtCrmClientLogger rolesGetUrl(String url) {
    final var body = JsonObject.of("service-url", url);
    events.add(LogEvent.builder()
        .error(Optional.empty())
        .body(Optional.ofNullable(body))
        .type(LogEventType.GET_REP_ROLE_FOR)
        .build());
    return this;
  }
  
  public SpringJwtCrmClientLogger rolesGetResp(ResponseEntity<String> resp) {
    final var body = JsonObject.of(
        "code", resp.getStatusCode(),
        "body", resp.getBody());
    
    events.add(LogEvent.builder()
        .error(Optional.empty())
        .body(Optional.ofNullable(body))
        .type(LogEventType.GET_REP_ROLE_RESP)
        .build()); 
    return this;
  }
  
  
  public SpringJwtCrmClientLogger rolesGetCompanyBody(JsonArray init) {
    final var body = JsonObject.of("company-roles", init);
    events.add(LogEvent.builder()
        .error(Optional.empty())
        .body(Optional.ofNullable(body))
        .type(LogEventType.GET_REP_COMPANY_ROLES)
        .build()); 
    return this;
    
  }
  public SpringJwtCrmClientLogger rolesGetPersonBody(JsonObject init) {
    final var body = JsonObject.of("person-roles", init);
    events.add(LogEvent.builder()
        .error(Optional.empty())
        .body(Optional.ofNullable(body))
        .type(LogEventType.GET_REP_PERSON_ROLES)
        .build()); 
    return this;
  }
  
  public SpringJwtCrmClientLogger rolesGetOk(UserRoles roles) {
    events.add(LogEvent.builder()
        .error(Optional.empty())
        .body(Optional.ofNullable(JsonObject.mapFrom(roles)))
        .type(LogEventType.GET_REP_ROLES_OK)
        .build()); 
    return this;
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
