package io.digiexpress.thena.cockpit.client.api;

import java.util.function.Consumer;

import io.digiexpress.thena.cockpit.client.api.entities.CockpitConfigProps;
import io.digiexpress.thena.cockpit.client.api.entities.CockpitConfigTenant;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public interface CockpitNewObject {

  interface NewCockpitConfig {
    NewCockpitConfig externalId(@Nullable String externalId);
    
    // nested builders for related entities
    NewCockpitConfig addTenant(Consumer<NewCockpitConfigTenant> tenant);
    NewCockpitConfig addProps(Consumer<NewCockpitConfigTenant> tenant);
    
    // state handling
    NewCockpitConfig onNewState(Consumer<CockpitContainer> handleNewState);
    void build();
  }
  

  interface NewCockpitConfigTenant {
    NewCockpitConfigTenant externalId(String externalId);
    NewCockpitConfigTenant externalBranch(@Nullable String externalBranch);
    
    NewCockpitConfigTenant tenantDescription(String tenantDescription);
    NewCockpitConfigTenant tenantExtension(@Nullable JsonObject tenantExtension);
    
    CockpitConfigTenant build();
  }


  interface NewCockpitConfigProps {
    NewCockpitConfigProps externalId(String externalId);
    NewCockpitConfigProps propsType(String propsType);
    NewCockpitConfigProps propsExtension(@Nullable JsonObject tenantExtension);
    
    CockpitConfigProps build();
  }
}