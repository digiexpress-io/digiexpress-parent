package io.resys.thena.api.entities.git;

import org.immutables.value.Value;

import io.resys.thena.api.entities.git.GitEntity.IsGitName;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;

// branch with a name
@Value.Immutable
public
interface Branch extends IsGitName, GitEntity, ThenaTable {
  // last commit in the branch
  String getCommit();
}