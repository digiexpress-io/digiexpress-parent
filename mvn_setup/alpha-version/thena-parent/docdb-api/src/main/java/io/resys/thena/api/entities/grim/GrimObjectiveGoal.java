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
public interface GrimObjectiveGoal extends IsGrimObject, ThenaTable {
  String getId();
  String getCommitId();
  String getCreatedWithCommitId();  
  String getObjectiveId();

  @Transient @JsonIgnore
  @Nullable GrimObjectiveGoalTransitives getTransitives();
  
  @Nullable String getGoalStatus();
  @Nullable LocalDate getStartDate();
  @Nullable LocalDate getDueDate();
  
  
  @JsonIgnore @Override default public GrimDocType getDocType() { return GrimDocType.GRIM_OBJECTIVE_GOAL; };
  
  @Value.Immutable
  interface GrimObjectiveGoalTransitives {
    @Nullable String getMissionId(); // transitive resolved using objective
    @Nullable String getTitle(); //Transitive from data table
    @Nullable String getDescription(); //Transitive from data table
    @Nullable OffsetDateTime getCreatedAt(); // Transitive from commit table
    @Nullable OffsetDateTime getUpdatedAt(); // Transitive from commit table
    @Nullable JsonObject getDataExtension(); // Transitive from data table
  }
}