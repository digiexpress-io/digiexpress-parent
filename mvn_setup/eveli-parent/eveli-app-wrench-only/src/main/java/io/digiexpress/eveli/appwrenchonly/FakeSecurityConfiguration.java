package io.digiexpress.eveli.appwrenchonly;

/*-
 * #%L
 * eveli-app-wrench-only
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

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

@ConditionalOnProperty(prefix = "wrench.fake-security", name = "enabled", havingValue = "true")
@Configuration
public class FakeSecurityConfiguration {  
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
  
  @Bean
  public AuthorizationManager<RequestAuthorizationContext> allowAll() {
    return new AuthorizationManager<RequestAuthorizationContext>() {
      @Override
      public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        return new AuthorizationDecision(true);
      }
    };
  }
  
  @Bean
  public AuthenticationManager authenticationManager() {
    return new AuthenticationManager() {
      @Override
      public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return new Authentication() {
          private static final long serialVersionUID = -6641305260982579586L;
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
            return Collections.emptyList();
          }
        };
      }
    };
  }
}
