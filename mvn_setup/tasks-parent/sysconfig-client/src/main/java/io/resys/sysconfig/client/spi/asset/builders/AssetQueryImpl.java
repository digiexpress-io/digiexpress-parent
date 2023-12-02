package io.resys.sysconfig.client.spi.asset.builders;

import io.dialob.client.api.DialobStore;
import io.resys.hdes.client.api.HdesStore;
import io.resys.sysconfig.client.api.AssetClient.Asset;
import io.resys.sysconfig.client.api.AssetClient.AssetQuery;
import io.resys.sysconfig.client.spi.asset.AssetClientConfig.AssetClients;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilComposer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AssetQueryImpl implements AssetQuery {

  private final Uni<AssetClients> clients;

  @Override
  public Multi<Asset> findAll() {
    // TODO Auto-generated method stub
    return null;
  }

  public void doWithClients(AssetClients clients) {
    
  }
  
  public void doWithStencil(AssetClients clients, StencilComposer.SiteState store) {
    
  }  
  
  public void doWithHdes(AssetClients clients, HdesStore.StoreState store) {
    
  }  
  public void doWithDialob(AssetClients clients, DialobStore.StoreState store) {
    
  }

}
