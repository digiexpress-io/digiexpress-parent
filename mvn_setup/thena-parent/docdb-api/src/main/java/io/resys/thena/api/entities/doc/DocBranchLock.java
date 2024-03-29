package io.resys.thena.api.entities.doc;

import java.util.Optional;

import org.immutables.value.Value;

import io.resys.thena.api.entities.CommitLockStatus;

@Value.Immutable
public interface DocBranchLock extends DocEntity {
  CommitLockStatus getStatus();
  Optional<Doc> getDoc();
  Optional<DocBranch> getBranch();
  Optional<DocCommit> getCommit();
  Optional<String> getMessage();
}