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
import io.smallrye.mutiny.Uni;

public interface GitDbState {
  Uni<GitDbInserts> insert(String repoNameOrId);
  GitDbInserts insert(Repo repo);
  
  Uni<GitDbQueries> query(String repoNameOrId);
  GitDbQueries query(Repo repo);
  
  GitRepo withRepo(Repo repo);
  Uni<GitRepo> withRepo(String repoNameOrId);
  <R> Uni<R> withTransaction(String repoId, String headName, TransactionFunction<R> callback);
  

  interface GitRepo {
    String getRepoName();
    Repo getRepo();
    GitDbInserts insert();
    GitDbQueries query();
  }
  
  @FunctionalInterface
  interface TransactionFunction<R> {
    Uni<R> apply(GitRepo repoState);
  }
}
