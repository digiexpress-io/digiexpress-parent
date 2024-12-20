package io.digiexpress.eveli.client.spi.task;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;

import io.digiexpress.eveli.client.api.ImmutableTask;
import io.digiexpress.eveli.client.api.ImmutableTaskComment;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.TaskPriority;
import io.digiexpress.eveli.client.api.TaskClient.TaskStatus;
import io.resys.thena.api.entities.grim.GrimAssignment;
import io.resys.thena.api.entities.grim.GrimMission;
import io.resys.thena.api.entities.grim.GrimRemark;

public class TaskMapper {
  public static final String ASSIGNMENT_TYPE_TASK_USER = "task_user";
  public static final String ASSIGNMENT_TYPE_TASK_ROLE = "task_role";
  public static final String LABEL_TYPE_KEYWORD = "keyword";
  public static final String VIEWER_WORKER = "WORKER";
  public static final String VIEWER_CUSTOMER = "CUSTOMER";
  
  public static final String COMMENT_EXTERNAL = "EXTERNAL";
  public static final String COMMENT_INTERNAL = "INTERNAL";
  
  
  public static ZonedDateTime toZoned(OffsetDateTime input) {
    if(input == null) {
      return null;
    }
    return input.toZonedDateTime();
  }
  

  public static TaskClient.TaskComment map(GrimRemark remark) {
    return ImmutableTaskComment.builder()
        .id(remark.getId())
        .created(TaskMapper.toZoned(remark.getTransitives().getCreatedAt()))
        .commentText(remark.getRemarkText())
        .userName(remark.getTransitives().getCreatedBy())
        .replyToId(remark.getParentId()) // probably bad idea, lazy relations
        .taskId(remark.getMissionId()) // probably bad idea, lazy relations
        .external(COMMENT_EXTERNAL.equals(remark.getRemarkType()))
        .source(TaskClient.TaskCommentSource.valueOf(remark.getRemarkSource()))
        .build();
  }
  
  
  public static TaskClient.Task map(GrimMission commited, Collection<GrimAssignment> assignments) {

    final var assignee = assignments.stream()
      .filter(e -> TaskMapper.ASSIGNMENT_TYPE_TASK_USER.equals(e.getAssignmentType()))
      .findFirst();
    
    final var task = ImmutableTask.builder()
      .version(commited.getCommitId())
      .clientIdentificator(commited.getReporterId())
      .description(commited.getDescription())
      .dueDate(commited.getDueDate())
      .id(commited.getId())

      .completed(TaskMapper.toZoned(commited.getCompletedAt()))
      .created(TaskMapper.toZoned(commited.getTransitives().getCreatedAt()))
      .questionnaireId(commited.getQuestionnaireId())
      .priority(TaskPriority.valueOf(commited.getMissionPriority()))
      .status(TaskStatus.valueOf(commited.getMissionStatus()))
      .subject(commited.getTitle())
      .taskRef(commited.getRefId())
      
      .updated(TaskMapper.toZoned(commited.getTransitives().getTreeUpdatedAt()))
      .updaterId(commited.getTransitives().getTreeUpdatedBy())
      
      .assignedUser(assignee.map(e -> e.getAssignee()).orElse(null))
      .assignedUserEmail(assignee.map(e -> e.getAssigneeContact()).orElse(null))

      .build();
    
    return task;
  }
}
