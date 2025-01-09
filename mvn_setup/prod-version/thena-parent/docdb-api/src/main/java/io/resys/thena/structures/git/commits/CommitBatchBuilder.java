package io.resys.thena.structures.git.commits;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.thena.api.actions.GitCommitActions.JsonObjectMerge;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.git.Blob;
import io.resys.thena.api.entities.git.Branch;
import io.resys.thena.api.entities.git.Commit;
import io.resys.thena.api.entities.git.Tree;
import io.resys.thena.api.entities.git.TreeValue;
import io.resys.thena.structures.git.GitInserts.GitBatch;
import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.experimental.Accessors;

public interface CommitBatchBuilder {
  CommitBatchBuilder commitAuthor(String commitAuthor);
  CommitBatchBuilder commitMessage(String commitMessage);
  CommitBatchBuilder toBeMerged(Map<String, JsonObjectMerge> toBeMerged);
  CommitBatchBuilder toBeInserted(Map<String, JsonObject> toBeInserted);
  CommitBatchBuilder toBeRemoved(Collection<String> toBeRemoved);
  GitBatch build();

  
  @lombok.Data @lombok.Builder(toBuilder = true) @Accessors(fluent = false)
  public static class CommitTreeState {
    private final String gid;
    private final Tenant repo;
    private final String refName;
    @Builder.Default private final Map<String, Blob> blobs = Collections.emptyMap();
    @Builder.Default private final Optional<Branch> ref = Optional.empty();
    @Builder.Default private final Optional<Tree> tree = Optional.empty();
    @Builder.Default private final Optional<Commit> commit = Optional.empty();
  }
  
  @lombok.Data @Accessors(fluent = false)
  public static class CommitTreeMutator {
    private final Map<String, Blob> nextBlobs = new LinkedHashMap<>();
    private final Map<String, TreeValue> nextTree = new LinkedHashMap<>();
    private final CommitLogger logger = new CommitLogger();
    private boolean dataDeleted = false;
    private boolean dataAdded = false; 
  }
  

  @Value.Immutable
  interface RedundentCommitTree {
    boolean isEmpty();
    Map<String, TreeValue> getTreeValues();
    Map<String, Blob> getBlobs();
    String getLog();
  }
  
  @Value.Immutable
  interface RedundentHashedBlob {
    String getName();
    String getHash();
    JsonObject getBlob();
  }
  
}
