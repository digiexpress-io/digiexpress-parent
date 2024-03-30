package io.resys.thena.structures.grim;

import java.util.List;
import java.util.Map;

import io.resys.thena.api.entities.grim.GrimAssignment;
import io.resys.thena.api.entities.grim.GrimCommit;
import io.resys.thena.api.entities.grim.GrimCommitTree;
import io.resys.thena.api.entities.grim.GrimCommitViewer;
import io.resys.thena.api.entities.grim.GrimLabel;
import io.resys.thena.api.entities.grim.GrimMission;
import io.resys.thena.api.entities.grim.GrimMissionData;
import io.resys.thena.api.entities.grim.GrimMissionLabel;
import io.resys.thena.api.entities.grim.GrimMissionLink;
import io.resys.thena.api.entities.grim.GrimObjective;
import io.resys.thena.api.entities.grim.GrimObjectiveGoal;
import io.resys.thena.api.entities.grim.GrimRemark;
import io.resys.thena.datasource.ThenaDataSource;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface GrimQueries {
  ThenaDataSource getDataSource();

  
  interface AssignmentQuery {
    Multi<GrimAssignment> findAll();
    Multi<GrimAssignment> findAllMissionId(String missionId);
  }
  interface CommitQuery {
    Multi<GrimCommit> findAll();
    Multi<GrimCommit> findAllMissionId(String missionId);
  }
  interface CommitTreeQuery {
    Multi<GrimCommitTree> findAll();
    Multi<GrimCommitTree> findAllMissionId(String missionId);
  }
  interface CommitViewerQuery {
    Multi<GrimCommitViewer> findAll();
    Multi<GrimCommitViewer> findAllMissionId(String missionId);
    Multi<GrimCommitViewer> findAllByUsedBy(String usedBy);
  }
  interface LabelQuery {
    Multi<GrimLabel> findAll();
    Multi<GrimLabel> findAllMissionId(String missionId);
  }
  interface MissionQuery {
    Multi<GrimMission> findAll();
    Uni<GrimMission> getById(String missionId, Map<String, List<String>> assginments); // assignment type -> assignees
  }
  interface MissionDataQuery {
    Multi<GrimMissionData> findAll();
    Multi<GrimMissionData> findAllMissionId(String missionId);
  }
  interface MissionLabelQuery {
    Multi<GrimMissionLabel> findAll();
    Multi<GrimMissionLabel> findAllMissionId(String missionId);
  }
  interface MissionLinkQuery {
    Multi<GrimMissionLink> findAll();
    Multi<GrimMissionLink> findAllMissionId(String missionId);
  }
  interface ObjectiveQuery {
    Multi<GrimObjective> findAll();
    Multi<GrimObjective> findAllMissionId(String missionId);
  }
  interface ObjectiveGoalQuery {
    Multi<GrimObjectiveGoal> findAll();
    Multi<GrimObjectiveGoal> findAllMissionId(String missionId);
  }
  interface RemarkQuery {
    Multi<GrimRemark> findAll();
    Multi<GrimRemark> findAllMissionId(String missionId);    
  }
}
