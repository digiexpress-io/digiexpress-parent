package io.resys.thena.api.entities.doc;

import java.util.Optional;

import org.immutables.value.Value;

import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

@Value.Immutable
public interface DocCommitTree extends ThenaTable {
  String getId();
  String getCommitId();
  String getDocId();
  Optional<String> getBranchId();
  DocCommitTreeOperation getOperationType();
  String getBodyType();
  
  @Nullable JsonArray getBodyPatch();
  @Nullable JsonObject getBodyBefore();
  @Nullable JsonObject getBodyAfter();

  enum DocCommitTreeOperation { ADD, REMOVE, MERGE }
}