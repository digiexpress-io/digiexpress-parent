package io.resys.thena.structures.doc.support;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.entities.doc.DocLock;
import io.resys.thena.api.entities.doc.ImmutableDoc;
import io.resys.thena.api.entities.doc.ImmutableDocBranch;
import io.resys.thena.api.entities.doc.ImmutableDocCommands;
import io.resys.thena.api.entities.doc.ImmutableDocCommit;
import io.resys.thena.structures.doc.DocInserts.DocBatchForOne;
import io.resys.thena.structures.doc.DocState;
import io.resys.thena.structures.doc.ImmutableDocBatchForOne;
import io.resys.thena.structures.doc.commitlog.DocCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchForOneDocModify {

  private final DocLock docLock;
  private final DocState tx;
  private final String author;
  private final String message;
  private List<JsonObject> commands = null;
  private Optional<String> ownerId;
  private Optional<String> parentId;
  private Optional<String> externalId;
  private Optional<JsonObject> appendMeta;
  private boolean remove;

  public BatchForOneDocModify externalId(Optional<String> externalId) { this.externalId = externalId; return this; }
  public BatchForOneDocModify parentId(Optional<String> parentId) { this.parentId = parentId; return this; }
  public BatchForOneDocModify ownerId(Optional<String> ownerId) { this.ownerId = ownerId; return this; }
  public BatchForOneDocModify remove(boolean remove) { this.remove = remove; return this; }
  public BatchForOneDocModify commands(List<JsonObject> log) { this.commands = log; return this; }
  public BatchForOneDocModify meta(Optional<JsonObject> meta) { this.appendMeta = meta; return this; }
  
  public DocBatchForOne create() {
    RepoAssert.notNull(docLock, () -> "docLock can't be empty!");
    RepoAssert.notNull(tx, () -> "tx to commit, no content!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    
    final var now = OffsetDateTime.now();
    
    final var commitBuilder = new DocCommitBuilder(tx.getTenantId(), ImmutableDocCommit.builder()
        .id(OidUtils.gen())
        .docId(docLock.getDoc().get().getId())
        .createdAt(now)
        .commitAuthor(this.author)
        .commitMessage(this.message)
        .parent(docLock.getDoc().get().getCommitId())
        .commitLog("")
        .build());
    

    final var doc = ImmutableDoc.builder()
      .from(docLock.getDoc().get())
      .meta(appendMeta == null ? docLock.getDoc().get().getMeta() : appendMeta.get())
      .status(remove ? Doc.DocStatus.ARCHIVED : Doc.DocStatus.IN_FORCE)
      .parentId(this.parentId == null ? docLock.getDoc().get().getParentId() : this.parentId.orElse(null))
      .ownerId(this.ownerId == null ? docLock.getDoc().get().getOwnerId() : this.ownerId.orElse(null))
      .externalId(this.externalId == null ? docLock.getDoc().get().getExternalId() : this.externalId.orElse(null))
      .commitId(commitBuilder.getCommitId())
      .build();
    
    commitBuilder.merge(docLock.getDoc().get(), doc);
    
    final var batchBuilder = ImmutableDocBatchForOne.builder();
    
    for(final var lock : docLock.getBranches()) {
      if(remove && lock.getBranch().get().getStatus() == Doc.DocStatus.ARCHIVED) {
        continue;
      }
      
      final var docBranch = ImmutableDocBranch.builder()
        .from(lock.getBranch().get())
        .status(remove ? Doc.DocStatus.ARCHIVED : Doc.DocStatus.IN_FORCE)
        .branchName(remove ? OidUtils.gen(): lock.getBranch().get().getBranchName())
        .build();
      
      batchBuilder.addDocBranch(docBranch);
      commitBuilder.merge(lock.getBranch().get(), docBranch);
    }
  
    final List<DocCommands> docLogs = commands == null ? Collections.emptyList() : Arrays.asList(
        ImmutableDocCommands.builder()
          .id(OidUtils.gen())
          .docId(doc.getId())
          .commitId(commitBuilder.getCommitId())
          .commands(commands)
          .createdAt(now)
          .createdBy(author)
          .build()
        );
    docLogs.forEach(command -> commitBuilder.add(command));
    
    final var commit = commitBuilder.close();
    
    return batchBuilder
        .doc(doc)
        .addDocCommit(commit.getItem1())
        .addAllDocCommitTree(commit.getItem2())
        .addAllDocCommands(docLogs)
        .log(commit.getItem1().getCommitLog())
        .addAllDocLock(docLock.getBranches())
        .build();
  }
  


}
