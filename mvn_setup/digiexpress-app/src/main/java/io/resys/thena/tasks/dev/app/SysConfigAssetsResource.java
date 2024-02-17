package io.resys.thena.tasks.dev.app;

import java.util.List;

import io.resys.hdes.client.spi.HdesComposerImpl;
import io.resys.sysconfig.client.api.AssetClient.AssetSource;
import io.resys.sysconfig.client.api.SysConfigClient;
import io.resys.sysconfig.client.api.model.SysConfig;
import io.resys.sysconfig.client.rest.SysConfigAssetsRestApi;
import io.resys.thena.projects.client.api.TenantConfigClient;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilClient;
import io.thestencil.client.api.StencilComposer.SiteState;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;


@Path("q/digiexpress/api")
public class SysConfigAssetsResource implements SysConfigAssetsRestApi {

  @Inject SysConfigClient sysConfigClient;
  @Inject CurrentTenant currentTenant;
  @Inject CurrentUser currentUser;
  @Inject TenantConfigClient tenantClient;
  
  @Override
  public Uni<List<AssetSource>> findAllAssetSources() {
    return getClient().onItem().transformToUni(client -> client.getAssets().assetSourceQuery().findAll());
  }
  
  private Uni<SysConfigClient> getClient() {
    return sysConfigClient.withTenantConfig(currentTenant.getTenantId());
  }

  @Override
  public Uni<io.resys.hdes.client.api.HdesComposer.ComposerState> findWrenchAssets(String sysConfigId) {
    return getClient().onItem()
        .transformToUni(client -> client.configQuery().get(sysConfigId).onItem()
            .transformToUni(config -> createWrenchComposerState(config, client)));
  }
  
  public Uni<io.resys.hdes.client.api.HdesComposer.ComposerState> createWrenchComposerState(SysConfig config, SysConfigClient client) {
    return client.getAssets().assetQuery().getWrenchState(config.getWrenchHead()).onItem()
    .transform(store -> new HdesComposerImpl(client.getAssets().getConfig().getHdes()).state(store));

  }

  @Override
  public Uni<StencilClient.Release> findStencilAssets(String sysConfigId) {
    return getClient().onItem()
    .transformToUni(client -> client.configQuery().get(sysConfigId).onItem()
        .transformToUni(config -> client.getAssets().assetQuery().getStencilState(config.getStencilHead())));
  }
}
