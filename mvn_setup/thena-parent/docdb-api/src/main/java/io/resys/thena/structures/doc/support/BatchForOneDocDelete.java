package io.resys.thena.structures.doc.support;

import java.time.OffsetDateTime;

import io.resys.thena.api.entities.doc.DocLock.DocBranchLock;
import io.resys.thena.api.entities.doc.ImmutableDocCommit;
import io.resys.thena.structures.doc.DocInserts.DocBatchForOne;
import io.resys.thena.structures.doc.DocInserts.DocBatchForOneType;
import io.resys.thena.structures.doc.DocState;
import io.resys.thena.structures.doc.ImmutableDocBatchForOne;
import io.resys.thena.structures.doc.commitlog.DocCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchForOneDocDelete {

  private final DocBranchLock lock; 
  private final DocState tx;
  private final String author;
  private final String message;
  

  public DocBatchForOne create() {
    RepoAssert.notNull(lock, () -> "lock can't be null!");
    RepoAssert.notNull(tx, () -> "repo can't be null!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    
    final var branchId = lock.getBranch().get().getId();
    final var now = OffsetDateTime.now();
    final var commitBuilder = new DocCommitBuilder(tx.getTenantId(), ImmutableDocCommit.builder()
        .id(OidUtils.gen())
        .docId(lock.getDoc().get().getId())
        .branchId(branchId)
        .createdAt(now)
        .commitAuthor(this.author)
        .commitMessage(this.message)
        .parent(lock.getDoc().get().getCommitId())
        .commitLog("")
        .build());
      commitBuilder.rm(lock.getDoc().get());
      
      final var commit = commitBuilder.close();
      return ImmutableDocBatchForOne.builder()
        .type(DocBatchForOneType.DELETE)
        .doc(lock.getDoc().get())
        .addDocLock(lock)
        .log(commit.getItem1().getCommitLog())
        .build();
  }
}
