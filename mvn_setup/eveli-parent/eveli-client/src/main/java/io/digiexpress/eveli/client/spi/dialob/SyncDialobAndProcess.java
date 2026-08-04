package io.digiexpress.eveli.client.spi.dialob;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.api.questionnaire.Questionnaire.Metadata.Status;
import io.digiexpress.eveli.client.api.ImmutableCompleteCustomerAssignmentCommand;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.MergeProcess;
import io.digiexpress.eveli.client.api.TaskClient.ProcessInstance;
import io.digiexpress.eveli.client.api.TaskClient.Task;
import io.digiexpress.eveli.client.api.TaskClient.TaskAssignmentStatus;
import io.resys.limaone.program.FlowProgram.FlowResult;
import io.resys.limaone.program.ImmutableParticipantForm;
import io.resys.limaone.spi.program.input.DefaultProgramInput;
import io.resys.thena.api.entities.grim.GrimProcess.GrimProcessStatus;
import io.resys.thena.api.entities.grim.GrimProcess.GrimProcessType;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


// background non blocking sync block
@RequiredArgsConstructor
@Slf4j
public class SyncDialobAndProcess {

  private final TaskClient taskClient;
  private final io.resys.limaone.program.Runtime envir;
  private final ObjectMapper objectMapper;
  
  
  public Uni<Void> executeFlowForInstance(ProcessInstance init, Optional<Questionnaire> questionnaire) {
    final Uni<Questionnaire> questionnaireUni = questionnaire.map(q -> Uni.createFrom().item(q)).orElseGet(() -> getQuestionnaire(init));
    final Uni<ProcessInstance> execution; 
    
    if(init.getType() == null) {
      execution = executeUserTask(init, questionnaireUni);
      
    } else if(init.getType() == GrimProcessType.CUSTOMER_ASSIGNMENT) {
      execution = executeCustomerAssignment(init, questionnaireUni);
      
    } else {
      log.error("Skipping execution because process of type: {} is not supported!", init.getType());
      return Uni.createFrom().voidItem();
    }
    
    return execution
      .onFailure().recoverWithUni((e) -> {
        log.error("Skipping execution because questionnaire: {} processing failed because of: {}!", init.getQuestionnaireId(), e.getMessage(), e);
        return Uni.createFrom().item(init);
      })
      .onItem().transformToUni((ignore) -> Uni.createFrom().voidItem());
  }
  

  private Uni<ProcessInstance> executeCustomerAssignment(ProcessInstance init, Uni<Questionnaire> questionnaireUni) {
    return Uni.combine().all().unis(questionnaireUni, getTask(init))
      .asTuple()
      .onItem().transformToUni(tuple -> {
      
        final var task = tuple.getItem2().orElseThrow(() -> new SyncDialobAndProcessException("Skipping execution: '" + init.getId() + "' because task MUST be defined for objective!"));
        final var questionnaire = tuple.getItem2();
        
        return taskClient.modifyProcess()
          .commitAuthor(SyncDialobAndProcess.class.getSimpleName())
          .commitMessage("Sync Form and Eveli")
          .id(init.getId().toString())
          .onAnyUni(merger -> {
            
            final var currentState = merger.getCurrentState();
            
            final var assignment = task.getCustomerAssignments().stream()
                .filter(t -> t.getQuestionnaireId().equals(currentState.getQuestionnaireId()))
                .findFirst();
              
            if(assignment.isEmpty()) {
              throw new SyncDialobAndProcessException("Skipping execution: '" + currentState.getId() + "' because assignment MUST be defined for questionnaire!");
            }
            
            if(assignment.get().getStatus() == TaskAssignmentStatus.COMPLETED) {
              throw new SyncDialobAndProcessException("Skipping execution: '" + currentState.getId() + "' because assignment MUST be 'OPEN'!");
            }
            return taskClient.taskBuilder()
                .userId(SyncDialobAndProcess.class.getSimpleName(), null)
                .completeCustomerAssignment(task.getId(), ImmutableCompleteCustomerAssignmentCommand.builder()
                    .targetDate(ZonedDateTime.now())
                    .assignmentId(assignment.get().getId())
                    .taskVersion(task.getVersion())
                    .build());
          })
          .merge((currentState, merge) -> merge
              .formBody(toJsonString(questionnaire))
              .status(GrimProcessStatus.COMPLETED)
              .build())
          .build();
        }
      );
  }
  
