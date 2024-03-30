package io.resys.thena.api.entities.grim;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.grim.ThenaGrimObject.IsGrimObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import jakarta.annotation.Nullable;

@Value.Immutable
public interface GrimObjectiveGoal extends IsGrimObject, ThenaTable {
  String getId();
  String getCommitId();
  
  String getObjectiveId();
  String getMissionId(); // transitive resolved using objective 
  
  @Nullable String getGoalStatus();
  
  @JsonIgnore @Override default public GrimDocType getDocType() { return GrimDocType.GRIM_OBJECTIVE_GOAL; };
}