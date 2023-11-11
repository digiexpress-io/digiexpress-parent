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
import io.resys.thena.docdb.models.doc.DocState;
import io.resys.thena.docdb.models.git.GitState;
import io.resys.thena.docdb.support.ErrorHandler;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface DbState {
  DbCollections getCollections();
  RepoBuilder project();
  ErrorHandler getErrorHandler();
  
  GitState toGitState();
  DocState toDocState();
  
  interface RepoBuilder {
    Uni<Repo> getByName(String name);
    Uni<Repo> getByNameOrId(String nameOrId);
    Multi<Repo> findAll();
    Uni<Repo> delete(Repo newRepo);
    Uni<Repo> insert(Repo newRepo);
  }
}
