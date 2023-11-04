package io.resys.thena.docdb.spi.commits;

import java.time.Duration;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.resys.thena.docdb.api.actions.CommitActions.CommitBuilder;
import io.resys.thena.docdb.api.actions.CommitActions.CommitResultEnvelope;
import io.resys.thena.docdb.api.actions.CommitActions.CommitResultStatus;
import io.resys.thena.docdb.api.actions.CommitActions.JsonObjectMerge;
import io.resys.thena.docdb.api.actions.ImmutableCommitResultEnvelope;
import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.ThenaGitObject.CommitLock;
import io.resys.thena.docdb.api.models.ThenaGitObject.CommitLockStatus;
import io.resys.thena.docdb.spi.ClientInsertBuilder.BatchStatus;
import io.resys.thena.docdb.spi.ClientState;
import io.resys.thena.docdb.spi.ClientState.ClientRepoState;
import io.resys.thena.docdb.spi.ImmutableLockCriteria;
import io.resys.thena.docdb.spi.commits.CommitBatchBuilder.CommitTreeState;
import io.resys.thena.docdb.spi.support.Identifiers;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CommitBuilderImpl implements CommitBuilder {

  private final ClientState state;
  private final Map<String, JsonObject> appendBlobs = new HashMap<>();
  private final Map<String, JsonObjectMerge> mergeBlobs = new HashMap<>();
  private final List<String> deleteBlobs = new ArrayList<>();
  
  private String headGid;
  private String repoId;
  private String headName;
  private String author;
  private String message;
  private String parentCommit;
  private Boolean parentIsLatest = Boolean.FALSE;

  @Override
  public CommitBuilder id(String headGid) {
    RepoAssert.isEmpty(headName, () -> "Can't defined id when head is defined!");
    this.headGid = headGid;
    return this;
  }
  @Override
  public CommitBuilder head(String repoId, String headName) {
    RepoAssert.isEmpty(headGid, () -> "Can't defined head when id is defined!");
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(headName, () -> "headName can't be empty!");
    RepoAssert.isName(headName, () -> "headName has invalid charecters!");
    this.repoId = repoId;
    this.headName = headName;
    return this;
  }
  @Override
  public CommitBuilder append(String name, JsonObject blob) {
    RepoAssert.notNull(blob, () -> "blob can't be empty!");
    RepoAssert.notEmpty(name, () -> "name can't be empty!");
    RepoAssert.isTrue(!this.mergeBlobs.containsKey(name), () -> "Blob with name: '" + name + "'can't be added because it's already marked for merge!");
    RepoAssert.isTrue(!this.appendBlobs.containsKey(name), () -> "Blob with name: '" + name + "' is already defined!");
    RepoAssert.isTrue(!this.deleteBlobs.contains(name), () -> "Blob with name: '" + name + "' can't be appended because it's been marked for removal!");
    this.appendBlobs.put(name, blob);
    return this;
  }
  @Override
  public CommitBuilder remove(String name) {
    RepoAssert.notEmpty(name, () -> "name can't be empty!");
    RepoAssert.isTrue(!this.mergeBlobs.containsKey(name), () -> "Blob with name: '" + name + "'can't be merged because it's already marked for removal!");
    RepoAssert.isTrue(!this.appendBlobs.containsKey(name), () -> "Blob with name: '" + name + "' can't be marked for removal because it's beed appended!");
    RepoAssert.isTrue(!this.deleteBlobs.contains(name), () -> "Blob with name: '" + name + "' is already marked for removal!");
    this.deleteBlobs.add(name);
    return this;
  }
  @Override
  public CommitBuilder remove(List<String> names) {
    names.forEach(this::remove);
    return this;
  }
  @Override
  public CommitBuilder merge(String name, JsonObjectMerge blob) {
    RepoAssert.notEmpty(name, () -> "name can't be empty!");
    RepoAssert.notNull(blob, () -> "merge can't be null!");
    RepoAssert.isTrue(!this.mergeBlobs.containsKey(name), () -> "Blob with name: '" + name + "' is already marked for merge!");
    RepoAssert.isTrue(!this.appendBlobs.containsKey(name), () -> "Blob with name: '" + name + "' can't be marked for removal because it's beed appended!");
    RepoAssert.isTrue(!this.deleteBlobs.contains(name), () -> "Blob with name: '" + name + "' is already marked for removal!");
    this.mergeBlobs.put(name, blob);
    return this;
  }
  @Override
  public CommitBuilder author(String author) {
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    this.author = author;
    return this;
  }
  @Override
  public CommitBuilder message(String message) {
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    this.message = message;
    return this;
  }
  @Override
  public CommitBuilder parent(String parentCommit) {
    this.parentCommit = parentCommit;
    return this;
  }
  @Override
  public CommitBuilder latestCommit() {
    this.parentIsLatest = true;
    return this;
  }
  @Override
  public Uni<CommitResultEnvelope> build() {
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.isTrue(!appendBlobs.isEmpty() || !deleteBlobs.isEmpty() || !mergeBlobs.isEmpty(), () -> "Nothing to commit, no content!");
    
    if(this.headGid != null) {
      final var id = Identifiers.fromRepoHeadGid(this.headGid);
      this.repoId = id[0];
      this.headName = id[1];
    }
    RepoAssert.notEmpty(repoId, () -> "Can't resolve repoId!");
    RepoAssert.notEmpty(headName, () -> "Can't resolve headName!");

    // final var totalOperation = appendBlobs.size() + deleteBlobs.size() + mergeBlobs.size();
    
    final var crit = ImmutableLockCriteria.builder().headName(headName).commitId(parentCommit).treeValueIds(mergeBlobs.keySet()).build();
    return this.state.withTransaction(repoId, headName, tx -> tx.query().commits().getLock(crit)
      .onItem().transformToUni(lock -> {
        final var validation = validateRepo(lock, parentCommit);
        if(validation != null) {
          return Uni.createFrom().item(validation);
        }
        return doInLock(lock, tx);

      })
    )
    .onFailure(err -> state.getErrorHandler().isLocked(err)).retry()
      .withJitter(0.3) // every retry increase time by x 3
      .withBackOff(Duration.ofMillis(100))
      .atMost(100);
   /*
    .onFailure(err -> state.getErrorHandler().isLocked(err)).invoke(error -> {
      error.printStackTrace();
      System.err.println(error.getMessage());
    });*/
    
  }
  
  private Uni<CommitResultEnvelope> doInLock(CommitLock lock, ClientRepoState tx) {
    final var gid = Identifiers.toRepoHeadGid(repoId, headName);  
    final var init = CommitTreeState.builder().ref(lock.getBranch()).refName(headName).gid(gid).repo(tx.getRepo());
    
    if(lock.getStatus() == CommitLockStatus.NOT_FOUND) {
      // nothing to add
    } else {
      init.commit(lock.getCommit()).tree(lock.getTree()).blobs(lock.getBlobs());
    }
    
    
    final var batch = new CommitBatchBuilderImpl(init.build())
        .commitParent(parentCommit)
        .commitAuthor(author)
        .commitMessage(message)
        .toBeInserted(appendBlobs)
        .toBeRemoved(deleteBlobs)
        .toBeMerged(mergeBlobs)
        .build();
    
    return tx.insert().batch(batch)
        .onItem().transform(rsp -> ImmutableCommitResultEnvelope.builder()
          .gid(gid)
          .commit(rsp.getCommit())
          .addMessages(rsp.getLog())
          .addAllMessages(rsp.getMessages())
          .status(visitStatus(rsp.getStatus()))
          .build());
  }

  private CommitResultEnvelope validateRepo(CommitLock state, String commitParent) {
    final var gid = Identifiers.toRepoHeadGid(repoId, headName);
    
    // cant merge on first commit
    if(state.getCommit().isEmpty() && !mergeBlobs.isEmpty()) {
      return (CommitResultEnvelope) ImmutableCommitResultEnvelope.builder()
          .gid(gid)
          .addMessages(ImmutableMessage.builder()
              .text(new StringBuilder()
                  .append("Commit to: '").append(gid).append("'")
                  .append(" is rejected.")
                  .append(" Your trying to merge objects to non existent head!")
                  .toString())
              .build())
          .status(CommitResultStatus.ERROR)
          .build();
      
    }
    
    
    // Unknown parent
    if(state.getCommit().isEmpty() && commitParent != null) {
      return (CommitResultEnvelope) ImmutableCommitResultEnvelope.builder()
          .gid(gid)
          .addMessages(ImmutableMessage.builder()
              .text(new StringBuilder()
                  .append("Commit to: '").append(gid).append("'")
                  .append(" is rejected.")
                  .append(" Your head is: '").append(commitParent).append("')")
                  .append(" but remote has no head.").append("'!")
                  .toString())
              .build())
          .status(CommitResultStatus.ERROR)
          .build();
      
    }
    
    
    // No parent commit defined for existing head
    if(state.getCommit().isPresent() && commitParent == null && !Boolean.TRUE.equals(parentIsLatest)) {
      return (CommitResultEnvelope) ImmutableCommitResultEnvelope.builder()
          .gid(gid)
          .addMessages(ImmutableMessage.builder()
              .text(new StringBuilder()
                  .append("Parent commit can only be undefined for the first commit!")
                  .append(" Parent commit for:")
                  .append(" '").append(gid).append("'")
                  .append(" is: '").append(state.getCommit().get().getId()).append("'!")
                  .toString())
              .build())
          .status(CommitResultStatus.ERROR)
          .build();
    }
    
    // Wrong parent commit
    if(state.getCommit().isPresent() && commitParent != null && 
        !commitParent.equals(state.getCommit().get().getId()) &&
        !Boolean.TRUE.equals(parentIsLatest)) {
      
      final var text = new StringBuilder()
        .append("Commit to: '").append(gid).append("'")
        .append(" is rejected.")
        .append(" Your head is: '").append(commitParent).append("')")
        .append(" but remote is: '").append(state.getCommit().get().getId()).append("'!")
        .toString();
      
      return ImmutableCommitResultEnvelope.builder()
          .gid(gid)
          .addMessages(ImmutableMessage.builder().text(text).build())
          .status(CommitResultStatus.ERROR)
          .build();
    }

    return null;
  }
  
  private static CommitResultStatus visitStatus(BatchStatus src) {
    if(src == BatchStatus.OK) {
      return CommitResultStatus.OK;
    } else if(src == BatchStatus.CONFLICT) {
      return CommitResultStatus.CONFLICT;
    }
    return CommitResultStatus.ERROR;
    
  }
}
