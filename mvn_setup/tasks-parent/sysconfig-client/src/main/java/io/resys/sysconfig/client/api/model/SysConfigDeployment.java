package io.resys.sysconfig.client.api.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.sysconfig.client.api.model.SysConfigDeploymentCommand.CreateSysConfigDeployment;

@Value.Immutable @JsonSerialize(as = ImmutableSysConfigDeployment.class) @JsonDeserialize(as = ImmutableSysConfigDeployment.class)
public interface SysConfigDeployment extends Document {
  Instant getCreated();
  Instant getUpdated();
  Instant getLiveDate();
  Boolean getDisabled();
  String getHash();
  List<SysConfigDeploymentTransaction> getTransactions();
  
  @JsonIgnore @Value.Default default DocumentType getDocumentType() { return DocumentType.SYS_CONFIG_DEPLOYMENT; }
  @JsonIgnore @Value.Default default SysConfigRelease getBody() {
    final var init = (CreateSysConfigDeployment) getTransactions().get(0).getCommands().get(0);
    return init.getBody();
  }

  @Value.Immutable @JsonSerialize(as = ImmutableSysConfigDeploymentTransaction.class) @JsonDeserialize(as = ImmutableSysConfigDeploymentTransaction.class)
  interface SysConfigDeploymentTransaction extends Serializable {
    String getId();
    List<SysConfigDeploymentCommand> getCommands(); 
  }
}
