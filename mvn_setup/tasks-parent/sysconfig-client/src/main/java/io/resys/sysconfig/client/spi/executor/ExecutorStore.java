package io.resys.sysconfig.client.spi.executor;

import java.time.Instant;
import java.util.Optional;

import io.dialob.client.api.DialobClient;
import io.resys.sysconfig.client.api.ExecutorClient.SysConfigSession;
import io.resys.sysconfig.client.api.model.SysConfigInstance;
import io.resys.sysconfig.client.api.model.SysConfigRelease;
import io.smallrye.mutiny.Uni;

public interface ExecutorStore {
  SysConfigReleaseQuery queryReleases();
  SysConfigInstanceQuery queryInstances();
  SysConfigSessionQuery querySessions();
  DialobFormQuery queryForms();
  
  Uni<SysConfigSession> save(SysConfigSession session);

  interface SysConfigSessionQuery {
    Uni<SysConfigSession> get(String instanceId);
  }
  
  interface SysConfigInstanceQuery {
    Uni<SysConfigInstance> get(String instanceId);
  }
  
  interface SysConfigReleaseQuery {
    Uni<Optional<SysConfigRelease>> get(Instant targetDate);
  }

  interface DialobFormQuery {
    Uni<Optional<DialobClient.ProgramWrapper>> get(String formId); 
  }
}
