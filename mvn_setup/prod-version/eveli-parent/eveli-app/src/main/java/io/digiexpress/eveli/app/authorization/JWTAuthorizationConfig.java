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
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

@Configuration
public class JWTAuthorizationConfig {

//Worker security filter
 @Bean
 @Profile("jwt")
 public SecurityFilterChain workerSecurity(
     HttpSecurity http, 
     AuthorizationManager<RequestAuthorizationContext> auth,
     AuthenticationManager authenticationManager,
     JwtIssuerAuthenticationManagerResolver resolver) throws Exception {
   
   return http
     .securityMatchers(matcher -> matcher.requestMatchers("/worker/**"))
     .authorizeHttpRequests(authorize -> authorize.anyRequest().access(auth))
     .csrf(Customizer.withDefaults())
     .authenticationManager(authenticationManager)
     .oauth2ResourceServer(oauth2->
     oauth2.authenticationManagerResolver(resolver))
     .build();
 }
 
 // Customer security filter
 @Bean
 @Profile("jwt")
 public SecurityFilterChain portalSecurity(
     HttpSecurity http, 
     AuthorizationManager<RequestAuthorizationContext> auth,
     AuthenticationManager authenticationManager,
     JwtIssuerAuthenticationManagerResolver resolver) throws Exception {
   
   return http
     .securityMatchers(matcher -> matcher.requestMatchers("/portal/secured/**"))
     .authorizeHttpRequests(authorize -> authorize.anyRequest().access(auth))
     .csrf(Customizer.withDefaults())
     .authenticationManager(authenticationManager)
     .oauth2ResourceServer(oauth2->
       oauth2.authenticationManagerResolver(resolver))
     .build();
 }
 

}
