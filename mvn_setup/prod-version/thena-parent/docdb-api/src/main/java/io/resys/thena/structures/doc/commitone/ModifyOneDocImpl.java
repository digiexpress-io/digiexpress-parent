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
import java.util.Optional;

import io.resys.thena.api.actions.DocCommitActions.ModifyOneDoc;
import io.resys.thena.api.actions.DocCommitActions.OneDocEnvelope;
import io.resys.thena.api.actions.ImmutableOneDocEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.doc.DocLock;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.spi.DbState;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.doc.DocInserts.DocBatchForMany;
import io.resys.thena.structures.doc.DocState;
import io.resys.thena.structures.doc.ImmutableDocBatchForMany;
import io.resys.thena.structures.doc.ImmutableDocLockCriteria;
import io.resys.thena.structures.doc.support.BatchForOneDocModify;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ModifyOneDocImpl implements ModifyOneDoc {

  private final DbState state;
  private final String repoId;
  
  private List<JsonObject> commands;
  private Optional<JsonObject> meta;
  private boolean remove;
  
  private String docId;
  private Optional<String> parentDocId;
  private Optional<String> externalId;
  private Optional<String> ownerId;
  private String author;
  private String message;

  @Override public ModifyOneDocImpl parentDocId(String parentDocId) { this.parentDocId = Optional.ofNullable(parentDocId); return this; }
  @Override public ModifyOneDocImpl externalId(String externalId) { this.externalId = Optional.ofNullable(externalId); return this; }
  @Override public ModifyOneDocImpl remove() { this.remove = true; return this; }
  @Override public ModifyOneDocImpl meta(JsonObject blob) { this.meta = Optional.ofNullable(blob); return this; }
  @Override public ModifyOneDocImpl docId(String docId) { this.docId = docId; return this; }
  @Override public ModifyOneDocImpl ownerId(String ownerId) { this.ownerId = Optional.ofNullable(ownerId); return this; }
  @Override public ModifyOneDocImpl commitAuthor(String author) { this.author = RepoAssert.notEmpty(author, () -> "commitAuthor can't be empty!"); return this; }
  @Override public ModifyOneDocImpl commitMessage(String message) { this.message = RepoAssert.notEmpty(message, () -> "commitMessage can't be empty!"); return this; }
  @Override public ModifyOneDocImpl commands(List<JsonObject> commands) { this.commands = commands; return this; }
  @Override
  public Uni<OneDocEnvelope> build() {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author, () -> "commitAuthor can't be empty!");
    RepoAssert.notEmpty(message, () -> "commitMessage can't be empty!");

    final var crit = ImmutableDocLockCriteria.builder().docId(docId).build();    
    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(repoId).build();
    return this.state.withDocTransaction(scope, tx -> tx.query().branches().getDocLock(crit).onItem().transformToUni(lock -> {
      final OneDocEnvelope validation = validateRepo(lock);
      if(validation != null) {
        return Uni.createFrom().item(validation);
      }
      return doInLock(lock, tx);
    }))
    .onFailure(err -> state.getDataSource().isLocked(err)).retry()
      .withJitter(0.3) // every retry increase time by x 3
      .withBackOff(Duration.ofMillis(100))
      .atMost(100);
  }
  
  private Uni<OneDocEnvelope> doInLock(DocLock docLock, DocState tx) {

    final var batch = new BatchForOneDocModify(docLock, tx, author, message)
        .commands(commands)
        .meta(meta)
        .ownerId(ownerId)
        .parentId(parentDocId)
        .remove(remove)
        .externalId(externalId)
        .create();
    
    return tx.insert().batchMany(ImmutableDocBatchForMany.builder().addItems(batch).repo(repoId).status(BatchStatus.OK).log("").build())
    .onItem().transform(rsp -> {
      
      if(rsp.getStatus() == BatchStatus.CONFLICT || rsp.getStatus() == BatchStatus.ERROR) {
        throw new ModifyOneDocException("Failed to modify document!", rsp);
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
  
  private OneDocEnvelope validateRepo(DocLock state) {
    // cant merge on first commit
    if(state.getDoc().isEmpty()) {
      return ImmutableOneDocEnvelope.builder()
          .repoId(repoId)
          .addMessages(ImmutableMessage.builder()
              .text(new StringBuilder()
                  .append("Commit to: '").append(repoId).append("'")
                  .append(" is rejected.")
                  .append(" Unknown docId: '").append(docId).append("'!")
                  .toString())
              .build())
          .status(CommitResultStatus.ERROR)
          .build();
      
    }
    return null;
  }
  
  
  public static class ModifyOneDocException extends RuntimeException {
    private static final long serialVersionUID = -6202574733069488724L;
    private final DocBatchForMany batch;
    public ModifyOneDocException(String message, DocBatchForMany batch) {
      super(message);
      this.batch = batch;
    }
    public DocBatchForMany getBatch() {
      return batch;
    }
  }
}
