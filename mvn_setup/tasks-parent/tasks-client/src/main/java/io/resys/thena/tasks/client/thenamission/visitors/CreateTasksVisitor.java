package io.resys.thena.tasks.client.thenamission.visitors;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.resys.thena.api.ThenaClient.GrimStructuredTenant;
import io.resys.thena.api.actions.GrimCommitActions.CreateManyMissions;
import io.resys.thena.api.actions.GrimCommitActions.ManyMissionsEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.grim.GrimMission;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewGoal;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewMission;
import io.resys.thena.tasks.client.api.actions.TaskActions.TaskAccessEvaluator;
import io.resys.thena.tasks.client.api.model.ImmutableChecklist;
import io.resys.thena.tasks.client.api.model.ImmutableChecklistItem;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.ImmutableTaskComment;
import io.resys.thena.tasks.client.api.model.ImmutableTaskExtension;
import io.resys.thena.tasks.client.api.model.ImmutableTaskTransaction;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.Task.ChecklistItem;
import io.resys.thena.tasks.client.api.model.Task.Status;
import io.resys.thena.tasks.client.api.model.TaskCommand;
import io.resys.thena.tasks.client.api.model.TaskCommand.CreateTask;
import io.resys.thena.tasks.client.thenamission.TaskStoreConfig;
import io.resys.thena.tasks.client.thenamission.support.EvaluateTaskAccess;
import io.resys.thena.tasks.client.thenamission.support.TaskException;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;


public class CreateTasksVisitor implements TaskStoreConfig.CreateManyTasksVisitor<Task> {
  private final List<? extends CreateTask> commands;
  private final EvaluateTaskAccess access;
  public static final String ASSIGNMENT_TYPE_TASK_USER = "task_user";
  public static final String ASSIGNMENT_TYPE_GOAL_USER = "goal_user";
  public static final String ASSIGNMENT_TYPE_TASK_ROLE = "task_role";
  public static final String LABEL_TYPE_TASK = "task_label";
  public static final String LINK_TYPE_TASK_EXTENSION = "task_extension";
  
  public static final String LINK_TYPE_TASK_EXTENSION_BODY = "body";
  public static final String LINK_TYPE_TASK_EXTENSION_TYPE = "type";  
  
  public CreateTasksVisitor(List<? extends CreateTask> commands, TaskAccessEvaluator access) {
    super();
    this.commands = commands;
    this.access = EvaluateTaskAccess.of(access);
  }
  
  @Override
  public CreateManyMissions start(GrimStructuredTenant config, CreateManyMissions builder) {
    for(final var command : commands) {
      builder.addMission(newMission -> createTask(command, newMission));
    }
    return builder.commitMessage("Creating tasks by: " + CreateTasksVisitor.class.getSimpleName());
  }

  private void createTask(CreateTask command, NewMission newMission) {
    newMission
    .onNewState(newState -> access.isCreateAccessGranted(CreateTasksVisitor.mapToTask(newState)))
    .addCommands(Arrays.asList(JsonObject.mapFrom(command)))
    .reporterId(command.getReporterId())
    .title(command.getTitle())
    .description(command.getDescription())
    .priority(command.getPriority().name())
    .dueDate(command.getDueDate())
    .startDate(command.getStartDate())
    .status(command.getStatus() == null ? Status.CREATED.name() : command.getStatus().name());
    
    command.getAssigneeIds().forEach(assigneeId -> newMission.addAssignees(newAssignee ->
      newAssignee.assignee(assigneeId).assignmentType(ASSIGNMENT_TYPE_TASK_USER).build()
    ));
    
    command.getRoles().forEach(roleId -> newMission.addAssignees(newAssignee ->
      newAssignee.assignee(roleId).assignmentType(ASSIGNMENT_TYPE_TASK_ROLE).build()
    ));
    
    command.getComments().forEach(comment -> newMission.addRemark(newRemark -> 
      newRemark
      .parentId(comment.getReplyToId())
      .remarkText(comment.getCommentText())
      .reporterId(comment.getUsername())
      .build()
    ));
    
    command.getLabels().forEach(label -> newMission.addLabels(newLabel -> 
      newLabel
      .labelType(LABEL_TYPE_TASK)
      .labelValue(label)
      .build()
    ));
    
    command.getExtensions().forEach(extension -> newMission.addLink(newLink -> 
      newLink
      .linkType(LINK_TYPE_TASK_EXTENSION)
      .linkValue(extension.getName())
      .linkBody(JsonObject.of(
          LINK_TYPE_TASK_EXTENSION_TYPE, extension.getType(),
          LINK_TYPE_TASK_EXTENSION_BODY, extension.getBody()
       ))
      .build()
    ));    

    command.getChecklist().forEach(checklist -> newMission.addObjective(newObjective -> {
        checklist.getItems().forEach(checklistItem -> newObjective.addGoal(newGoal -> createGoal(checklistItem, newGoal)));
        newObjective.title(checklist.getTitle()).build();
      })
    );
    
    newMission.build();
  }
  
