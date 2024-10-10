package io.resys.thena.tasks.dev.app.security;

import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.security.HttpSecurityPolicy;
import io.resys.permission.client.api.model.Principal;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class SecurityPolicyHttp implements HttpSecurityPolicy {

  @Inject private IdentitySupplier cache;
  
  
  @Override
  public Uni<CheckResult> checkPermission(
      RoutingContext request, 
      Uni<SecurityIdentity> identityUni, 
      AuthorizationRequestContext requestContext) {
    
    
    return identityUni.onItem().transformToUni(identity -> {
      final var principal = (JsonWebToken) identity.getPrincipal();
      final var sub = (String) principal.getClaim(Claims.sub.name());
      final var email = (String) principal.getClaim(Claims.email.name());    
      
      return cache.getPrincipalPermissions(sub, email)
      .onItem().transform(am -> Tuple2.of(identity, am));
    })
    .onItem().transform(i -> {
      if (isPermitted(request, i.getItem1(), i.getItem2())) {
        log.debug("application security policy: user: {} is granted access for path: '{}', method: '{}'", 
            i.getItem1().getPrincipal().getName(), 
            request.request().path(),
            request.request().method()
        );
        return CheckResult.PERMIT;
      }
      log.warn("application security policy: user: {} is denied access for path: '{}', method: '{}', known permissions: {}", 
          i.getItem1().getPrincipal().getName(),
          request.request().path(),
          request.request().method(),
          i.getItem2().getPermissions()
      );
      return CheckResult.DENY;
    });
  }
  
  @Override 
  public String name() {
    // null == global policy
    return null;
  }
 
  private static boolean isPermitted(RoutingContext event, SecurityIdentity _identity, Principal principal) {
    final var path = event.request().path();
    final var httpMethod = HttpMethodInterm.parse(event);
    
    // DEMO
    if(path.contains("demo/api")) {
      return true;//principal.getPermissions().contains(BuiltInDataPermissions.DATA_DEMO.name());
    }
    
    // CRM
    if(path.contains("digiexpress/api/customers")) {
      switch (httpMethod) {
      case READ: return principal.getPermissions().contains(BuiltInDataPermissions.DATA_CRM_READ.name());
      case WRITE: return principal.getPermissions().contains(BuiltInDataPermissions.DATA_CRM_WRITE.name());
      case DELETE: return principal.getPermissions().contains(BuiltInDataPermissions.DATA_CRM_DELETE.name());
      default: return true;
      }
    }
    
    // DIALOB
    if(path.contains("digiexpress/api/dialob")) {
      switch (httpMethod) {
      case READ: return principal.getPermissions().contains(BuiltInDataPermissions.DATA_DIALOB_READ.name());
      case WRITE: return principal.getPermissions().contains(BuiltInDataPermissions.DATA_DIALOB_WRITE.name());
      case DELETE: return principal.getPermissions().contains(BuiltInDataPermissions.DATA_DIALOB_DELETE.name());
      default: return true;
      }
    }

    // WRENCH
    if(path.contains("digiexpress/api/hdes")) {
      switch (httpMethod) {
      case READ: return principal.getPermissions().contains(BuiltInDataPermissions.DATA_WRENCH_READ.name());
      case WRITE: return principal.getPermissions().contains(BuiltInDataPermissions.DATA_WRENCH_WRITE.name());
      case DELETE: return principal.getPermissions().contains(BuiltInDataPermissions.DATA_WRENCH_DELETE.name());
      default: return true;
      }
    }
    
    // authorization/access management 
    if(path.contains("digiexpress/api/am")) {
      switch (httpMethod) {
      case READ: return principal.getPermissions().contains(BuiltInDataPermissions.DATA_PERMISSIONS_READ.name());
      //case WRITE: return principal.getPermissions().contains(BuiltInDataPermissions.DATA_PERMISSIONS_WRITE.name());
      case DELETE: return principal.getPermissions().contains(BuiltInDataPermissions.DATA_PERMISSIONS_DELETE.name());
      default: return true;
      }
    }
    
    // STENCIL
    if(path.contains("digiexpress/api/stencil")) {
      switch (httpMethod) {
      case READ: return principal.getPermissions().contains(BuiltInDataPermissions.DATA_STENCIL_READ.name());
      case WRITE: return principal.getPermissions().contains(BuiltInDataPermissions.DATA_STENCIL_WRITE.name());
      case DELETE: return principal.getPermissions().contains(BuiltInDataPermissions.DATA_STENCIL_DELETE.name());
      default: return true;
      }
    }
    
    // SYS CONFIG I
    if(path.contains("digiexpress/api/sys-configs-asset-sources")) {
      switch (httpMethod) {
      case READ: return principal.getPermissions().contains(BuiltInDataPermissions.DATA_SYSCONFIG_READ.name());
      case WRITE: return principal.getPermissions().contains(BuiltInDataPermissions.DATA_SYSCONFIG_WRITE.name());
      case DELETE: return principal.getPermissions().contains(BuiltInDataPermissions.DATA_SYSCONFIG_DELETE.name());
      default: return true;
      }
    }
    // SYS CONFIG II
    if(path.contains("digiexpress/api/sys-configs")) {
      switch (httpMethod) {
      case READ: return principal.getPermissions().contains(BuiltInDataPermissions.DATA_SYSCONFIG_READ.name());
      case WRITE: return principal.getPermissions().contains(BuiltInDataPermissions.DATA_SYSCONFIG_WRITE.name());
      case DELETE: return principal.getPermissions().contains(BuiltInDataPermissions.DATA_SYSCONFIG_DELETE.name());
      default: return true;
      }
    }
    
    // TENANTS
    if(path.contains("digiexpress/api/tenants")) {
      switch (httpMethod) {
      case READ: return principal.getPermissions().contains(BuiltInDataPermissions.DATA_TENANT_READ.name());
      case WRITE: return principal.getPermissions().contains(BuiltInDataPermissions.DATA_TENANT_WRITE.name());
      case DELETE: return principal.getPermissions().contains(BuiltInDataPermissions.DATA_TENANT_DELETE.name());
      default: return true;
      }
    }
    
    return true;
  }
  
  
  private static enum HttpMethodInterm {
    READ, WRITE, DELETE;
    
    public static HttpMethodInterm parse(RoutingContext event) {
      final io.vertx.core.http.HttpMethod method = event.request().method();
      if(method.equals(io.vertx.core.http.HttpMethod.GET)) {
        return READ;
      } else if(method.equals(io.vertx.core.http.HttpMethod.OPTIONS)) {
        return READ;
      } else if(method.equals(io.vertx.core.http.HttpMethod.PUT)) {
        return WRITE;
      } else if(method.equals(io.vertx.core.http.HttpMethod.POST)) {
        return WRITE;
      } else if(method.equals(io.vertx.core.http.HttpMethod.DELETE)) {
        return WRITE;
      }
      return null;
    }
  }
}
