package io.resys.thena.tasks.client.spi.visitors;

import java.time.LocalDate;
import java.time.LocalDateTime;

/*-
 * #%L
 * thena-tasks-client
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.resys.thena.docdb.api.actions.BranchActions.BranchObjectsQuery;
import io.resys.thena.docdb.api.actions.ImmutableMatchCriteria;
import io.resys.thena.docdb.api.actions.PullActions.MatchCriteriaType;
import io.resys.thena.docdb.api.models.BlobContainer.BlobVisitor;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.ThenaObjects.BranchObjects;
import io.resys.thena.tasks.client.api.model.Document;
import io.resys.thena.tasks.client.api.model.Export;
import io.resys.thena.tasks.client.api.model.Export.ExportEvent;
import io.resys.thena.tasks.client.api.model.Export.ExportEventType;
import io.resys.thena.tasks.client.api.model.ImmutableExport;
import io.resys.thena.tasks.client.api.model.ImmutableExportEvent;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.TaskCommand;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeTaskStatus;
import io.resys.thena.tasks.client.spi.store.DocumentConfig;
import io.resys.thena.tasks.client.spi.store.DocumentConfig.DocBranchVisitor;
import io.resys.thena.tasks.client.spi.store.DocumentStoreException;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExportVisitor implements DocBranchVisitor<Export>, BlobVisitor<List<ExportEvent>> {
  private final String name;
  private final LocalDate startDate;
  private final LocalDate endDate;
  private final LocalDateTime targetDate;
  private final Map<String, Integer> ids = new HashMap<>();
  
  @Override
  public BranchObjectsQuery start(DocumentConfig config, BranchObjectsQuery builder) {
    return builder.docsIncluded()
        .matchBy(Arrays.asList(ImmutableMatchCriteria.builder()
            .key("documentType").value(Document.DocumentType.TASK.name())
            .type(MatchCriteriaType.EQUALS)
            .build()));
  }
  @Override
  public BranchObjects visitEnvelope(DocumentConfig config, QueryEnvelope<BranchObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocumentStoreException.builder("FIND_ALL_TASKS_FOR_EXPORT_FAIL").add(config, envelope).build();
    }
    return envelope.getObjects();
  }

  @Override
  public Export end(DocumentConfig config, BranchObjects ref) {
    if(ref == null) {
      return visitExport(Collections.emptyList());
    }
   
    return visitExport(ref.accept(this).stream().flatMap(e -> e.stream()).toList());
  }
  
  @Override
  public List<ExportEvent> visit(JsonObject blobValue) {
    final var task = blobValue.mapTo(ImmutableTask.class);
    final var result = new ArrayList<ExportEvent>();
    result.addAll(this.vistTaskCreated(task));
    result.addAll(this.vistTaskActions(task));
    return result;
  }
  
  private Export visitExport(List<ExportEvent> events) {
    return ImmutableExport.builder()
        .startDate(startDate)
        .endDate(endDate)
        .name(name)
        .created(targetDate)
        .id("not implemented yet")
        .hash("not implemented yet")
        .events(events)
        .build();
  }

  
  private List<ExportEvent> vistTaskActions(Task task) {
    final var events = new ArrayList<ExportEvent>();
    final var previous = new ArrayList<TaskCommand>();
    for(final var tx : task.getTransactions()) {
      for(final var command : tx.getCommands()) {
        events.addAll(visitTaskAction(task, command, previous));
        previous.add(command);
      }
    }
    return events;
  }
  
  private List<ExportEvent> visitTaskAction(Task task, TaskCommand command, List<TaskCommand> previous) {
    final var events = new ArrayList<ExportEvent>();
    if(previous.isEmpty()) {
      return events;
    }
    if(command instanceof ChangeTaskStatus) {
      return visitChangeTaskStatus(task, (ChangeTaskStatus) command, previous);
    } 
        
    events.add(ImmutableExportEvent.builder()
        .id(allocateId(task))
        .taskId(task.getId())
        .eventType(ExportEventType.TASK_UPDATED)
        .eventDate(command.getTargetDate().toLocalDate())
        .build());
    return events;
  }
  
  private List<ExportEvent> visitChangeTaskStatus(Task task, ChangeTaskStatus command, List<TaskCommand> previous) {
    final var events = new ArrayList<ExportEvent>();
    events.add(ImmutableExportEvent.builder()
        .id(allocateId(task))
        .taskId(task.getId())
        .eventType(ExportEventType.TASK_STATUS_SET)
        .eventDate(command.getTargetDate().toLocalDate())
        .taskStatus(command.getStatus())
        .build());
    return events;
  }
  
  private List<ExportEvent> vistTaskCreated(Task task) {
    final var events = new ArrayList<ExportEvent>();
    events.add(ImmutableExportEvent.builder()
        .id(allocateId(task))
        .taskId(task.getId())
        .eventType(ExportEventType.TASK_STATUS_SET)
        .eventDate(task.getCreated().toLocalDate())
        .taskStatus(Task.Status.CREATED)
        .build());
    return events;
  }
  
  private String allocateId(Task task) {
    if(ids.containsKey(task.getId())) {
      var old = ids.get(task.getId());
      ids.put(task.getId(), ++ old);
    } else {
      ids.put(task.getId(), 1);
    }

    return ids.get(task.getId()) + "";
  }

}
