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

import io.resys.thena.docdb.api.actions.PullActions.MatchCriteria;
import io.resys.thena.docdb.api.models.Message;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.ThenaEnvelope;
import io.resys.thena.docdb.api.models.ThenaGitObject.Commit;
import io.resys.thena.docdb.api.models.ThenaGitObject.Tree;
import io.resys.thena.docdb.api.models.ThenaGitObjects.CommitObjects;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;

public interface CommitActions {
  CommitBuilder commitBuilder();
  CommitQuery commitQuery();
  Uni<List<Commit>> findAllCommits(String repoId);  // head GID to what to append
  Uni<List<Tree>> findAllCommitTrees(String repoId);  // head GID to what to append
  
  interface CommitBuilder {
    CommitBuilder id(@Nullable String headGid); // OPTIONAL head GID to what to append
    CommitBuilder parent(String parentCommit); // for validations
    CommitBuilder latestCommit();
    CommitBuilder head(String projectName, String branchName); // head GID to what to append
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
    CommitQuery projectName(String projectName);
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
    Repo.CommitResultStatus getStatus();
    List<Message> getMessages();
  }
}
