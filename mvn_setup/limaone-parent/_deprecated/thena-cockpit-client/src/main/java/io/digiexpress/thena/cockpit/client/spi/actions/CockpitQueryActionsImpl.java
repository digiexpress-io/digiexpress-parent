package io.digiexpress.thena.cockpit.client.spi.actions;

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

import io.digiexpress.thena.cockpit.client.api.CockpitQueryActions;
import io.digiexpress.thena.cockpit.client.spi.queries.CockpitAvailableTenantsQueryImpl;
import io.digiexpress.thena.cockpit.client.spi.queries.CockpitAwareQueryImpl;
import io.digiexpress.thena.cockpit.client.spi.queries.CockpitQueryImpl;
import io.digiexpress.thena.cockpit.client.tables.CockpitDb;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class CockpitQueryActionsImpl implements CockpitQueryActions {
  private final CockpitDb startingState;
  private final CockpitAwareQueryImpl awareQuery;
  private final String repoId;
  
  @Override
  public CockpitQuery cockpitQuery() {
    return new CockpitQueryImpl(startingState.withTenant(repoId));
  }

  @Override
  public CockpitAwareQuery cockpitAwareQuery() {
    return awareQuery;
  }

  @Override
  public CockpitAvailableTenantsQuery cockpitAvailableTenantsQuery() {
    return new CockpitAvailableTenantsQueryImpl(startingState.withTenant(repoId));
  }
}
