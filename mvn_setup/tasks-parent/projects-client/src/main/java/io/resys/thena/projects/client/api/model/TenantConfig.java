package io.resys.thena.projects.client.api.model;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.thena.support.ErrorMsg;
import io.vertx.core.json.JsonObject;



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

  
  @JsonIgnore
  default TenantRepoConfig getRepoConfig(TenantRepoConfigType type) {
    final var config = getRepoConfigs().stream().filter(entry -> entry.getRepoType() == type).findFirst();
    if(config.isEmpty()) {
      throw new TenantConfigDocumentException(ErrorMsg.builder()
      .withCode("REPO_CONFIG_NOT_FOUND")
      .withProps(JsonObject.of("tenantId", getId() , "repoConfigs", getRepoConfigs()))
      .withMessage("Can't find repo config of type: " + type+ "!")
      .toString());
    }
    return config.get();
  }


  enum TenantStatus { IN_FORCE, ARCHIVED }
  enum TenantRepoConfigType { 
    WRENCH, STENCIL, TASKS, DIALOB, CRM, TENANT, SYS_CONFIG, USER_PROFILE, PERMISSIONS
  }
  
  
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
  
  
  class TenantConfigDocumentException extends RuntimeException {
    private static final long serialVersionUID = 2015078308320434722L;
    public TenantConfigDocumentException(String code) {
      super(code);
    }
  }
}
