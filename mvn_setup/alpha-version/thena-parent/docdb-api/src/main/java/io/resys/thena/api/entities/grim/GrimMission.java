package io.resys.thena.api.entities.grim;

import java.beans.Transient;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.grim.ThenaGrimObject.IsGrimObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

@Value.Immutable
public interface GrimMission extends IsGrimObject, ThenaTable {
  String getId();
  String getCommitId();
  String getCreatedWithCommitId();
  String getUpdatedTreeWithCommitId();
  
  @Transient @JsonIgnore
  @Nullable GrimMissionTransitives getTransitives();
  
  @Nullable String getParentMissionId();
  @Nullable String getExternalId();
  @Nullable String getMissionStatus();
  @Nullable String getMissionPriority();
  @Nullable String getReporterId();
  @Nullable LocalDate getStartDate();
  @Nullable LocalDate getDueDate();

  @Nullable OffsetDateTime getArchivedAt();
  @Nullable String getArchivedStatus();
  
  @JsonIgnore @Override default public GrimDocType getDocType() { return GrimDocType.GRIM_MISSION; };
  
  @Value.Immutable
  interface GrimMissionTransitives {
    OffsetDateTime getCreatedAt(); // Transitive from commit table
    OffsetDateTime getUpdatedAt(); // Transitive from commit table
    OffsetDateTime getTreeUpdatedAt(); // Transitive from commit table
    String getTitle(); //Transitive from data table
    String getDescription(); //Transitive from data table
    @Nullable JsonObject getDataExtension();
  }
}