package io.resys.sysconfig.client.api.model;

import java.time.Instant;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable @JsonSerialize(as = ImmutableSysConfigLiveVersion.class) @JsonDeserialize(as = ImmutableSysConfigLiveVersion.class)
public interface SysConfigLiveVersion extends Document {
  
  Instant getUpdated();
  Instant getLastCheck();
  String getReleaseHash();
  String getReleaseCreated();
  String getReleaseId();
  String getReleaseName();
  
  @Value.Default default DocumentType getDocumentType() { return DocumentType.SYS_CONFIG_LIVE_VERSION; }
}
