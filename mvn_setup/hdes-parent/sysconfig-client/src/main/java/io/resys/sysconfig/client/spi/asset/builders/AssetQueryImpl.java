package io.resys.sysconfig.client.spi.asset.builders;

import java.util.List;

import io.dialob.client.api.DialobClient;
import io.dialob.client.api.DialobStore;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesStore;
import io.resys.sysconfig.client.api.AssetClient.Asset;
import io.resys.sysconfig.client.api.AssetClient.AssetClientConfig;
import io.resys.sysconfig.client.api.AssetClient.AssetQuery;
import io.resys.sysconfig.client.api.AssetClient.DialobAsset;
import io.resys.sysconfig.client.api.AssetClient.StencilAssets;
import io.resys.sysconfig.client.api.AssetClient.WrenchAssets;
import io.resys.sysconfig.client.spi.asset.visitors.CreateHdesTransientRelease;
import io.resys.sysconfig.client.spi.asset.visitors.CreateStencilTransientRelease;
import io.resys.sysconfig.client.spi.asset.visitors.FindDialobFormsVisitor;
import io.resys.sysconfig.client.spi.asset.visitors.FindHdesRelease;
import io.resys.sysconfig.client.spi.asset.visitors.FindStencilRelease;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilClient;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.thestencil.client.api.StencilStore;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AssetQueryImpl implements AssetQuery {

  private final Uni<AssetClientConfig> clients;

  @Override
  public Multi<Asset> findAll() {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public Uni<WrenchAssets> getWrenchAsset() {
    return this.clients.onItem().transformToUni(configs -> {
      final HdesClient client = configs.getHdes();
      final HdesStore store = client.store();
      final Uni<HdesStore.StoreState> state = store.query().get();
      
      return state.onItem().transform(loaded -> new CreateHdesTransientRelease(client, store, loaded).toWrenchAssets());
    });
  }
  
  @Override
  public Uni<io.resys.hdes.client.api.HdesStore.StoreState> getWrenchState() {
    return this.clients.onItem().transformToUni(configs -> {
      final HdesClient client = configs.getHdes();
      final HdesStore store = client.store();
      return store.query().get();
    });
  }

  @Override
  public Uni<StencilAssets> getStencilAsset() {
    return this.clients.onItem().transformToUni(configs -> {
      final StencilClient client = configs.getStencil();
      final StencilStore store = client.getStore();
      final Uni<SiteState> state = store.query().head();
      
      return state.onItem().transform(loaded -> new CreateStencilTransientRelease(client, store, loaded).toStencilAssets());
    });
  }

  @Override
  public Uni<List<DialobAsset>> getDialobAssets(List<String> formId) {
    return this.clients.onItem().transformToUni(configs -> {
      final DialobClient client = configs.getDialob();
      final DialobStore store = client.getConfig().getStore();
      final Uni<DialobStore.StoreState> state = store.query().get();
      return state.onItem().transform(loaded -> new FindDialobFormsVisitor(client, store, loaded).visit(formId));
    });
  }


  @Override
  public Uni<StencilClient.Release> getStencilState() {
    return this.clients.onItem().transformToUni(configs -> {
      final StencilClient client = configs.getStencil();
      final StencilStore store = client.getStore();
      final Uni<SiteState> state = store.query().head();
      
      return state.onItem().transform(loaded -> new CreateStencilTransientRelease(client, store, loaded).toStencilState());
    });
  }
}
