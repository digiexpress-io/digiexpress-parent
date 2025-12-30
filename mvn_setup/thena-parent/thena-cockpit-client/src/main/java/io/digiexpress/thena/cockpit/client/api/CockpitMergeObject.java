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

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import io.digiexpress.thena.cockpit.client.api.CockpitNewObject.NewCockpitConfigProps;
import io.digiexpress.thena.cockpit.client.api.CockpitNewObject.NewCockpitConfigTenant;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;



// Generic interfaces for create/update/delete operations 
public interface CockpitMergeObject {

  interface MergeCockpitConfig {
    // State access
    MergeCockpitConfig onCurrentState(Consumer<CockpitContainer> handleCurrentState);
    CockpitContainer getCurrentState();
    
    MergeCockpitConfig externalId(@Nullable String externalId);

    // Collection bulk replacement operations
    <T> MergeCockpitConfig setAllProps(String propsType, List<T> replacements, Function<T, Consumer<NewCockpitConfigProps>> props);
    <T> MergeCockpitConfig setAllTenants(List<T> replacements, Function<T, Consumer<NewCockpitConfigTenant>> props);
    
    // Add new child entities
    MergeCockpitConfig addProps(Consumer<NewCockpitConfigProps> props);
    MergeCockpitConfig addTenant(Consumer<NewCockpitConfigTenant> tenant);

    
    // Modify existing child entities by ID
    MergeCockpitConfig modifyProps(String propsId, Consumer<MergeCockpitProps> props);
    MergeCockpitConfig modifyTenant(String tenantId, Consumer<MergeCockpitTenant> tenant);

    
    // Remove child entities by ID
    MergeCockpitConfig removeProps(String propsId);
    MergeCockpitConfig removeTenant(String tenantId);

    
    void build();
  }
  
  interface MergeCockpitTenant {
    MergeCockpitTenant externalId(String externalId);
    MergeCockpitTenant externalBranch(@Nullable String externalBranch);
    
    MergeCockpitTenant tenantDescription(String tenantDescription);
    MergeCockpitTenant tenantExtension(@Nullable JsonObject tenantExtension);
    
    void build();
  }
  
  // Merge interfaces for child entities
  interface MergeCockpitProps {
    MergeCockpitProps externalId(String externalId);
    MergeCockpitProps propsType(String propsType);
    MergeCockpitProps propsExtension(@Nullable JsonObject propsExtension);
    void build();
  }
  

}
