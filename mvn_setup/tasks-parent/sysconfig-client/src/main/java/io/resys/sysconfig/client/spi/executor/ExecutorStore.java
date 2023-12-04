package io.resys.sysconfig.client.spi.executor;

import java.util.List;
import java.util.Optional;

import io.dialob.client.api.DialobClient;
import io.resys.sysconfig.client.api.ExecutorClient.SysConfigSession;
import io.resys.sysconfig.client.api.model.SysConfigInstance;
import io.resys.sysconfig.client.api.model.SysConfigRelease;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfig;
import io.smallrye.mutiny.Uni;

public interface ExecutorStore {
  
  SysConfigReleaseQuery queryReleases();
  SysConfigInstanceQuery queryInstances();
  SysConfigSessionQuery querySessions();
  DialobFormQuery queryForms();
  
  Uni<SysConfigSession> save(SysConfigSession session);
  Uni<ExecutorStore> withTenantConfig(String tenantConfigId);
  ExecutorStore withTenantConfig(String tenantConfigId, List<TenantRepoConfig> tenantConfig);

  interface SysConfigSessionQuery {
    Uni<SysConfigSession> get(String instanceId);
  }
  
  interface SysConfigInstanceQuery {
    Uni<SysConfigInstance> get(String instanceId);
  }
  
  interface SysConfigReleaseQuery {
    Uni<SysConfigRelease> get(String releaseId);
  }

  interface DialobFormQuery {
    Uni<Optional<DialobClient.ProgramWrapper>> get(String formId); 
  }
}
