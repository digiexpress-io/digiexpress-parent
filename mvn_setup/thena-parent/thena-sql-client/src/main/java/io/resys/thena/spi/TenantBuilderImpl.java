package io.resys.thena.spi;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import io.resys.thena.api.actions.ImmutableCreatedTenant;
import io.resys.thena.api.actions.TenantActions;
import io.resys.thena.api.actions.TenantActions.TenantOperationStatus;
import io.resys.thena.api.actions.TenantActions.CreateOneTenant;
import io.resys.thena.api.actions.TenantActions.CreatedTenant;
import io.resys.thena.api.entities.ImmutableTenant;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.support.Identifiers;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class TenantBuilderImpl implements TenantActions.CreateOneTenant {

  private final TenantDataSource state;
  private String externalId;
  private String name;
  private StructureType type;
  
  public TenantBuilderImpl(TenantDataSource state, StructureType type) {
    super();
    this.state = state;
    this.type = type;
    this.name = state.getDataSource().getTenant().getName();
  }

  public TenantBuilderImpl externalId(String externalId) {
    this.externalId = externalId;
    return this;
  }
  
  @Override
  public CreateOneTenant name(String name) {
    this.name = name;
    return this;
  }
  
  @Override
  public CreateOneTenant name(String name, StructureType type) {
    this.name = name;
    this.type = type;
    return this;
  }

  @Override
  public Uni<Tuple2<Boolean, CreatedTenant>> buildOnlyIfNotCreated() {
    log.debug("Creating repository '{}' of type {}.", name, type);

    RepoAssert.notEmpty(name, () -> "repo name not defined!");
    RepoAssert.notNull(type, () -> "type name not defined!");
    RepoAssert.isName(name, () -> "repo name has invalid characters!");

    return state.tenant().getByName(name)
        .onItem().transformToUni((Tenant existing) -> {

        if(existing == null) {
          return createTenant().onItem().transform(c -> Tuple2.of(true, c));
        }
        
        log.error("Existing repository found with name '{}'", name);
        final var resp = ImmutableCreatedTenant.builder()
          .repo(existing)
          .status(TenantOperationStatus.OK)
          .build();
        return Uni.createFrom().item(Tuple2.of(false, resp));
      });
  }
  
  
  @Override
  public Uni<CreatedTenant> build() {
    log.debug("Creating repository '{}' of type {}.", name, type);

    RepoAssert.notEmpty(name, () -> "repo name not defined!");
    RepoAssert.notNull(type, () -> "type name not defined!");
    RepoAssert.isName(name, () -> "repo name has invalid characters!");

    return state.tenant().getByName(name)
      .onItem().transformToUni((Tenant existing) -> {

      if(existing == null) {
        return createTenant();
      }
      
      log.error("Existing repository found with name '{}'", name);      
      return Uni.createFrom().item(ImmutableCreatedTenant.builder()
          .status(TenantOperationStatus.CONFLICT)
          .addMessages(nameNotUnique(existing.getName(), existing.getId()))
          .build());
    });
  }
  
  
  private Uni<CreatedTenant> createTenant() {
    return state.tenant().findAll()
    .collect().asList().onItem()
    .transformToUni((allRepos) -> {
      final var codeName = name.toUpperCase();
      final var prefixStart = codeName.substring(0, Math.min(codeName.length(), 10));
      
      final var prefix = prefixStart.replace("-", "_") + (allRepos.size() + 10) + "_" ;
      final var newRepo = ImmutableTenant.builder()
          .id(Identifiers.uuid())
          .rev(Identifiers.uuid())
          .type(type)
          .name(name)
          .externalId(externalId)
          .prefix(prefix)
          .build();
      
      return state.tenant().insert(newRepo)
        .onItem().transform(next -> (CreatedTenant) ImmutableCreatedTenant.builder()
            .repo(next)
            .status(TenantOperationStatus.OK)
            .build());
    });
  } 
  
  private Message nameNotUnique(String name, String id) {
    return ImmutableMessage.builder()
          .text(new StringBuilder()
          .append("Repo with name: '").append(name).append("' already exists,")
          .append(" id: '").append(id).append("'")
          .append("!")
          .toString())
        .build();
  }


}
