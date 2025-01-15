package io.digiexpress.eveli.client.spi.task;

import java.util.Optional;

import io.digiexpress.eveli.client.api.TaskClient.TaskDiff;
import io.resys.thena.api.ThenaClient.GrimStructuredTenant;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimContainerVersion;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.jsonpatch.JsonPatch;
import io.resys.thena.jsonpatch.JsonPatch.JsonPatchOp;
import io.resys.thena.jsonpatch.model.PatchType;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TaskDiffVisitor {
  
  private final TaskStore ctx;
  private final String taskId;
  private final String commitId;
  
  public Uni<TaskDiff> accept() {
    final var tenantName = ctx.getConfig().getTenantName();
    final var tenant = ctx.getConfig().getClient().grim(tenantName);
    return tenant.find().commitQuery().findCommit(taskId, commitId)
        .onItem().transform(resp -> visitVersion(resp, tenant));
  }
  
  private TaskDiff visitVersion(QueryEnvelope<GrimContainerVersion> envelope, GrimStructuredTenant config) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw TaskException.builder("GET_TASK_VERSION_BY_ID_AND_COMMIT")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(taskId, commitId))
        .build();
    }
    
    final var previous = Optional.ofNullable(envelope.getObjects().getParentVersion())
      .map(TaskMapper::map)
      .map(e -> JsonObject.mapFrom(e))
      .orElse(null);
    final var next = Optional.ofNullable(envelope.getObjects().getCurrentVersion())
      .map(TaskMapper::map)
      .map(e -> JsonObject.mapFrom(e))
      .orElse(null);
    
    final var diff = JsonPatch.diff(previous, next);
    visitPatch(diff);
    
    
    return null;
  }
  
  public void visitPatch(JsonPatch patch) {
    final var operations = patch.getStruct();
    operations.forEach(op -> visitOperation(op));
  }
  
  public void visitOperation(JsonPatchOp operation) {
    System.out.println(operation.getRaw());
    System.out.println(operation.getValueType());
  }
}
