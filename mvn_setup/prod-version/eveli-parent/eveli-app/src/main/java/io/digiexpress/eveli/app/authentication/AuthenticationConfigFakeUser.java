package io.digiexpress.eveli.app.authentication;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*-
 * #%L
 * eveli-app
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

import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

import io.digiexpress.eveli.client.api.AuthClient;
import io.digiexpress.eveli.client.api.AuthClient.Liveness;
import io.digiexpress.eveli.client.api.CrmClient;
import io.digiexpress.eveli.client.api.ImmutableCustomer;
import io.digiexpress.eveli.client.api.ImmutableCustomerContact;
import io.digiexpress.eveli.client.api.ImmutableCustomerPrincipal;
import io.digiexpress.eveli.client.api.ImmutableCustomerRoles;
import io.digiexpress.eveli.client.api.ImmutableUser;
import io.digiexpress.eveli.client.api.ImmutableUserPrincipal;




/**
 * Fake impl. for testing locally logged in user configuration
 */
@Configuration
@Profile("fake-user")
public class AuthenticationConfigFakeUser  {
  
  @Bean
  public AuthenticationManager authenticationManager() {
    return new AuthenticationManager() {
      @Override
      public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return new Authentication() {
          @Override
          public String getName() {
            return null;
          }
          @Override
          public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
          }
          @Override
          public boolean isAuthenticated() {
            return true;
          }
          @Override
          public Object getPrincipal() {
            return null;
          }
          @Override
          public Object getDetails() {
            return null;
          }
          @Override
          public Object getCredentials() {
            return null;
          }
          @Override
          public Collection<? extends GrantedAuthority> getAuthorities() {
            return null;
          }
        };
      }
    };
  }


  
  @Bean
  public AuthClient authClientFakeUser() {
    return new AuthClient() {
      @Override
      public User getUser() {
        return ImmutableUser.builder()
            .isAuthenticated(true)
            .principal(ImmutableUserPrincipal.builder()
                .isAdmin(true)
                .username("tester")
                .email("tester@resys.io")
                .roles(Arrays.asList())
                .build())
            .build();
      }
      @Override
      public Liveness getLiveness() {
        // TODO Auto-generated method stub
        return null;
      }
    };
  }
  
  @Bean
  public CrmClient crm() {
    return new CrmClient() {
      @Override
      public Liveness getLiveness() {
        return null;
      }
      @Override
      public Customer getCustomer() {
        return ImmutableCustomer.builder()
            .principal(ImmutableCustomerPrincipal.builder()
                .id("my-ssn")
                .ssn("my-ssn")
                .username("same vimes")
                .firstName("same")
                .lastName("vimes")
                .protectionOrder(false)
                .contact(ImmutableCustomerContact.builder()
                    .email("same.vimes@resys.io")
                    .addressValue("test-street")
                    .build())
                .build())
            .type(CustomerType.AUTH_CUSTOMER)
            .build();
      }

      @Override
      public CustomerRoles getCustomerRoles() {
        final var customer = getCustomer();
        
        return ImmutableCustomerRoles.builder()
            .identifier(customer.getPrincipal().getSsn())
            .username(customer.getPrincipal().getUsername())
            .build();
      }
    };
  }
}
