package io.resys.sysconfig.client.spi.asset.visitors;

import java.time.Instant;

import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesStore;
import io.resys.sysconfig.client.api.AssetClient.WrenchAssets;
import io.resys.sysconfig.client.api.ImmutableWrenchAssets;
import io.resys.sysconfig.client.spi.asset.exceptions.AssetClientException;
import io.resys.sysconfig.client.spi.support.ErrorMsg;
import io.resys.thena.docdb.support.OidUtils;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FindHdesRelease {

  private final HdesClient client;
  private final HdesStore store;
  private final HdesStore.StoreState state;
  
  public WrenchAssets visit(String releaseId) {
    if(state.getTags().containsKey(releaseId)) {
      throw new AssetClientException(ErrorMsg.builder()
          .withCode("WRENCH_RELEASE_BY_ID_NOT_FOUND")
          .withProps(JsonObject.of("releaseId", releaseId, "repoId", store.getRepoName()))
          .withMessage("Can't get wrench release by id!")
          .toString());
    }
    
    final var entity = state.getTags().get(releaseId);
    final var composerState = CreateHdesTransientRelease.state(this.client, this.state);
    final var tag = client.ast().commands(entity.getBody()).tag();
    final var assetBody = client.mapper().toJson(tag); 
    
    final var entries = composerState.getFlows().values().stream()
        .map(flow -> CreateHdesTransientRelease.createEntries(composerState, flow))
        .toList();
    
    return ImmutableWrenchAssets.builder()
        .id(entity.getId())
        .created(Instant.now())
        .name(tag.getName())
        .addAllFlows(entries)
        .assetBody(assetBody)
        .flows(entries)
        .build();
  }
}
