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
public interface TenantConfig extends Serializable {
  
  String getId();
  String getName();
  @Nullable String getVersion();
  TenantStatus getStatus();

  @Nullable Instant getCreated();
  @Nullable Instant getUpdated();
  @Nullable Instant getArchived();
  
  TenantPreferences getPreferences();
  List<TenantRepoConfig> getRepoConfigs();
  
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


  @Value.Immutable @JsonSerialize(as = ImmutableTenantRepoConfig.class) @JsonDeserialize(as = ImmutableTenantRepoConfig.class)
  interface TenantRepoConfig {
    String getRepoId();
    TenantRepoConfigType getRepoType();
    
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableTenantPreferences.class) @JsonDeserialize(as = ImmutableTenantPreferences.class)
  interface TenantPreferences {
    String getLandingApp();
  }
  
  

  enum TenantStatus { IN_FORCE, ARCHIVED }
  enum TenantRepoConfigType { 
    WRENCH, STENCIL, TASKS, DIALOB, CRM, TENANT, SYS_CONFIG, USER_PROFILE, PERMISSIONS
  }
  
  public static final String TENANT_CONFIG = "TENANT_CONFIG";
  public static final String APP_BACKOFFICE = "app-frontoffice";

  class TenantConfigDocumentException extends RuntimeException {
    private static final long serialVersionUID = 2015078308320434722L;
    public TenantConfigDocumentException(String code) {
      super(code);
    }
  }
}
