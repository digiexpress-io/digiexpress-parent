package io.resys.limaone.authoring;

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
import java.util.Optional;

import io.resys.limaone.authoring.TID.NewAlias;
import io.resys.limaone.persistence.DefaultModelImpl;
import io.resys.limaone.persistence.ImmutableAuthoringConfig;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.resys.thena.api.actions.TenantActions.TenantAware;
import io.resys.thena.api.entities.Alias;
import io.resys.thena.api.entities.Alias.AliasConfig;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class NewAliasImpl implements NewAlias {
  private final TenantAware<?> client;
  private final AuthoringConfig config;
  
  private String aliasName;
  private String aliasDesc;
  private List<AliasConfig> aliasConfig = new ArrayList<>();
  

  @Override
  public NewAlias aliasName(String aliasName) {
    this.aliasName = Objects.requireNonNull(aliasName, () -> "aliasName must be defined!");
    return this;
  }
  @Override
  public NewAlias aliasDesc(String aliasDesc) {
    this.aliasDesc = aliasDesc;
    return this;
  }
  @Override
  public NewAlias aliasConfig(List<AliasConfig> aliasConfig) {
    this.aliasConfig.addAll(Objects.requireNonNull(aliasConfig, () -> "aliasConfig must be defined!"));
    return this;
  }

  @Override
  public Uni<Alias> build() {
    Objects.requireNonNull(aliasName, () -> "aliasName must be defined!");
    
    final var currentUser = config.getEnvir().getCurrentUser().get();
    final var referenceName = config.getEnvir().getDefaultTenantName();
    
    return Uni.combine().all().unis(
      client.getActions().queryTenants().id(referenceName).getOne(),
      client.getActions().queryTenants().id(aliasName).findAll().collect().asList()
    )
    .asTuple()
    .onItem().transformToUni(tuple -> {
      
      if(tuple.getItem2().isEmpty()) {
        return client.getActions().createOneTenant()
          .comment("alias tenant")
          .name(aliasName, StructureType.fs)
          .buildOnlyIfNotCreated()
          .onItem().transformToUni(created -> {
          
            if(created.getItem1()) {
              final var tenantName = Optional.ofNullable(created.getItem2().getRepo().getName());
              final var config = ImmutableAuthoringConfig.builder()
                .from(this.config)
                .persistence(this.config.getPersistence().withTenant(tenantName))
                .build();
              return new DefaultModelImpl(config, false)
                  .buildDefaultModel()
                  .onItem().transform(ignore -> created);
            }
            return Uni.createFrom().item(created);
            
          })
          .onItem().transform(created -> Tuple2.of(tuple.getItem1(), created.getItem2().getRepo()));
      }
      
      return Uni.createFrom().item(Tuple2.of(tuple.getItem1(), tuple.getItem2().getFirst()));
    })
    
    .onItem().transformToUni(tuple -> {
        final var found = tuple.getItem1();
        final var mapTo = tuple.getItem2();
        
        return client.getActions().createOneAlias()
          .aliasConfig(aliasConfig)
          .aliasName(aliasName)
          .aliasDesc(aliasDesc)
          .aliasTenantId(mapTo.getId())
          .refTenantId(found.getId())
          .author(currentUser.getUserName())
          .build();
      });
  }
}
