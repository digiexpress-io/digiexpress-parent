package io.resys.thena.api.entities.grim;

import java.time.OffsetDateTime;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.grim.ThenaGrimObject.IsGrimObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

@Value.Immutable
public interface GrimMissionData extends IsGrimObject, ThenaTable {
  String getId();
  String getCommitId();
  String getCreatedWithCommitId();
  String getMissionId();

  @Nullable OffsetDateTime getCreatedAt(); // Transitive from commit table
  @Nullable OffsetDateTime getUpdatedAt(); // Transitive from commit table
  
  String getTitle();
  String getDescription();
  
  @Nullable JsonObject getDataExtension();
  @Nullable GrimOneOfRelations getRelation(); // one of sub entities
  
  @JsonIgnore @Override default public GrimDocType getDocType() { return GrimDocType.GRIM_MISSION_DATA; };
}