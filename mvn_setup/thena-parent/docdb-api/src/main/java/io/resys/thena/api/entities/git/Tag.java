package io.resys.thena.api.entities.git;

import java.time.LocalDateTime;

import org.immutables.value.Value;

import io.resys.thena.api.entities.git.GitEntity.IsGitName;

@Value.Immutable
public
interface Tag extends IsGitName, GitEntity {
  // id of a commit
  String getCommit();
  LocalDateTime getDateTime();
  String getAuthor();
  String getMessage();
}