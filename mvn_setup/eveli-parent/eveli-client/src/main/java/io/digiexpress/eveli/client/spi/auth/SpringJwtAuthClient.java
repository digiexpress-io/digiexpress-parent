package io.digiexpress.eveli.client.spi.auth;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import io.digiexpress.eveli.client.api.ImmutableLiveness;
import io.digiexpress.eveli.client.api.ImmutableUser;
import io.digiexpress.eveli.client.api.ImmutableUserPrincipal;
import io.digiexpress.eveli.client.api.WorkerAuthClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SpringJwtAuthClient implements WorkerAuthClient {
  private final List<String> adminRoles;
  private final Boolean everybodyIsAdmin;

  @Override
  public User getUser() {
    final var authentication = SecurityContextHolder.getContext().getAuthentication();
    if(authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
      return ImmutableUser.builder()
          .isAuthenticated(false)
          .principal(ImmutableUserPrincipal.builder()
              .sub("")
              .email("")
              .isAdmin(false)
              .username("UNAUTHENTICATED")
              .build())
          .build();
    }
    
    final Jwt token = (Jwt) authentication.getPrincipal();
    final var roles = authentication.getAuthorities().stream().map(auth -> auth.getAuthority()).collect(Collectors.toList());
    final var isAdmin = everybodyIsAdmin == null ? roles.stream().filter(role -> adminRoles.contains(role)).findAny().isPresent() : everybodyIsAdmin;
    
    return ImmutableUser.builder()
        .isAuthenticated(true)
        .principal(ImmutableUserPrincipal.builder()
            .isAdmin(isAdmin)
            .sub(getSub(token))
            .username(getUserName(token))
            .email(getEmail(token))
            .roles(roles)
            .build())
        .build();
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
  
  private String getEmail(Jwt principal) {
    String email = "";
    if (principal != null) {
      email = Objects.toString(principal.getClaimAsString("email"), "");
    }
    return email;
  }
  
  private String getUserName(Jwt principal) {
    String userName = "";
    if(principal != null) {
      userName = Objects.toString(principal.getClaimAsString("name"), "");
    }
    return userName;
      
  }
  
  private String getSub(Jwt principal) {
    String sub = "";
    if(principal != null) {
      sub = Objects.toString(principal.getSubject(), "");
    }
    return sub;
  }

}
