package io.digiexpress.eveli.client.spi.task;

import io.digiexpress.eveli.client.api.TaskClient;
import io.resys.thena.api.ThenaClient.GrimStructuredTenant;
import io.resys.thena.api.actions.GrimQueryActions.MissionRemarkQuery;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class GetOneTaskCommentByIdVisitor implements TaskStoreConfig.QueryOneTaskCommentsVisitor<TaskClient.TaskComment> {
  private final String commentId;
  
  @Override
  public Uni<QueryEnvelope<GrimMissionContainer>> start(GrimStructuredTenant config, MissionRemarkQuery query) {
    return query.getOneByRemarkId(commentId);
  }

  @Override
  public GrimMissionContainer visitEnvelope(GrimStructuredTenant config, QueryEnvelope<GrimMissionContainer> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw TaskException.builder("GET_TASK_COMMENT_BY_ID_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(commentId))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null || result.getRemarks().isEmpty()) {
      throw TaskException.builder("GET_TASK_COMMENT_BY_ID_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(commentId))
        .build();
    }
    return result;
  }

  @Override
  public Uni<TaskClient.TaskComment> end(GrimStructuredTenant config, GrimMissionContainer commit) {
    return Uni.createFrom().item(TaskMapper.map(commit.getRemarks().values().iterator().next()));
  }
}
