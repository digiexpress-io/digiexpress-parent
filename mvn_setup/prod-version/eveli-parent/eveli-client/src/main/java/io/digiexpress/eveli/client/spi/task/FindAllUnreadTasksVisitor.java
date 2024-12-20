package io.digiexpress.eveli.client.spi.task;

import java.util.List;

import io.resys.thena.api.ThenaClient.GrimStructuredTenant;
import io.resys.thena.api.actions.GrimQueryActions.MissionQuery;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FindAllUnreadTasksVisitor implements TaskStoreConfig.QueryTasksVisitor<List<String>> {
  private final String userId;
  private final List<String> roles;
  private final String viewerType;
  
  @Override
  public MissionQuery start(GrimStructuredTenant config, MissionQuery query) {
    
    if(!roles.isEmpty()) {
      query.addAssignment(TaskMapper.ASSIGNMENT_TYPE_TASK_ROLE, false, roles);
    }
    return query
        .addAssignment(TaskMapper.ASSIGNMENT_TYPE_TASK_USER, false, userId)
        .notViewed(userId, viewerType)
        .atLeastOneRemarkWithAnyType();

  }

  @Override
  public List<GrimMissionContainer> visitEnvelope(GrimStructuredTenant config, QueryEnvelopeList<GrimMissionContainer> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw TaskException.builder("FIND_ALL_UNREAD_TASKS_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(userId).addArgs(roles.toArray(new String[] {})))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw TaskException.builder("FIND_ALL_UNREAD_TASKS_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(userId).addArgs(roles.toArray(new String[] {})))
        .build();
    }
    return result;
  }

  @Override
  public Uni<List<String>> end(GrimStructuredTenant config, List<GrimMissionContainer> commit) {
    return Uni.createFrom().item(commit.stream().map(e -> e.getMission().getId()).toList());
  }
}
