package io.resys.sysconfig.client.spi.asset.visitors;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.sysconfig.client.api.AssetClient;
import io.resys.sysconfig.client.api.AssetClient.StencilAssetEntry;
import io.resys.sysconfig.client.api.AssetClient.StencilAssets;
import io.resys.thena.support.OidUtils;
import io.resys.sysconfig.client.api.ImmutableStencilAssetEntry;
import io.resys.sysconfig.client.api.ImmutableStencilAssets;
import io.thestencil.client.api.ImmutableCreateRelease;
import io.thestencil.client.api.StencilClient;
import io.thestencil.client.api.StencilClient.Entity;
import io.thestencil.client.api.StencilClient.Release;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.thestencil.client.api.StencilStore;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateStencilTransientRelease {
  private final StencilClient client;
  private final StencilStore store;
  private final SiteState state;
  
  
  public Release toStencilState(String releaseId) {
    final var init = ImmutableCreateRelease.builder()
        .id(OidUtils.gen())
        .name("transient-release: " + releaseId + " at: " + Instant.now().toString())
        .build();
    
    final var transientEntity = io.thestencil.client.spi.builders.CreateBuilderImpl.release(init, state, client);
    return transientEntity.getBody();
  }  
  public StencilAssets toStencilAssets(String releaseId) {
    final var init = ImmutableCreateRelease.builder()
        .id(OidUtils.gen())
        .name("transient-release: " + releaseId + " at: " + Instant.now().toString())
        .build();
    
    final var transientEntity = io.thestencil.client.spi.builders.CreateBuilderImpl.release(init, state, client);
    final var assetBody = store.getConfig().getSerializer().toString(transientEntity); 
    
    return ImmutableStencilAssets.builder()
        .id(OidUtils.gen())
        .version("")
        .created(Instant.now())
        .name(init.getName())
        .assetBody(assetBody.encode())
        .workflows(createWorkflows(transientEntity))
        .build();
  }
  
  public static List<AssetClient.StencilAssetEntry> createWorkflows(Entity<Release> entity) {
    final var body = entity.getBody();
    
    return body.getWorkflows().stream()
        .map(wk -> {
          final var wkName = wk.getValue();
          final StencilAssetEntry entry = ImmutableStencilAssetEntry.builder()
              .workflowName(wkName)
              .locales(wk.getLabels().stream().collect(Collectors.toMap(e -> e.getLocale(), e -> e.getLabelValue())))
              .build();
          return entry;
        })
        .toList();
  }
}
