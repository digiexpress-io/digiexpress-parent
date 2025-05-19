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

import java.util.Map;
import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import io.digiexpress.eveli.client.api.GamutAuthClient;
import io.digiexpress.eveli.client.api.GamutAuthClient.Customer;
import io.digiexpress.eveli.client.api.GamutAuthClient.CustomerAddress;
import io.digiexpress.eveli.client.api.GamutAuthClient.CustomerType;
import io.digiexpress.eveli.client.api.ImmutableCustomer;
import io.digiexpress.eveli.client.api.ImmutableCustomerAddress;
import io.digiexpress.eveli.client.api.ImmutableCustomerContact;
import io.digiexpress.eveli.client.api.ImmutableCustomerPrincipal;
import io.digiexpress.eveli.client.api.ImmutableCustomerRepresentedCompany;
import io.digiexpress.eveli.client.api.ImmutableCustomerRepresentedPerson;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomerVisitor {
  private final SpringJwtCrmClientLogger logger;

  public Customer accept() {
    final var authentication = SecurityContextHolder.getContext().getAuthentication();
    if(authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
      logger.unauth();
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
    
    try {
      final Jwt token = (Jwt) authentication.getPrincipal();
      logger.jwtOk(token);
      
      final var principal = toCustomer(token);
      logger.customerOk(principal);
      
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
    } catch(RuntimeException e) {
      logger.jwtError(e);
      throw e;
    }
  }

  public SpringJwtCrmClientLogger getLogger() {
    return logger;
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
  private GamutAuthClient.CustomerRepresentedPerson toRepresentedPerson(Jwt idToken) {
    final var value = (Map<String, Object>) idToken.getClaim("representedPerson");
    if(value == null) {
      return null;
    }
    
    
    final var name = value.get("name");
    final var personId =  value.get("personId");

    
    return ImmutableCustomerRepresentedPerson.builder()
        .name(name.toString())
        .personId(personId.toString())
        .representativeName(getRepresentativeName(name.toString()))
        .build();
  }
  

  @SuppressWarnings({ "unchecked" })
  private GamutAuthClient.CustomerRepresentedCompany toRepresentedCompany(Jwt idToken) {
    final var value = (Map<String, Object>) idToken.getClaim("representedOrganization");
    if(value == null) {
      return null;
    }
    
    final var name = value.get("name");
    final var companyId = value.get("identifier");
    
    return ImmutableCustomerRepresentedCompany.builder()
        .name(name.toString())
        .companyId(companyId.toString())
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
  
  
  
  private static class CustomerJwtParsingException extends RuntimeException {
    private static final long serialVersionUID = 1781444267360040922L;
    public CustomerJwtParsingException(String message) {
      super(message);
    }
  }
}
