package io.resys.thena.api.registry.grim;

import java.time.LocalDate;
import java.util.List;

import org.immutables.value.Value;

import io.resys.thena.api.actions.GrimQueryActions.ArchiveQueryType;
import jakarta.annotation.Nullable;

@Value.Immutable
public interface GrimMissionFilter {
  List<String> getMissionIds();
  List<GrimAssignmentFilter> getAssignments();
  ArchiveQueryType getArchived();

  @Nullable String getReporterId();
  @Nullable String getLikeTitle();
  @Nullable String getLikeDescription();
  
  @Nullable LocalDate getFromCreatedOrUpdated();
  
  
  @Value.Immutable
  interface GrimAssignmentFilter {
    String getAssignmentType();
    String getAssignmentValue(); 
  }
}
