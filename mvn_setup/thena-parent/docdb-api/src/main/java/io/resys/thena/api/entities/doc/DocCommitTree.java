package io.resys.thena.api.entities.doc;

import java.util.Optional;

import org.immutables.value.Value;

import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

@Value.Immutable
public interface DocCommitTree extends ThenaTable {
  String getId();
  String getCommitId();
  String getDocId();
  Optional<String> getBranchName();
  DocCommitTreeOperation getOperationType();

  @Nullable JsonObject getBodyPatch();
  @Nullable JsonObject getBodyValue();

  enum DocCommitTreeOperation { ADD, REMOVE, MERGE }
}