package io.digiexpress.eveli.app.authorization;

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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import io.digiexpress.eveli.client.api.AuthClient;
import io.digiexpress.eveli.client.api.CrmClient;
import io.digiexpress.eveli.client.spi.auth.SpringSecurityPolicy;

@Configuration
public class AuthorizationConfig {

//Worker security filter
 @Bean
 public SecurityFilterChain workerSecurity(
     HttpSecurity http, 
     AuthorizationManager<RequestAuthorizationContext> auth,
     AuthenticationManager authenticationManager) throws Exception {
   
   return http
     .securityMatchers(matcher -> matcher.requestMatchers("/worker/**"))
     .authorizeHttpRequests(authorize -> authorize.anyRequest().access(auth))
     .csrf(Customizer.withDefaults())
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
     .csrf(Customizer.withDefaults())
     .httpBasic(Customizer.withDefaults())
     .formLogin(form -> form
         .loginPage("/login-customer")
         .permitAll()
     )
     .authenticationManager(authenticationManager)
     .build();
 }
 
  
  @Bean
  public SpringSecurityPolicy authorization(AuthClient auth, CrmClient crm) {
    return new SpringSecurityPolicy(auth, crm);
  }
}
