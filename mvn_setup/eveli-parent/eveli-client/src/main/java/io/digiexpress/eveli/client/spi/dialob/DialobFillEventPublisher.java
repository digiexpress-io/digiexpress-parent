package io.digiexpress.eveli.client.spi.dialob;

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

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import io.dialob.api.proto.Action;
import io.dialob.api.proto.Actions;
import io.dialob.api.questionnaire.Questionnaire;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.GamutClient.UserActionFillEvent;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DialobFillEventPublisher {
  private final ApplicationEventPublisher publisher;
  private final ProcessClient processClient;
  private final DialobClient dialobClient;
  private final SyncDialobAndProcess syncDialobAndProcess;
  
  public void publishEvent(UserActionFillEvent event) {
    publisher.publishEvent(event);
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
            syncDialobAndProcess.executeFlowForInstance(answeredInstance.get());
          }
          
        }
      } catch(Exception e) {
        log.error("Failed to check for complete event for session id: {}!", event.getSessionId());
      }
    }
  }
}
