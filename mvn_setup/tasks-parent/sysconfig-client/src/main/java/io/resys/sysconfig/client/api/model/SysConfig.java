package io.resys.sysconfig.client.api.model;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable @JsonSerialize(as = ImmutableSysConfig.class) @JsonDeserialize(as = ImmutableSysConfig.class)
public interface SysConfig extends Document {
  String getId();
  String getName();
  Instant getCreated();
  Instant getUpdated();
  
  String getWrenchHead();
  String getStencilHead();  
  List<SysConfigTransaction> getTransactions();
  
  @Value.Default default DocumentType getDocumentType() { return DocumentType.SYS_CONFIG_ACTIVE; }
  
  @Value.Immutable @JsonSerialize(as = ImmutableSysConfigTransaction.class) @JsonDeserialize(as = ImmutableSysConfigTransaction.class)
  interface SysConfigTransaction extends Serializable {
    String getId();
    List<SysConfigCommand> getCommands(); 
  }
}
