package io.digiexpress.eveli.client.spi.task.visitors;

import java.io.Serializable;
import java.util.HashMap;

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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.Task;
import io.digiexpress.eveli.client.api.TaskClient.TransferTaskCommand;
import io.digiexpress.eveli.client.api.TaskFileClient;
import io.digiexpress.eveli.client.api.TaskFileClient.TaskFile;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import io.digiexpress.eveli.client.spi.dms.DocContainerClient;
import io.digiexpress.eveli.client.spi.dms.DocContainerEnvelope;
import io.digiexpress.eveli.client.spi.dms.ImmutableDoc;
import io.digiexpress.eveli.client.spi.task.TaskException;
import io.digiexpress.eveli.client.spi.task.TaskMapper;
import io.digiexpress.eveli.client.spi.task.TaskStore;
import io.resys.limaone.program.FlowProgram.FlowResult;
import io.resys.limaone.spi.program.input.DefaultProgramInput;
import io.resys.thena.api.entities.grim.GrimProcess;
import io.resys.thena.api.envelope.CommitResultStatus;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class TransferTaskVisitor {
  
  private final io.resys.limaone.program.Runtime envir;
  private final TaskStore ctx;
  private final TaskFileClient taskFileClient;
  private final DocContainerClient docContainerClient;

  private final String userId;
  private final String taskId;
  private final TransferTaskCommand command;

  private final String flowName = "resolve_task_transfer_props";
  
  public Uni<TaskClient.Task> accept() {
    
    return ctx.getConfig().accept(new GetOneTaskByIdVisitor(taskId))
    .onItem().transformToUni(task -> 
      // calculate extra props
      // find all the files
      // pass the task down the pipeline 
      Uni.combine().all().unis(
        Uni.createFrom().item(getQuestionnairePropsFromFlow(task)), 
        getTaskFiles(), 
        Uni.createFrom().item(task)).asTuple()
    )
    .onItem().transformToUni(tuple -> {
      return createDocContainer(tuple.getItem3(), tuple.getItem1(), tuple.getItem2())
          .onItem().transformToUni(created -> updateTask(created, tuple.getItem1(), tuple.getItem2(), tuple.getItem3()));
    });

  }
  
  
  
  private Uni<DocContainerEnvelope> createDocContainer(Task task, Map<String, String> props, List<TaskFile> files) {
    final var container = docContainerClient.createDoc().task(task);
    
    for(final var file : files) {
      container.addDocument(ImmutableDoc.builder()
          .body(file.getBody())
          .bodyType(file.getBodyType())
          .mimeType(file.getMimeType())
          .name(file.getName())
          .externalId(file.getExternalId())
          .build()); 
    }
    Map<String,String> allProps = new HashMap<>(task.getDocumentProperties());
    allProps.putAll(props);
    allProps.putAll(command.getTransferProps());
    return container
        .title(command.getTransferTitle())
        .draftedBy(userId)
        .decidedBy(userId)
        .createdBy(task.getClientIdentificator())
        .externalId(taskId)
        .props(allProps)
        .build();
  }
  
  private Uni<List<TaskFile>> getTaskFiles() {
    
    final var config = ctx.getConfig();
    final var grim = config.getClient().grim(config.getTenantName());
    
    final Uni<Optional<GrimProcess>> procQuery;
    if(command.getProcessId() == null) {
      procQuery = grim.find().missionProcsQuery().findOneByMissionId(taskId);
    } else {
      procQuery = grim.find().missionProcsQuery().findOneById(command.getProcessId());
    }
    
    return procQuery.onItem().transformToUni(process -> {
      
      return taskFileClient.queryTaskFiles().findAll(taskId, process.map(e -> e.getId())).onItem().transform(files -> {
        if(files.isEmpty()) {
          throw TaskException.builder("TRANSFER_TASK_FAIL_NO_FILES_TO_TRANSFER")
            .add(
                "transfer-files-fail", 
                "Task transfer must contain at least 1 file!", JsonObject.mapFrom(command)).build(); 
        }
        return files;
      });
      
    });
  }
  
  
  private Uni<TaskClient.Task> updateTask(
      DocContainerEnvelope env, 
      Map<String, String> props, 
      List<TaskFile> files, 
      Task previousVerison) {
    
    final var config = ctx.getConfig();
    final var tenant = config.getClient().grim(ctx.getConfig().getTenantName());
    
    return tenant.commit().modifyOneMission().missionId(taskId)
      .modifyMission(merge -> {        
        docContainerClient.updateTask().doc(env.getObjects()).userId(userId).task(merge).build();
      })
      .commitAuthor(userId)
      .commitMessage("Adding task viewer by: " + TransferTaskVisitor.class.getSimpleName())
      .build()
      .onItem().transform(envelope -> {
        if(envelope.getStatus() == CommitResultStatus.OK) {
          return envelope;
        }
        throw TaskException.builder("TRANSFER_TASK_STATUS_UPDATE_FAIL").add(tenant, envelope).build(); 
      })
      .onItem().transform(TaskMapper::map);
  }

  
  private Map<String, String> getQuestionnairePropsFromFlow(Task task) {

    try {
      final var flow = envir.getBundle().queryFlows().name(flowName).findOne();
      if (flow.isEmpty()) {
        return Map.<String, String>of();
      }
      final String questionnaireId = task.getQuestionnaireId(); 
      final String assigneeId = task.getAssignedUser();
      
      TaskAssert.notNull(questionnaireId, () -> "questionnaireId must be defined!");
      final var flowInput = new HashMap<String, Serializable>();
      flowInput.put("questionnaireId", questionnaireId);
      flowInput.put("assigneeId", assigneeId);
      flowInput.put("assignedRoles", task.getAssignedRoles() != null ? String.join(",",task.getAssignedRoles()) : "");
      final FlowResult run = flow.get().run(DefaultProgramInput.of(flowInput)).andGetBody();
      
      return run.getReturns().entrySet().stream()
          .filter(e -> e.getValue() != null)
          .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().toString()));
    
    } catch(Exception error) {
      return Map.of("failed", ExceptionUtils.getStackTrace(error)); 
    }      
  }
}
