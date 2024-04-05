package io.resys.thena.structures.grim;

import java.util.Collection;

import io.resys.thena.api.entities.grim.GrimAnyObject;
import io.resys.thena.api.entities.grim.GrimCommitViewer;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.api.registry.grim.GrimCommitViewerRegistry.AnyObjectCriteria;
import io.resys.thena.datasource.ThenaDataSource;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface GrimQueries {
  ThenaDataSource getDataSource();
  MissionQuery missions();
  CommitViewerQuery commitViewer();
  
  interface CommitViewerQuery {
    Multi<GrimAnyObject> findAnyObjects(Collection<AnyObjectCriteria> commits);
    Multi<GrimCommitViewer> findAllViewersByUsed(String userId, String usedBy, Collection<String> commits);
  }

  
  interface MissionQuery {
    MissionQuery viewer(String userId, String usedBy);
    MissionQuery excludeDocs(GrimDocType ...docs); // multiple will be OR
    MissionQuery missionId(String ...missionId); // multiple will be OR
    MissionQuery addAssignment(String assignmentType, String assignmentValue); // multiple will be OR
    
    Multi<GrimMissionContainer> findAll();
    Uni<GrimMissionContainer> getById(String missionId);
  }
}
