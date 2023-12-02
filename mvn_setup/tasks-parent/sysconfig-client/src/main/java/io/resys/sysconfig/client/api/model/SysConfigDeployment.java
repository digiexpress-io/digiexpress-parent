package io.resys.sysconfig.client.api.model;

import java.time.Instant;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable @JsonSerialize(as = ImmutableSysConfigDeployment.class) @JsonDeserialize(as = ImmutableSysConfigDeployment.class)
public interface SysConfigDeployment extends Document {
  Instant getLiveDate();
  Boolean getDisabled();
  String getHash();
  SysConfigRelease getBody();
  
  @Value.Default default DocumentType getDocumentType() { return DocumentType.SYS_CONFIG_DEPLOYMENT; }
}
