package io.resys.thena.api.actions;

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

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.git.Diff;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.api.envelope.ThenaEnvelope;
import io.smallrye.mutiny.Uni;

public interface GitDiffActions {

  DiffQuery diffQuery();
  
  interface DiffQuery {
    DiffQuery left(String headOrCommitOrTag);
    DiffQuery right(String headOrCommitOrTag);
    Uni<DiffResult<Diff>> get();
  }
  
  enum DiffResultStatus {
    OK, ERROR
  }
  
  @Value.Immutable
  interface DiffResult<T> extends ThenaEnvelope {
    @Nullable
    Tenant getRepo();    
    @Nullable
    T getObjects();
    DiffResultStatus getStatus();
    List<Message> getMessages();
  }
}
