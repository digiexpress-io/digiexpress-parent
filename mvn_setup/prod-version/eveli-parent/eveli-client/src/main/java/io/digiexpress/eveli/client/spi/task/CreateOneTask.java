package io.digiexpress.eveli.client.spi.task;

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.CreateTaskCommand;
import io.digiexpress.eveli.client.api.TaskClient.Task;
import io.digiexpress.eveli.client.api.TaskClient.TaskPriority;
import io.digiexpress.eveli.client.api.TaskClient.TaskStatus;
import io.digiexpress.eveli.client.event.TaskNotificator;
import io.resys.thena.api.ThenaClient.GrimStructuredTenant;
import io.resys.thena.api.actions.GrimCommitActions.CreateOneMission;
import io.resys.thena.api.actions.GrimCommitActions.OneMissionEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewMission;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateOneTask implements TaskStoreConfig.CreateOneTaskVisitor<TaskClient.Task> {
  private final String userId;
  private final TaskNotificator notificator;
  private final CreateTaskCommand command;
  
  private void createTask(CreateTaskCommand commmand, NewMission mission) {
    final var status = commmand.getStatus() == null ? TaskStatus.NEW: commmand.getStatus();
    final var priority = commmand.getPriority() == null ? TaskPriority.NORMAL: commmand.getPriority();
    final var usedFor = commmand.getQuestionnaireId() == null ? TaskMapper.VIEWER_WORKER : TaskMapper.VIEWER_CUSTOMER;
    
    mission
      .reporterId(commmand.getClientIdentificator())
      .title(commmand.getSubject())
      .description(commmand.getDescription())
      .dueDate(commmand.getDueDate())
      .status(status.name())
      .priority(priority.name())
      .questionnaireId(commmand.getQuestionnaireId())
      .addViewer(newViewer -> newViewer.userId(userId).usedFor(usedFor).build());
    
    // add roles
    for(final var role : commmand.getAssignedRoles()) {
      mission
      .addAssignees(newAss -> newAss
        .assignee(role)
        .assignmentType(TaskMapper.ASSIGNMENT_TYPE_TASK_ROLE)
        .build()
      );
    }

    // assign to given user    
    if(commmand.getAssignedUser() != null) {
      mission
      .addAssignees(newAss -> newAss
        .assignee(commmand.getAssignedUser())
        .assigneeContact(commmand.getAssignedUserEmail())
        .assignmentType(TaskMapper.ASSIGNMENT_TYPE_TASK_USER)
        .build()
      );
    }
    
    for(final var keyword : commmand.getKeyWords()) {
      mission.addLabels(newLabel -> newLabel
          .labelType(TaskMapper.LABEL_TYPE_KEYWORD)
          .labelValue(keyword)
          .build());
    }

    mission.build();
  }

  @Override
  public CreateOneMission start(GrimStructuredTenant config, CreateOneMission builder) {
    builder.mission(newMission -> createTask(command, newMission));
    return builder
        .commitAuthor(userId)
        .commitMessage("Creating task by: " + CreateOneTask.class.getSimpleName());
  }

  @Override
  public OneMissionEnvelope visitEnvelope(GrimStructuredTenant config, OneMissionEnvelope envelope) {
    if(envelope.getStatus() == CommitResultStatus.OK) {
      return envelope;
    }
    throw TaskException.builder("CREATE_TASKS_SAVE_FAIL").add(config, envelope).build(); 
  }

  @Override
  public Uni<Task> end(GrimStructuredTenant config, OneMissionEnvelope commited) {
    final var task = TaskMapper.map(commited.getMission(), commited.getAssignments(), commited.getRemarks());
    notificator.handleTaskCreation(task, userId); 
    return Uni.createFrom().item(task);
  }
  
  
}
