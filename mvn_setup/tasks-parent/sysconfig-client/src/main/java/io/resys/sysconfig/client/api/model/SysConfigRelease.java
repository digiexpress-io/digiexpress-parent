package io.resys.sysconfig.client.api.model;
import java.time.Instant;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.sysconfig.client.api.model.SysConfig.SysConfigService;

@Value.Immutable @JsonSerialize(as = ImmutableSysConfigRelease.class) @JsonDeserialize(as = ImmutableSysConfigRelease.class)
public interface SysConfigRelease extends Document {
  String getId();
  String getName();
  Instant getCreated();
  Instant getScheduledAt();
  String getAuthor();
  
  List<SysConfigAsset> getAssets();
  List<SysConfigService> getServices();
  
  @Value.Default default DocumentType getDocumentType() { return DocumentType.SYS_CONFIG_RELEASE; }


  @Value.Immutable @JsonSerialize(as = ImmutableSysConfigAsset.class) @JsonDeserialize(as = ImmutableSysConfigAsset.class)
  interface SysConfigAsset {
    String getId();
    String getName();
    Instant getUpdated();
    
    String getBody();
    AssetType getBodyType();
  }
  
  enum AssetType { DIALOB, WRENCH, STENCIL }
  
}
