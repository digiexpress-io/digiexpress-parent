package io.resys.thena.tasks.dev.app.security;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import io.resys.permission.client.api.PermissionClient;
import io.resys.thena.projects.client.api.TenantConfigClient;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.resys.thena.tasks.dev.app.CurrentTenant;
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
  @Inject TenantConfigClient tenantClient;
  
  @ConfigProperty(name = "tenant.failSafeUsers")
  String failSafeUsers;
  
  @CacheResult(cacheName = PrincipalCache.CACHE_NAME)
  public Uni<List<String>> getPrincipalPermissions(String principalId, String email) {
    if(isFailSafeUser(principalId, email)) {
      return Uni.createFrom().item(
          Arrays.asList(BuiltInDataPermissions.values())
          .stream().map(e -> e.name())
          .toList());
    }
    
    return getClient().onItem()
        .transformToUni(client -> {
          
          return client.principalQuery().get(principalId)
          .onFailure()
          .recoverWithUni(e -> {
            log.warn("Failed to find principal by id: {}, trying to find by email: {}, error: {}", principalId, email, e.getMessage(), e);
            return client.principalQuery().get(email);
          })
          .onItem().transform(found -> found.getPermissions())
          .onFailure().recoverWithItem(e -> {
            log.error("Failed to find principal by id: {} and email: {}, error: {}", principalId, email, e.getMessage(), e);
            return Collections.emptyList();
          });
        });
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
