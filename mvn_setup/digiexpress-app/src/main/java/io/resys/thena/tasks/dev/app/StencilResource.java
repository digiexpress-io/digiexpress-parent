package io.resys.thena.tasks.dev.app;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.thena.projects.client.api.ProjectClient;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.resys.thena.tasks.dev.app.user.CurrentTenant;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilComposer;
import io.thestencil.client.rest.StencilRestApi;
import io.thestencil.client.rest.StencilRestApiTemplate;
import jakarta.ws.rs.Path;

@Path("q/digiexpress/api/stencil")
public class StencilResource extends StencilRestApiTemplate implements StencilRestApi {
  private final ProjectClient tenantClient;
  private final CurrentTenant currentTenant;
  
  public StencilResource(
      StencilComposer client, ObjectMapper objectMapper, 
      ProjectClient tenantClient, 
      CurrentTenant currentTenant) {
    super(client, objectMapper);
    this.tenantClient = tenantClient;
    this.currentTenant = currentTenant;
  }
  
  @Override
  protected Uni<StencilComposer> getClient() {
    return getStencilConfig().onItem().transform(config -> super.client.withRepo(config.getRepoId()));
  }
  
  private Uni<TenantRepoConfig> getStencilConfig() {
    return tenantClient.queryActiveTenantConfig().get(currentTenant.tenantId())
    .onItem().transform(config -> {
      final var crmConfig = config.getRepoConfigs().stream().filter(entry -> entry.getRepoType() == TenantRepoConfigType.STENCIL).findFirst().get();
      return crmConfig;
    });
  }

}
