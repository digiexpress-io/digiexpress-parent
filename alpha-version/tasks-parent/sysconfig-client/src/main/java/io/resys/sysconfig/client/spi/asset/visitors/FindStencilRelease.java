package io.resys.sysconfig.client.spi.asset.visitors;

import java.time.Instant;

import io.resys.sysconfig.client.api.AssetClient.StencilAssets;
import io.resys.sysconfig.client.api.ImmutableStencilAssets;
import io.resys.sysconfig.client.spi.asset.exceptions.AssetClientException;
import io.resys.thena.support.ErrorMsg;
import io.thestencil.client.api.StencilClient;
import io.thestencil.client.api.StencilClient.Release;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.thestencil.client.api.StencilStore;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FindStencilRelease {

  private final StencilClient client;
  private final StencilStore store;
  private final SiteState state;
  
  public Release toStencilState(String releaseId) {
    if(state.getReleases().containsKey(releaseId)) {
      throw new AssetClientException(notFoundMsg(releaseId));
    }
    
    final var transientEntity = state.getReleases().get(releaseId);
     
    return transientEntity.getBody();
  }

  public StencilAssets toStencilAssets(String releaseId) {
    if(state.getReleases().containsKey(releaseId)) {
      throw new AssetClientException(notFoundMsg(releaseId));
    }
    
    final var transientEntity = state.getReleases().get(releaseId);
    final var assetBody = store.getConfig().getSerializer().toString(transientEntity); 
    
    return ImmutableStencilAssets.builder()
        .id(transientEntity.getId())
        .created(Instant.now())
        .name(transientEntity.getBody().getName())
        .assetBody(assetBody.encode())
        .workflows(CreateStencilTransientRelease.createWorkflows(transientEntity))
        .build();
  }
  
  private String notFoundMsg(String releaseId) {
    return ErrorMsg.builder()
    .withCode("STENCIL_RELEASE_BY_ID_NOT_FOUND")
    .withProps(JsonObject.of("releaseId", releaseId, "repoId", store.getRepoName()))
    .withMessage("Can't get stencil release by id!")
    .toString(); 
  }
}
