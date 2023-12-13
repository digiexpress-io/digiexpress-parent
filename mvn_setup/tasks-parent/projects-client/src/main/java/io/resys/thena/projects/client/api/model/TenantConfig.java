package io.resys.thena.projects.client.api.model;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable @JsonSerialize(as = ImmutableTenantConfig.class) @JsonDeserialize(as = ImmutableTenantConfig.class)
public interface TenantConfig extends Document {
  String getId();
  String getName();
  Instant getCreated();
  Instant getUpdated();
  @Nullable Instant getArchived();
  TenantStatus getStatus();
  
  TenantPreferences getPreferences();
  List<TenantRepoConfig> getRepoConfigs();
  
  List<TenantConfigTransaction> getTransactions(); 
  @Value.Default default DocumentType getDocumentType() { return DocumentType.TENANT_CONFIG; }



  enum TenantStatus { IN_FORCE, ARCHIVED }
  enum TenantRepoConfigType { WRENCH, STENCIL, TASKS, DIALOB, CRM, TENANT, SYS_CONFIG, USER_PROFILE }
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableTenantRepoConfig.class) @JsonDeserialize(as = ImmutableTenantRepoConfig.class)
  interface TenantRepoConfig {
    String getRepoId();
    TenantRepoConfigType getRepoType();
    
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableTenantPreferences.class) @JsonDeserialize(as = ImmutableTenantPreferences.class)
  interface TenantPreferences {
    String getLandingApp();
  }
  
  public static final String APP_BACKOFFICE = "app-frontoffice";

  @Value.Immutable @JsonSerialize(as = ImmutableTenantConfigTransaction.class) @JsonDeserialize(as = ImmutableTenantConfigTransaction.class)
  interface TenantConfigTransaction extends Serializable {
    String getId();
    List<TenantConfigCommand> getCommands(); 
  }
}
