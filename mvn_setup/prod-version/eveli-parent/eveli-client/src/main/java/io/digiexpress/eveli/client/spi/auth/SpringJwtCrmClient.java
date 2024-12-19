package io.digiexpress.eveli.client.spi.auth;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
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

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import io.digiexpress.eveli.client.api.AuthClient.Liveness;
import io.digiexpress.eveli.client.api.CrmClient;
import io.digiexpress.eveli.client.api.ImmutableCustomer;
import io.digiexpress.eveli.client.api.ImmutableCustomerAddress;
import io.digiexpress.eveli.client.api.ImmutableCustomerContact;
import io.digiexpress.eveli.client.api.ImmutableCustomerPrincipal;
import io.digiexpress.eveli.client.api.ImmutableCustomerRepresentedCompany;
import io.digiexpress.eveli.client.api.ImmutableCustomerRepresentedPerson;
import io.digiexpress.eveli.client.api.ImmutableCustomerRoles;
import io.digiexpress.eveli.client.api.ImmutableLiveness;
import io.thestencil.iam.api.ImmutableUserRoles;
import io.thestencil.iam.api.ImmutableUserRolesPrincipal;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SpringJwtCrmClient implements CrmClient {
  private final String hostUrl;
  private final String serviceUrlCompany;
  private final String serviceUrlPerson;
  
  @Override
  public CustomerRoles getCustomerRoles() {
    if (StringUtils.isNotEmpty(hostUrl)) {
      final var rest = new RestTemplate();
      rest.setUriTemplateHandler(new DefaultUriBuilderFactory(hostUrl));
      final var request = getCurrentHttpRequest();
      final var cookie = request.getHeader("cookie");
      
      final var headers = new HttpHeaders();
      headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
      headers.set("cookie", cookie);
      final HttpEntity<String> requestEntity = new HttpEntity<String>(null, headers);
      
      
      final var isPerson = getCustomer().getType() == CustomerType.REP_PERSON;
      final var serviceUrl = isPerson ? serviceUrlPerson : serviceUrlCompany;
      
      final var uri = UriComponentsBuilder.fromHttpUrl(serviceUrl).build().toUri();
      
      final var entity = rest.exchange(uri, HttpMethod.GET, requestEntity, String.class);
      return getRoles(entity, isPerson);
    }
    else {
      return ImmutableCustomerRoles.builder().identifier("").username("").build();
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
    final var authentication = SecurityContextHolder.getContext().getAuthentication();
    if(!authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
      return ImmutableCustomer.builder()
          .type(CustomerType.ANON)
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
    
    final Jwt token = (Jwt) authentication.getPrincipal();
    final var principal = toCustomer(token);
    
    final CustomerType type;
    if(principal.getRepresentedId() == null) {
      type = CustomerType.AUTH_CUSTOMER;
    } else if(principal.getRepresentedCompany() != null) {
      type = CustomerType.REP_COMPANY;
    } else if(principal.getRepresentedPerson() != null) {
      type = CustomerType.REP_PERSON;
    } else {
      throw new CustomerJwtParsingException("Can't resolve customer type from the JWT!");
    }
    
    
    return ImmutableCustomer.builder()
        .type(type)
        .principal(principal)
        .build();
  }
  
  private static class CustomerJwtParsingException extends RuntimeException {
    private static final long serialVersionUID = 1781444267360040922L;
    public CustomerJwtParsingException(String message) {
      super(message);
    }
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
  private CrmClient.CustomerRepresentedPerson toRepresentedPerson(Jwt idToken) {
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
  private CrmClient.CustomerRepresentedCompany toRepresentedCompany(Jwt idToken) {
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

  @SuppressWarnings("unchecked")
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
  
  
  private static HttpServletRequest getCurrentHttpRequest() {
    final var requestAttributes = RequestContextHolder.getRequestAttributes();
    if (requestAttributes instanceof ServletRequestAttributes) {
      return ((ServletRequestAttributes) requestAttributes).getRequest();
    }
    return null;
  }
  
  

  private ImmutableCustomerRoles getRoles(ResponseEntity<String> resp, boolean isPersonRoles) {
    if (!resp.getStatusCode().is2xxSuccessful()) {
      String error = "Can't create response, e = " + resp.getStatusCode()  + " | " + resp.getHeaders();
      log.error("USER ROLES: Error: {} body: {}", error, resp.getBody());
      return ImmutableCustomerRoles.builder().identifier("").username("").build();
    }
    
    final ImmutableUserRoles userRoles;
    if(isPersonRoles) {
      final JsonObject body = new JsonObject(resp.getBody());
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
    
    return ImmutableCustomerRoles.builder()
        .identifier(userRoles.getPrincipal().getIdentifier())
        .username(userRoles.getPrincipal().getName())
        .addAllRoles(userRoles.getRoles())
        .build();
  }
}
