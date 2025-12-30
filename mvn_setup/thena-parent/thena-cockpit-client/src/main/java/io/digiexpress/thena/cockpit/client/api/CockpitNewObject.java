package io.digiexpress.thena.cockpit.client.api;

/*-
 * #%L
 * thena-cockpit-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.function.Consumer;

import io.digiexpress.thena.cockpit.client.api.entities.CockpitConfigProps;
import io.digiexpress.thena.cockpit.client.api.entities.CockpitConfigTenant;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public interface CockpitNewObject {

  interface NewCockpitConfig {
    NewCockpitConfig externalId(@Nullable String externalId);
    NewCockpitConfig configDesc(String configDesc);
    NewCockpitConfig configName(String configName);
    
    // nested builders for related entities
    NewCockpitConfig addTenant(Consumer<NewCockpitConfigTenant> tenant);
    NewCockpitConfig addProps(Consumer<NewCockpitConfigProps> tenant);
    
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
    NewCockpitConfigProps propsType(String propsType);
    NewCockpitConfigProps propsExtension(@Nullable JsonObject tenantExtension);
    
    CockpitConfigProps build();
  }
}
