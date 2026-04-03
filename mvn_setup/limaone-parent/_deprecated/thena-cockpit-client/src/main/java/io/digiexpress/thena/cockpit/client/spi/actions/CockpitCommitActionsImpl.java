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

import io.digiexpress.thena.cockpit.client.api.CockpitCommitActions;
import io.digiexpress.thena.cockpit.client.tables.CockpitDb;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CockpitCommitActionsImpl implements CockpitCommitActions {
  private final CockpitDb state;
  private final String tenantId;
  
  @Override
  public CreateOneCockpitConfig createOneCockpitConfig() {
    return new CreateOneCockpitConfigImpl(state, tenantId);
  }
  @Override
  public ModifyOneCockpitConfig modifyOneCockpitConfig() {
    return new ModifyOneCockpitConfigImpl(state, tenantId);
  }
}
