package io.digiexpress.thena.cockpit.client.spi;

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

import java.util.List;
import java.util.Optional;

import io.digiexpress.thena.cockpit.client.api.CockpitAware;
import io.digiexpress.thena.cockpit.client.api.CockpitQueryActions.CockpitAwareQuery;
import io.digiexpress.thena.cockpit.client.api.entities.CockpitConfigTenant;
import io.digiexpress.thena.cockpit.client.api.entities.ImmutableCockpitConfigTenant;
import io.smallrye.mutiny.Multi;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CockpitAwareQueryImpl implements CockpitAwareQuery {

  private final List<CockpitAware<?>> aware;
  
  @Override
  public Multi<CockpitConfigTenant> findAll() {
    return Multi.createFrom().items(aware.stream())
        .onItem().transform(item ->
          ImmutableCockpitConfigTenant
            .builder()
            .id(item.getCockpitAwareProps().getTenantName())
            .cockpitConfigId(item.getCockpitAwareProps().getTenantName())
            .commitId("")
            .createdCommitId("")
            
            .externalId(item.getCockpitAwareProps().getTenantName())
            .externalBranch("")
            
            .cockpitConfigTenantDesc("Hardcoded system default from @CockpitAware")
            .cockpitConfigTenantType(item.getCockpitAwareProps().getTenantType())
            .cockpitConfigTenantExtension(Optional.empty())
            .build()
        );
  }

}
