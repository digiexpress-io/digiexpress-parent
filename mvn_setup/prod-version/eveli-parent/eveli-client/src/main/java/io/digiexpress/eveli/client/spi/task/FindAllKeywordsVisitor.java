package io.digiexpress.eveli.client.spi.task;

import java.util.Arrays;
import java.util.List;

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.Task;
import io.resys.thena.api.ThenaClient.GrimStructuredTenant;
import io.resys.thena.api.actions.GrimQueryActions.MissionQuery;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FindAllKeywordsVisitor implements TaskStoreConfig.QueryTasksVisitor<TaskClient.Task> {
  private final String taskId;
  
  @Override
  public MissionQuery start(GrimStructuredTenant config, MissionQuery query) {
    return query.;
  }

  @Override
  public  List<GrimMissionContainer> visitEnvelope(GrimStructuredTenant config, QueryEnvelopeList<GrimMissionContainer> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw TaskException.builder("GET_TASKS_BY_ID_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(taskId))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null || result.isEmpty()) {
      throw TaskException.builder("GET_TASKS_BY_ID_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(taskId))
        .build();
    }
    return result;
  }

  @Override
  public Uni<Task> end(GrimStructuredTenant config, List<GrimMissionContainer> commit) {
    final var container = commit.iterator().next();
    return Uni.createFrom().item(TaskMapper.map(container.getMission(), container.getAssignments().values()));
  }
}
