package io.resys.thena.tasks.dev.app;

import java.util.List;

import io.resys.hdes.client.spi.HdesComposerImpl;
import io.resys.sysconfig.client.api.AssetClient.AssetSource;
import io.resys.sysconfig.client.api.SysConfigClient;
import io.resys.sysconfig.client.api.model.SysConfig;
import io.resys.sysconfig.client.rest.SysConfigAssetsRestApi;
import io.resys.thena.projects.client.api.ProjectClient;
import io.resys.thena.tasks.dev.app.user.CurrentTenant;
import io.resys.thena.tasks.dev.app.user.CurrentUser;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;

@Singleton
@Path("q/digiexpress/api")
public class SysConfigAssetsResource implements SysConfigAssetsRestApi {

  @Inject private SysConfigClient sysConfigClient;
  @Inject private CurrentTenant currentTenant;
  @Inject private CurrentUser currentUser;
  @Inject private ProjectClient tenantClient;
  
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
