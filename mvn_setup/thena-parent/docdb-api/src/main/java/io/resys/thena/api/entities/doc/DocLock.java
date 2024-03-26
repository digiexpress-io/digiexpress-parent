package io.resys.thena.api.entities.doc;

import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.thena.api.entities.CommitLockStatus;

@Value.Immutable
public  
interface DocLock extends DocEntity {
  CommitLockStatus getStatus();
  Optional<Doc> getDoc();
  List<DocBranchLock> getBranches();
  Optional<String> getMessage();
}