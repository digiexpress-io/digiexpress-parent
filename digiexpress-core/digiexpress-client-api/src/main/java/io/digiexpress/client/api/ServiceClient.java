package io.digiexpress.client.api;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

import org.immutables.value.Value;

import io.dialob.client.api.DialobClient;
import io.digiexpress.client.api.ServiceDocument.ServiceReleaseDocument;
import io.resys.hdes.client.api.HdesClient;
import io.resys.thena.docdb.api.DocDB;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.MigrationBuilder.LocalizedSite;
import io.thestencil.client.api.StencilClient;

public interface ServiceClient {
  ServiceEnvirBuilder envir();
  ServiceRepoBuilder repo();
  ServiceExecutorBuilder executor(ServiceEnvir envir);
  ServiceClientConfig getConfig();
  QueryFactory getQuery();
  
  interface ServiceClientException {}

  interface ServiceExecutorBuilder {
    CreateProcessExecutor create(String nameOrId);
    FillProcessExecutor fill(ProcessState state);
    FlowProcessExecutor flow(ProcessState state);
    ArticleExecutor article();
  }

  // returns new process instance and new fill session
  interface CreateProcessExecutor {
    CreateProcessExecutor targetDate(LocalDateTime now);
    CreateProcessExecutor actions(Map<String, Serializable> initVariables);
    CreateProcessExecutor action(String variableName, Serializable variableValue);
    ExecutionBody<Map<String, Serializable>> build();
  }

  // continues fill
  interface FillProcessExecutor {
    FillProcessExecutor actions(io.dialob.api.proto.Actions userActions);
    ExecutionBody<io.dialob.api.proto.Actions> build();
  }
  
  interface FlowProcessExecutor {
    
  }

  // returns stencil content
  interface ArticleExecutor {
    ArticleExecutor actions(String locale);
    ExecutionBody<LocalizedSite> build();
  }
  
  @Value.Immutable
  interface ExecutionBody<T> {
    ProcessState getState();
    T getActions();
  }

  interface ServiceRepoBuilder {
    ServiceRepoBuilder repoStencil(String repoStencil);
    ServiceRepoBuilder repoHdes(String repoHdes);
    ServiceRepoBuilder repoDialob(String repoDialob);
    ServiceRepoBuilder repoService(String repoService);

    Uni<ServiceClient> load();
    Uni<ServiceClient> create();
    ServiceClient build();
  }
  
  
  interface ServiceEnvirBuilder {
    ServiceEnvirBuilder add(ServiceReleaseDocument release);
    ServiceEnvir build();
  }
  
  @Value.Immutable
  interface ServiceClientConfig {
    ServiceStore getStore();
    ServiceCache getCache();
    ServiceMapper getMapper();
    
    DialobClient getDialob();
    HdesClient getHdes();
    StencilClient getStencil();
    CompressionMapper getCompression();
    DocDB getDocDb();
  }
}
