package io.resys.thena.structures.doc.commitone;

import java.time.OffsetDateTime;

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

import java.util.List;

import io.resys.thena.api.actions.DocCommitActions.CreateOneDoc;
import io.resys.thena.api.actions.DocCommitActions.OneDocEnvelope;
import io.resys.thena.api.actions.ImmutableOneDocEnvelope;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.spi.DbState;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.doc.DocInserts.DocBatchForMany;
import io.resys.thena.structures.doc.DocState;
import io.resys.thena.structures.doc.ImmutableDocBatchForMany;
import io.resys.thena.structures.doc.actions.DocObjectsQueryImpl;
import io.resys.thena.structures.doc.support.BatchForOneDocCreate;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;


@RequiredArgsConstructor
@Setter @Accessors(fluent = true)
public class CreateOneDocImpl implements CreateOneDoc {

  private final DbState state;
  
  private JsonObject branchContent;
  private List<JsonObject> commands;
  private JsonObject meta;

  private String parentDocId;
  private final String repoId;
  private String docId;
  private String externalId;
  private String docType;
  private String branchName = DocObjectsQueryImpl.BRANCH_MAIN;
  private String commitAuthor;
  private String commitMessage;
  private String ownerId;
  private String docName;
  private String docSubStatus;
  private Boolean excludeBranchContentFromLog;
  private OffsetDateTime docStartsAt;
  private OffsetDateTime docEndsAt;

  @Override
  public CreateOneDocImpl commitLogExcludesBranchBody() {
    excludeBranchContentFromLog = Boolean.TRUE;
    return this;
  }
  
  @Override
  public Uni<OneDocEnvelope> build() {
    RepoAssert.notEmpty(branchName, () -> "branchName can't be empty!");
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(commitAuthor, () -> "author can't be empty!");
    RepoAssert.notEmpty(commitMessage, () -> "message can't be empty!");
    RepoAssert.notEmpty(docType, () -> "docType can't be empty!");
    RepoAssert.notNull(branchContent, () -> "Nothing to commit, no content!");
        
    final var scope = ImmutableTxScope.builder().commitAuthor(commitAuthor).commitMessage(commitMessage).tenantId(repoId).build();
    return this.state.withDocTransaction(scope, this::doInTx);
  }
  
  private Uni<OneDocEnvelope> doInTx(DocState tx) {  
    final var batch = new BatchForOneDocCreate(tx.getTenantId(), commitAuthor, commitMessage, excludeBranchContentFromLog)
        .docId(docId)
        .docType(docType)
        .docName(docName)
        .docStartsAt(docStartsAt)
        .docEndsAt(docEndsAt)
        .docSubStatus(docSubStatus)
        .ownerId(ownerId)
        .externalId(externalId)
        .parentDocId(parentDocId)
        .branchName(branchName)
        .commands(commands)
        .meta(meta)
        .branchContent(branchContent)
        .create();

    return tx.insert().batchMany(ImmutableDocBatchForMany.builder()
        .addItems(batch)
        .repo(repoId)
        .status(BatchStatus.OK)
        .log("")
        .build())
      .onItem().transform(rsp -> {
        if(rsp.getStatus() == BatchStatus.CONFLICT || rsp.getStatus() == BatchStatus.ERROR) {
          throw new CreateOneDocException("Failed to create document!", rsp);
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

  
  public static class CreateOneDocException extends RuntimeException {
    private static final long serialVersionUID = -6202574733069488724L;
    private final DocBatchForMany batch;
    public CreateOneDocException(String message, DocBatchForMany batch) {
      super(message);
      this.batch = batch;
    }
    public DocBatchForMany getBatch() {
      return batch;
    }
  }
}
