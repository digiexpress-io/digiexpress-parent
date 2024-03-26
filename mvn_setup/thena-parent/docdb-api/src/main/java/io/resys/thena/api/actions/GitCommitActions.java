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
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.thena.api.actions.GitPullActions.MatchCriteria;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.git.Blob;
import io.resys.thena.api.entities.git.Commit;
import io.resys.thena.api.entities.git.Tree;
import io.resys.thena.api.envelope.BlobContainer;
import io.resys.thena.api.envelope.GitContainer;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.ThenaEnvelope;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;

public interface GitCommitActions {
  CommitBuilder commitBuilder();
  CommitQuery commitQuery();
  Uni<List<Commit>> findAllCommits();  // head GID to what to append
  Uni<List<Tree>> findAllCommitTrees();  // head GID to what to append
  
  interface CommitBuilder {
    CommitBuilder parent(String parentCommit); // for validations
    CommitBuilder latestCommit();
    CommitBuilder branchName(String branchName); // head GID to what to append
    CommitBuilder append(String docId, JsonObject doc);
    CommitBuilder merge(String docId, JsonObjectMerge doc);
    CommitBuilder remove(String docId);
    CommitBuilder remove(List<String> docId);
    CommitBuilder author(String author);
    CommitBuilder message(String message);
    Uni<CommitResultEnvelope> build();
  }
  
  // build REF world state, no blobs by default
  interface CommitQuery {
    CommitQuery branchNameOrCommitOrTag(String branchNameOrCommitOrTag);
    CommitQuery docsIncluded();
    CommitQuery docsIncluded(boolean load);
    CommitQuery matchBy(List<MatchCriteria> matchCriteria);
    Uni<QueryEnvelope<CommitObjects>> get();
  }

  @FunctionalInterface
  interface JsonObjectMerge {
    JsonObject apply(JsonObject previousState);
  }
  
  @Value.Immutable
  interface CommitResultEnvelope extends ThenaEnvelope {
    String getGid(); // repo/head
    @Nullable
    Commit getCommit();
    CommitResultStatus getStatus();
    List<Message> getMessages();
  }
  
  @Value.Immutable
  interface CommitObjects extends GitContainer, BlobContainer  {
    Tenant getRepo();
    Commit getCommit();
    Tree getTree();
    Map<String, Blob> getBlobs(); //only if loaded
    
    default <T> List<T> accept(BlobVisitor<T> visitor) {
      return getTree().getValues().values().stream()
          .map(treeValue -> getBlobs().get(treeValue.getBlob()))
          .map(blob -> visitor.visit(blob.getValue()))
          .collect(Collectors.toUnmodifiableList());
    }
  }
}
