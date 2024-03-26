package io.resys.thena.api.entities.git;

import java.time.LocalDateTime;
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.thena.api.entities.git.GitEntity.IsGitObject;

@Value.Immutable
public
interface Commit extends IsGitObject, GitEntity {
  String getAuthor();
  LocalDateTime getDateTime();
  String getMessage();
  
  // Parent commit id
  Optional<String> getParent();
  
  // This commit is merge commit, that points to a commit in different branch
  Optional<String> getMerge();
  
  // Tree id that describes list of (resource name - content) entries
  String getTree();
}