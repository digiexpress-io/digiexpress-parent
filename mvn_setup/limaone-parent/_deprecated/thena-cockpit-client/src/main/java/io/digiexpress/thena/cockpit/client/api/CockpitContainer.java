package io.digiexpress.thena.cockpit.client.api;

/*-
 * #%L
 * thena-contract-client
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

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.thena.cockpit.client.api.entities.CockpitCommit;
import io.digiexpress.thena.cockpit.client.api.entities.CockpitCommitTree;
import io.digiexpress.thena.cockpit.client.api.entities.CockpitConfig;
import io.digiexpress.thena.cockpit.client.api.entities.CockpitConfigProps;
import io.digiexpress.thena.cockpit.client.api.entities.CockpitConfigTenant;
import io.resys.thena.api.envelope.ThenaContainer;


@Value.Immutable
@JsonSerialize(as = ImmutableCockpitContainer.class)
@JsonDeserialize(as = ImmutableCockpitContainer.class)
public interface CockpitContainer extends ThenaContainer {
  CockpitConfig getConfig();
  List<CockpitCommit> getCommits();
  List<CockpitCommitTree> getCommitTrees();
  List<CockpitConfigProps> getProps();
  List<CockpitConfigTenant> getTenants();

  
}
