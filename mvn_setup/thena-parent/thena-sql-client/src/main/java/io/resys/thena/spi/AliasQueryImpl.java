package io.resys.thena.spi;

/*-
 * #%L
 * thena-sql-client
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

import java.util.Objects;
import java.util.UUID;

import io.resys.thena.api.actions.TenantActions.AliasQuery;
import io.resys.thena.api.entities.Alias;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class AliasQueryImpl implements AliasQuery {
  private final TenantDataSource state;

  private UUID id;
  
  @Override
  public AliasQuery id(UUID id) {
    this.id = Objects.requireNonNull(id, () -> "id must be defined!");
    return this;
  }

  @Override
  public Multi<Alias> findAll() {
    if(id != null) {
      return state.alias().findByNameOrId(id.toString())
          .onItem().transformToMulti(e -> {
            if(e.isEmpty()) {
              return Multi.createFrom().empty();
            }
            return Multi.createFrom().item(e.get());
          });
    }
    return state.alias().findAll();
  }

  @Override
  public Uni<Alias> deleteOne() {
    return getOne().onItem().transformToUni(alias -> state.alias().delete(alias));
  }

  @Override
  public Uni<Alias> getOne() {
    return state.alias().getByNameOrId(id.toString());
  }
}
