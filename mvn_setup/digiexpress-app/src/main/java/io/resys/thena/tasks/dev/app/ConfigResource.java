package io.resys.thena.tasks.dev.app;

import java.time.Instant;

import io.resys.thena.projects.client.api.TenantConfigClient;
import io.resys.thena.projects.client.api.model.ImmutableCreateTenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.resys.thena.tasks.dev.app.BeanFactory.CurrentTenant;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("q/digiexpress/api")
public class ConfigResource {
  
  @Inject TenantConfigClient tenantClient;
  @Inject CurrentTenant currentTenant;
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("config/current-tenants")
  public Uni<TenantConfig> currentTenant() {
    return tenantClient.repo().query().repoName(currentTenant.getTenantsStoreId(), TenantRepoConfigType.TENANT).get(currentTenant.getTenantId())
        .onItem().transformToUni(config -> {
          if(config.isEmpty()) {
            return createTenantConfig().onItem().transformToUni(this::createChildRepos);
          }
          return Uni.createFrom().item(config.get());
        });
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("config/health")
  public Uni<CurrentTenant> currentHealth() {
    return Uni.createFrom().item(currentTenant);
  } 
  
  public Uni<TenantConfig> createTenantConfig() {
    return tenantClient.repo().query().repoName(currentTenant.getTenantsStoreId(), TenantRepoConfigType.TENANT).createIfNot()
        .onItem().transformToUni(created -> {
          return tenantClient.tenantConfig().createTenantConfig().createOne(ImmutableCreateTenantConfig.builder()
              .repoId(currentTenant.getTenantsStoreId())
              .name(currentTenant.getTenantId())
              .targetDate(Instant.now())
              .build());
        });
  }
  
  public Uni<TenantConfig> createChildRepos(TenantConfig config) {
    return Multi.createFrom().items(config.getRepoConfigs().stream())
        .onItem().transformToUni(this::createChildRepo)
        .concatenate().collect().asList()
        .onItem().transform(junk -> config);
  }
  
  public Uni<TenantConfigClient> createChildRepo(TenantRepoConfig config) {
    return tenantClient.repo().query().repoName(config.getRepoId(), config.getRepoType()).createIfNot();
  }
}
