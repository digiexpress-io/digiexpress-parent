package io.resys.thena.tasks.dev.app;

import io.dialob.client.api.DialobComposer;
import io.dialob.client.api.DialobComposerRestApi;
import io.resys.thena.projects.client.api.ProjectClient;
import io.resys.thena.projects.client.api.TenantConfig.TenantRepoConfig;
import io.resys.thena.projects.client.api.TenantConfig.TenantRepoConfigType;
import io.resys.thena.tasks.dev.app.user.CurrentTenant;
import io.resys.thena.tasks.dev.app.user.CurrentUser;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;

@Path("q/digiexpress/api")
@Singleton
public class DialobResource extends DialobComposerRestApi {
  
  @Inject CurrentTenant currentTenant;
  @Inject CurrentUser currentUser;
  @Inject ProjectClient tenantClient;
  @Inject DialobComposer dialobComposer;


  public Uni<DialobComposer> getComposer() {
    return getConfig().onItem().transform(config -> dialobComposer.withTenantId(config.getRepoId()));
  }
  
  private Uni<TenantRepoConfig> getConfig() {
    return tenantClient.queryActiveTenantConfig().get(currentTenant.tenantId())
    .onItem().transform(config -> {
      final var dialobConfig = config.getRepoConfigs().stream().filter(entry -> entry.getRepoType() == TenantRepoConfigType.DIALOB).findFirst().get();
      return dialobConfig;
    });
  } 
}
