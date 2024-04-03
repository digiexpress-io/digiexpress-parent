package io.resys.thena.api.entities.grim;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.annotation.Nullable;

public interface ThenaGrimObject {
  interface IsGrimObject extends ThenaGrimObject { String getId(); GrimDocType getDocType(); }

  
  //transient object to resolve one of the connections to objective/goal/remark
  @Value.Immutable
  interface GrimOneOfRelations {
  
    @Nullable String getObjectiveId();
    @Nullable String getRemarkId();
    @Nullable String getObjectiveGoalId();
    GrimRelationType getRelationType();
    
    @JsonIgnore 
    default public String getTargetId() {
      switch (getRelationType()) {
      case GOAL: return getObjectiveGoalId();
      case OBJECTIVE: return getObjectiveId();
      case REMARK: return getRemarkId();
      default: throw new IllegalArgumentException("Unexpected value: " + getRelationType());
      }
    }
  }

  enum GrimRelationType {
    GOAL, OBJECTIVE, REMARK
  }
  
  enum GrimDocType {
    GRIM_MISSION,
    GRIM_MISSION_LINKS,
    GRIM_MISSION_LABEL,
    GRIM_OBJECTIVE,
    GRIM_OBJECTIVE_GOAL,
    GRIM_REMARK,
    GRIM_COMMANDS,

    GRIM_ASSIGNMENT,
    GRIM_MISSION_DATA,
    
    // infra object, tx log
    GRIM_COMMIT_VIEWER,
    GRIM_COMMIT,
  }
}
