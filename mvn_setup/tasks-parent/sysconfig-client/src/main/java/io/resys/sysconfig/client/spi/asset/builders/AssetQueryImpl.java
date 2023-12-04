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
  public Uni<WrenchAssets> getWrenchAsset(String releaseId) {
    return this.clients.onItem().transformToUni(configs -> {
      final HdesClient client = configs.getHdes();
      final HdesStore store = client.store();
      final Uni<HdesStore.StoreState> state = store.query().get();
      
      if(isReleaseId(releaseId)) {
        return state.onItem().transform(loaded -> new FindHdesRelease(client, store, loaded).visit(releaseId));
      }
      return state.onItem().transform(loaded -> new CreateHdesTransientRelease(client, store, loaded).visit(releaseId));
    });
  }

  @Override
  public Uni<StencilAssets> getStencilAsset(String releaseId) {
    return this.clients.onItem().transformToUni(configs -> {
      final StencilClient client = configs.getStencil();
      final StencilStore store = client.getStore();
      final Uni<SiteState> state = store.query().head();
      
      if(isReleaseId(releaseId)) {
        return state.onItem().transform(loaded -> new FindStencilRelease(client, store, loaded).visit(releaseId));
      }
      return state.onItem().transform(loaded -> new CreateStencilTransientRelease(client, store, loaded).visit(releaseId));
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
  
  private boolean isReleaseId(String id) {
    final var criteria = id == null ? "": id.toLowerCase().trim();
    if(criteria.isEmpty()) {
      return false;
    }
    if(id.equals("main")) {
      return false;
    } else if(id.equals("latest")) {
      return false;
    } else if(id.equals("dev")) {
      return false;
    }
    return true;
  }
}
