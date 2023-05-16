package io.digiexpress.client.api;

import org.immutables.value.Value;

import io.dialob.client.api.DialobClient;
import io.digiexpress.client.api.AssetExecutor.DialobExecutor;
import io.digiexpress.client.api.AssetExecutor.HdesExecutor;
import io.digiexpress.client.api.AssetExecutor.ProcessExecutor;
import io.digiexpress.client.api.AssetExecutor.StencilExecutor;
import io.digiexpress.client.api.ClientEntity.ServiceRelease;
import io.resys.hdes.client.api.HdesClient;
import io.resys.thena.docdb.api.DocDB;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilClient;

public interface Client {
  AssetEnvirBuilder envir();
  AssetExecutorBuilder executor(AssetEnvir envir);
  TenantBuilder tenant();
  ClientConfig getConfig();
  ClientQuery getQuery();
  
  interface ClientException {}

  interface AssetExecutorBuilder {
    ProcessExecutor process(String nameOrId);
    DialobExecutor dialob(AssetExecutorEntity.ProcessState state);
    HdesExecutor hdes(AssetExecutorEntity.ProcessState state);
    StencilExecutor stencil();
  }
  interface AssetEnvirBuilder {
    AssetEnvirBuilder add(ServiceRelease release);
    AssetEnvir build();
  }
  
  interface TenantBuilder {
    TenantBuilder repoStencil(String repoStencil);
    TenantBuilder repoHdes(String repoHdes);
    TenantBuilder repoDialob(String repoDialob);
    TenantBuilder repoProject(String repoService);

    Uni<Client> load();
    Uni<Client> create();
    Client build();
  }
  
  @Value.Immutable
  interface ClientConfig {
    ClientStore getStore();
    ClientCache getCache();
    Parser getParser();
    
    DialobClient getDialob();
    HdesClient getHdes();
    StencilClient getStencil();
    Archiver getArchiver();
    DocDB getDocDb();
  }
}
