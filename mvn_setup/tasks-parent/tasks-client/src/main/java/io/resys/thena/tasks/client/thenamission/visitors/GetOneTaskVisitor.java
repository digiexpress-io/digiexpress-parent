package io.resys.thena.tasks.client.thenamission.visitors;

import java.util.Arrays;
import java.util.List;

import io.resys.thena.api.ThenaClient.GrimStructuredTenant;
import io.resys.thena.api.actions.GrimQueryActions.MissionQuery;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.resys.thena.tasks.client.api.actions.TaskActions.TaskAccessEvaluator;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.thenamission.TaskStoreConfig;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetOneTaskVisitor implements TaskStoreConfig.QueryTasksVisitor<Task> {
  
  private final String id;
  private final TaskAccessEvaluator access;
  
  @Override
  public MissionQuery start(GrimStructuredTenant config, MissionQuery query) {
    return query.addMissionId(Arrays.asList(id));
  }

  @Override
  public List<GrimMissionContainer> visitEnvelope(GrimStructuredTenant config, QueryEnvelopeList<GrimMissionContainer> envelope) {
    
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw TaskException.builder("GET_TASK_BY_ID_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(id))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw TaskException.builder("GET_TASK_BY_ID_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(id))
        .build();
    }
    return result;
  }

  @Override
  public Uni<Task> end(GrimStructuredTenant config, List<GrimMissionContainer> commit) {
    final var task = CreateTasksVisitor.mapToTask(commit.iterator().next());
    return Uni.createFrom().item(task);
  }
}
