package io.digiexpress.thena.cockpit.client.api;

/*-
 * #%L
 * thena-Cockpit-client
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
import java.util.Optional;

import io.digiexpress.thena.cockpit.client.api.entities.CockpitConfigTenant;
import io.digiexpress.thena.cockpit.client.api.entities.CockpitDocType;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface CockpitQueryActions {

  CockpitQuery cockpitQuery();
  CockpitAwareQuery cockpitAwareQuery();
  
  interface CockpitAwareQuery {
    // return all registered clients
    Multi<CockpitConfigTenant> findAll();
  }
  
  interface CockpitQuery {
    
    CockpitQuery lockForUpdate();
    
    // optimization, exclude explicitly doc-s that we don't need 
    CockpitQuery excludeDocs(CockpitDocType... docs);
    
    CockpitQuery addCockpitId(String ids);
    CockpitQuery addAllCockpitId(List<String> ids); // include only data for given Cockpit
    
    Uni<CockpitContainer> getOne(String id);
    Uni<Optional<CockpitContainer>> findOne();
    Multi<CockpitContainer> findAll();
  } 
}