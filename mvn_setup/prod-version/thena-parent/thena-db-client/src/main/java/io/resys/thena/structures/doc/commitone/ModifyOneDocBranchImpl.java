package io.resys.thena.structures.doc.commitone;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import java.time.Duration;
import java.util.List;

import io.resys.thena.api.actions.DocCommitActions.ModifyOneDocBranch;
import io.resys.thena.api.actions.DocCommitActions.OneDocEnvelope;
import io.resys.thena.api.actions.GitCommitActions.JsonObjectMerge;
import io.resys.thena.api.actions.ImmutableOneDocEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.doc.DocLock.DocBranchLock;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.spi.DbState;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.doc.DocInserts.DocBatchForMany;
import io.resys.thena.structures.doc.DocState;
import io.resys.thena.structures.doc.ImmutableDocBatchForMany;
import io.resys.thena.structures.doc.ImmutableDocBranchLockCriteria;
import io.resys.thena.structures.doc.actions.DocObjectsQueryImpl;
import io.resys.thena.structures.doc.support.BatchForOneBranchModify;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;


@RequiredArgsConstructor
@Setter @Accessors(fluent = true)
public class ModifyOneDocBranchImpl implements ModifyOneDocBranch {

  private final DbState state;
  private JsonObject replace = null;
  private List<JsonObject> commands = null;
  private JsonObjectMerge merge = null;
  private String commit = null; //versionToModify
  
  private String docId;
  private final String repoId;
  private String branchName = DocObjectsQueryImpl.BRANCH_MAIN;
  private String commitAuthor;
  private String commitMessage;
  private boolean remove;
  private Boolean excludeBranchContentFromLog;

  @Override
  public ModifyOneDocBranchImpl commitLogExcludesBranchBody() {
    excludeBranchContentFromLog = Boolean.TRUE;
    return this;
  }
  @Override
  public ModifyOneDocBranchImpl parentIsLatest() {
    this.commit = null;
    return this;
  }
  @Override
  public ModifyOneDocBranchImpl remove() {
    this.remove = true;
    return this;
  }
  
  @Override
  public Uni<OneDocEnvelope> build() {
    RepoAssert.notEmpty(branchName, () -> "branchName can't be empty!");
    RepoAssert.notEmpty(docId, () -> "docId can't be empty!");
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(commitAuthor, () -> "author can't be empty!");
    RepoAssert.notEmpty(commitMessage, () -> "message can't be empty!");
    RepoAssert.isTrue(replace != null || merge != null, () -> "Nothing to commit, no content!");
    
    final var crit = ImmutableDocBranchLockCriteria.builder()
        .branchName(branchName)
        .docId(docId)
        .build();
    final var scope = ImmutableTxScope.builder().commitAuthor(commitAuthor).commitMessage(commitMessage).tenantId(repoId).build();
    return this.state.withDocTransaction(scope, tx -> tx.query().branches().getBranchLock(crit).onItem().transformToUni(lock -> {
      final OneDocEnvelope validation = validateRepo(lock);
      if(validation != null) {
        return Uni.createFrom().item(validation);
      }
      return doInLock(lock, tx);
    }))
    .onFailure(err -> this.state.getDataSource().isLocked(err)).retry()
      .withJitter(0.3) // every retry increase time by x 3
      .withBackOff(Duration.ofMillis(100))
      .atMost(100);
  }
  
  private OneDocEnvelope validateRepo(DocBranchLock state) {
    // Wrong parent commit
    if(state.getCommit().isPresent() && commit != null && 
        !commit.equals(state.getCommit().get().getId())) {
      
      final var text = new StringBuilder()
        .append("Commit to: '").append(repoId).append("'")
        .append(" is rejected.")
        .append(" Your head is: '").append(commit).append("')")
        .append(" but remote is: '").append(state.getCommit().get().getId()).append("'!")
        .toString();
      
      return ImmutableOneDocEnvelope.builder()
          .repoId(repoId)
          .addMessages(ImmutableMessage.builder().text(text).build())
          .status(CommitResultStatus.ERROR)
          .build();
    }

    
    // cant merge on first commit
    if(state.getBranch().isEmpty()) {
      return ImmutableOneDocEnvelope.builder()
          .repoId(repoId)
          .addMessages(ImmutableMessage.builder()
              .text(new StringBuilder()
                .append("Commit to: '").append(repoId).append("'")
                .append(" is rejected.")
                .append(" Unknown branch: '").append(branchName).append("'!")
                .toString())
              .build())
          .status(CommitResultStatus.ERROR)
          .build();
      
    }
    return null;
  }

  private Uni<OneDocEnvelope> doInLock(DocBranchLock lock, DocState tx) {  
    final var batch = new BatchForOneBranchModify(lock, tx, commitAuthor, commitMessage, excludeBranchContentFromLog)
      .replace(replace)
      .merge(merge)
      .commands(commands)
      .remove(remove)
      .create();

    return tx.insert().batchMany(ImmutableDocBatchForMany.builder().addItems(batch).repo(repoId).status(BatchStatus.OK).log("").build())
      .onItem().transform(rsp -> {
        
        if(rsp.getStatus() == BatchStatus.CONFLICT || rsp.getStatus() == BatchStatus.ERROR) {
          throw new ModifyOneDocBranchException("Failed to modify document branch!", rsp);
        }
        return ImmutableOneDocEnvelope.builder()
          .repoId(repoId)
          .doc(batch.getDoc().get())
          .commit(batch.getDocCommit().iterator().next())
          .branch(batch.getDocBranch().iterator().next())
          .commands(batch.getDocCommands())
          .commitTree(batch.getDocCommitTree())
          .addMessages(ImmutableMessage.builder().text(rsp.getLog()).build())
          .addAllMessages(rsp.getMessages())
          .status(BatchStatus.mapStatus(rsp.getStatus()))
          .build();
      });
  }
  
  public static class ModifyOneDocBranchException extends RuntimeException {
    private static final long serialVersionUID = -6202574733069488724L;
    private final DocBatchForMany batch;
    public ModifyOneDocBranchException(String message, DocBatchForMany batch) {
      super(message);
      this.batch = batch;
    }
    public DocBatchForMany getBatch() {
      return batch;
    }
  }
}
