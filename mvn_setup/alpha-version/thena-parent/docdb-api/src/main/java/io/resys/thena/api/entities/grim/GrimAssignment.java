package io.resys.thena.api.entities.grim;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.grim.ThenaGrimObject.IsGrimObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import jakarta.annotation.Nullable;

@Value.Immutable
public interface GrimAssignment extends IsGrimObject, ThenaTable {
  String getId();
  String getCommitId();
  String getMissionId();
  String getAssignee();
  String getAssignmentType();
  @Nullable GrimOneOfRelations getRelation(); // one of sub entities
  
  default boolean isMatch(String targetId) {
    if(targetId == null) {
      return false;
    }
    if(getMissionId().equals(targetId)) {
      return true;
    }
    if(getRelation() == null) {
      return false;
    }
    return getRelation().getTargetId().equals(targetId);
  }
  
  @JsonIgnore @Override default public GrimDocType getDocType() { return GrimDocType.GRIM_ASSIGNMENT; };
}