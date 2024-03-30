package io.resys.thena.api.actions;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.grim.ThenaGrimChanges.GoalChanges;
import io.resys.thena.api.entities.grim.ThenaGrimChanges.MissionChanges;
import io.resys.thena.api.entities.grim.ThenaGrimChanges.ObjectiveChanges;
import io.resys.thena.api.entities.grim.ThenaGrimChanges.RemarkChanges;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.api.envelope.ThenaEnvelope;
import io.smallrye.mutiny.Uni;



public interface GrimCommitActions {
  
  CreateOneMission createOneMission();
  CreateManyMissions createManyMissions();
  
  ModifyOneMission modifyOneMission();
  ModifyManyMissions modifyManyMission();  
  
  
  interface ModifyOneMission {
    ModifyOneMission commitAuthor(String author);
    ModifyOneMission commitMessage(String message);
    ModifyOneMission commitCommands(List<?> commands);
    ModifyOneMission missionId(String missionId);
    ModifyOneMission modifyMission(Supplier<MissionChanges> addMission);
    
    ModifyOneMission removeGoal(String goalId);
    ModifyOneMission removeObjective(String objectiveId);
    ModifyOneMission removeRemark(String remarkId);
    
    ModifyOneMission modifyGoal(String goalId, Supplier<GoalChanges> goal);
    ModifyOneMission modifyObjective(String objectiveId, Supplier<ObjectiveChanges> objective);
    ModifyOneMission modifyRemark(String remarkId, Supplier<RemarkChanges> objective);
    Uni<OneMissionEnvelope> build();
  }
  
  interface ModifyManyMissions {
    ModifyManyMissions commitAuthor(String author);
    ModifyManyMissions commitMessage(String message);
    ModifyManyMissions commitCommands(List<?> commands);
    ModifyManyMissions modifyMission(String missionId, Supplier<MissionChanges> addMission);
    
    // changes existing
    MissionChanges modifyGoal(String goalId, Supplier<GoalChanges> goal);
    MissionChanges modifyObjective(String objectiveId, Supplier<ObjectiveChanges> objective);
    MissionChanges modifyRemark(String remarkId, Supplier<RemarkChanges> objective);
    
    MissionChanges removeGoal(String goalId);
    MissionChanges removeObjective(String objectiveId);
    MissionChanges removeRemark(String remarkId);
    
    Uni<ManyMissionsEnvelope> build();
  }
  
  interface CreateManyMissions {
    CreateManyMissions commitAuthor(String author);
    CreateManyMissions commitMessage(String message);
    CreateManyMissions commitCommands(List<?> commands);
    CreateManyMissions addMission(Supplier<MissionChanges> addMission);
    Uni<ManyMissionsEnvelope> build();
  }
  
  
  interface CreateOneMission {
    CreateOneMission commitAuthor(String author);
    CreateOneMission commitMessage(String message);
    CreateOneMission commitCommands(List<?> commands);
    CreateOneMission mission(Supplier<MissionChanges> addMission);
    Uni<OneMissionEnvelope> build();
  }

  @Value.Immutable
  interface ManyMissionsEnvelope extends ThenaEnvelope {
    String getRepoId();
    CommitResultStatus getStatus();
    List<Message> getMessages();
    @Nullable List<GrimMissionContainer> getMissions();
  }
  @Value.Immutable
  interface OneMissionEnvelope extends ThenaEnvelope {
    String getRepoId();
    CommitResultStatus getStatus();
    List<Message> getMessages();
    @Nullable GrimMissionContainer getMission();
  }
}
