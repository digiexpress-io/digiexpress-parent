package io.resys.thena.tasks.dev.app.security;

import java.util.Arrays;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.api.model.ImmutablePrincipal;
import io.resys.permission.client.api.model.Principal;
import io.resys.thena.api.entities.org.OrgActorStatusType;
import io.resys.thena.projects.client.api.ProjectClient;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.resys.thena.tasks.dev.app.user.CurrentTenant;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ApplicationScoped
public class PrincipalCache {
  private static final String CACHE_NAME = "PRINCIPAL_CACHE";
  @Inject PermissionClient client;
  @Inject CurrentTenant currentTenant;
  @Inject ProjectClient tenantClient;
  
  @ConfigProperty(name = "tenant.failSafeUsers")
  String failSafeUsers;

  @CacheResult(cacheName = PrincipalCache.CACHE_NAME)
  public Uni<Principal> getPrincipalPermissions(String principalId, String email) {
    return getClient().onItem()
        .transformToUni(client -> {
          return client.principalQuery().get(principalId)
          .onFailure()
          .recoverWithUni(e -> {
            log.warn("Failed to find principal by id: {}, trying to find by email: {}, error: {}", principalId, email, e.getMessage(), e);
            return client.principalQuery().get(email);
          });
        }).onItem().transform(principal -> {
          if(isFailSafeUser(principalId, email)) {
            final var failSafe = Arrays.asList(BuiltInDataPermissions.values()).stream().map(e -> e.name()).toList();
            return ImmutablePrincipal.builder().from(principal).addAllPermissions(failSafe).build();
          }
          return principal;
        })
        .onFailure().recoverWithItem(() -> onFailureRecoverWith(principalId, email));
  }
  
  private Principal onFailureRecoverWith(String principalId, String email) {
    if(isFailSafeUser(principalId, email)) {
      final var failSafe = Arrays.asList(BuiltInDataPermissions.values()).stream().map(e -> e.name()).toList();
      return ImmutablePrincipal.builder()
          .id(principalId)
          .version("")
          .email(email)
          .status(OrgActorStatusType.IN_FORCE)
          .name(principalId)
          .addAllPermissions(failSafe).build();
    }
    
    return ImmutablePrincipal.builder()
        .id(principalId)
        .version("")
        .email(email)
        .status(OrgActorStatusType.IN_FORCE)
        .name(principalId)
        .build();
  }
  
  @CacheInvalidate(cacheName = PrincipalCache.CACHE_NAME)
  public Uni<Void> invalidate() {
    return Uni.createFrom().voidItem();
  }  
  private Uni<PermissionClient> getClient() {
    return getPermissionConfig().onItem().transform(config -> client.withRepoId(config.getRepoId()));
  }
  private Uni<TenantRepoConfig> getPermissionConfig() {
    return tenantClient.queryActiveTenantConfig().get(currentTenant.tenantId())
      .onItem().transform(config -> config.getRepoConfig(TenantRepoConfigType.PERMISSIONS));
  }
  
  private boolean isFailSafeUser(String principalId, String email) {
    return Arrays.asList(this.failSafeUsers.split(";")).stream()
        .map(e -> e.trim().toLowerCase())
        .filter(e -> e.equals(principalId.toLowerCase()) || e.equals(email.toLowerCase()))
        .count() > 0;
  }
}
