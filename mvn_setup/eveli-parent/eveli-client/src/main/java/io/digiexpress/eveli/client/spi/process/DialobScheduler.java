package io.digiexpress.eveli.client.spi.process;

import java.time.OffsetDateTime;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import java.util.concurrent.TimeUnit;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dialob.api.proto.Action;
import io.dialob.api.proto.Actions;
import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.api.questionnaire.Questionnaire.Metadata.Status;
import io.digiexpress.eveli.client.api.GamutClient.UserActionFillEvent;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.ProcessClient.ProcessInstance;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class DialobScheduler {
  
  private final ProcessClient processClient;
  private final DialobClient dialobClient;
  private final ObjectMapper objectMapper;
  
  
  private void executeFlowForInstance(ProcessInstance init) {
      // resync
    
      try {
        final var optional = processClient.queryInstances().findOneById(init.getId().toString());
        if(optional.isEmpty()) {
          log.debug("Skipping execution: {} because task is already created, process status handling is probably wrong!", init.getId());
          return;          
        }
        
        final var instance = optional.get();
        
        if(instance.getTaskId() != null) {
          log.debug("Skipping execution: {} because task is already created, process status handling is probably wrong!", instance.getId());
          return;
        }
        
        Questionnaire questionnaire = null;
        try {
          questionnaire = dialobClient.getQuestionnaireAndMetaById(instance.getQuestionnaireId());
          if(questionnaire.getMetadata().getStatus() != Status.COMPLETED) {
            log.debug("Skipping execution because questionnaire: {} state is not completed!", instance.getQuestionnaireId());
            return;
          }
        } catch(Exception e) { }
        
        if(questionnaire == null) {
          log.error("Skipping execution because questionnaire: {} could not be found!", instance.getQuestionnaireId());
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
        
      } catch(Exception e) {
        log.error("Failed to run flow for process instance: {}, e: {}!", init.getId(), e.getMessage(), e);
      }
  }
  
  @Scheduled(fixedRate = 24, timeUnit = TimeUnit.HOURS)
  public void executeFlow() {
    for(final var instance : processClient.queryInstances().findAllAnsweredFrom(OffsetDateTime.now().minusMonths(6))) {
      executeFlowForInstance(instance);
    }
  }
  
  @Scheduled(fixedRate = 24, timeUnit = TimeUnit.HOURS)
  public void rejectProcessesWithDeadline() {
    processClient.queryInstances().findAllExpired().forEach(instance -> {
      log.warn("Expiry for process instance: {}, e: {}!", instance.getId());
      processClient.changeInstanceStatus().rejected(instance.getId().toString());
    });
  }
  
  @Async
  @EventListener
  public void handleFillCompleted(UserActionFillEvent event) {
  
    // dum dum method
    if(event.getResponseBody().contains("\"type\":\"COMPLETE\"")) {
      try {
        final var actions = new JsonObject(event.getResponseBody()).mapTo(Actions.class);
        
        if(actions.getActions().isEmpty()) {
          return;
        }
        
        final var questionnaire = dialobClient.getDialobById(event.getSessionId());
        if(questionnaire.unwrap().getMetadata().getStatus() != Questionnaire.Metadata.Status.COMPLETED) {
          log.debug("Skipping session sync because questionnaire {} status is {}", event.getSessionId(), questionnaire.unwrap().getMetadata().getStatus());          
        }
        
        final var completed = actions.getActions().stream().filter(action -> action.getType() == Action.Type.COMPLETE).findFirst().isPresent();
        if(completed) {
          final var instance = processClient.queryInstances().findOneByQuestionnaireId(event.getSessionId()).get();
          processClient.changeInstanceStatus().answered(instance.getId().toString());
          
          
          final var answeredInstance = processClient.queryInstances().findOneById(String.valueOf(instance.getId()));
          if(answeredInstance.isPresent()) {
            log.debug("Executing flow directly for process: {} after dialob completion event!", instance.getId());
            executeFlowForInstance(answeredInstance.get());
          }
          
        }
      } catch(Exception e) {
        log.error("Failed to check for complete event for session id: {}!", event.getSessionId());
      }
    }
  }
}
