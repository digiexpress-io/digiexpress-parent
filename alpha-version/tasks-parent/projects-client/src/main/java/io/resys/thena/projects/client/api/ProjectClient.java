package io.resys.thena.projects.client.api;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.projects.client.api.TenantConfig.TenantRepoConfigType;
import io.resys.thena.projects.client.api.TenantConfigCommand.CreateTenantConfig;
import io.resys.thena.projects.client.api.TenantConfigCommand.TenantConfigUpdateCommand;
import io.smallrye.mutiny.Uni;

public interface ProjectClient {

  ProjectClient withRepoId(String repoId);
  RepositoryQuery query();
  Uni<Tenant> getRepo();
  
  CreateTenantConfigAction createTenantConfig();
  UpdateTenantConfigAction updateTenantConfig();
  ActiveTenantConfigQuery queryActiveTenantConfig();

  
  
  
  
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
    Uni<List<TenantConfig>> deleteAll();
  }
  
  public interface RepositoryQuery {
    RepositoryQuery repoName(String repoName, TenantRepoConfigType type);
    ProjectClient build();

    Uni<ProjectClient> deleteAll();
    Uni<ProjectClient> delete();
    Uni<ProjectClient> create();
    Uni<ProjectClient> createIfNot();
    
    Uni<Optional<TenantConfig>> get(String tenantId);
  } 

}
