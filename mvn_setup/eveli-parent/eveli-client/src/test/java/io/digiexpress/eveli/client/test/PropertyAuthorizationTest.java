package io.digiexpress.eveli.client.test;

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

import io.digiexpress.eveli.client.api.GamutAuthClient;
import io.digiexpress.eveli.client.api.WorkerAuthClient;
import io.digiexpress.eveli.client.config.EveliAutoConfigPermissions;
import io.digiexpress.eveli.client.spi.auth.SpringSecurityPolicy;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SpringBootTest(classes= {EveliAutoConfigPermissions.class})
@SuppressWarnings({"unchecked", "rawtypes"})
@EnableConfigurationProperties
public class PropertyAuthorizationTest {

  @Autowired
  private EveliAutoConfigPermissions yamlProperties;

  @MockitoBean
  RequestAuthorizationContext context;
  @MockitoBean
  HttpServletRequest request;
  @MockitoBean
  Authentication authentication;
  @MockitoBean
  WorkerAuthClient portalClient;
  @MockitoBean
  GamutAuthClient crmClient;
  @MockitoBean
  private WorkerAuthClient.User portalUser;

  Collection userRole = createAuthorities("ROLE_USER");
  Collection adminRole = createAuthorities("ROLE_ADMIN");
  Collection otherRole = createAuthorities("ROLE_NOUSER");
  @Test
  public void testAllAccess() {

    SpringSecurityPolicy policy = setupPolicy();

    Mockito.when(request.getServletPath()).thenReturn("/worker/rest/api/tasks");
    
    Mockito.when(request.getMethod()).thenReturn("GET");
    Assertions.assertTrue(testAccess(userRole, policy));
    Assertions.assertTrue(testAccess(adminRole, policy));
    Assertions.assertFalse(testAccess(otherRole, policy));
    
    Mockito.when(request.getMethod()).thenReturn("POST");
    Assertions.assertFalse(testAccess(userRole, policy));
    Assertions.assertTrue(testAccess(adminRole, policy));
    Assertions.assertFalse(testAccess(otherRole, policy));
    
    Mockito.when(request.getMethod()).thenReturn("PUT");
    Assertions.assertFalse(testAccess(userRole, policy));
    Assertions.assertTrue(testAccess(adminRole, policy));
    Assertions.assertFalse(testAccess(otherRole, policy));
  }

  private boolean testAccess(Collection userRoles, SpringSecurityPolicy policy) {
    Mockito.when(authentication.getAuthorities()).thenReturn(userRoles);
    AuthorizationResult result = policy.authorize(() -> authentication, context);
    return result.isGranted();
    
  }

  @Test
  public void testOneAccess() {
    SpringSecurityPolicy policy = setupPolicy();
    Mockito.when(request.getServletPath()).thenReturn("/worker/rest/api/tasks/1");
    
    Mockito.when(request.getMethod()).thenReturn("GET");
    Assertions.assertTrue(testAccess(userRole, policy));
    Assertions.assertTrue(testAccess(adminRole, policy));
    Assertions.assertFalse(testAccess(otherRole, policy));
    
    Mockito.when(request.getMethod()).thenReturn("PUT");
    Assertions.assertFalse(testAccess(userRole, policy));
    Assertions.assertTrue(testAccess(adminRole, policy));
    Assertions.assertFalse(testAccess(otherRole, policy));

  }

  private SpringSecurityPolicy setupPolicy() {
    SpringSecurityPolicy policy = new SpringSecurityPolicy(portalClient, crmClient, yamlProperties);
    Mockito.when(context.getRequest()).thenReturn(request);
    Mockito.when(portalClient.getUser()).thenReturn(portalUser);
    Mockito.when(portalUser.isAuthenticated()).thenReturn(true);
    return policy;
  }

  private Collection<GrantedAuthority> createAuthorities(String... authorities) {
    List<GrantedAuthority> result = new ArrayList<>();
    for (String auth : authorities) {
      result.add(new SimpleGrantedAuthority(auth));
    }
    return result;
  }


}
