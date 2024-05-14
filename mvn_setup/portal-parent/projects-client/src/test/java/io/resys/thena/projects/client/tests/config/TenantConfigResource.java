package io.resys.thena.projects.client.tests.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.projects.client.api.ImmutableTenantConfig;
import io.resys.thena.projects.client.api.ImmutableTenantPreferences;
import io.resys.thena.projects.client.api.ImmutableTenantRepoConfig;
import io.resys.thena.projects.client.api.TenantConfig;
import io.resys.thena.projects.client.api.TenantConfig.TenantRepoConfigType;
import io.resys.thena.projects.client.api.TenantConfig.TenantStatus;
import io.resys.thena.projects.client.api.TenantConfigCommand.ArchiveTenantConfig;
import io.resys.thena.projects.client.api.TenantConfigCommand.CreateTenantConfig;
import io.resys.thena.projects.client.api.TenantConfigCommand.TenantConfigUpdateCommand;
import io.resys.thena.projects.client.api.TenantConfigRestApi;
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
