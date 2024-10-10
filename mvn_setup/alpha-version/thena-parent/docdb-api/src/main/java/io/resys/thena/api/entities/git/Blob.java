package io.resys.thena.api.entities.git;

import org.immutables.value.Value;

import io.resys.thena.api.entities.git.GitEntity.IsGitObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import io.vertx.core.json.JsonObject;

@Value.Immutable
public
interface Blob extends IsGitObject, GitEntity, ThenaTable {
  JsonObject getValue();
}