package io.resys.thena.api.entities.grim;

import java.time.OffsetDateTime;

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
  String getCreatedWithCommitId();
  String getMissionId();
  String getExternalId();

  String getLinkType();
  @Nullable JsonObject getLinkBody();
  @Nullable GrimMissionLinkTransitives getTransitives();
  
  @Nullable GrimOneOfRelations getRelation(); // one of sub entities
  
  @JsonIgnore @Override default public GrimDocType getDocType() { return GrimDocType.GRIM_MISSION_LINKS; };
  
  
  @Value.Immutable
  interface GrimMissionLinkTransitives {
    OffsetDateTime getCreatedAt(); // Transitive from commit table
    OffsetDateTime getUpdatedAt(); // Transitive from commit table
  }
}