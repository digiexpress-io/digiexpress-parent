package io.digiexpress.eveli.client.iam;

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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import io.digiexpress.eveli.client.api.AuthClient;
import io.digiexpress.eveli.client.api.ImmutablePrincipal;
import io.digiexpress.eveli.client.api.ImmutableUser;

public class SpringJwtAuthClient implements AuthClient {

  @Override
  public User getUser() {
    final var authentication = SecurityContextHolder.getContext().getAuthentication();
    if(!authentication.isAuthenticated()) {
      return ImmutableUser.builder()
          .type(UserType.ANON)
          .principal(ImmutablePrincipal.builder()
              .userName("UNAUTHENTICATED")
              .email("")
              .build())
          .build();
    }
    
    final Jwt token = (Jwt) authentication.getCredentials();
    return ImmutableUser.builder()
        .type(UserType.AUTH)
        .roles(authentication.getAuthorities().stream().map(auth -> auth.getAuthority()).collect(Collectors.toList()))
        .principal(ImmutablePrincipal.builder()
            .userName(authentication.getName())
            .email(getEmail(token))
            .representedId(getRepresentedId(token))
            .build())
        .build();
  }

  private String getRepresentedId(Jwt principal) {
    Map<String, Object> map = (Map<String, Object>) Optional.ofNullable(principal).map(p->p.getClaims()).map(c->c.get("representedPerson")).orElse(null);
    if (map != null) {
      Object value = map.get("personId");
      if (value != null) {
        return value.toString();
      }
    } else {
      map = (Map<String, Object>)Optional.ofNullable(principal).map(p->p.getClaims()).map(c->c.get("representedOrganization")).orElse(null);
      if (map != null) {
        Object value = map.get("identifier");
        if (value != null) {
          return value.toString();
        }
      }
    }
    return null;
  }

  private String getEmail(Jwt principal) {
    String email = "";
    if (principal != null) {
      email = principal.getClaimAsString("email");
    }
    return email;
  }
}
