package io.digiexpress.eveli.client.spi.task;

import java.util.List;

import io.digiexpress.eveli.client.api.TaskClient;
import io.resys.thena.api.ThenaClient.GrimStructuredTenant;
import io.resys.thena.api.actions.GrimQueryActions.MissionRemarkQuery;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FindAllTaskCommentsByTaskIdVisitor implements TaskStoreConfig.QueryOneTaskCommentsVisitor<List<TaskClient.TaskComment>> {
  private final String missionId;
  
  @Override
  public Uni<QueryEnvelope<GrimMissionContainer>> start(GrimStructuredTenant config, MissionRemarkQuery query) {
    return query.findAllByMissionId(missionId);
  }

  @Override
  public GrimMissionContainer visitEnvelope(GrimStructuredTenant config, QueryEnvelope<GrimMissionContainer> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw TaskException.builder("FIND_ALL_TASK_COMMENTS_BY_TASK_ID_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(missionId))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw TaskException.builder("FIND_ALL_TASK_COMMENTS_BY_TASK_ID_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(missionId))
        .build();
    }
    return result;
  }

  @Override
  public Uni<List<TaskClient.TaskComment>> end(GrimStructuredTenant config, GrimMissionContainer commit) {
    return Uni.createFrom().item(
      commit.getRemarks().values().stream().map(TaskMapper::map).toList()
    );
  }
}
