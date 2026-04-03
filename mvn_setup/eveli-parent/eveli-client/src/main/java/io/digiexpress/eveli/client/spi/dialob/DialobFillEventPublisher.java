package io.digiexpress.eveli.client.spi.dialob;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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
import io.digiexpress.eveli.client.api.GamutClient.UserActionFillEvent;
import io.digiexpress.eveli.client.api.TaskClient;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DialobFillEventPublisher {
  private final ApplicationEventPublisher publisher;
  private final io.resys.limaone.program.Runtime runtime;
  private final SyncDialobAndProcess syncDialobAndProcess;
  private final TaskClient taskClient;
  
  public void publishEvent(UserActionFillEvent event) {
    publisher.publishEvent(event);
  }
  
  @Async
  @EventListener
  public CompletableFuture<?> handleFillCompleted(UserActionFillEvent event) {
    if(event.getResponseBody().contains("\"type\":\"COMPLETE\"")) {
      return completeProcess(event)
        .onFailure().recoverWithUni((e) -> {
          log.error("Skipping execution because questionnaire: {} processing failed because of: {}!", event.getSessionId(), e.getMessage(), e);
          return Uni.createFrom().voidItem();
        })
        .subscribeAsCompletionStage()
        .toCompletableFuture();
    } 
    
    return Uni.createFrom().voidItem()
        .subscribeAsCompletionStage()
        .toCompletableFuture();
  }
  
  
  private Uni<Void> completeProcess(UserActionFillEvent event) {
    final var actions = new JsonObject(event.getResponseBody()).mapTo(Actions.class);
    
    if(actions.getActions().isEmpty()) {
      return Uni.createFrom().voidItem();
    }
    
    final var questionnaire = runtime.getProperties().getFormDb().withTenant().formInstanceQuery().findOneSync(event.getSessionId()).get();
    final var isCompleted = (
        questionnaire.metadata().getStatus() == Questionnaire.Metadata.Status.COMPLETED &&
        actions.getActions().stream().filter(action -> action.getType() == Action.Type.COMPLETE).findFirst().isPresent()
    );
    
    if(!isCompleted) {
      log.error("Skipping session sync because questionnaire: {} status is: {}", event.getSessionId(), questionnaire.metadata().getStatus());
      return Uni.createFrom().voidItem();
    }
    
    return taskClient.queryTaskProcesess().findOneByQuestionnaireId(event.getSessionId())
      .onItem().transformToUni(proc -> {
        if(proc.isPresent()) {
          return syncDialobAndProcess.executeFlowForInstance(proc.get(), Optional.of(questionnaire.getQuestionnaire()));
        }
        return Uni.createFrom().voidItem();
      });

  }
}
