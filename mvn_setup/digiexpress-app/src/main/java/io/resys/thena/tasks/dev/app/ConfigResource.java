package io.resys.thena.tasks.dev.app;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.resys.thena.projects.client.api.TenantConfigClient;
import io.resys.thena.projects.client.api.model.ImmutableCreateTenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.resys.userprofile.client.api.UserProfileClient;
import io.resys.userprofile.client.api.model.ImmutableUpsertUserProfile;
import io.resys.userprofile.client.api.model.ImmutableUserDetails;
import io.resys.userprofile.client.api.model.UserProfile;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("q/digiexpress/api")
public class ConfigResource {
  
  @Inject JsonWebToken jwt;
  @Inject TenantConfigClient tenantClient;
  @Inject CurrentTenant currentTenant;
  @Inject CurrentUser currentUser;
  @Inject UserProfileClient userProfileClient;
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("config/current-tenants")
  public Uni<TenantConfig> currentTenant() {
    return tenantClient.query().repoName(currentTenant.tenantsStoreId(), TenantRepoConfigType.TENANT).get(currentTenant.tenantId())
        .onItem().transformToUni(config -> {
          if(config.isEmpty()) {
            return createTenantConfig().onItem().transformToUni(this::createChildRepos);
          }
          return Uni.createFrom().item(config.get());
        });
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("config/current-user-profile")
  public Uni<UserProfile> currentUserProfile() {
    return currentTenant().onItem().transformToUni(config -> {
      final var userProfileConfig = config.getRepoConfigs().stream().filter(entry -> entry.getRepoType() == TenantRepoConfigType.USER_PROFILE).findFirst().get();
      return userProfileClient.withRepoId(userProfileConfig.getRepoId()).createUserProfile().createOne(ImmutableUpsertUserProfile.builder()
        .userId(currentUser.getUserId())
        .targetDate(Instant.now())
        .id(currentUser.getUserId())
        .details(ImmutableUserDetails.builder()
            .firstName(currentUser.getGivenName())
            .lastName(currentUser.getFamilyName())
            .email(currentUser.getEmail())
            .build())
        .build());
      
    });
  }
  
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("config/current-user")
  public Uni<CurrentUser> currentUser() {
    
    
    return Uni.createFrom().item(currentUser);
  }
  
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("config/health")
  public Uni<CurrentTenant> currentHealth() {
    return Uni.createFrom().item(currentTenant);
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("config/jwt")
  public Uni<JsonObject> currentJwt() {
    Map<String, Object> result = new HashMap<>();
    jwt.getClaimNames().forEach(claim -> result.put(claim, jwt.getClaim(claim)));
    return Uni.createFrom().item(JsonObject.mapFrom(result));
  } 
    
  public Uni<TenantConfig> createTenantConfig() {
    return tenantClient.query().repoName(currentTenant.tenantsStoreId(), TenantRepoConfigType.TENANT).createIfNot()
        .onItem().transformToUni(created -> tenantClient.createTenantConfig().createOne(ImmutableCreateTenantConfig.builder()
            .repoId(currentTenant.tenantsStoreId())
            .name(currentTenant.tenantId())
            .targetDate(Instant.now())
            .build()));
  }
  
  public Uni<TenantConfig> createChildRepos(TenantConfig config) {
    return Multi.createFrom().items(config.getRepoConfigs().stream())
        .onItem().transformToUni(this::createChildRepo)
        .concatenate().collect().asList()
        .onItem().transform(junk -> config);
  }
  
  public Uni<TenantConfigClient> createChildRepo(TenantRepoConfig config) {
    return tenantClient.query()
        .repoName(config.getRepoId(), config.getRepoType()).createIfNot();
  }
}
