package io.resys.thena.api.entities.git;

import java.util.Map;
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.thena.api.entities.CommitLockStatus;

@Value.Immutable
public  
interface CommitLock extends GitEntity {
  CommitLockStatus getStatus();
  Optional<Branch> getBranch();
  Optional<Commit> getCommit();
  Optional<Tree> getTree();
  Map<String, Blob> getBlobs();
  Optional<String> getMessage();
}