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

import io.resys.thena.api.actions.TenantActions;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.smallrye.mutiny.Uni;



public class TenantActionsImpl implements TenantActions {
  private final TenantDataSource state;
  private final StructureType type;
  
  public TenantActionsImpl(TenantDataSource state, StructureType type) {
    super();
    this.state = state;
    this.type = type;
  }
  
  public TenantActionsImpl(TenantDataSource state) {
    super();
    this.state = state;
    this.type = null;
  }

  @Override
  public TenantQuery queryTenants() {
    return new TenantQueryImpl(state, type);
  }

  @Override
  public CreateOneTenant createOneTenant() {
    return new TenantBuilderImpl(state, type);
  }
  @Override
  public Uni<Void> deleteAllTenants() {

    final var existingRepos = queryTenants().findAll();
    return existingRepos.onItem().transformToUni((repo) -> {
      
      final var repoId = repo.getId();
      final var rev = repo.getRev();
      
      return queryTenants().id(repoId).rev(rev).deleteOne();
    })
    .concatenate().collect().asList()
    .onItem().transformToUni(junk -> state.tenant().delete());
    
  }
}
