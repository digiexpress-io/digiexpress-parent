package io.resys.thena.docdb.spi.doc.commits;

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
import java.util.Map;
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.actions.CommitActions.JsonObjectMerge;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.ThenaDocObject.Doc;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocBranch;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocCommit;
import io.resys.thena.docdb.api.models.ThenaGitObject.Blob;
import io.resys.thena.docdb.api.models.ThenaGitObject.TreeValue;
import io.resys.thena.docdb.spi.DocDbInserts.DocBatch;
import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.experimental.Accessors;

public interface DocCommitBatchBuilder {
  DocCommitBatchBuilder commitAuthor(String commitAuthor);
  DocCommitBatchBuilder commitMessage(String commitMessage);
  DocCommitBatchBuilder toBeMerged(Map<String, JsonObjectMerge> toBeMerged);
  DocCommitBatchBuilder toBeInserted(Map<String, JsonObject> toBeInserted);
  DocCommitBatchBuilder toBeRemoved(Collection<String> toBeRemoved);
  DocBatch build();

  
  @lombok.Data @lombok.Builder(toBuilder = true) @Accessors(fluent = false)
  public static class DocCommitTreeState {
    private final Repo repo;
    private final String branchName;
    private final String docId;
    private final String externalId;
    @Builder.Default private final Optional<Doc> doc = Optional.empty();
    @Builder.Default private final Optional<DocBranch> branch = Optional.empty();
    @Builder.Default private final Optional<DocCommit> commit = Optional.empty();
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