  private void createGoal(ChecklistItem item, NewGoal newGoal) {
    item.getAssigneeIds().forEach(assigneeId -> newGoal.addAssignees(newAssignee ->
      newAssignee.assignee(assigneeId).assignmentType(ASSIGNMENT_TYPE_GOAL_USER).build()
    ));
  
    newGoal
    .dueDate(item.getDueDate())
    .title(item.getTitle())
    .status(item.getCompleted().toString())
    .build();
  }
  
  @Override
  public List<GrimMission> visitEnvelope(GrimStructuredTenant config, ManyMissionsEnvelope envelope) {
    if(envelope.getStatus() == CommitResultStatus.OK) {
      return envelope.getMissions();
    }
    throw TaskException.builder("CREATE_TASKS_SAVE_FAIL").add(config, envelope).build(); 
  }

  @Override
  public Uni<List<Task>> end(GrimStructuredTenant config, List<GrimMission> commit) {
    return config.find().missionQuery().addMissionId(commit.stream().map(m -> m.getId()).toList()).findAll()
        .onItem().transformToMulti(items -> Multi.createFrom().items(items.getObjects().stream()))
        .onItem().transform(CreateTasksVisitor::mapToTask)
        .collect().asList()
        ;
  }

  public static Task mapToTask(GrimMissionContainer src) {
    final var mission = src.getMission();
    final var task = ImmutableTask.builder()
    .id(mission.getId())
    .treeVersion(src.getMission().getUpdatedTreeWithCommitId())
    .reporterId(mission.getReporterId())
    .title(mission.getTransitives().getTitle())
    .description(mission.getTransitives().getDescription())
    .version(mission.getCommitId())
    .created(mission.getTransitives().getCreatedAt().toInstant())
    .updated(mission.getTransitives().getUpdatedAt().toInstant())
    .archived(mission.getArchivedAt() == null ? null : mission.getArchivedAt().toInstant())
    .startDate(mission.getStartDate())
    .dueDate(mission.getDueDate())
    .parentId(mission.getParentMissionId())
    .transactions(src.getCommands().values().stream()
        .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
        .map(command -> ImmutableTaskTransaction.builder()
            .id(command.getId())
            .commands(command.getCommands().stream().map(e -> e.mapTo(TaskCommand.class)).toList())
            .build())
        .toList())
   .assigneeIds(src.getAssignments().values().stream()
       .filter(e -> e.getAssignmentType().equals(ASSIGNMENT_TYPE_TASK_USER))
       .map(e -> e.getAssignee())
       .sorted()
       .toList())
   .roles(src.getAssignments().values().stream()
       .filter(e -> e.getAssignmentType().equals(ASSIGNMENT_TYPE_TASK_ROLE))
       .map(e -> e.getAssignee())
       .sorted()
       .toList())
   .status(Task.Status.valueOf(mission.getMissionStatus()))
   .priority(Task.Priority.valueOf(mission.getMissionPriority()))
   
   .labels(src.getMissionLabels().values().stream()
       .filter(e -> e.getLabelType().equals(LABEL_TYPE_TASK))
       .map(e -> e.getLabelValue())
       .sorted()
       .toList())

   .extensions(src.getLinks().values().stream()
       .filter(e -> e.getLinkType().equals(LINK_TYPE_TASK_EXTENSION))
       .map(e -> ImmutableTaskExtension.builder()
           .id(e.getId())
           .name(e.getExternalId())
           .body(e.getLinkBody().getString(LINK_TYPE_TASK_EXTENSION_BODY))
           .type(e.getLinkBody().getString(LINK_TYPE_TASK_EXTENSION_TYPE))
           .created(e.getTransitives().getCreatedAt().toInstant())
           .updated(e.getTransitives().getUpdatedAt().toInstant())
           .build())
       .toList())
   
   .comments(src.getRemarks().values().stream()
       .filter(remark -> remark.getRelation() == null)
       .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
       .map(remark -> ImmutableTaskComment.builder()
           .id(remark.getId())
           .commentText(remark.getRemarkText())
           .created(remark.getCreatedAt().toInstant())
           .username(remark.getReporterId())
           .replyToId(remark.getParentId())
           .build())
       .toList());

    
    final var goals = src.getGoals().values().stream().collect(Collectors.groupingBy(e -> e.getObjectiveId()));

    for(final var objective : src.getObjectives().values()) {
      final var checklist = ImmutableChecklist.builder()
          .id(objective.getId())
          .title(objective.getTransitives().getTitle());

      for(final var goal : Optional.ofNullable(goals.get(objective.getId())).orElse(Collections.emptyList())) {
        checklist.addItems(ImmutableChecklistItem.builder()
            .id(goal.getId())
            .title(goal.getTransitives().getTitle())
            .dueDate(goal.getDueDate())
            .completed(Boolean.parseBoolean(goal.getGoalStatus()))
            .assigneeIds(src.getAssignments().values().stream()
               .filter(e -> e.getAssignmentType().equals(ASSIGNMENT_TYPE_GOAL_USER))
               .filter(e -> e.isMatch(goal.getId()))
               .map(e -> e.getAssignee())
               .sorted()
               .toList()
             )
            .build());
      }
      task.addChecklist(checklist.build());
    }

    return task.build();
  }
}
