package io.resys.thena.tasks.dev.app;

import io.resys.thena.projects.client.api.ProjectClient;
import io.resys.thena.projects.client.api.model.ImmutableCreateTenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.ArchiveTenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.CreateTenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.TenantConfigUpdateCommand;
import io.resys.thena.projects.client.rest.TenantConfigRestApi;
import io.resys.thena.tasks.dev.app.user.CurrentTenant;
import io.resys.thena.tasks.dev.app.user.CurrentUser;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Path("q/digiexpress/api")
public class TenantsResource implements TenantConfigRestApi {

  @Inject ProjectClient tenantConfigClient;
  @Inject CurrentTenant currentTenant;
  @Inject CurrentUser currentUser;
  
  
  @Override
  public Uni<List<io.resys.thena.projects.client.api.model.TenantConfig>> findTenantConfigs() {
    return tenantConfigClient.queryActiveTenantConfig().findAll();
  }
  @Override
  public Uni<List<io.resys.thena.projects.client.api.model.TenantConfig>> createTenantConfigs(List<CreateTenantConfig> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> ImmutableCreateTenantConfig.builder().from(command)
            .targetDate(Instant.now())
            .userId(currentUser.userId())
            .build())
        .collect(Collectors.toList());
    return tenantConfigClient.createTenantConfig().createMany(modifiedCommands)
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
  public Uni<List<io.resys.thena.projects.client.api.model.TenantConfig>> updateTenantConfigs(List<TenantConfigUpdateCommand> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> command.withTargetDate(Instant.now()).withUserId(currentUser.userId()))
        .collect(Collectors.toList());
    return tenantConfigClient.updateTenantConfig().updateMany(modifiedCommands);
  }
  @Override
  public Uni<List<io.resys.thena.projects.client.api.model.TenantConfig>> deleteTenantConfigs(List<ArchiveTenantConfig> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> command
            .withTargetDate(Instant.now())
            .withUserId(currentUser.userId()))
        .collect(Collectors.toList());
    return tenantConfigClient.updateTenantConfig().updateMany(modifiedCommands);
  }
  @Override
  public Uni<io.resys.thena.projects.client.api.model.TenantConfig> updateOneTenantConfig(String projectId, List<TenantConfigUpdateCommand> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> command
            .withTargetDate(Instant.now())
            .withUserId(currentUser.userId()))
        .collect(Collectors.toList());
    return tenantConfigClient.updateTenantConfig().updateOne(modifiedCommands);
  }
}
