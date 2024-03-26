package io.resys.thena.docdb.spi;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import io.resys.thena.docdb.api.actions.ImmutableRepoResult;
import io.resys.thena.docdb.api.actions.TenantModel;
import io.resys.thena.docdb.api.actions.TenantModel.RepoResult;
import io.resys.thena.docdb.api.actions.TenantModel.RepoStatus;
import io.resys.thena.docdb.api.exceptions.RepoException;
import io.resys.thena.docdb.api.models.ImmutableRepo;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.Repo.RepoType;
import io.resys.thena.docdb.support.Identifiers;
import io.resys.thena.docdb.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class RepoBuilderImpl implements TenantModel.RepoBuilder {

  private final DbState state;
  private String name;
  private RepoType type;
  
  public RepoBuilderImpl name(String name, RepoType type) {
    this.name = name;
    this.type = type;
    return this;
  }
  
  @Override
  public Uni<RepoResult> build() {
    log.debug("Creating repository '{}' of type {}.", name, type);

    RepoAssert.notEmpty(name, () -> "repo name not defined!");
    RepoAssert.notNull(type, () -> "type name not defined!");
    RepoAssert.isName(name, () -> "repo name has invalid characters!");

    return state.project().getByName(name)
      .onItem().transformToUni((Repo existing) -> {
      
      final Uni<RepoResult> result;
      if(existing != null) {
        log.error("Existing repository found with name '{}'", name);
        result = Uni.createFrom().item(ImmutableRepoResult.builder()
            .status(RepoStatus.CONFLICT)
            .addMessages(RepoException.builder().nameNotUnique(existing.getName(), existing.getId()))
            .build());
      } else {
        result = state.project().findAll()
        .collect().asList().onItem()
        .transformToUni((allRepos) -> { 
          
          final var newRepo = ImmutableRepo.builder()
              .id(Identifiers.uuid())
              .rev(Identifiers.uuid())
              .type(type)
              .name(name)
              .prefix("nested_" + (allRepos.size() + 10) + "_")
              .build();
          
          return state.project().insert(newRepo)
            .onItem().transform(next -> (RepoResult) ImmutableRepoResult.builder()
                .repo(next)
                .status(RepoStatus.OK)
                .build());
        });
      }
      return result;
    });
  }
}
