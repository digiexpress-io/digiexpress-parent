package io.resys.sysconfig.client.api.model;
import java.time.Instant;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.vertx.core.json.JsonObject;

@Value.Immutable @JsonSerialize(as = ImmutableSysConfigRelease.class) @JsonDeserialize(as = ImmutableSysConfigRelease.class)
public interface SysConfigRelease extends Document {
  String getId();
  String getName();
  Instant getCreated();
  Instant getScheduledAt();
  String getAuthor();
  
  List<SysConfigReleaseAsset> getAssets();
  List<SysConfigReleaseService> getServices();
  
  @Value.Default default DocumentType getDocumentType() { return DocumentType.SYS_CONFIG_RELEASE; }

  @Value.Immutable @JsonSerialize(as = ImmutableSysConfigRelease.class) @JsonDeserialize(as = ImmutableSysConfigRelease.class)  
  interface SysConfigReleaseService {
    String getId(); 
    String getServiceName();
    String getFormId();
    String getFlowName();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableSysConfigReleaseAsset.class) @JsonDeserialize(as = ImmutableSysConfigReleaseAsset.class)
  interface SysConfigReleaseAsset {
    String getId();
    String getName();
    Instant getUpdated();
    
    JsonObject getBody();
    SysConfigReleaseAssetType getType();
  }
  
  enum SysConfigReleaseAssetType { DIALOB, WRENCH, STENCIL }
  
}
