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
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Data @Accessors(fluent = true)
public class TenantQueryImpl implements TenantActions.TenantQuery {

  private final DbState state;
  private String id;
  private String rev;

  @Override
  public Multi<Tenant> findAll() {
   return state.tenant().findAll(); 
  }

  @Override
  public Uni<Tenant> get() {
    RepoAssert.notEmpty(id, () -> "Define id or name!");
    return state.tenant().getByNameOrId(id);
  }

  @Override
  public Uni<Tenant> delete() {
    return get().onItem().transformToUni(repo -> state.tenant().delete(repo));
  }
}
