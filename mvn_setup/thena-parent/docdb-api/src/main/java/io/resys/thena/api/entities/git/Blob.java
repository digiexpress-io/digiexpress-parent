package io.resys.thena.api.entities.git;

import org.immutables.value.Value;

import io.resys.thena.api.entities.git.GitEntity.IsGitObject;
import io.vertx.core.json.JsonObject;

@Value.Immutable
public
interface Blob extends IsGitObject, GitEntity {
  JsonObject getValue();
}