package io.resys.thena.docdb.api.actions;

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

import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.models.Message;
import io.resys.thena.docdb.api.models.Repo;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface RepoActions {

  QueryBuilder query();
  CreateBuilder create();

  interface QueryBuilder {
    QueryBuilder id(String id);
    QueryBuilder rev(String rev);
    Multi<Repo> find();
    Uni<Repo> get();
  }
  
  interface CreateBuilder {
    CreateBuilder name(String name);
    Uni<RepoResult> build();
  }
  
  enum RepoStatus {
    OK, CONFLICT
  }
  
  @Value.Immutable
  interface RepoResult {
    @Nullable
    Repo getRepo();
    RepoStatus getStatus();
    List<Message> getMessages();
  }
}
