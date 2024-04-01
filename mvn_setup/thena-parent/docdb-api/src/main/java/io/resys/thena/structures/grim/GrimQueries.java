package io.resys.thena.structures.grim;

import java.util.List;

import io.resys.thena.api.entities.grim.GrimLabel;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.datasource.ThenaDataSource;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface GrimQueries {
  ThenaDataSource getDataSource();
  LabelQuery labels();

  interface LabelQuery {
    Uni<List<GrimLabel>> findAll();    
  }
  
  interface MissionQuery {
    MissionQuery addMissionIdFilter(String missionId); // multiple will be OR
    MissionQuery addAssignmentFilter(String assignmentType, String assignmentValue); // multiple will be OR
    
    Multi<GrimMissionContainer> findAll();
    Uni<GrimMissionContainer> getById(String missionId);
  }
}
