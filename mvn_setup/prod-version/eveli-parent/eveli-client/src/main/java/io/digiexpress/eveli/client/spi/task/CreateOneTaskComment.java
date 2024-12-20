package io.digiexpress.eveli.client.spi.task;

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.CreateTaskCommentCommand;
import io.digiexpress.eveli.client.api.TaskClient.TaskCommentSource;
import io.digiexpress.eveli.client.event.TaskNotificator;
import io.resys.thena.api.ThenaClient.GrimStructuredTenant;
import io.resys.thena.api.actions.GrimCommitActions.ModifyOneMission;
import io.resys.thena.api.actions.GrimCommitActions.OneMissionEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeMission;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateOneTaskComment implements TaskStoreConfig.MergeTaskVisitor<TaskClient.TaskComment> {
  private final String userId;
  private final TaskNotificator notificator;
  private final CreateTaskCommentCommand command;
  
  private String createdRemarkId;
  
  private void createTaskComment(CreateTaskCommentCommand command, MergeMission merge) {
    final var remarkType = Boolean.TRUE.equals(command.getExternal()) ? TaskMapper.COMMENT_EXTERNAL : TaskMapper.COMMENT_INTERNAL;
    final var usedFor = command.getSource() == TaskCommentSource.FRONTDESK ? TaskMapper.VIEWER_WORKER : TaskMapper.VIEWER_CUSTOMER;
    
    merge.addRemark(createComment -> {
      // create new comment
      final var remarkId = createComment
        .remarkText(command.getCommentText())
        .reporterId(userId)
        .remarkType(remarkType)
        .remarkSource(command.getSource().name())
        .parentId(command.getReplyToId())
        .build();
      
      // internally store new comment id
      setRemarkId(remarkId);
    })
    .addViewer(newViewer -> newViewer.userId(userId).usedFor(usedFor).build())
    .build();
  }
  
  private void setRemarkId(String remarkId) {
    this.createdRemarkId = remarkId;
  }

  @Override
  public ModifyOneMission start(GrimStructuredTenant config, ModifyOneMission builder) {
    builder.missionId(command.getTaskId()).modifyMission(merge -> createTaskComment(command, merge));
    return builder
        .commitAuthor(userId)
        .commitMessage("Creating tasks by: " + CreateOneTask.class.getSimpleName());
  }

  @Override
  public OneMissionEnvelope visitEnvelope(GrimStructuredTenant config, OneMissionEnvelope envelope) {
    if(envelope.getStatus() == CommitResultStatus.OK) {
      return envelope;
    }
    throw TaskException.builder("CREATE_TASK_COMMENT_SAVE_FAIL").add(config, envelope).build(); 
  }

  @Override
  public Uni<TaskClient.TaskComment> end(GrimStructuredTenant config, OneMissionEnvelope commited) {
    final var createdRemark = commited.getRemarks().stream()
        .filter(r -> r.getId().equals(createdRemarkId))
        .findFirst().get();
    
    final var comment = TaskMapper.map(createdRemark);
    final var task = TaskMapper.map(commited.getMission(), commited.getAssignments(), commited.getRemarks());
    
    if (comment.getExternal()) {
      notificator.sendNewCommentNotificationToClient(comment, task);
    }
    return Uni.createFrom().item(comment);
  }
}
