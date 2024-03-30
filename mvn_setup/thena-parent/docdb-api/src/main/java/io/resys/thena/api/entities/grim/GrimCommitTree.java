package io.resys.thena.api.entities.grim;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.grim.ThenaGrimObject.IsGrimObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

@Value.Immutable
public interface GrimCommitTree extends IsGrimObject, ThenaTable {
  String getId();
  String getCommitId();
  @Nullable String getMissionId();  //transitive
  @Nullable String getLabelId();    //transitive
  String getCommandType();
  JsonObject getCommandBody();
  
  @JsonIgnore @Override default public GrimDocType getDocType() { return GrimDocType.GRIM_COMMIT_TREE; };
}