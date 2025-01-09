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

import io.resys.thena.api.actions.ImmutableTenantCommitResult;
import io.resys.thena.api.actions.TenantActions;
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import io.resys.thena.api.actions.TenantActions.CommitStatus;
import io.resys.thena.api.entities.ImmutableTenant;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.exceptions.RepoException;
import io.resys.thena.support.Identifiers;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class TenantBuilderImpl implements TenantActions.TenantBuilder {

  private final DbState state;
  private String externalId;
  private String name;
  private StructureType type;

  public TenantBuilderImpl externalId(String externalId) {
    this.externalId = externalId;
    return this;
  }
  @Override
  public TenantBuilderImpl name(String name, StructureType type) {
    this.name = name;
    this.type = type;
    return this;
  }
  
  @Override
  public Uni<TenantCommitResult> build() {
    log.debug("Creating repository '{}' of type {}.", name, type);

    RepoAssert.notEmpty(name, () -> "repo name not defined!");
    RepoAssert.notNull(type, () -> "type name not defined!");
    RepoAssert.isName(name, () -> "repo name has invalid characters!");

    
    
    return state.tenant().getByName(name)
      .onItem().transformToUni((Tenant existing) -> {
     
      final Uni<TenantCommitResult> result;
      if(existing != null) {
        log.error("Existing repository found with name '{}'", name);
        result = Uni.createFrom().item(ImmutableTenantCommitResult.builder()
            .status(CommitStatus.CONFLICT)
            .addMessages(RepoException.builder().nameNotUnique(existing.getName(), existing.getId()))
            .build());
      } else {
        result = state.tenant().findAll()
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
            .onItem().transform(next -> (TenantCommitResult) ImmutableTenantCommitResult.builder()
                .repo(next)
                .status(CommitStatus.OK)
                .build());
        });
      }
      return result;
    });
  }
}
