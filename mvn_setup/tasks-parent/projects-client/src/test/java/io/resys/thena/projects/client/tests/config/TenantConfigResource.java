package io.resys.thena.projects.client.tests.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.projects.client.api.model.Document.DocumentType;
import io.resys.thena.projects.client.api.model.ImmutableCreateTenantConfig;
import io.resys.thena.projects.client.api.model.ImmutableTenantConfig;
import io.resys.thena.projects.client.api.model.ImmutableTenantConfigTransaction;
import io.resys.thena.projects.client.api.model.ImmutableTenantPreferences;
import io.resys.thena.projects.client.api.model.ImmutableTenantRepoConfig;
import io.resys.thena.projects.client.api.model.TenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantStatus;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.ArchiveTenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.CreateTenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.TenantConfigCommandType;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.TenantConfigUpdateCommand;
import io.resys.thena.projects.client.rest.TenantConfigRestApi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Path;

@Path("q/digiexpress/api")
@ApplicationScoped
public class TenantConfigResource implements TenantConfigRestApi {

  private final ImmutableTenantConfig mockTenantConfig = ImmutableTenantConfig.builder()
      .id("tenant-1")
      .name("abc-company")
      .version("v1.0")
      .archived(ProjectTestCase.getTargetDate())
      .created(ProjectTestCase.getTargetDate())
      .updated(ProjectTestCase.getTargetDate())
      .status(TenantStatus.IN_FORCE)
      .preferences(ImmutableTenantPreferences.builder()
          .landingApp("tasks")
          .build())
      .addRepoConfigs(ImmutableTenantRepoConfig.builder()
          .repoId("repo-1")
          .repoType(TenantRepoConfigType.TASKS)
          .build())
      .addTransactions(
          ImmutableTenantConfigTransaction.builder()
          .id("transation-1")
          .addCommands(ImmutableCreateTenantConfig
              .builder()
              .commandType(TenantConfigCommandType.CreateTenantConfig)
              .repoId("repo-1")
              .name("tasks-tenant")
              .build())
          .build())
      .documentType(DocumentType.TENANT_CONFIG)
      .build();

  @Override
  public Uni<List<TenantConfig>> findTenantConfigs() {
    return Uni.createFrom()
        .item(Arrays.asList(mockTenantConfig));
  }

  @Override
  public Uni<List<TenantConfig>> createTenantConfigs(List<CreateTenantConfig> commands) {
    return Uni.createFrom().item(commands.stream().map(e -> mockTenantConfig).collect(Collectors.toList()));
  }

  @Override
  public Uni<List<TenantConfig>> updateTenantConfigs(List<TenantConfigUpdateCommand> commands) {
    return Uni.createFrom().item(commands.stream().map(e -> mockTenantConfig).collect(Collectors.toList()));
  }

  @Override
  public Uni<List<TenantConfig>> deleteTenantConfigs(List<ArchiveTenantConfig> commands) {
    return Uni.createFrom().item(Arrays.asList(mockTenantConfig, mockTenantConfig));
  }

  @Override
  public Uni<TenantConfig> updateOneTenantConfig(String tenantConfigId, List<TenantConfigUpdateCommand> commands) {
    return Uni.createFrom().item(mockTenantConfig);
  }
}
