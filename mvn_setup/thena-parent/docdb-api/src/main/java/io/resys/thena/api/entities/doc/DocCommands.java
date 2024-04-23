package io.resys.thena.api.entities.doc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import io.vertx.core.json.JsonObject;

@Value.Immutable
public interface DocCommands extends DocEntity, ThenaTable {
  String getId();
  String getCommitId();
  
  String getDocId();
  Optional<String> getBranchName();
  OffsetDateTime getCreatedAt(); // transitive from commit

  List<JsonObject> getCommands();
  
}