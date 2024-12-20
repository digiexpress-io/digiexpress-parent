package io.digiexpress.eveli.client.spi.task;

import java.time.OffsetDateTime;

import io.digiexpress.eveli.client.api.TaskClient;
import io.resys.thena.api.ThenaClient.GrimStructuredTenant;
import io.resys.thena.api.actions.GrimCommitActions.ModifyOneMission;
import io.resys.thena.api.actions.GrimCommitActions.OneMissionEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeMission;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteOneTask implements TaskStoreConfig.MergeTaskVisitor<TaskClient.Task> {
  private final String userId;
  private final String email;
  private final String taskId;
  private TaskClient.Task previousVersion;
  
  public void archiveTasks(MergeMission merge) {
    previousVersion = TaskMapper.map(merge.getCurrentState().getMission(), merge.getCurrentState().getAssignments().values());
    
    merge
    .archivedAt(OffsetDateTime.now())
    
    // change is viewed by worker who deleted it
    .addViewer(viewer -> viewer.userId(userId).usedFor(TaskMapper.VIEWER_WORKER).build())
    
    .build();
  }
  
  @Override
  public ModifyOneMission start(GrimStructuredTenant config, ModifyOneMission builder) {
    builder.missionId(taskId).modifyMission(merge -> archiveTasks(merge));
    return builder
        .commitAuthor(userId)
        .commitMessage("Update task by: " + DeleteOneTask.class.getSimpleName());
  }

  @Override
  public OneMissionEnvelope visitEnvelope(GrimStructuredTenant config, OneMissionEnvelope envelope) {
    if(envelope.getStatus() == CommitResultStatus.OK) {
      return envelope;
    }
    throw TaskException.builder("DELETE_ONE_TASK_FAIL").add(config, envelope).build(); 
  }

  @Override
  public Uni<TaskClient.Task> end(GrimStructuredTenant config, OneMissionEnvelope commited) {
    final var task = TaskMapper.map(commited.getMission(), commited.getAssignments());
    return Uni.createFrom().item(task);
  }
}
