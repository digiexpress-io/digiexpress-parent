package io.resys.thena.tasks.dev.app;

import java.util.List;

import io.resys.sysconfig.client.api.AssetClient.AssetSource;
import io.resys.sysconfig.client.api.SysConfigClient;
import io.resys.sysconfig.client.rest.SysConfigAssetsRestApi;
import io.resys.thena.projects.client.api.TenantConfigClient;
import io.smallrye.mutiny.Uni;
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
}
