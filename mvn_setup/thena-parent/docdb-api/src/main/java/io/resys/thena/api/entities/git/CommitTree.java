package io.resys.thena.api.entities.git;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.annotation.Nullable;

import org.immutables.value.Value;

@Value.Immutable
public 
interface CommitTree extends GitEntity {
  String getCommitId();
  
  String getCommitAuthor();
  LocalDateTime getCommitDateTime();
  String getCommitMessage();
  @Nullable String getCommitParent();
  @Nullable String getCommitMerge();
  
  String getBranchName();
  String getTreeId();
  Optional<TreeValue> getTreeValue();
  
  // Only if loaded
  Optional<Blob> getBlob();
}