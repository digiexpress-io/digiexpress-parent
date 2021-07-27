package io.resys.hdes.docdb.api.actions;

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

import io.resys.hdes.docdb.api.models.Diff;
import io.resys.hdes.docdb.api.models.Message;
import io.resys.hdes.docdb.api.models.Repo;
import io.smallrye.mutiny.Uni;

public interface DiffActions {

  HeadDiffBuilder head();
  
  interface HeadDiffBuilder {
    HeadDiffBuilder repo(String repoIdOrName);
    HeadDiffBuilder left(String headOrCommitOrTag);
    HeadDiffBuilder right(String headOrCommitOrTag);
    Uni<DiffResult<Diff>> build();
  }
  
  enum DiffStatus {
    OK, ERROR
  }
  
  @Value.Immutable
  interface DiffResult<T> {
    @Nullable
    Repo getRepo();    
    @Nullable
    T getObjects();
    DiffStatus getStatus();
    List<Message> getMessages();
  }
}
