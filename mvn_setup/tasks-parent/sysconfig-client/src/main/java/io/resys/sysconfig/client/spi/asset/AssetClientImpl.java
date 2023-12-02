package io.resys.sysconfig.client.spi.asset;

import java.util.List;

import io.resys.sysconfig.client.api.AssetClient;
import io.resys.sysconfig.client.api.ImmutableAssetClientConfig;
import io.resys.sysconfig.client.spi.asset.exceptions.AssetClientException;
import io.resys.sysconfig.client.spi.support.ErrorMsg;
import io.resys.thena.projects.client.api.TenantConfigClient;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.resys.thena.projects.client.spi.store.MainBranch;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AssetClientImpl implements AssetClient {

  private final TenantConfigClient tenantClient;
  private final AssetClientConfig clientConfig;

  @Override
  public AssetQuery assetQuery() {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public AssetClientConfig getConfig() {
    if(clientConfig.getRepoConfigs().isEmpty()) {
      throw new AssetClientException(ErrorMsg.builder()
          .withCode("TENANT_CONFIG_NOT_LOADED")
          .withProps(JsonObject.of("tenantConfigId", clientConfig.getTenantConfigId()))
          .withMessage("Can't get asset configuration because they are not configured or loaded!")
          .toString());
    }
    return clientConfig;
  }
  @Override
  public Uni<AssetClient> withTenantConfig(String tenantConfigId) {
    return tenantClient.queryActiveTenantConfig().get(tenantConfigId)
        .onItem().transform(tenant -> withTenantConfig(tenantConfigId, tenant.getRepoConfigs()));
  }
  @Override
  public AssetClient withTenantConfig(String tenantConfigId, List<TenantRepoConfig> tenantConfig) {
    final var dialob = tenantConfig.stream().filter(entry -> entry.getRepoType() == TenantRepoConfigType.DIALOB).findFirst();
    final var wrench = tenantConfig.stream().filter(entry -> entry.getRepoType() == TenantRepoConfigType.WRENCH).findFirst();
    final var stencil = tenantConfig.stream().filter(entry -> entry.getRepoType() == TenantRepoConfigType.STENCIL).findFirst();
    
    if(dialob.isEmpty() || wrench.isEmpty() || stencil.isEmpty()) {
      throw new AssetClientException(ErrorMsg.builder()
          .withCode("TENANT_CONFIG_INCOMPLETE")
          .withProps(JsonObject.of(
              "dialobOk", dialob.isPresent(),
              "wrenchOk", wrench.isPresent(),
              "stencilOk", stencil.isPresent()
              ))
          .withMessage("Can't get asset configuration because it's incomplete!")
          .toString());
    }
    
    return new AssetClientImpl(tenantClient, ImmutableAssetClientConfig.builder()
        .from(clientConfig)
        .addAllRepoConfigs(tenantConfig)
        .hdes(clientConfig.getHdes().withRepo(wrench.get().getRepoId(), MainBranch.HEAD_NAME))
        .stencil(clientConfig.getStencil().withRepo(stencil.get().getRepoId(), MainBranch.HEAD_NAME))
        .dialob(clientConfig.getDialob().withRepo(dialob.get().getRepoId(), MainBranch.HEAD_NAME))
        .build());
  }
  @Override
  public AssetClient withRepoId(String repoId) {
    return new AssetClientImpl(tenantClient.withRepoId(repoId), clientConfig);
  }
}
