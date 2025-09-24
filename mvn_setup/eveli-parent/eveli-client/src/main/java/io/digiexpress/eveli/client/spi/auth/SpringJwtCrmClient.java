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

import io.digiexpress.eveli.client.api.GamutAuthClient;
import io.digiexpress.eveli.client.api.ImmutableLiveness;
import io.digiexpress.eveli.client.api.WorkerAuthClient.Liveness;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@RequiredArgsConstructor
public class SpringJwtCrmClient implements GamutAuthClient {
  private final RestTemplate rest;
  private final String serviceUrlCompany;
  private final String serviceUrlPerson;

  public interface Logger extends AutoCloseable {
    default void unauth() {}

    default void jwtError(Exception e) {}

    default void jwtOk(Jwt token) {}

    default void customerOk(GamutAuthClient.CustomerPrincipal token) {}

    default void rolesGetUrl(String url) {}

    default void rolesGetResp(ResponseEntity<String> resp) {}

    default void rolesGetCompanyBody(JsonArray init) {}

    default void rolesGetPersonBody(JsonObject init) {}

    default void rolesGetOk(CustomerRoleVisitor.UserRoles roles) {}

    @Override
    default void close() { }
  }

  @Override
  public CustomerRoles getCustomerRoles() {
    try (var logger = createLogger()) {
      return new CustomerRoleVisitor(logger, rest, serviceUrlCompany, serviceUrlPerson).accept();
    }
  }

  @Override
  @Nullable
  public Liveness getLiveness() {
    final var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof Jwt token) {
      final var now = LocalDateTime.now();
      final var then = LocalDateTime.ofInstant(token.getExpiresAt(), ZoneId.systemDefault());
      return ImmutableLiveness.builder()
          .issuedAtTime(token.getIssuedAt().toEpochMilli())
          .expiresIn(Duration.between(now, then).toSeconds())
          .build();
    }
    return null;
  }

  @Override
  public Customer getCustomer() {
    try (var logger = createLogger()) {
      return new CustomerVisitor(logger).accept();
    }
  }

  private static @NotNull SpringJwtCrmClient.Logger createLogger() {
    if (log.isDebugEnabled()) {
      return new SpringJwtCrmClientLogger(log::debug);
    }
    return new SpringJwtCrmClient.Logger() {};
  }
}