  private Uni<ProcessInstance> executeUserTask(ProcessInstance init, Uni<Questionnaire> questionnaireUni) {
    return questionnaireUni.onItem().transformToUni(questionnaire -> {
        final var runtime = getRuntime(init);
        return taskClient.modifyProcess()
          .commitAuthor(SyncDialobAndProcess.class.getSimpleName())
          .commitMessage("Sync Form and Eveli")
          .id(init.getId().toString())
          .onAnyUni((merger) -> Uni.createFrom().item(executeWorkflow(questionnaire, merger, runtime))
              .onItem().invoke(flow -> merger
                .flowBody(flow.map(this::toJsonString).orElse("{}"))
                .formBody(toJsonString(questionnaire))
              )
              // ENABLE this for artificially testing tx locks .onItem().delayIt().by(Duration.ofMinutes(1))
          )
          .onTask((task, merger) -> task.stream().forEach(t -> merger.taskId(t.getId())))
          .merge((currentState, merge) -> merge.status(GrimProcessStatus.ANSWERED).build())
          .build();
      });
  }
  
  private Uni<Optional<Task>> getTask(ProcessInstance init) {
    return init.getTaskId() == null ? 
        Uni.createFrom().item(Optional.empty()) : 
        taskClient.queryTasks().getOneById(init.getTaskId()).onItem().transform(Optional::of);
  }
  
  private Uni<Questionnaire> getQuestionnaire(ProcessInstance init) {
    
    return envir.getProperties().getFormDb().withTenant().formInstanceQuery().findOne(init.getQuestionnaireId())
      .onItem().transform(questionnaire -> {
        
        if(questionnaire.isEmpty()) {
          log.debug("Skipping execution because questionnaire: {} state is not completed!", init.getQuestionnaireId());
          throw new SyncDialobAndProcessException(
              "Can't transfer questionnaire to flow, " + 
              "questionnaire for process: '" + init.getQuestionnaireId() + "' has been deleted!"
          );
        }
        
        if(questionnaire.get().metadata().getStatus() != Status.COMPLETED) {
          log.debug("Skipping execution because questionnaire: {} state is not completed!", init.getQuestionnaireId());
          throw new SyncDialobAndProcessException(
              "Can't transfer questionnaire to flow, " + 
              "expected status: 'COMPLETED' " + 
              "but was: '" + questionnaire.get().metadata().getStatus() + "'"
          );
        }
        return questionnaire.get().getQuestionnaire();
      });
  }
  
  private io.resys.limaone.program.Runtime getRuntime(ProcessInstance init) {
    return envir.withTenant(Optional.ofNullable(init.getCockpitId()));
  }
  
  private String toJsonString(Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch(Exception e) {
      throw new SyncDialobAndProcessException("Failed to writeValueAsString for: " + object.getClass().getSimpleName());
    }
  }
  
  private Optional<FlowResult> executeWorkflow(Questionnaire questionnaire, MergeProcess merger, io.resys.limaone.program.Runtime runtime) {
    
    final var instance = merger.getCurrentState();
    
    if(instance.getTaskId() != null) {
      log.debug("Skipping execution: {} because task is already created, process status handling is probably wrong!", instance.getId());
      merger.skip();
      return Optional.empty();
    }
    
    final var flowInput = new HashMap<String, Serializable>();
    flowInput.put("processId", instance.getId());
    
    final var wk = runtime.getBundle().queryWorkflows()
      .name(instance.getWorkflowName())
      .findOne()
      .get();
    
    return wk.runFlow(
      ImmutableParticipantForm.builder().questionnaireId(instance.getQuestionnaireId()).build(), 
      DefaultProgramInput.of(flowInput)
    )
    .getFlow();
  }
  
  private static class SyncDialobAndProcessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SyncDialobAndProcessException(String message) {
      super(message);
    }
  }
}
