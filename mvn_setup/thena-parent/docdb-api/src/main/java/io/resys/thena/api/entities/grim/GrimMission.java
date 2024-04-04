package io.resys.thena.api.entities.grim;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.grim.ThenaGrimObject.IsGrimObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import jakarta.annotation.Nullable;

@Value.Immutable
public interface GrimMission extends IsGrimObject, ThenaTable {
  String getId();
  String getCommitId();
  String getCreatedWithCommitId();
  String getUpdatedTreeWithCommitId();
  
  @Nullable OffsetDateTime getCreatedAt(); // Transitive from commit table
  @Nullable OffsetDateTime getUpdatedAt(); // Transitive from commit table
  @Nullable OffsetDateTime getTreeUpdatedAt(); // Transitive from commit table
  
  @Nullable String getParentMissionId();
  @Nullable String getExternalId();
  @Nullable String getMissionStatus();
  @Nullable String getMissionPriority();
  @Nullable String getReporterId();
  @Nullable LocalDate getStartDate();
  @Nullable LocalDate getDueDate();

  @Nullable LocalDate getArchivedDate();
  @Nullable String getArchivedStatus();
  
  @JsonIgnore @Override default public GrimDocType getDocType() { return GrimDocType.GRIM_MISSION; };
}