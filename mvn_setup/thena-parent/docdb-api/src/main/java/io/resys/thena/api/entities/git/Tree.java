package io.resys.thena.api.entities.git;

import java.util.Map;

import org.immutables.value.Value;

import io.resys.thena.api.entities.git.GitEntity.IsGitObject;

// World state 
@Value.Immutable
public
interface Tree extends IsGitObject, GitEntity {
  // resource name - blob id
  Map<String, TreeValue> getValues();
}