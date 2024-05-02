package io.resys.thena.tasks.dev.app;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.model.Principal;
import io.resys.thena.projects.client.api.ProjectClient;
import io.resys.thena.projects.client.api.TenantConfig;
import io.resys.thena.projects.client.api.TenantConfig.TenantRepoConfigType;
import io.resys.thena.tasks.dev.app.security.IdentitySupplier;
import io.resys.thena.tasks.dev.app.user.CurrentTenant;
import io.resys.thena.tasks.dev.app.user.CurrentUser;
import io.resys.thena.tasks.dev.app.user.CurrentUserConfig;
import io.resys.thena.tasks.dev.app.user.ImmutableCurrentPermissions;
import io.resys.thena.tasks.dev.app.user.ImmutableCurrentUserConfig;
import io.resys.userprofile.client.api.UserProfileClient;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("q/digiexpress/api")
@Singleton
public class ConfigResource {
  
  @Inject JsonWebToken jwt;
  @Inject CurrentUser currentUser;
  @Inject IdentitySupplier cache;
  @Inject CurrentTenant currentTenant;
  @Inject ProjectClient tenantClient;
  @Inject PermissionClient permissionsClient;
  @Inject UserProfileClient userProfileClient;
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("config/current-user")
  public Uni<CurrentUserConfig> currentUserConfig() {
    return tenantClient.query().repoName(currentTenant.tenantsStoreId(), TenantRepoConfigType.TENANT).get(currentTenant.tenantId())
    .onItem().transformToUni(tenantConfig -> 
        cache.getPrincipalPermissions(currentUser.getUserId(), currentUser.getEmail())
        .onItem().transformToUni(principal -> getProfile(tenantConfig.get(), principal))
    );
  }

  
  private Uni<CurrentUserConfig> getProfile(TenantConfig tenantConfig, Principal principal) {
    return userProfileClient
    .withRepoId(tenantConfig.getRepoConfig(TenantRepoConfigType.USER_PROFILE).getRepoId())
    .userProfileQuery().get(principal.getId())
    .onItem().transform(profile -> {
      
      final var permissions = ImmutableCurrentPermissions.builder()
        .principal(principal)
        .addAllPermissions(principal.getPermissions())
        .build();
    
      return ImmutableCurrentUserConfig.builder()
        .profile(profile)
        .tenant(tenantConfig)
        .permissions(permissions)
        .build();
      
    });
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("config/cache")
  public Uni<Void> cache() {
    return cache.invalidate();
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("config/health")
  public Uni<Void> health() {
    return Uni.createFrom().voidItem();
  }
   
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("config/jwt")
  public Uni<JsonObject> currentJwt() {
    Map<String, Object> result = new HashMap<>();
    jwt.getClaimNames().forEach(claim -> result.put(claim, jwt.getClaim(claim)));
    return Uni.createFrom().item(JsonObject.mapFrom(result));
  } 

}
