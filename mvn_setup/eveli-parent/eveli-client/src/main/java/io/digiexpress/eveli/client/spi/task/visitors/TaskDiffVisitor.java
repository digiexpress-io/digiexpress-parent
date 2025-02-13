package io.digiexpress.eveli.client.spi.task.visitors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Optional;

import io.digiexpress.eveli.client.api.ImmutableTaskDiff;
import io.digiexpress.eveli.client.api.ImmutableTaskDiffValue;
import io.digiexpress.eveli.client.api.TaskClient.TaskDiff;
import io.digiexpress.eveli.client.api.TaskClient.TaskDiffValue;
import io.digiexpress.eveli.client.spi.task.TaskException;
import io.digiexpress.eveli.client.spi.task.TaskMapper;
import io.digiexpress.eveli.client.spi.task.TaskStore;
import io.resys.thena.api.ThenaClient.GrimStructuredTenant;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimContainerVersion;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.jsonpatch.JsonPatch;
import io.resys.thena.jsonpatch.JsonPatch.JsonPatchOp;
import io.resys.thena.jsonpatch.JsonPatch.JsonPatchValueType;
import io.resys.thena.jsonpatch.model.PatchType;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TaskDiffVisitor {
  
  private final TaskStore ctx;
  private final String taskId;
  private final String commitId;
  private final ImmutableTaskDiff.Builder diff = ImmutableTaskDiff.builder();
  private final StringBuilder diffLog = new StringBuilder();
  
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
    
    final var task = Optional.ofNullable(envelope.getObjects().getCurrentVersion()).map(TaskMapper::map);
    final var previous = Optional.ofNullable(envelope.getObjects().getParentVersion())
      .map(TaskMapper::map)
      .map(e -> JsonObject.mapFrom(e))
      .orElse(null);
    final var next = 
      task
      .map(e -> JsonObject.mapFrom(e))
      .orElse(null);
    
    final var diff = JsonPatch.diff(previous, next);
    visitPatch(diff);
    
    return this.diff
        .task(task.orElse(null))
        .taskId(taskId)
        .version(commitId)
        .log(diffLog.toString())
        .build();
  }
  
  public void visitPatch(JsonPatch patch) {
    final var operations = patch.getStruct();
    operations.forEach(op -> visitOperation(op));
  }
  
  public void visitOperation(JsonPatchOp operation) {
    final var path = operation.getPath();
    if(path.isEmpty()) {
      diff.addValues(visitDiffObject(operation));
    } else if(operation.getValueType() == JsonPatchValueType.OBJECT) {
      diff.addAllValues(visitDiffObjectChildren(operation));
    } else {
      diff.addAllValues(visitDiffObjectField(operation));  
    }
  }
  
  
  private TaskDiffValue visitDiffObject(JsonPatchOp operation) {
    diffLog
      .append(System.lineSeparator())
      .append("object created: ").append(System.lineSeparator())
      .append("  op: ").append(operation.getOp()).append(System.lineSeparator())
      .append("  path: ").append(operation.getPath()).append(System.lineSeparator());
    
    final var root = ImmutableTaskDiffValue.builder()
      .op(operation.getOp())
      .path(operation.getPath())
      .raw(operation.getRaw())
      .build();
    return root;
  }
  
  private List<TaskDiffValue> visitDiffObjectField(JsonPatchOp operation) {
    final var from = Optional.ofNullable(operation.getRaw().getMap().get(PatchType.NAMES_FROM_VALUE))
        .map(e -> e.toString()).orElse("");
    
    final var to = Optional.ofNullable(operation.getRaw().getMap().get(PatchType.NAMES_VALUE))
        .map(e -> e.toString()).orElse("");
    final var path = operation.getPath();
    
    diffLog
      .append(System.lineSeparator())
      .append("field modified: ").append(System.lineSeparator())
      .append("  op: ").append(operation.getOp()).append(System.lineSeparator())
      .append("  path: ").append(operation.getPath()).append(System.lineSeparator())
      .append("  from: ").append(from).append(System.lineSeparator())
      .append("  to: ").append(to).append(System.lineSeparator())

    
      .append("field modified(field path): ").append(System.lineSeparator())
      .append("  op: ").append(operation.getOp()).append(System.lineSeparator())
      .append("  path: ").append(path + "/" + to).append(System.lineSeparator())
      .append("  from: ").append(from).append(System.lineSeparator())
      .append("  to: ").append(to).append(System.lineSeparator());
    

    final String value = Optional.ofNullable(operation.getValue()).map(e -> e.toString()).orElse(null);
    return Arrays.asList(
        ImmutableTaskDiffValue.builder()
          .op(operation.getOp())
          .path(path)
          .value(value)
          .raw(operation.getRaw())
          .build(),
        
        ImmutableTaskDiffValue.builder()
          .op(operation.getOp())
          .path(path + "/" + to)
          .value(value)
          .raw(operation.getRaw())
          .build()
      );
  }
  
  private List<TaskDiffValue> visitDiffObjectChildren(JsonPatchOp operation) {
    final JsonObject value = operation.getValue();
    final List<TaskDiffValue> result = new ArrayList<>();
    value.forEach(entry -> {
      result.add(visitDiffObjectChildField(operation, entry.getKey(), entry.getValue()));
    });
    return Collections.unmodifiableList(result);
  }
  
  private TaskDiffValue visitDiffObjectChildField(JsonPatchOp operation, String key, Object value) {
    final var path = operation.getPath();
    final var nestedKey = path + "/" + key;
    final String nestedValue = Optional.ofNullable(value).map(e -> e.toString()).orElse(null);
    final var nestedPath = nestedKey + "/" + nestedValue;
    
    diffLog
      .append(System.lineSeparator())
      .append("field modified: ").append(System.lineSeparator())
      .append("  op: ").append(operation.getOp()).append(System.lineSeparator())
      .append("  path: ").append(nestedPath).append(System.lineSeparator())
      .append("  to: ").append(nestedValue).append(System.lineSeparator())
      ;
    
    return ImmutableTaskDiffValue.builder()
      .op(operation.getOp())
      .path(nestedPath)
      .value(nestedValue)
      .raw(value)
      .build();
  }
}
