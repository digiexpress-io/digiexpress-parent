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

import io.resys.thena.docdb.api.models.Repo;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface ClientState {
  ClientCollections getCollections();
  RepoBuilder repos();
  ErrorHandler getErrorHandler();
  
  Uni<ClientInsertBuilder> insert(String repoNameOrId);
  ClientInsertBuilder insert(Repo repo);
  
  Uni<ClientQuery> query(String repoNameOrId);
  ClientQuery query(Repo repo);
  
  ClientRepoState withRepo(Repo repo);
  Uni<ClientRepoState> withRepo(String repoNameOrId);
  
  interface RepoBuilder {
    Uni<Void> create();
    Uni<Repo> getByName(String name);
    Uni<Repo> getByNameOrId(String nameOrId);
    Multi<Repo> find();
    Uni<Repo> insert(Repo newRepo);
  }
  
  interface ClientRepoState {
    ClientInsertBuilder insert();
    ClientQuery query();
  }
}
