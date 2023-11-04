package io.resys.thena.docdb.spi;

import javax.annotation.Nullable;

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

import org.immutables.value.Value;

import io.resys.thena.docdb.api.models.Repo;
import io.smallrye.mutiny.Uni;

public interface DocDbState {

  <R> Uni<R> withTransaction(String repoId, TransactionFunction<R> callback);
  

  interface DocRepo {
    String getRepoName();
    Repo getRepo();
    
    DocDbInserts insert();
    DocDbQueries query();
  }
  
  @FunctionalInterface
  interface TransactionFunction<R> {
    Uni<R> apply(DocRepo repoState);
  }

  @Value.Immutable
  interface DocLockCriteria {
    @Nullable String getVersionId();
    @Nullable String getDocId();
    @Nullable String getExternalId();
    @Nullable String getBranchName();
  }  
}
