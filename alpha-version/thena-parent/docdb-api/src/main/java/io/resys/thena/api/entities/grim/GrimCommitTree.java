package io.resys.thena.api.entities.grim;

import org.immutables.value.Value;

import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

@Value.Immutable
public interface GrimCommitTree extends ThenaTable {
  String getId();
  String getCommitId();
  @Nullable String getMissionId();  //transitive
  GrimCommitTreeOperation getOperationType();
  
  @Nullable JsonObject getBodyBefore();
  @Nullable JsonObject getBodyAfter();
  
  
  public enum GrimCommitTreeOperation {
    ADD, REMOVE, MERGE
  }
}