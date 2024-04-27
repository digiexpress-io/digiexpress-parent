package io.resys.thena.tasks.dev.app.security;

import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.security.HttpSecurityPolicy;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class AppHttpSecPolicy implements HttpSecurityPolicy {

  @Override
  public Uni<CheckResult> checkPermission(RoutingContext request, Uni<SecurityIdentity> identity, AuthorizationRequestContext requestContext) {
    return identity.onItem().transform(i -> {
      if (isPermitted(request, i)) {
        log.debug("application security policy: user: {} is granted access for path: '{}', method: '{}'", 
            i.getPrincipal().getName(), 
            request.request().path(),
            request.request().method()
        );
        return CheckResult.PERMIT;
      }
      log.warn("application security policy: user: {} is denied access for path: '{}', method: '{}'", 
          i.getPrincipal().getName(), 
          request.request().path(),
          request.request().method()
      );
      return CheckResult.DENY;
    });
  }
  
  @Override 
  public String name() {
    // null == global policy
    return null;
  }
 
  private static boolean isPermitted(RoutingContext event, SecurityIdentity identity) {
    final var path = event.request().path();
    final var httpMethod = HttpMethodInterm.parse(event);
    
    // DEMO
    if(path.contains("demo/api")) {
      return identity.hasRole(BuiltInDataPermissions.DATA_DEMO.name());
    }
    
    // CRM
    if(path.contains("digiexpress/api/customers")) {
      switch (httpMethod) {
      case READ: return identity.hasRole(BuiltInDataPermissions.DATA_CRM_READ.name());
      case WRITE: return identity.hasRole(BuiltInDataPermissions.DATA_CRM_WRITE.name());
      case DELETE: return identity.hasRole(BuiltInDataPermissions.DATA_CRM_DELETE.name());
      default: return false;
      }
    }
    
    // DIALOB
    if(path.contains("digiexpress/api/dialob")) {
      switch (httpMethod) {
      case READ: return identity.hasRole(BuiltInDataPermissions.DATA_DIALOB_READ.name());
      case WRITE: return identity.hasRole(BuiltInDataPermissions.DATA_DIALOB_WRITE.name());
      case DELETE: return identity.hasRole(BuiltInDataPermissions.DATA_DIALOB_DELETE.name());
      default: return false;
      }
    }

    // WRENCH
    if(path.contains("digiexpress/api/hdes")) {
      switch (httpMethod) {
      case READ: return identity.hasRole(BuiltInDataPermissions.DATA_WRENCH_READ.name());
      case WRITE: return identity.hasRole(BuiltInDataPermissions.DATA_WRENCH_WRITE.name());
      case DELETE: return identity.hasRole(BuiltInDataPermissions.DATA_WRENCH_DELETE.name());
      default: return false;
      }
    }
    
    // authorization/access management 
    if(path.contains("digiexpress/api/am")) {
      switch (httpMethod) {
      case READ: return identity.hasRole(BuiltInDataPermissions.DATA_PERMISSIONS_READ.name());
      case WRITE: return identity.hasRole(BuiltInDataPermissions.DATA_PERMISSIONS_WRITE.name());
      case DELETE: return identity.hasRole(BuiltInDataPermissions.DATA_PERMISSIONS_DELETE.name());
      default: return false;
      }
    }
    
    // STENCIL
    if(path.contains("digiexpress/api/stencil")) {
      switch (httpMethod) {
      case READ: return identity.hasRole(BuiltInDataPermissions.DATA_STENCIL_READ.name());
      case WRITE: return identity.hasRole(BuiltInDataPermissions.DATA_STENCIL_WRITE.name());
      case DELETE: return identity.hasRole(BuiltInDataPermissions.DATA_STENCIL_DELETE.name());
      default: return false;
      }
    }
    
    // SYS CONFIG I
    if(path.contains("digiexpress/api/sys-configs-asset-sources")) {
      switch (httpMethod) {
      case READ: return identity.hasRole(BuiltInDataPermissions.DATA_SYSCONFIG_READ.name());
      case WRITE: return identity.hasRole(BuiltInDataPermissions.DATA_SYSCONFIG_WRITE.name());
      case DELETE: return identity.hasRole(BuiltInDataPermissions.DATA_SYSCONFIG_DELETE.name());
      default: return false;
      }
    }
    // SYS CONFIG II
    if(path.contains("digiexpress/api/sys-configs")) {
      switch (httpMethod) {
      case READ: return identity.hasRole(BuiltInDataPermissions.DATA_SYSCONFIG_READ.name());
      case WRITE: return identity.hasRole(BuiltInDataPermissions.DATA_SYSCONFIG_WRITE.name());
      case DELETE: return identity.hasRole(BuiltInDataPermissions.DATA_SYSCONFIG_DELETE.name());
      default: return false;
      }
    }
    
    // TENANTS
    if(path.contains("digiexpress/api/tenants")) {
      switch (httpMethod) {
      case READ: return identity.hasRole(BuiltInDataPermissions.DATA_TENANT_READ.name());
      case WRITE: return identity.hasRole(BuiltInDataPermissions.DATA_TENANT_WRITE.name());
      case DELETE: return identity.hasRole(BuiltInDataPermissions.DATA_TENANT_DELETE.name());
      default: return false;
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
