package io.digiexpress.client.api;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.dialob.client.api.DialobClient;
import io.dialob.client.api.DialobStore.StoreState;
import io.digiexpress.client.api.ServiceDocument.ServiceReleaseDocument;
import io.digiexpress.client.api.ServiceStore.StoreEntity;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesClient.FlowExecutor;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.MigrationBuilder.LocalizedSite;
import io.thestencil.client.api.StencilClient;

public interface ServiceClient {
  ServiceEnvirBuilder envir();
  ServiceRepoBuilder repo();
  ServiceExecutorBuilder executor(ServiceEnvir envir);
  
  ServiceClientConfig getConfig();
  

  interface ServiceClientException {}

  interface ServiceExecutorBuilder {
    ProcessExecutor process(String nameOrId, @Nullable String rev);
    FlowExecutor flow(ProcessState state);
    FillExecutor fill(ProcessState state);
    ArticleExecutor article();
  }

  // returns new process instance and new fill session
  interface ProcessExecutor {
    ArticleExecutor actions(Map<String, Serializable> initVariables);
    ArticleExecutor action(String variableName, Serializable variableValue);
    ExecutionBody<ProcessState> build();
  }
  
  // returns stencil content
  interface ArticleExecutor {
    ArticleExecutor actions(String locale);
    ExecutionBody<LocalizedSite> build();
  }
  
  // continues fill
  interface FillExecutor {
    FillExecutor actions(io.dialob.api.proto.Actions userActions);
    ExecutionBody<io.dialob.api.proto.Actions> build();
  }
  
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
    ServiceEnvirBuilder from(ServiceEnvir previous);
    ServiceEnvirBuilder from(StoreState state);
    ServiceEnvirBuilder from(ServiceReleaseDocument release);
    ServiceEnvirValueBuilder addCommand();
    ServiceEnvir build();
  }

  
  interface ServiceEnvirValueBuilder {
    ServiceEnvirValueBuilder id(String externalId);
    ServiceEnvirValueBuilder version(String version);
    ServiceEnvirValueBuilder cachless();
    
    ServiceEnvirValueBuilder rev(StoreEntity v);
    ServiceEnvirValueBuilder process(StoreEntity v);
    ServiceEnvirValueBuilder release(StoreEntity v);
    ServiceEnvirValueBuilder config(StoreEntity v);
    
    ServiceEnvirBuilder build();
  }
  
  
  @Value.Immutable
  interface ServiceClientConfig {
    ServiceStore getStore();
    ServiceCache getCache();
    ServiceMapper getMapper();
    
    DialobClient getDialob();
    HdesClient getHdes();
    StencilClient getStencil();
  }
}
