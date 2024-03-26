package io.resys.thena.api.entities.git;

import org.immutables.value.Value;

import io.resys.thena.api.entities.git.GitEntity.IsGitName;

// branch with a name
@Value.Immutable
public
interface Branch extends IsGitName, GitEntity {
  // last commit in the branch
  String getCommit();
}