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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.RestTemplate;

import io.digiexpress.eveli.client.api.WorkerAuthClient.Liveness;
import io.digiexpress.eveli.client.api.GamutAuthClient;
import io.digiexpress.eveli.client.api.ImmutableLiveness;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SpringJwtCrmClient implements GamutAuthClient {
  private final RestTemplate rest;
  private final String serviceUrlCompany;
  private final String serviceUrlPerson;
  
  @Override
  public CustomerRoles getCustomerRoles() {
    final var logger = new SpringJwtCrmClientLogger();
    try {
      return new CustomerRoleVisitor(logger, rest, serviceUrlCompany, serviceUrlPerson).accept();
    } finally {
      logger.close();
    }
  }

  @Override
  public Liveness getLiveness() {
    final var authentication = SecurityContextHolder.getContext().getAuthentication();
    final Jwt token = (Jwt) authentication.getPrincipal();
    
    final var now = LocalDateTime.now();
    final var then = LocalDateTime.ofInstant(token.getExpiresAt(), ZoneId.systemDefault());
    return ImmutableLiveness.builder()
        .issuedAtTime(token.getIssuedAt().toEpochMilli())
        .expiresIn(Duration.between(now, then).toSeconds())
        .build();
  }

  @Override
  public Customer getCustomer() {
    final var logger = new SpringJwtCrmClientLogger();
    try {
      return new CustomerVisitor(logger).accept();
    } finally {
      logger.close();
    }
  }  
}
