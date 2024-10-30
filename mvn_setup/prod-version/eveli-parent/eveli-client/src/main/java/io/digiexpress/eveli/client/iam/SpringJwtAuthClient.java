package io.digiexpress.eveli.client.iam;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

import javax.json.JsonString;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import io.digiexpress.eveli.client.api.AuthClient;
import io.digiexpress.eveli.client.api.ImmutableCustomer;
import io.digiexpress.eveli.client.api.ImmutableCustomerAddress;
import io.digiexpress.eveli.client.api.ImmutableCustomerContact;
import io.digiexpress.eveli.client.api.ImmutableCustomerPrincipal;
import io.digiexpress.eveli.client.api.ImmutableCustomerRepresentedCompany;
import io.digiexpress.eveli.client.api.ImmutableCustomerRepresentedPerson;
import io.digiexpress.eveli.client.api.ImmutableLiveness;
import io.digiexpress.eveli.client.api.ImmutableWorker;
import io.digiexpress.eveli.client.api.ImmutableWorkerPrincipal;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpringJwtAuthClient implements AuthClient {

  @Override
  public Worker getWorker() {
    final var authentication = SecurityContextHolder.getContext().getAuthentication();
    if(!authentication.isAuthenticated()) {
      return ImmutableWorker.builder()
          .type(UserType.ANON)
          .principal(ImmutableWorkerPrincipal.builder()
              .username("UNAUTHENTICATED")
              .email("")
              .build())
          .build();
    }
    
    final Jwt token = (Jwt) authentication.getCredentials();
    return ImmutableWorker.builder()
        .type(UserType.AUTH)
        .principal(ImmutableWorkerPrincipal.builder()
            .username(authentication.getName())
            .email(getEmail(token))
            .roles(authentication.getAuthorities().stream().map(auth -> auth.getAuthority()).collect(Collectors.toList()))
            .build())
        .build();
  }
 
  @Override
  public CustomerRoles getCustomerRoles() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Liveness getLiveness() {
    final var authentication = SecurityContextHolder.getContext().getAuthentication();
    final Jwt token = (Jwt) authentication.getCredentials();
    
    final var now = LocalDateTime.now();
    final var then = LocalDateTime.ofInstant(token.getExpiresAt(), ZoneId.systemDefault());
    return ImmutableLiveness.builder()
        .issuedAtTime(token.getIssuedAt().toEpochMilli())
        .expiresIn(Duration.between(now, then).toSeconds())
        .build();
  
  }

  @Override
  public Customer getCustomer() {
    final var authentication = SecurityContextHolder.getContext().getAuthentication();
    if(!authentication.isAuthenticated()) {
      return ImmutableCustomer.builder()
          .type(UserType.ANON)
          .principal(ImmutableCustomerPrincipal.builder()
                .id("UNAUTHENTICATED")
                .ssn("anon")
                .username("UNAUTHENTICATED")
                .firstName("anon")
                .lastName("anon")
                .protectionOrder(false)
                .contact(ImmutableCustomerContact.builder().email("anon@resys.io").build())
              .build())
          .build();
    }
    
    final Jwt token = (Jwt) authentication.getCredentials();
    
    return ImmutableCustomer.builder()
        .type(UserType.AUTH)
        .principal(toCustomer(token))
        .build();
  }
  
  
  
  
  private String getEmail(Jwt principal) {
    String email = "";
    if (principal != null) {
      email = principal.getClaimAsString("email");
    }
    return email;
  }

  private ImmutableCustomerPrincipal toCustomer(Jwt idToken) {
    final var sub = (String) idToken.getClaim("sub");
    final var firstName = orEmpty((String) idToken.getClaim("firstNames"));
    final var lastName = orEmpty((String) idToken.getClaim("lastName"));
    final var ssn = (String) idToken.getClaim("personalIdentityCode");
    final var email = (String) idToken.getClaim("email");
    
    final var address = toAddress(idToken);
    final var protectionOrder = "true".equals(idToken.getClaim("protectionOrder"));
    
    return ImmutableCustomerPrincipal.builder()
        .username(firstName + " " + lastName)
        .firstName(firstName)
        .lastName(lastName)
        .ssn(orEmpty(ssn))
        .id(sub)
        .representedId(getRepresentedId(idToken))
        .protectionOrder(protectionOrder)
        .representedPerson(toRepresentedPerson(idToken))
        .representedCompany(toRepresentedCompany(idToken))
        .contact(ImmutableCustomerContact.builder()
            .email(orEmpty(email))
            .address(address)
            .addressValue(toAddressValue(address))
            .build())
        .build();
  }
  private String toAddressValue(CustomerAddress src) {
    if(src == null) {
      return null;
    }
    return orEmpty(src.getStreet()) + ", " + orEmpty(src.getPostalCode()) + " " + orEmpty(src.getLocality());
  }
  
  private ImmutableCustomerAddress toAddress(Jwt idToken) {
    return ImmutableCustomerAddress.builder()
        .postalCode(orEmpty(idToken.getClaim("postalCode")))
        .locality(orEmpty(idToken.getClaim("locality")))
        .street(orEmpty(idToken.getClaim("streetAddress")))
        .country(orEmpty(idToken.getClaim("country")))
        .build();
  }
  
  @SuppressWarnings({ "unchecked" })
  private AuthClient.CustomerRepresentedPerson toRepresentedPerson(Jwt idToken) {
    final var value = (Map<String, Object>) idToken.getClaim("representedPerson");
    if(value == null) {
      return null;
    }
    
    
    final var name = (JsonString) value.get("name");
    final var personId = (JsonString) value.get("personId");

    
    return ImmutableCustomerRepresentedPerson.builder()
        .name(name.getString())
        .personId(personId.getString())
        .representativeName(getRepresentativeName(name.getString()))
        .build();
  }
  

  @SuppressWarnings({ "unchecked" })
  private AuthClient.CustomerRepresentedCompany toRepresentedCompany(Jwt idToken) {
    final var value = (Map<String, Object>) idToken.getClaim("representedOrganization");
    if(value == null) {
      return null;
    }
    
    final var name = (JsonString) value.get("name");
    final var companyId = (JsonString) value.get("identifier");
    
    return ImmutableCustomerRepresentedCompany.builder()
        .name(name.getString())
        .companyId(companyId.getString())
        .build();
  }
  
  
  private static String orEmpty(String value) {
    return value == null ? "" : value; 
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
  
  private String[] getRepresentativeName(String name) {
    final var splitAt = name.indexOf(" ");
    if(splitAt <= 0) {
      return new String[] {" ", name.trim()};
    }
    return new String[] {name.substring(0, splitAt).trim(), name.substring(splitAt).trim()};
  }
}
