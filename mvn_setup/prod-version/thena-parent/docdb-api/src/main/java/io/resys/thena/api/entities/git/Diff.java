package io.resys.thena.api.entities.git;

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

@Value.Immutable
public interface Diff {
  enum DiffActionType { MODIFIED, CREATED, DELETED, RENAMED }
  enum DivergenceType { BEHIND, AHEAD, EQUAL, CONFLICT }

  Tenant getRepo();
  List<Divergence> getDivergences();
  
  @Value.Immutable
  interface Divergence {
    DivergenceType getType();
    DivergenceRef getHead(); // current head commit
    DivergenceRef getMain(); // commit from where divergence starts
    List<DiffAction> getActions(); // only if loaded
  }
  
  @Value.Immutable
  interface DivergenceRef {
    List<String> getRefs();
    List<String> getTags();
    Integer getCommits();
    Commit getCommit();
  }
  
  @Value.Immutable
  interface DiffAction {
    DiffActionType getType();
    @Nullable
    DiffBlob getValue();
    @Nullable
    DiffBlob getTarget();
  }
  
  @Value.Immutable
  interface DiffBlob {
    String getId();
    String getName();
    String getContent();
  }
}
