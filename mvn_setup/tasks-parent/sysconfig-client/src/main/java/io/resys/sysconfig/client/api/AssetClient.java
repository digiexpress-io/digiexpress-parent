package io.resys.sysconfig.client.api;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.dialob.client.api.DialobClient;
import io.resys.hdes.client.api.HdesClient;
import io.resys.sysconfig.client.api.model.SysConfigRelease.AssetType;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfig;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilClient;

public interface AssetClient {

  AssetQuery assetQuery();
  AssetClient withRepoId(String repoId);
  
  Uni<AssetClient> withTenantConfig(String tenantConfigId);
  AssetClient withTenantConfig(String tenantConfigId, List<TenantRepoConfig> tenantConfig);
  AssetClientConfig getConfig();
  
  interface AssetQuery {
    Uni<WrenchAssets> getWrenchAsset(String releaseId);
    Uni<StencilAssets> getStencilAsset(String releaseId);
    Uni<List<DialobAsset>> getDialobAssets(List<String> formId);

    Multi<Asset> findAll();
  }
  
  
  @Value.Immutable
  interface AssetClientConfig {
    String getTenantConfigId();
    List<TenantRepoConfig> getRepoConfigs();
    DialobClient getDialob();
    HdesClient getHdes();
    StencilClient getStencil();
  }
  
  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.PROPERTY,
      property = "assetType")
  @JsonSubTypes({
    @Type(value = ImmutableDialobAsset.class, name = "DIALOB"),
    @Type(value = ImmutableWrenchAssets.class, name = "WRENCH"),
    @Type(value = ImmutableStencilAssets.class, name = "STENCIL"),
  })
  interface Asset {
    String getId();
    String getVersion();
    String getName();
    Instant getCreated();
    AssetType getAssetType();
    String getAssetBody();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableDialobAsset.class) @JsonDeserialize(as = ImmutableDialobAsset.class)
  interface DialobAsset extends Asset {
    String getFormTagName();
    List<DialboAssetEntry> getFormTags();
    @JsonIgnore @Value.Default @Override default AssetType getAssetType() { return AssetType.DIALOB; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableWrenchAssets.class) @JsonDeserialize(as = ImmutableWrenchAssets.class)
  interface WrenchAssets extends Asset {
    List<WrenchAssetEntry> getFlows();
    @JsonIgnore @Value.Default @Override default AssetType getAssetType() { return AssetType.WRENCH; }  
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableStencilAssets.class) @JsonDeserialize(as = ImmutableStencilAssets.class)
  interface StencilAssets extends Asset {
    List<StencilAssetEntry> getWorkflows();
    @JsonIgnore @Value.Default @Override default AssetType getAssetType() { return AssetType.STENCIL; }
  }
  

  @Value.Immutable @JsonSerialize(as = ImmutableDialboAssetEntry.class) @JsonDeserialize(as = ImmutableDialboAssetEntry.class)
  interface DialboAssetEntry {
    String getFormId();
    String getFormTagName();
    Instant getCreated();
    List<AssetParam> getParams();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableWrenchAssetEntry.class) @JsonDeserialize(as = ImmutableWrenchAssetEntry.class)
  interface WrenchAssetEntry {
    String getFlowName();
    List<AssetParam> getParams(); 
  }

  @Value.Immutable @JsonSerialize(as = ImmutableStencilAssetEntry.class) @JsonDeserialize(as = ImmutableStencilAssetEntry.class)
  interface StencilAssetEntry {
    String getWorkflowName();
    Map<String, String> getLocales();
  }

  @Value.Immutable @JsonSerialize(as = ImmutableAssetParam.class) @JsonDeserialize(as = ImmutableAssetParam.class)
  interface AssetParam {
    String getName();
    String getType();
    Optional<String> getDefault();
    boolean isInput();
    boolean isRequired();
  }
}
