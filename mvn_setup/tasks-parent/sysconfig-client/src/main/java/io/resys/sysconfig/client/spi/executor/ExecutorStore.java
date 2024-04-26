package io.resys.sysconfig.client.spi.executor;

import java.util.List;

import org.immutables.value.Value;

import io.dialob.client.api.DialobClient;
import io.resys.hdes.client.api.programs.FlowProgram;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import io.resys.sysconfig.client.api.ExecutorClient.SysConfigSession;
import io.resys.sysconfig.client.api.model.SysConfigInstance;
import io.resys.sysconfig.client.api.model.SysConfigRelease;
import io.resys.thena.projects.client.api.TenantConfig.TenantRepoConfig;
import io.smallrye.mutiny.Uni;

public interface ExecutorStore {
  
  SysConfigReleaseQuery queryReleases();
  SysConfigInstanceQuery queryInstances();
  SysConfigSessionQuery querySessions();
  DialobFormQuery queryForms();
  WrenchFlowQuery queryFlows();
  
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
  interface WrenchFlowQuery {
    WrenchFlowQuery releaseId(String releaseId);
    Uni<WrenchFlow> get(String flowId); 
  }
  interface DialobFormQuery {
    DialobFormQuery releaseId(String releaseId);
    Uni<DialobClient.ProgramWrapper> get(String formId); 
  }
  
  @Value.Immutable 
  interface WrenchFlow {
    FlowProgram getFlow();
    ProgramEnvir getEnvir();
  }
}
