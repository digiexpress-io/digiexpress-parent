package io.resys.thena.tasks.dev.app;

import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.projects.client.api.ProjectClient;
import io.resys.thena.projects.client.api.TenantConfigRestApi;
import io.resys.thena.projects.client.api.TenantConfigCommand.ArchiveTenantConfig;
import io.resys.thena.projects.client.api.TenantConfigCommand.CreateTenantConfig;
import io.resys.thena.projects.client.api.TenantConfigCommand.TenantConfigUpdateCommand;
import io.resys.thena.tasks.dev.app.user.CurrentTenant;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;

@Path("q/digiexpress/api")
@Singleton
public class TenantsResource implements TenantConfigRestApi {

  @Inject private ProjectClient tenantConfigClient;
  @Inject private CurrentTenant currentTenant;
  
  @Override
  public Uni<List<io.resys.thena.projects.client.api.TenantConfig>> findTenantConfigs() {
    return tenantConfigClient.queryActiveTenantConfig().findAll();
  }
  @Override
  public Uni<List<io.resys.thena.projects.client.api.TenantConfig>> createTenantConfigs(List<CreateTenantConfig> commands) {

    return tenantConfigClient.createTenantConfig().createMany(commands)
        .onItem().transformToUni(items -> {
          
          // create repo for each configuration item
          return Multi.createFrom().items(items.stream().flatMap(e -> e.getRepoConfigs().stream()))
              .onItem().transformToUni(config -> tenantConfigClient.query()
              .repoName(config.getRepoId(), config.getRepoType())
              .createIfNot().onItem().transform(created -> created)  
          )
          .concatenate().collect().asList()
          .onItem().transform(junk -> items);
          
        });
  }
  @Override
  public Uni<List<io.resys.thena.projects.client.api.TenantConfig>> updateTenantConfigs(List<TenantConfigUpdateCommand> commands) {

    return tenantConfigClient.updateTenantConfig().updateMany(commands);
  }
  @Override
  public Uni<List<io.resys.thena.projects.client.api.TenantConfig>> deleteTenantConfigs(List<ArchiveTenantConfig> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> (TenantConfigUpdateCommand) command)
        .collect(Collectors.toList());
    return tenantConfigClient.updateTenantConfig().updateMany(modifiedCommands);
  }
  @Override
  public Uni<io.resys.thena.projects.client.api.TenantConfig> updateOneTenantConfig(String projectId, List<TenantConfigUpdateCommand> commands) {
    return tenantConfigClient.updateTenantConfig().updateOne(commands);
  }
}
