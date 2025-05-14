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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.immutables.value.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.eveli.client.api.CrmClient.CustomerRoles;
import io.digiexpress.eveli.client.api.CrmClient.CustomerType;
import io.digiexpress.eveli.client.api.ImmutableCustomerRoles;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RequiredArgsConstructor
@Slf4j
public class CustomerRoleVisitor {
  private final SpringJwtCrmClientLogger logger;
  private final RestTemplate rest;
  private final String serviceUrlCompany;
  private final String serviceUrlPerson;
  
  @Value.Immutable @JsonSerialize(as = ImmutableUserRoles.class) @JsonDeserialize(as = ImmutableUserRoles.class)
  interface UserRoles {
    List<String> getRoles();
    @Nullable
    UserRolesPrincipal getPrincipal(); 
  }
  @Value.Immutable @JsonSerialize(as = ImmutableUserRolesPrincipal.class) @JsonDeserialize(as = ImmutableUserRolesPrincipal.class)
  interface UserRolesPrincipal {
    String getIdentifier();
    String getName();
  }
  

  public CustomerRoles accept() {
    final var cookie = getCookie();
    
    final var headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    headers.set("cookie", cookie);
    final HttpEntity<String> requestEntity = new HttpEntity<String>(null, headers);
    
    final var isPerson = new CustomerVisitor(logger).accept().getType() == CustomerType.REP_PERSON;
    final var serviceUrl = isPerson ? serviceUrlPerson : serviceUrlCompany;
    logger.rolesGetUrl(serviceUrl);
    
    
    final var entity = rest.exchange(serviceUrl, HttpMethod.GET, requestEntity, String.class);
    return getRoles(entity, isPerson);
  }
  
  
  public static String getCookie() {
    final var requestAttributes = RequestContextHolder.getRequestAttributes();
    if (requestAttributes instanceof ServletRequestAttributes) {
      final var request = ((ServletRequestAttributes) requestAttributes).getRequest();
      final var cookie = request.getHeader("cookie");
      return cookie;
    }
    return null;
  }
  

  private ImmutableCustomerRoles getRoles(ResponseEntity<String> resp, boolean isPersonRoles) {
    logger.rolesGetResp(resp);
    if (!resp.getStatusCode().is2xxSuccessful()) {
      String error = "Can't create response, e = " + resp.getStatusCode()  + " | " + resp.getHeaders();
      log.error("USER ROLES: Error: {} body: {}", error, resp.getBody());
      return ImmutableCustomerRoles.builder().identifier("").username("").build();
    }
    
    final ImmutableUserRoles userRoles;
    if(isPersonRoles) {
      final JsonObject body = new JsonObject(resp.getBody());
      logger.rolesGetPersonBody(body);
      if(body.isEmpty()) {
        return ImmutableCustomerRoles.builder().identifier("").username("").build();
      }

      final var jsonRoles = body.getJsonArray("roles");
      final var roles = jsonRoles.stream().map(data -> (String) data).collect(Collectors.toList());
      final var jsonPrincipal = body.getJsonObject("principal");
      final var principal = jsonPrincipal == null ? null : ImmutableUserRolesPrincipal.builder()
          .name(jsonPrincipal.getString("name"))
          .identifier(jsonPrincipal.getString("personId"))
          .build();
      
      userRoles = ImmutableUserRoles.builder()
        .roles(roles)
        .principal(principal)
        .build();
    } else {
      final JsonArray bodies = new JsonArray(resp.getBody());
      logger.rolesGetCompanyBody(bodies);
      if(bodies.isEmpty()) {
        return ImmutableCustomerRoles.builder().identifier("").username("").build();
      }
      
      final var body = bodies.getJsonObject(0);
      final var jsonName = body.getString("name");
      final var jsonIdentifier = body.getString("identifier");
      final var jsonRoles = body.getJsonArray("roles");
      final var roles = jsonRoles.stream().map(data -> (String) data).collect(Collectors.toList());
      
      final var principal = jsonIdentifier == null ? null : ImmutableUserRolesPrincipal.builder()
          .name(jsonName)
          .identifier(jsonIdentifier)
          .build(); 
      userRoles = ImmutableUserRoles.builder()
          .roles(roles)
          .principal(principal)
          .build();
    }
    
    logger.rolesGetOk(userRoles);
    
    return ImmutableCustomerRoles.builder()
        .identifier(userRoles.getPrincipal().getIdentifier())
        .username(userRoles.getPrincipal().getName())
        .addAllRoles(userRoles.getRoles())
        .build();
  }
  
}
