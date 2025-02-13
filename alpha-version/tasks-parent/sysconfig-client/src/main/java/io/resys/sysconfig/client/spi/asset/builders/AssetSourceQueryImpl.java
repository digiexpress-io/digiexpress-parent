package io.resys.sysconfig.client.spi.asset.builders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.dialob.client.api.DialobClient;
import io.dialob.client.api.DialobComposer;
import io.dialob.client.api.DialobComposer.ComposerState;
import io.dialob.client.spi.DialobComposerImpl;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesStore;
import io.resys.sysconfig.client.api.AssetClient.AssetBranchType;
import io.resys.sysconfig.client.api.AssetClient.AssetClientConfig;
import io.resys.sysconfig.client.api.AssetClient.AssetSource;
import io.resys.sysconfig.client.api.AssetClient.AssetSourceQuery;
import io.resys.sysconfig.client.api.ImmutableAssetSource;
import io.resys.sysconfig.client.api.model.SysConfigRelease.AssetType;
import io.resys.thena.structures.doc.actions.DocObjectsQueryImpl;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilClient;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.thestencil.client.api.StencilStore;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AssetSourceQueryImpl implements AssetSourceQuery {

  private final Uni<AssetClientConfig> clients;

  @Override
  public Uni<List<AssetSource>> findAll() {
    final var wrench = getWrenchAsset();
    final var stencil = getStencilAsset();
    final var dialob = getDialobAssets();
    return Uni.combine().all().unis(wrench, stencil, dialob).asTuple().onItem().transform(tuple -> {
      final var result = new ArrayList<AssetSource>();
      result.addAll(tuple.getItem1());
      result.addAll(tuple.getItem2());
      result.addAll(tuple.getItem3());
      return Collections.unmodifiableList(result);
    });  
  }
  

  public Uni<List<AssetSource>> getDialobAssets() {
    return this.clients.onItem().transformToUni(configs -> {
      final DialobClient client = configs.getDialob();
      final DialobComposer composer = new DialobComposerImpl(client);
      final var state = composer.get();
      
      return state.onItem().transform(loaded -> getForms(loaded));
    });
  }
  
  public List<AssetSource> getForms(ComposerState state) {
    final List<AssetSource> result = new ArrayList<>();
    
    for(final var revision : state.getRevs().values()) {
      for(final var version : revision.getEntries()) {
        
        result.add(ImmutableAssetSource.builder()
            .id(version.getId())
            .assetName(revision.getName())
            .labelName(state.getForms().get(version.getFormId()).getData().getMetadata().getLabel())
            .assetType(AssetType.DIALOB)
            .branchName(version.getRevisionName())
            .branchType(AssetBranchType.RELEASE)
            .build());
        
      }
      result.add(ImmutableAssetSource.builder()
          .id(revision.getHead())
          .assetName(state.getForms().get(revision.getHead()).getName())
          .labelName(state.getForms().get(revision.getHead()).getData().getMetadata().getLabel())
          .assetType(AssetType.DIALOB)
          .branchName(DocObjectsQueryImpl.BRANCH_MAIN)
          .branchType(AssetBranchType.BRANCH)
          .build());
    }
    
    
    return Collections.unmodifiableList(result);
  }
  

  public Uni<List<AssetSource>> getStencilAsset() {
    return this.clients.onItem().transformToUni(configs -> {
      final StencilClient client = configs.getStencil();
      final StencilStore store = client.getStore();
      final Uni<SiteState> state = store.query().head();
      
      final var tags = state.onItem().transform(loaded -> loaded.getReleases().values().stream()
        .map(e -> {
          
          final AssetSource source = ImmutableAssetSource.builder()
              .id(e.getId())
              .assetName(e.getBody().getName())
              .assetType(AssetType.STENCIL)
              .branchName(e.getBody().getName())
              .branchType(AssetBranchType.RELEASE)
              .labelName("")
              .build();
          return source;
        })
        .toList());
      
      final var branches = store.queryBranches().findAll().onItem().transform(branchs -> branchs.stream()
        .map(e -> {
          final AssetSource source = ImmutableAssetSource.builder()
              .id(e.getName() + "/" + e.getCommitId())
              .assetName(e.getName())
              .assetType(AssetType.STENCIL)
              .branchName(e.getName())
              .labelName("")
              .branchType(AssetBranchType.BRANCH)
              .build();
          return source;
        }).toList());
        
      return Uni.combine().all().unis(tags, branches).asTuple().onItem().transform(tuple -> {
        final var result = new ArrayList<AssetSource>();
        result.addAll(tuple.getItem1());
        result.addAll(tuple.getItem2());
        return Collections.unmodifiableList(result);
      });  
    });
  }
  
  public Uni<List<AssetSource>> getWrenchAsset() {
    return this.clients.onItem().transformToUni(configs -> {
      final HdesClient client = configs.getHdes();
      final HdesStore store = client.store();
      final Uni<HdesStore.StoreState> state = store.query().get();
      
      final var tags = state.onItem().transform(loaded -> loaded.getTags().values().stream()
        .map(e -> {
          final var tag = client.ast().commands(e.getBody()).tag();
          final AssetSource source = ImmutableAssetSource.builder()
              .id(e.getId())
              .assetName(tag.getName())
              .assetType(AssetType.WRENCH)
              .branchName(tag.getName())
              .labelName("")
              .branchType(AssetBranchType.RELEASE)
              .build();
          return source;
        })
        .toList());
      
      final var branches = store.queryBranches().findAll().onItem().transform(branchs -> branchs.stream()
        .map(e -> {
          final AssetSource source = ImmutableAssetSource.builder()
              .id(e.getName() + "/" + e.getCommitId())
              .assetName(e.getName())
              .assetType(AssetType.WRENCH)
              .branchName(e.getName())
              .labelName("")
              .branchType(AssetBranchType.BRANCH)
              .build();
          return source;
        }).toList());
        
      return Uni.combine().all().unis(tags, branches).asTuple().onItem().transform(tuple -> {
        final var result = new ArrayList<AssetSource>();
        result.addAll(tuple.getItem1());
        result.addAll(tuple.getItem2());
        return Collections.unmodifiableList(result);
      });  
    });
  }
}
