package io.resys.sysconfig.client.api;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.resys.sysconfig.client.api.model.SysConfig;
import io.resys.sysconfig.client.api.model.SysConfigCommand.CreateSysConfig;
import io.resys.sysconfig.client.api.model.SysConfigCommand.CreateSysConfigRelease;
import io.resys.sysconfig.client.api.model.SysConfigCommand.SysConfigUpdateCommand;
import io.resys.sysconfig.client.api.model.SysConfigDeployment;
import io.resys.sysconfig.client.api.model.SysConfigDeploymentCommand.CreateSysConfigDeployment;
import io.resys.sysconfig.client.api.model.SysConfigRelease;
import io.resys.thena.docdb.api.models.Repo;
import io.smallrye.mutiny.Uni;

public interface SysConfigClient {

  RepositoryQuery repoQuery();
  Uni<Repo> getRepo();
  SysConfigClient withRepoId(String repoId);
  
  CreateSysConfigAction createConfig();
  UpdateSysConfigAction updateConfig();
  
  CreateSysConfigDeploymentAction createDeployment();
  
  SysConfigReleaseQuery releaseQuery();
  SysConfigQuery configQuery();
  
  interface CreateSysConfigDeploymentAction {
    
  }

  interface CreateSysConfigAction {
    Uni<SysConfigDeployment> createOne(CreateSysConfigDeployment command);
    Uni<SysConfig> createOne(CreateSysConfig command);
    Uni<SysConfigRelease> createOne(CreateSysConfigRelease command);
  }

  interface UpdateSysConfigAction {
    Uni<SysConfig> updateOne(SysConfigUpdateCommand command);
    Uni<SysConfig> updateOne(List<SysConfigUpdateCommand> commands);
  }
  
  interface SysConfigQuery {
    Uni<List<SysConfig>> findAll();
    Uni<List<SysConfig>> findByIds(Collection<String> ids);
    Uni<SysConfig> get(String id);
    Uni<List<SysConfig>> deleteAll(String userId, Instant targetDate);
    //Uni<List<SysConfig>> deleteOne(String id, String userId, Instant targetDate);    
  }

  interface SysConfigReleaseQuery {
    Uni<List<SysConfigRelease>> findAll();
    Uni<List<SysConfigRelease>> findByIds(Collection<String> ids);
    Uni<SysConfigRelease> get(String id);
    Uni<List<SysConfigRelease>> deleteAll(String userId, Instant targetDate);
    Uni<List<SysConfigRelease>> deleteOne(String id, String userId, Instant targetDate);
  }
  
  
  interface RepositoryQuery {
    RepositoryQuery repoName(String repoName);
    SysConfigClient build();

    Uni<SysConfigClient> deleteAll();
    Uni<SysConfigClient> delete();
    Uni<SysConfigClient> create();
    Uni<SysConfigClient> createIfNot();
    
    Uni<Optional<SysConfigClient>> get(String repoId);
  } 
  
}
