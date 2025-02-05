package io.digiexpress.eveli.client.spi.mq;

import java.time.Duration;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.event.EventListener;

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.Task;
import io.digiexpress.eveli.client.api.TaskClient.TaskDiff;
import io.digiexpress.eveli.client.spi.process.ProcessClientImpl;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliRuntime;
import io.digiexpress.thena.mq.client.api.ThenaMqClient;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope.OperationStatus;
import io.resys.hdes.client.api.programs.FlowProgram.FlowResult;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class QueueWriter {
  private final TaskClient taskClient;
  private final ThenaMqClient mqClient;
  private final EveliEnvirClient envir;
  private final String flowName = "task_mq_router";
  
  
  @Data
  @AllArgsConstructor
  public static class MqEvent {
    private final String taskId;
    private final String commitId;
  }
  
  
  @EventListener(MqEvent.class)
  public void publishMessageToQueue(MqEvent event) {
    final var runtime = envir.runtimeQuery().getOne().await().atMost(ProcessClientImpl.asset_setup_duration);
    final String taskId = event.getTaskId();
    final String commitId = event.getCommitId();
    
    taskClient.queryTasks().getOneTaskDiff(taskId, commitId)
    .onItem().transformToUni(diff -> {
      final var queues = getQueues(diff, runtime);
      return createMessage(diff.getTask(), queues);
    }).await().atMost(Duration.ofMinutes(10));
    
  }
  
  private Uni<Optional<QueueMessage>> createMessage(Task task, List<String> routingKeys) {
    return mqClient.messageBuilder()
      .routingKey(routingKeys)
      .bodyId(task.getId())
      .bodyType("TASK")
      .bodyValue(JsonObject.mapFrom(task))
      .comment("Created by calling flow")
      .createdBy(flowName)
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
  
  @SuppressWarnings("unchecked")
  private List<String> getQueues(TaskDiff diff, EveliRuntime envir) {
    try {
      final List<String> queues = new ArrayList<>();

      for(final var diffValue : diff.getValues()) {
        final FlowResult run = envir.getWrench()
            .inputMap(Map.of(
                "operation", diffValue.getOp().operationName(),
                "path", diffValue.getPath()
            ))
            .flow(flowName)
            .andGetBody();
        final List<Map<String, Object>> dtMatches = (List<Map<String, Object>>) run.getReturns().get("");
        

        for(final var match : dtMatches) {
          if(!Boolean.TRUE.equals(match.get("enabled"))) {
            continue;
          }
          final var queueName = match.get("queue");
          if(queueName != null) {
            queues.add(queueName.toString());            
          }
        }
      }
      return queues;
    } catch(Exception e) {e.printStackTrace();
      log.error("Failed to resolved flow queues of task diff:\r\n{}\r\n{}", diff, e.getMessage(), e);
      return Collections.emptyList();
    }
  }

}
