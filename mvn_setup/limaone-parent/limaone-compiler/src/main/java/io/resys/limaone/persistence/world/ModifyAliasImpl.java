package io.resys.limaone.persistence.world;

/*-
 * #%L
 * limaone-compiler
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import io.resys.limaone.authoring.TID.ModifyAlias;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.resys.thena.api.actions.TenantActions.TenantAware;
import io.resys.thena.api.entities.Alias;
import io.resys.thena.api.entities.Alias.AliasConfig;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ModifyAliasImpl implements ModifyAlias {
  private final TenantAware<?> client;
  private final AuthoringConfig config;
  
  private UUID aliasId;
  private String aliasDesc;
  private final List<AliasConfig> aliasConfig = new ArrayList<>();
  
  @Override
  public ModifyAlias aliasId(UUID aliasId) {
    Objects.requireNonNull(aliasId, () -> "aliasId must be defined!");
    return this;
  }
  @Override
  public ModifyAlias aliasDesc(String aliasDesc) {
    this.aliasDesc = aliasDesc;
    return this;
  }
  @Override
  public ModifyAlias aliasConfig(List<AliasConfig> aliasConfig) {
    aliasConfig.addAll(Objects.requireNonNull(aliasConfig, () -> "aliasConfig must be defined!"));
    return this;
  }

  @Override
  public Uni<Alias> build() {
    Objects.requireNonNull(aliasId, () -> "aliasId must be defined!");
    final var currentUser = config.getEnvir().getCurrentUser().get();
    
    
    return client.getActions().modifyOneAlias()
        .aliasConfig(aliasConfig)
        .aliasDesc(aliasDesc)
        .author(currentUser.getUserName())
        .aliasId(aliasDesc)
        .build();
  }
}
