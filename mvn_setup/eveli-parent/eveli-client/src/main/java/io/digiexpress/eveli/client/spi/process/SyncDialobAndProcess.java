package io.digiexpress.eveli.client.spi.process;

import java.time.Duration;
import java.time.ZonedDateTime;

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

import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.api.questionnaire.Questionnaire.Metadata.Status;
import io.digiexpress.eveli.client.api.ImmutableCompleteCustomerAssignmentCommand;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.ProcessClient.ProcessInstance;
import io.digiexpress.eveli.client.api.ProcessClient.ProcessType;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.TaskAssignmentStatus;
import io.digiexpress.eveli.dialob.api.DialobClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


// background non blocking sync block
@RequiredArgsConstructor
@Slf4j
public class SyncDialobAndProcess {

  private final ProcessClient processClient;
  private final TaskClient taskClient;
  private final DialobClient dialobClient;
  private final ObjectMapper objectMapper;
  
  
  private void executeCustomerAssignment(Questionnaire questionnaire, ProcessInstance instance) throws JsonProcessingException {
    if(instance.getTaskId() == null) {
      log.error("Skipping execution: {} because task MUST be defined for objective!", instance.getId());
      return; 
    }
    
    final var task = taskClient.queryTasks().getOneById(instance.getTaskId()).await().atMost(Duration.ofMinutes(1));
    
    final var assignment = task.getCustomerAssignments().stream()
      .filter(t -> t.getQuestionnaireId().equals(instance.getQuestionnaireId()))
      .findFirst();
    
    if(assignment.isEmpty()) {
      log.error("Skipping execution: {} because assignment MUST be defined for questionnaire!", instance.getId());
      return; 
    }
    
    if(assignment.get().getStatus() == TaskAssignmentStatus.COMPLETED) {
      log.error("Skipping execution: {} because assignment MUST be 'OPEN'!", instance.getId());
      return;
    }
    
    taskClient.taskBuilder()
      .userId(SyncDialobAndProcess.class.getSimpleName(), null)
      .completeCustomerAssignment(task.getId(), ImmutableCompleteCustomerAssignmentCommand.builder()
          .targetDate(ZonedDateTime.now())
          .assignmentId(assignment.get().getId())
          .taskVersion(task.getVersion())
          .build())
      .await().atMost(Duration.ofMinutes(1));
    
    
    processClient.createBodyBuilder()
      .processInstanceId(instance.getId())
      .formBody(objectMapper.writeValueAsString(questionnaire))
      .build();
    
    processClient.changeInstanceStatus()
      .completed(instance.getId().toString());
  }
  
  private void executeUserTask(Questionnaire questionnaire, ProcessInstance instance) throws JsonProcessingException {
    
    if(instance.getTaskId() != null) {
      log.debug("Skipping execution: {} because task is already created, process status handling is probably wrong!", instance.getId());
      return;
    }
    
    processClient.createBodyBuilder()
      .processInstanceId(instance.getId())
      .formBody(objectMapper.writeValueAsString(questionnaire))
      .build();
    
    final var flow = processClient.createExecutor().processInstance(instance).execute();
    
    processClient.createBodyBuilder()
      .processInstanceId(instance.getId())
      .flowBody(objectMapper.writeValueAsString(flow))
      .build();
    
  }
  
  
  @Transactional
  public void executeFlowForInstance(ProcessInstance init) {
    // resync
  
    try {

      Questionnaire questionnaire = null;
      try {
        questionnaire = dialobClient.getQuestionnaireAndMetaById(init.getQuestionnaireId());
        if(questionnaire.getMetadata().getStatus() != Status.COMPLETED) {
          log.debug("Skipping execution because questionnaire: {} state is not completed!", init.getQuestionnaireId());
          return;
        }
      } catch(Exception e) { 
        log.error("Skipping execution because questionnaire: {} reading failed!", init.getQuestionnaireId(), e);
        return;
      }
      
      final var optional = processClient.queryInstances().findOneByIdAndLock(init.getId().toString());
      if(optional.isEmpty()) {
        log.debug("Skipping execution: {} because task is already created, process status handling is probably wrong!", init.getId());
        return;          
      }
      
      final var instance = optional.get();
      // default process execution
      if(instance.getType() == null) {
        executeUserTask(questionnaire, instance);
        
      } else if(instance.getType() == ProcessType.CUSTOMER_ASSIGNMENT) {
        executeCustomerAssignment(questionnaire, instance);
      }

      
      
    } catch(Exception e) {
      log.error("Failed to run flow for process instance: {}, e: {}!", init.getId(), e.getMessage(), e);
    }
  }
}
