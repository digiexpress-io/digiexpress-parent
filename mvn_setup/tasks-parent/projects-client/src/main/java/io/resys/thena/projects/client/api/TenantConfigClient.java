package io.resys.thena.projects.client.api;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.resys.thena.projects.client.api.actions.RepositoryActions;
import io.resys.thena.projects.client.api.actions.TenantConfigActions;
import io.resys.thena.projects.client.api.model.TenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.CreateTenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.TenantConfigUpdateCommand;
import io.smallrye.mutiny.Uni;

public interface TenantConfigClient {
  TenantConfigActions tenantConfig();
  RepositoryActions repo();
  
  
  interface CreateTenantConfigAction {
    Uni<TenantConfig> createOne(CreateTenantConfig command);
    Uni<List<TenantConfig>> createMany(List<? extends CreateTenantConfig> commands);
  }

  interface UpdateTenantConfigAction {
    Uni<TenantConfig> updateOne(TenantConfigUpdateCommand command);
    Uni<TenantConfig> updateOne(List<TenantConfigUpdateCommand> commands);
    Uni<List<TenantConfig>> updateMany(List<TenantConfigUpdateCommand> commands);
  }

  interface ActiveTenantConfigQuery {
    Uni<List<TenantConfig>> findAll();
    Uni<List<TenantConfig>> findByIds(Collection<String> tenantConfigIds);
    Uni<TenantConfig> get(String tenantConfigId);
    Uni<List<TenantConfig>> deleteAll(String userId, Instant targetDate);
  }
  
  public interface RepositoryQuery {
    RepositoryQuery repoName(String repoName, TenantRepoConfigType type);
    TenantConfigClient build();

    Uni<TenantConfigClient> deleteAll();
    Uni<TenantConfigClient> delete();
    Uni<TenantConfigClient> create();
    Uni<TenantConfigClient> createIfNot();
    
    Uni<Optional<TenantConfig>> get(String tenantId);
  } 

}
