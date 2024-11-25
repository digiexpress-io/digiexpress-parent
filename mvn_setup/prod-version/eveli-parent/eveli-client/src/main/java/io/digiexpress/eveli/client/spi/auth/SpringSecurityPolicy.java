package io.digiexpress.eveli.client.spi.auth;

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

import java.util.function.Supplier;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import io.digiexpress.eveli.client.api.AuthClient;
import io.digiexpress.eveli.client.api.CrmClient;
import io.digiexpress.eveli.client.api.CrmClient.CustomerType;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class SpringSecurityPolicy implements AuthorizationManager<RequestAuthorizationContext> {
  
  private final AuthClient authClient;
  private final CrmClient crmClient;
  
  private final static String PORTAL_LOGIN_PATH = "/portal/login";
  private final static String PORTAL_LOGOUT_PATH = "/portal/logout";
  
  private final static String WORKER_LOGIN_PATH = "/worker/login";
  private final static String WORKER_LOGOUT_PATH = "/worker/logout";
  
  private final static String WORKER_PATH = "/worker/rest/api";
  private final static String SITE_PATH = "/portal/site";
  private final static String IAM_PATH = "/portal/secured/iam";
  private final static String ACTIONS_PATH = "/portal/secured/actions";

  @Override
  public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
    final var path = context.getRequest().getServletPath();
    
    // LOGIN/LOGOUT
    if( path.equals(PORTAL_LOGIN_PATH) ||
        path.equals(WORKER_LOGIN_PATH) || 
        
        path.equals(PORTAL_LOGOUT_PATH) ||
        path.equals(WORKER_LOGOUT_PATH)) {
      return new AuthorizationDecision(true);    
    }

    // worker side
    if(path.startsWith(WORKER_PATH) && authClient.getUser().isAuthenticated()) {
      return new AuthorizationDecision(true);      
    }
    
    // portal iam must be logged in
    if(path.startsWith(IAM_PATH) && 
        Arrays.asList(
            CustomerType.REP_COMPANY,
            CustomerType. REP_PERSON,
            CustomerType.AUTH_CUSTOMER)
        .contains(crmClient.getCustomer().getType())) {

      return new AuthorizationDecision(true);    
    }

    // portal actions must be logged in
    if(path.startsWith(ACTIONS_PATH) && 
        Arrays.asList(
            CustomerType.REP_COMPANY,
            CustomerType. REP_PERSON,
            CustomerType.AUTH_CUSTOMER)
        .contains(crmClient.getCustomer().getType())) {

      return new AuthorizationDecision(true);    
    }

    // anybody can access portal site
    if(path.equals(SITE_PATH)) {
      return new AuthorizationDecision(true);    
    }
    
    return new AuthorizationDecision(false);
  }
}
