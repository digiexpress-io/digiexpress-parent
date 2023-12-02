package io.resys.sysconfig.client.api.model;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import javax.annotation.Nullable;

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
  
  List<SysConfigService> getServices();
  List<SysConfigTransaction> getTransactions();
  
  @Value.Default default DocumentType getDocumentType() { return DocumentType.SYS_CONFIG; }
  
  @Value.Immutable @JsonSerialize(as = ImmutableSysConfigTransaction.class) @JsonDeserialize(as = ImmutableSysConfigTransaction.class)
  interface SysConfigTransaction extends Serializable {
    String getId();
    List<SysConfigCommand> getCommands(); 
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableSysConfigService.class) @JsonDeserialize(as = ImmutableSysConfigService.class)  
  interface SysConfigService {
    @Nullable String getId(); //on creating 
    String getServiceName();
    String getFormId();
    String getFlowName();
    List<String> getLocales();
  }
}
