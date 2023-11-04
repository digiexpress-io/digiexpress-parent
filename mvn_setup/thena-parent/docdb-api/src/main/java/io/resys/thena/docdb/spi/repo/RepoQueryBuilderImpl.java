package io.resys.thena.docdb.spi.repo;

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

import io.resys.thena.docdb.api.actions.RepoActions;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Data @Accessors(fluent = true)
public class RepoQueryBuilderImpl implements RepoActions.RepoQuery {

  private final DbState state;
  private String id;
  private String rev;
  

  @Override
  public Multi<Repo> findAll() {
   return state.project().findAll(); 
  }

  @Override
  public Uni<Repo> get() {
    RepoAssert.notEmpty(id, () -> "Define id or name!");
    return state.project().getByNameOrId(id);
  }

  @Override
  public Uni<Repo> delete() {
    return get().onItem().transformToUni(repo -> state.project().delete(repo));
  }
}
