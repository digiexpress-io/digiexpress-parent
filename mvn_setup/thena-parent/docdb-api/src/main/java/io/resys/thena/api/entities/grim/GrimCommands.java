package io.resys.thena.api.entities.grim;

import java.time.OffsetDateTime;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.grim.ThenaGrimObject.IsGrimObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import io.vertx.core.json.JsonObject;

@Value.Immutable
public interface GrimCommands extends IsGrimObject, ThenaTable {
  String getId();
  String getCommitId();
  OffsetDateTime getCreatedAt(); // transitive from commit
  String getMissionId();
  List<JsonObject> getCommands();
  
  @JsonIgnore @Override default public GrimDocType getDocType() { return GrimDocType.GRIM_COMMANDS; };
}