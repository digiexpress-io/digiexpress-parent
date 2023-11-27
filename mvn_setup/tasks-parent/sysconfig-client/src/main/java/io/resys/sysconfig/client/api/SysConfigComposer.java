package io.resys.sysconfig.client.api;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.sysconfig.client.api.model.SysConfigRelease.SysConfigReleaseAssetType;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface SysConfigComposer {

  ComposerReleaseMetaQuery releaseMetaQuery();

  interface ComposerReleaseMetaQuery {
    ComposerReleaseMetaQuery matchIds(String...ids);
    ComposerReleaseMetaQuery assetTypes(SysConfigReleaseAssetType...types);

    Uni<ComposerReleaseMeta> get();
    Multi<ComposerReleaseMeta> findAll();
  }
  
  
  interface DialboReleaseMeta extends ComposerReleaseMeta {
    String getFormTechnicalName();
    String getFormTagName();
    List<DialboReleaseMetaEntry> getFormTags();
    @JsonIgnore @Value.Default @Override default SysConfigReleaseAssetType getAssetType() { return SysConfigReleaseAssetType.DIALOB; }
  }
  
  interface WrenchReleaseMeta extends ComposerReleaseMeta {
    List<WrenchReleaseMetaEntry> getFlows();
    @JsonIgnore @Value.Default @Override default SysConfigReleaseAssetType getAssetType() { return SysConfigReleaseAssetType.WRENCH; }  
  }
  
  interface StencilReleaseMeta extends ComposerReleaseMeta {
    List<StencilReleaseMetaEntry> getWorkflows();
    @JsonIgnore @Value.Default @Override default SysConfigReleaseAssetType getAssetType() { return SysConfigReleaseAssetType.STENCIL; }
  }
  
  interface ComposerReleaseMeta {
    String getId();
    String getName();
    Instant getCreated();
    SysConfigReleaseAssetType getAssetType();
  }
  
  interface DialboReleaseMetaEntry {
    String getFormId();
    String getFormTagName();
    Instant getCreated();
    List<ComposerReleaseMetaParam> getParams();
  }
  
  interface WrenchReleaseMetaEntry {
    String getFlowName();
    List<ComposerReleaseMetaParam> getParams(); 
  }
  
  interface StencilReleaseMetaEntry {
    String getName();
    Map<String, String> getLocales();
  }
  
  interface ComposerReleaseMetaParam {
    String getName();
    String getType();
    Optional<String> getDefault();
    boolean isInput();
    boolean isRequired();
  }
}
