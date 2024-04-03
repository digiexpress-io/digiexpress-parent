package io.resys.thena.api.entities.grim;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.grim.ThenaGrimObject.IsGrimObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

@Value.Immutable
public interface GrimMissionLink extends IsGrimObject, ThenaTable {
  String getId();
  String getCommitId();
  String getMissionId();
  String getExternalId();

  String getLinkType();
  @Nullable JsonObject getLinkBody();
  
  @Nullable GrimOneOfRelations getRelation(); // one of sub entities
  
  @JsonIgnore @Override default public GrimDocType getDocType() { return GrimDocType.GRIM_MISSION_LINKS; };
}