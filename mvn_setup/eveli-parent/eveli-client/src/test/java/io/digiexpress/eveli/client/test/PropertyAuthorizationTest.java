package io.digiexpress.eveli.client.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
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
import org.springframework.test.context.junit4.SpringRunner;

import io.digiexpress.eveli.client.api.AuthClient;
import io.digiexpress.eveli.client.api.CrmClient;
import io.digiexpress.eveli.client.config.EveliAutoConfigPermissions;
import io.digiexpress.eveli.client.spi.auth.SpringSecurityPolicy;
import jakarta.servlet.http.HttpServletRequest;

@RunWith(SpringRunner.class)
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
  AuthClient portalClient;
  @MockitoBean
  CrmClient crmClient;
  @MockitoBean
  private AuthClient.User portalUser;

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
