package io.resys.thena.projects.client.api.actions;

import java.util.Optional;

import io.resys.thena.projects.client.api.TenantConfigClient;
import io.resys.thena.projects.client.api.model.TenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.smallrye.mutiny.Uni;

public interface RepositoryQuery {
  RepositoryQuery repoName(String repoName, TenantRepoConfigType type);
  TenantConfigClient build();

  Uni<TenantConfigClient> deleteAll();
  Uni<TenantConfigClient> delete();
  Uni<TenantConfigClient> create();
  Uni<TenantConfigClient> createIfNot();
  
  Uni<Optional<TenantConfig>> get(String tenantId);
} 
