package io.resys.thena.tasks.client.thenamission.visitors;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.api.ThenaClient.GrimStructuredTenant;
import io.resys.thena.api.actions.GrimQueryActions.GrimArchiveQueryType;
import io.resys.thena.api.actions.GrimQueryActions.MissionQuery;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.resys.thena.tasks.client.api.actions.TaskActions.TaskAccessEvaluator;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.Task.TaskExtensionType;
import io.resys.thena.tasks.client.thenamission.TaskStoreConfig;
import io.resys.thena.tasks.client.thenamission.support.EvaluateTaskAccess;
import io.resys.thena.tasks.client.thenamission.support.TaskException;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FindAllTasksByCustomerVisitor implements TaskStoreConfig.QueryTasksVisitor<List<Task>> {
  private final String customer;
  private final TaskAccessEvaluator access;
  
  @Override
  public MissionQuery start(GrimStructuredTenant config, MissionQuery query) {
    return query
    .addLink(TaskExtensionType.CUSTOMER.name(), customer)
    .archived(GrimArchiveQueryType.ONLY_IN_FORCE);
  }

  @Override
  public List<GrimMissionContainer> visitEnvelope(GrimStructuredTenant config, QueryEnvelopeList<GrimMissionContainer> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw TaskException.builder("GET_TASKS_BY_CUSTOMER_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(Arrays.asList(customer).stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw TaskException.builder("GET_TASKS_BY_CUSTOMER_FAIL")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(Arrays.asList(customer).stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    return result;
  }

  @Override
  public Uni<List<Task>> end(GrimStructuredTenant config, List<GrimMissionContainer> commit) {
    final var access = EvaluateTaskAccess.of(this.access);
    return Uni.createFrom().item(commit.stream()
        .map(CreateTasksVisitor::mapToTask)
        .map(access::isReadAccessGranted)
        .toList());
  }
}
