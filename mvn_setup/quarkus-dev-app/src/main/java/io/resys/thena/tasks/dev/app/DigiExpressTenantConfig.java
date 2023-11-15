package io.resys.thena.tasks.dev.app;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.projects.client.api.TenantConfigClient;
import io.resys.thena.projects.client.api.model.ImmutableCreateTenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.CreateTenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.TenantConfigUpdateCommand;
import io.resys.thena.projects.client.rest.TenantConfigRestApi;
import io.resys.thena.tasks.dev.app.BeanFactory.CurrentTenant;
import io.resys.thena.tasks.dev.app.BeanFactory.CurrentUser;
import io.resys.thena.tasks.dev.app.DemoResource.HeadState;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("q/digiexpress/api")
public class DigiExpressTenantConfig implements TenantConfigRestApi {

  @Inject TenantConfigClient tenantConfigClient;
  @Inject CurrentTenant currentTenant;
  @Inject CurrentUser currentUser;
  
  
  @Override
  public Uni<List<io.resys.thena.projects.client.api.model.TenantConfig>> findTenantConfigs() {
    return tenantConfigClient.tenantConfig().queryActiveTenantConfig().findAll();
  }
  @Override
  public Uni<List<io.resys.thena.projects.client.api.model.TenantConfig>> createTenantConfigs(List<CreateTenantConfig> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> ImmutableCreateTenantConfig.builder().from(command)
            .targetDate(Instant.now())
            .userId(currentUser.getUserId())
            .build())
        .collect(Collectors.toList());
    return tenantConfigClient.tenantConfig().createTenantConfig().createMany(modifiedCommands)
        .onItem().transformToUni(items -> {
          
          // create repo for each configuration item
          return Multi.createFrom().items(items.stream().flatMap(e -> e.getRepoConfigs().stream()))
              .onItem().transformToUni(config -> tenantConfigClient.repo().query()
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
        .map(command -> command.withTargetDate(Instant.now()).withUserId(currentUser.getUserId()))
        .collect(Collectors.toList());
    return tenantConfigClient.tenantConfig().updateTenantConfig().updateMany(modifiedCommands);
  }
  @Override
  public Uni<List<io.resys.thena.projects.client.api.model.TenantConfig>> deleteTenantConfigs(List<TenantConfigUpdateCommand> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> command
            .withTargetDate(Instant.now())
            .withUserId(currentUser.getUserId()))
        .collect(Collectors.toList());
    return tenantConfigClient.tenantConfig().updateTenantConfig().updateMany(modifiedCommands);
  }
  @Override
  public Uni<io.resys.thena.projects.client.api.model.TenantConfig> updateOneTenantConfig(String projectId, List<TenantConfigUpdateCommand> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> command
            .withTargetDate(Instant.now())
            .withUserId(currentUser.getUserId()))
        .collect(Collectors.toList());
    return tenantConfigClient.tenantConfig().updateTenantConfig().updateOne(modifiedCommands);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("init")
  public Uni<HeadState> init() {
    return tenantConfigClient.repo().query().repoName(currentTenant.getProjectId(), TenantRepoConfigType.TENANT).createIfNot()
        .onItem().transform(created -> HeadState.builder().created(true).build());
  }
}
