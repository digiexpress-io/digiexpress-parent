package io.resys.sysconfig.client.spi.asset;

import java.util.Optional;

import io.dialob.client.api.DialobClient;
import io.resys.hdes.client.api.HdesClient;
import io.resys.thena.projects.client.api.model.TenantConfig;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilClient;

public interface AssetClientConfig {
  
  AssetClientConfig query();
  
  interface AssetClientConfigQuery {
    AssetClientConfigQuery tenant(Optional<TenantConfig> config);
    AssetClientConfigQuery tenant(TenantConfig config);
    AssetClientConfigQuery tenant(String tenantConfigId);
    Uni<AssetClients> get();
  }
  
  interface AssetClients {  
    HdesClient getHdes();
    StencilClient getStencil();
    DialobClient getDialob(); 
  }
}
