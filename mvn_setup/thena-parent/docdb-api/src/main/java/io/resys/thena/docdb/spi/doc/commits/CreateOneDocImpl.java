package io.resys.thena.docdb.spi.doc.commits;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.resys.thena.docdb.api.actions.CommitActions.CommitResultStatus;
import io.resys.thena.docdb.api.actions.DocCommitActions.CreateOneDoc;
import io.resys.thena.docdb.api.actions.DocCommitActions.OneDocEnvelope;
import io.resys.thena.docdb.api.actions.ImmutableOneDocEnvelope;
import io.resys.thena.docdb.api.models.ImmutableDoc;
import io.resys.thena.docdb.api.models.ImmutableDocBranch;
import io.resys.thena.docdb.api.models.ImmutableDocCommit;
import io.resys.thena.docdb.api.models.ImmutableDocLog;
import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocLog;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocStatus;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.spi.DocDbState.DocRepo;
import io.resys.thena.docdb.spi.GitDbInserts.BatchStatus;
import io.resys.thena.docdb.spi.ImmutableDocBatch;
import io.resys.thena.docdb.spi.OidUtils;
import io.resys.thena.docdb.spi.git.commits.CommitLogger;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.resys.thena.docdb.spi.support.Sha2;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateOneDocImpl implements CreateOneDoc {

  private final DbState state;
  private JsonObject appendBlobs = null;
  private JsonObject appendLogs = null;
  private JsonObject appendMeta = null;

  private String repoId;
  private String docId;
  private String externalId;
  private String docType;
  private String branchName;
  private String author;
  private String message;

  @Override
  public CreateOneDocImpl repoId(String repoId) {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    this.repoId = repoId;
    return this;
  }
  @Override
  public CreateOneDocImpl branchName(String branchName) {
    RepoAssert.isName(branchName, () -> "branchName has invalid charecters!");
    this.branchName = branchName;
    return this;
  }
  @Override
  public CreateOneDocImpl append(JsonObject blob) {
    RepoAssert.notNull(blob, () -> "blob can't be empty!");
    this.appendBlobs = blob;
    return this;
  }
  @Override
  public CreateOneDocImpl meta(JsonObject blob) {
    RepoAssert.notNull(blob, () -> "merge can't be null!");
    this.appendMeta = blob;
    return this;
  }
  @Override
  public CreateOneDocImpl author(String author) {
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    this.author = author;
    return this;
  }
  @Override
  public CreateOneDocImpl message(String message) {
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    this.message = message;
    return this;
  }
  @Override
  public CreateOneDocImpl docId(String docId) {
    this.docId = docId;
    return this;
  }
  @Override
  public CreateOneDocImpl externalId(String externalId) {
    this.externalId = externalId;
    return this;
  }
  @Override
  public CreateOneDocImpl docType(String docType) {
    this.docType = docType;
    return this;
  }
  @Override
  public CreateOneDocImpl log(JsonObject doc) {
    this.appendLogs = doc;
    return this;
  }
  @Override
  public Uni<OneDocEnvelope> build() {
    RepoAssert.notEmpty(branchName, () -> "branchName can't be empty!");
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(docType, () -> "docType can't be empty!");
    RepoAssert.isTrue(appendBlobs != null, () -> "Nothing to commit, no content!");
        
    return this.state.toDocState().withTransaction(repoId, this::doInTx);
  }
  
  private Uni<OneDocEnvelope> doInTx(DocRepo tx) {  
    final var branchId = OidUtils.gen();
    
    final var doc = ImmutableDoc.builder()
        .id(Optional.ofNullable(docId).orElse(OidUtils.gen()))
        .externalId(Optional.ofNullable(externalId).orElse(OidUtils.gen()))
        .type(docType)
        .status(DocStatus.IN_FORCE)
        .meta(appendMeta)
        .build();
    
    final var template = ImmutableDocCommit.builder()
      .id("commit-template")
      .docId(doc.getId())
      .branchId(branchId)
      .dateTime(LocalDateTime.now())      
      .author(this.author)
      .message(this.message)
      .parent(Optional.empty())
      .build();
    final var commit = ImmutableDocCommit.builder()
      .from(template)
      .id(Sha2.commitId(template))
      .branchId(branchId)
      .build();
    final var docBranch = ImmutableDocBranch.builder()
      .id(branchId)
      .docId(doc.getId())
      .commitId(commit.getId())
      .branchName(branchName)
      .value(appendBlobs)
      .status(DocStatus.IN_FORCE)
      .build();
    
    final List<DocLog> docLogs = appendLogs == null ? Collections.emptyList() : Arrays.asList(
        ImmutableDocLog.builder()
          .id(OidUtils.gen())
          .docId(doc.getId())
          .branchId(branchId)
          .docCommitId(commit.getId())
          .value(appendLogs)
          .build()
        );

    final var logger = new CommitLogger();
    logger
      .append(" | created")
      .append(System.lineSeparator())
      .append("  + doc:        ").append(doc.getId())
      .append(System.lineSeparator())
      .append("  + doc branch: ").append(docBranch.getId())
      .append(System.lineSeparator())
      .append("  + doc commit: ").append(commit.getId())
      .append(System.lineSeparator());
    
    if(!docLogs.isEmpty()) {
      logger
      .append("  + doc log:    ").append(docLogs.stream().findFirst().get().getId())
      .append(System.lineSeparator());
    }

    final var batch = ImmutableDocBatch.builder()
      .repo(tx.getRepo())
      .status(BatchStatus.OK)
      .doc(doc)
      .addDocBranch(docBranch)
      .addDocCommit(commit)
      .addAllDocLogs(docLogs)
      .log(ImmutableMessage.builder().text(logger.toString()).build())
      .build();

    return tx.insert().batch(batch)
      .onItem().transform(rsp -> ImmutableOneDocEnvelope.builder()
        .repoId(repoId)
        .doc(doc)
        .commit(rsp.getDocCommit().iterator().next())
        .branch(docBranch)
        .addMessages(rsp.getLog())
        .addAllMessages(rsp.getMessages())
        .status(mapStatus(rsp.getStatus()))
        .build());
  }
  
  private static CommitResultStatus mapStatus(BatchStatus src) {
    if(src == BatchStatus.OK) {
      return CommitResultStatus.OK;
    } else if(src == BatchStatus.CONFLICT) {
      return CommitResultStatus.CONFLICT;
    }
    return CommitResultStatus.ERROR; 
  }
}
