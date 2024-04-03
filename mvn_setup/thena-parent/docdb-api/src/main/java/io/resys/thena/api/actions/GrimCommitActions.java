package io.resys.thena.api.actions;

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.grim.GrimMission;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeMission;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewGoal;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewMission;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewObjective;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewRemark;
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
    ModifyOneMission modifyMission(Consumer<NewMission> addMission);
    
    ModifyOneMission removeGoal(String goalId);
    ModifyOneMission removeObjective(String objectiveId);
    ModifyOneMission removeRemark(String remarkId);
    
    ModifyOneMission modifyGoal(String goalId, Consumer<NewGoal> goal);
    ModifyOneMission modifyObjective(String objectiveId, Consumer<NewObjective> objective);
    ModifyOneMission modifyRemark(String remarkId, Consumer<NewRemark> objective);
    Uni<OneMissionEnvelope> build();
  }
  
  interface ModifyManyMissions {
    ModifyManyMissions commitAuthor(String author);
    ModifyManyMissions commitMessage(String message);
    ModifyManyMissions modifyMission(String missionId, Consumer<MergeMission> mergeMission);
    
    Uni<ManyMissionsEnvelope> build();
  }
  
  interface CreateManyMissions {
    CreateManyMissions commitAuthor(String author);
    CreateManyMissions commitMessage(String message);
    CreateManyMissions addMission(Consumer<NewMission> addMission);
    Uni<ManyMissionsEnvelope> build();
  }
  
  
  interface CreateOneMission {
    CreateOneMission commitAuthor(String author);
    CreateOneMission commitMessage(String message);
    CreateOneMission mission(Consumer<NewMission> addMission);
    Uni<OneMissionEnvelope> build();
  }

  @Value.Immutable
  interface ManyMissionsEnvelope extends ThenaEnvelope {
    String getRepoId();
    CommitResultStatus getStatus();
    List<Message> getMessages();
    @Nullable String getLog();
    @Nullable List<GrimMission> getMissions();
  }
  @Value.Immutable
  interface OneMissionEnvelope extends ThenaEnvelope {
    String getRepoId();
    CommitResultStatus getStatus();
    List<Message> getMessages();
    @Nullable GrimMission getMission();
  }
}
