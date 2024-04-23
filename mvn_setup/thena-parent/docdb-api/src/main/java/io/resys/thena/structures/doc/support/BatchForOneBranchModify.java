package io.resys.thena.structures.doc.support;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.api.actions.GitCommitActions.JsonObjectMerge;
import io.resys.thena.api.actions.ImmutableManyDocsEnvelope;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.entities.doc.DocLock.DocBranchLock;
import io.resys.thena.api.entities.doc.ImmutableDocBranch;
import io.resys.thena.api.entities.doc.ImmutableDocCommands;
import io.resys.thena.api.entities.doc.ImmutableDocCommit;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.doc.DocInserts.DocBatchForMany;
import io.resys.thena.structures.doc.DocInserts.DocBatchForOne;
import io.resys.thena.structures.doc.DocState;
import io.resys.thena.structures.doc.ImmutableDocBatchForOne;
import io.resys.thena.structures.doc.commitlog.DocCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchForOneBranchModify {

  private final DocBranchLock lock; 
  private final DocState tx;
  private final String author;
  private final String message;
  
  private JsonObject appendBlobs;
  private List<JsonObject> commands;
  private JsonObjectMerge appendMerge;
  
  
  private boolean remove;

  public BatchForOneBranchModify remove(boolean remove) { this.remove = remove; return this; }
  public BatchForOneBranchModify replace(JsonObject append) { this.appendBlobs = append; return this; }
  public BatchForOneBranchModify merge(JsonObjectMerge merge) { this.appendMerge = merge; return this; }
  public BatchForOneBranchModify commands(List<JsonObject> doc) { this.commands = doc; return this; }
  

  public DocBatchForOne create() {
    RepoAssert.notNull(lock, () -> "lock can't be null!");
    RepoAssert.notNull(tx, () -> "repo can't be null!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.isTrue(appendBlobs != null || appendMerge != null, () -> "nothing to commit, no content!");
    
    final var branchId = lock.getBranch().get().getId();
    final var doc = lock.getDoc().get();
    
    final var commitBuilder = new DocCommitBuilder(tx.getTenantId(), ImmutableDocCommit.builder()
        .id(OidUtils.gen())
        .docId(doc.getId())
        .branchId(branchId)
        .createdAt(OffsetDateTime.now())
        .commitAuthor(this.author)
        .commitMessage(this.message)
        .parent(lock.getBranch().get().getCommitId())
        .build());
    

    final var docBranch = ImmutableDocBranch.builder()
      .from(lock.getBranch().get())
      .value(appendBlobs)
      .commitId(commitBuilder.getCommitId())
      .status(remove ? Doc.DocStatus.ARCHIVED : Doc.DocStatus.IN_FORCE)
      .value(appendBlobs == null ? appendMerge.apply(lock.getBranch().get().getValue()): appendBlobs)
      .build();
    commitBuilder.merge(lock.getBranch().get(), docBranch);
    
    final List<DocCommands> docLogs = commands == null ? Collections.emptyList() : Arrays.asList(
        ImmutableDocCommands.builder()
          .id(OidUtils.gen())
          .docId(doc.getId())
          .branchId(branchId)
          .commitId(commitBuilder.getCommitId())
          .commands(commands)
          .build()
        );
    docLogs.forEach(command -> commitBuilder.add(command));

    final var commit = commitBuilder.close();
    return ImmutableDocBatchForOne.builder()
      .doc(doc)
      .addDocBranch(docBranch)
      .addDocCommit(commit.getItem1())
      .addAllDocCommitTree(commit.getItem2())
      .addAllDocCommands(docLogs)
      .addDocLock(lock)
      .log(commit.getItem1().getCommitLog())
      .build();
  }
  
  public static ManyDocsEnvelope mapTo(DocBatchForMany rsp) {
    return ImmutableManyDocsEnvelope.builder()
    .repoId(rsp.getRepo())
    .doc(rsp.getItems().stream()
        .filter(i -> i.getDoc().isPresent())
        .map(i -> i.getDoc().get())
        .collect(Collectors.toList()))
    .commits(rsp.getItems().stream()
        .flatMap(i -> i.getDocCommit().stream())
        .collect(Collectors.toList()))
    .commitTree(rsp.getItems().stream()
        .flatMap(i -> i.getDocCommitTree().stream())
        .collect(Collectors.toList()))
    
    .branch(rsp.getItems().stream()
        .flatMap(i -> i.getDocBranch().stream())
        .collect(Collectors.toList()))
    .addAllCommands(rsp.getItems().stream()
        .flatMap(i -> i.getDocCommands().stream())
        .collect(Collectors.toList()))
    .addAllMessages(rsp.getItems().stream()
        .flatMap(i -> i.getMessages().stream())
        .collect(Collectors.toList()))
    
    .status(BatchStatus.mapStatus(rsp.getStatus()))
    .build();
  }
}
