package io.digiexpress.eveli.app.authentication;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import io.digiexpress.eveli.client.api.GamutAuthClient;
import io.digiexpress.eveli.client.api.ImmutableCustomer;
import io.digiexpress.eveli.client.api.ImmutableCustomerAddress;
import io.digiexpress.eveli.client.api.ImmutableCustomerContact;
import io.digiexpress.eveli.client.api.ImmutableCustomerPrincipal;
import io.digiexpress.eveli.client.api.ImmutableCustomerRoles;
import io.digiexpress.eveli.client.api.ImmutableLiveness;
import io.digiexpress.eveli.client.api.ImmutableUser;
import io.digiexpress.eveli.client.api.ImmutableUserPrincipal;
import io.digiexpress.eveli.client.api.WorkerAuthClient;
import io.digiexpress.eveli.client.api.WorkerAuthClient.Liveness;




/**
 * Fake impl. for testing locally logged in user configuration
 */
@Configuration
@Profile("fake-user")
public class AuthenticationConfigFakeUser  {
  
  public static String[] ROLES = {"TASK_ADMIN","TASK_WORKER","FEEDBACK_ADMIN","FEEDBACK_VIEWER","ASSET_ADMIN","Authorized"};

  
//Worker security filter
 @Bean
 public SecurityFilterChain workerSecurity(
     HttpSecurity http, 
     AuthorizationManager<RequestAuthorizationContext> auth,
     AuthenticationManager authenticationManager) throws Exception {
   
   return http
     .securityMatchers(matcher -> matcher.requestMatchers("/worker/**"))
     .authorizeHttpRequests(authorize -> authorize.anyRequest().access(auth))
     .csrf(t -> t.disable())
     .httpBasic(Customizer.withDefaults())
     .formLogin(form -> form
         .loginPage("/login-worker")
         .permitAll()
     )
     .authenticationManager(authenticationManager)
     .build();
 }
 
 // Customer security filter
 @Bean
 public SecurityFilterChain portalSecurity(
     HttpSecurity http, 
     AuthorizationManager<RequestAuthorizationContext> auth,
     AuthenticationManager authenticationManager) throws Exception {
   
   return http
     .securityMatchers(matcher -> matcher.requestMatchers("/portal/secured/**"))
     .authorizeHttpRequests(authorize -> authorize.anyRequest().access(auth))
     .csrf(t -> t.disable())
     .httpBasic(Customizer.withDefaults())
     .formLogin(form -> form
         .loginPage("/login-customer")
         .permitAll()
     )
     .authenticationManager(authenticationManager)
     .build();
 }
  
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
            return Arrays.stream(ROLES).map(role->new SimpleGrantedAuthority("ROLE_"+role)).collect(Collectors.toList());
          }
        };
      }
    };
  }
  
  @Bean
  public WorkerAuthClient authClientFakeUser() {
    return new WorkerAuthClient() {
      @Override
      public User getUser() {
        return ImmutableUser.builder()
            .isAuthenticated(true)
            .principal(ImmutableUserPrincipal.builder()
                .isAdmin(true)
                .sub("John Smith")
                .username("John Smith")
                .email("john.smith@resys.io")
                .roles(Arrays.stream(ROLES).map(r->"ROLE_"+r).collect(Collectors.toList()))
                .build())
            .build();
      }
      @Override
      public Liveness getLiveness() {
        return ImmutableLiveness.builder()
            .issuedAtTime(System.currentTimeMillis())
            .expiresIn(1000)
            .build();
      }
    };
  }
  
  @Bean
  public GamutAuthClient crm() {
    return new GamutAuthClient() {
      @Nullable
      @Override
      public Liveness getLiveness() {
        return null;
      }
      @Override
      public Customer getCustomer() {
        return ImmutableCustomer.builder()
            .principal(ImmutableCustomerPrincipal.builder()
                .id("12345678")
                .ssn("230469-449B")
                .username("sam-vimes")
                .firstName("Sam")
                .lastName("Vimes")
                .protectionOrder(false)
                
                /*
                .representedCompany(ImmutableCustomerRepresentedCompany.builder()
                    .companyId("Serial-X")
                    .name("Night Watch")
                    .build())
                */
                
                .contact(ImmutableCustomerContact.builder()
                    .email("sam.vimes@resys.io")
                    .address(ImmutableCustomerAddress.builder()
                        .country("FI")
                        .locality("POLVIJÄRVI")
                        .street("Kylpylaitoksentie 87")
                        .postalCode("83700")
                        .build())
                    .build())
                
                .build())
            .type(CustomerType.AUTH_CUSTOMER)
            .build();
      }

      @Override
      public CustomerRoles getCustomerRoles() {
        return ImmutableCustomerRoles.builder()
            .build();
      }
    };
  }
  
  @Bean
  public AuthorizationManager allowAll() {
    return new AuthorizationManager<RequestAuthorizationContext>() {
      @Override
      public AuthorizationDecision check(
          Supplier<Authentication> authentication,
          RequestAuthorizationContext object) {

        return new AuthorizationDecision(true);
      }
    };
  }
}
