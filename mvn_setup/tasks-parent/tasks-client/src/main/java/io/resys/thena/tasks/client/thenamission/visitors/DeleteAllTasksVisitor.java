package io.resys.thena.tasks.client.thenamission.visitors;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.api.ThenaClient.GrimStructuredTenant;
import io.resys.thena.api.actions.GrimQueryActions.MissionQuery;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.resys.thena.tasks.client.api.model.ImmutableArchiveTask;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.thenagit.store.DocumentStoreException;
import io.resys.thena.tasks.client.thenamission.TaskStoreConfig;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DeleteAllTasksVisitor implements TaskStoreConfig.QueryTasksVisitor<List<Task>> {
  private final String userId;
  private final Instant targetDate;
  
  @Override
  public MissionQuery start(GrimStructuredTenant config, MissionQuery query) {
    //query.addAssignment(CreateTasksVisitor.ASSIGNMENT_TYPE_TASK_USER, user) 
    return query;
  }

  @Override
  public List<GrimMissionContainer> visitEnvelope(GrimStructuredTenant config, QueryEnvelopeList<GrimMissionContainer> envelope) {
    /*
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocumentStoreException.builder("GET_TASKS_BY_ASSIGNEES_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(users.stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw DocumentStoreException.builder("GET_TASKS_BY_ASSIGNEES_FAIL")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(users.stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    ImmutableArchiveTask.builder()
    .taskId(taskId)
    .userId(userId)
    .targetDate(targetDate)
    .build()*/
    return null;
  }

  @Override
  public Uni<List<Task>> end(GrimStructuredTenant config, List<GrimMissionContainer> commit) {
    return Uni.createFrom().item(commit.stream().map(CreateTasksVisitor::mapToTask).toList());
  }
}
