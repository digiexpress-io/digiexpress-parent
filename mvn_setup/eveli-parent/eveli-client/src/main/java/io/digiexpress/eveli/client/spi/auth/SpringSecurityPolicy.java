package io.digiexpress.eveli.client.spi.auth;

import java.util.Arrays;
import java.util.Set;

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

import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.util.AntPathMatcher;

import io.digiexpress.eveli.client.api.WorkerAuthClient;
import io.digiexpress.eveli.client.api.GamutAuthClient;
import io.digiexpress.eveli.client.api.GamutAuthClient.CustomerType;
import io.digiexpress.eveli.client.config.EveliAutoConfigPermissions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RequiredArgsConstructor
@Slf4j
public class SpringSecurityPolicy implements AuthorizationManager<RequestAuthorizationContext> {
  
  private final WorkerAuthClient authClient;
  private final GamutAuthClient crmClient;
  private final EveliAutoConfigPermissions authProps;
  
  private final static String PORTAL_LOGIN_PATH = "/portal/login";
  private final static String PORTAL_LOGOUT_PATH = "/portal/logout";
  
  private final static String WORKER_LOGIN_PATH = "/worker/login";
  private final static String WORKER_LOGOUT_PATH = "/worker/logout";
  
  private final static String WORKER_PATH = "/worker/rest/api";
  private final static String TENANT_CONFIG_PATH = "/worker/rest/api/tenant-configs";
  private final static String SITE_PATH = "/portal/site";
  private final static String IAM_PATH = "/portal/secured/iam";
  private final static String ACTIONS_PATH = "/portal/secured/actions";
  
  private final static AntPathMatcher matcher = new AntPathMatcher();

  @Override
  public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
    final var path = context.getRequest().getServletPath();
    log.debug("Authorization check for path: {}, auth user: {}, portal user: {}", path, authClient.getUser(), crmClient.getCustomer());
    // LOGIN/LOGOUT
    if( path.equals(PORTAL_LOGIN_PATH) ||
        path.equals(WORKER_LOGIN_PATH) || 
        
        path.equals(PORTAL_LOGOUT_PATH) ||
        path.equals(WORKER_LOGOUT_PATH)) {
      log.debug("Login/logout path, authorized");
      return new AuthorizationDecision(true);    
    }
    
    if (path.equals(TENANT_CONFIG_PATH)) {
      log.debug("Tenant config, authorized");
      return new AuthorizationDecision(true);      
    }

    // worker side
    if(path.startsWith(WORKER_PATH) && authClient.getUser().isAuthenticated()) {
      final String method = context.getRequest().getMethod().toUpperCase();
      Set<String> userRoles = authentication.get().getAuthorities().stream().map(auth->auth.getAuthority()).collect(Collectors.toSet());
      log.debug("Worker REST API path, user authenticated, checking roles");
      boolean access = findAccess(path, method, userRoles);
      log.debug("Worker REST API path, access check result: {}", access);
      return new AuthorizationDecision(access);      
    }
    
    // portal iam must be logged in
    if(path.startsWith(IAM_PATH) && 
        Arrays.asList(
            CustomerType.REP_COMPANY,
            CustomerType. REP_PERSON,
            CustomerType.AUTH_CUSTOMER)
        .contains(crmClient.getCustomer().getType())) {
      log.debug("Portal IAM path, user authenticated, authorized");
      return new AuthorizationDecision(true);    
    }

    // portal actions must be logged in
    if(path.startsWith(ACTIONS_PATH) && 
        Arrays.asList(
            CustomerType.REP_COMPANY,
            CustomerType. REP_PERSON,
            CustomerType.AUTH_CUSTOMER)
        .contains(crmClient.getCustomer().getType())) {
      log.debug("Portal REST API path, user authenticated, authorized");
      return new AuthorizationDecision(true);    
    }

    // anybody can access portal site
    if(path.equals(SITE_PATH)) {
      log.debug("Portal path, authorized");
      return new AuthorizationDecision(true);    
    }
    log.debug("No match, not authorized");
    return new AuthorizationDecision(false);
  }

  protected boolean findAccess(final String path, final String method, Set<String> userRoles) {
    return authProps.getWorker().stream().anyMatch(auth->{
      return auth.getMethod().contains(method) 
        && matcher.match(auth.getPathPattern(), path)
        && auth.getRoles().stream().anyMatch(authRole->userRoles.contains(authRole));
    });
  }
}
