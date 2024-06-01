package io.resys.sysconfig.client.spi.asset;

import io.resys.sysconfig.client.api.AssetClient;
import io.resys.sysconfig.client.api.ImmutableAssetClientConfig;
import io.resys.sysconfig.client.spi.asset.builders.AssetQueryImpl;
import io.resys.sysconfig.client.spi.asset.builders.AssetSourceQueryImpl;
import io.resys.thena.structures.doc.actions.DocObjectsQueryImpl;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AssetClientImpl implements AssetClient {

  private final AssetClientConfig clientConfig;

  @Override
  public AssetSourceQuery assetSourceQuery() {
    return new AssetSourceQueryImpl(Uni.createFrom().item(clientConfig));
  }
  
  @Override
  public AssetQuery assetQuery() {
    return new AssetQueryImpl(Uni.createFrom().item(clientConfig));
  }
  @Override
  public AssetClientConfig getConfig() {
    return clientConfig;
  }
  @Override
  public AssetClient withTenantId(String repoId) {
    return new AssetClientImpl(ImmutableAssetClientConfig.builder()
        .from(clientConfig)
        .tenantId(repoId)
        .hdes(clientConfig.getHdes().withRepo(repoId, DocObjectsQueryImpl.BRANCH_MAIN))
        .stencil(clientConfig.getStencil().withRepo(repoId, DocObjectsQueryImpl.BRANCH_MAIN))
        .dialob(clientConfig.getDialob().withTenant(repoId, DocObjectsQueryImpl.BRANCH_MAIN))
        .build());
  }
}
