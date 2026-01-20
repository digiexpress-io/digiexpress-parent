package io.digiexpress.thena.cockpit.client.spi.queries;

/*-
 * #%L
 * thena-cockpit-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.util.Optional;

import io.digiexpress.thena.cockpit.client.api.CockpitQueryActions.CockpitAvailableTenantsQuery;
import io.digiexpress.thena.cockpit.client.api.entities.CockpitConfigTenant;
import io.digiexpress.thena.cockpit.client.api.entities.ImmutableCockpitConfigTenant;
import io.digiexpress.thena.cockpit.client.tables.CockpitDb;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CockpitAvailableTenantsQueryImpl implements CockpitAvailableTenantsQuery {
  private final Uni<CockpitDb> state;
  
  @Override
  public Multi<CockpitConfigTenant> findAll() {

    return state.onItem().transformToMulti(state -> state.tenant().findAll())
        .onItem().transform(tenant -> ImmutableCockpitConfigTenant.builder()
            
            .id(tenant.getId())
            .cockpitConfigId("")
            .commitId(tenant.getRev())
            .createdCommitId(tenant.getRev())
            
            .externalId(tenant.getName())
            .externalBranch("")
            
            .cockpitConfigTenantDesc(Optional.ofNullable(tenant.getComment()).orElse(""))
            .cockpitConfigTenantType(Optional.ofNullable(tenant.getLabel()).orElse(""))
            .cockpitConfigTenantExtension(Optional.empty())
            .build());
  }

}
