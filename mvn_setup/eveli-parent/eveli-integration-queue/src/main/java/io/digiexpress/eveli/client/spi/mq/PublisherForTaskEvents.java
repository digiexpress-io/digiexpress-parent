package io.digiexpress.eveli.client.spi.mq;

/*-
 * #%L
 * eveli-integration-queue
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

import java.time.Duration;
import java.util.Optional;

import org.springframework.context.event.EventListener;

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.TaskDiff;
import io.digiexpress.eveli.client.spi.mq.MqEventPublisher.MqEvent;
import io.digiexpress.eveli.client.spi.mq.WrenchFlowCommand.TaskNotification;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.digiexpress.thena.mq.client.api.ThenaMqClient;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope.OperationStatus;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class PublisherForTaskEvents {
  private final TaskClient taskClient;
  private final ThenaMqClient mqClient;
  private final EveliEnvirClient envir;
  
  @EventListener(MqEvent.class)
  public void publishMessageToQueue(MqEvent event) {
    
    final String taskId = event.getTaskId();
    final String commitId = event.getCommitId();
    
    taskClient.queryTasks()
      .getOneTaskDiff(taskId, commitId)
      .onItem().transformToMulti(diff -> {
        return new WrenchFlowCommand(envir).getQueueMessages(diff)
          .onItem().transformToMulti(items -> Multi.createFrom().items(items.stream()))
          .onItem().transform(notification -> createMessage(diff, notification));
      })
      .collect().asList()
      .await().atMost(Duration.ofMinutes(10));
  }
  
  private Uni<Optional<QueueMessage>> createMessage(TaskDiff task, TaskNotification notification) {
    
    /*
    if(routingKeys.isEmpty()) {
      log.debug("Skipping queue, nowhere to route");
      return Uni.createFrom().item(Optional.empty());
    }*/
    
    return mqClient.messageBuilder()
      .routingKey(notification.getQueue())
      .bodyId(task.getTaskId())
      .bodyType("TASK")
      .bodyValue(JsonObject.mapFrom(notification))
      .comment("Created by calling flow")
      .createdBy(PublisherForTaskEvents.class.getSimpleName())
      .build()
      .onItem().transform(resp -> {
        
        if( resp.getOperationStatus() == OperationStatus.ERROR || 
            resp.getOperationStatus() == OperationStatus.CONFLICT) {
          
          final var allLogs = resp.getOperationLogs().stream().map(e -> e.getText()).toList();
          final var logs = String.join("\r\n", allLogs);
          log.error("Failed to start MQ config because of:\r\n{}", logs);
        }
        
        return Optional.ofNullable(resp.getObject());
      });
  }
 
}
