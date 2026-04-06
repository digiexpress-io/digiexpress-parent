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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.eveli.client.api.TaskClient.TaskCommentSource;
import io.digiexpress.eveli.client.api.TaskClient.TaskDiff;
import io.digiexpress.eveli.client.api.TaskClient.TaskDiffValue;
import io.resys.limaone.spi.program.input.DefaultProgramInput;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@RequiredArgsConstructor
@Slf4j
public class WrenchFlowCommand {
  private static final String CHANGE_TYPE_TASK_ROLES_UPDATED = "TASK_ROLES_UPDATED";
  private final io.resys.limaone.program.Runtime envir;

  private final String flowName = "task_mq_router";
  private final String default_locale = "fi";
  private final TaskCommentSource source;

  public Uni<List<TaskNotification>> getQueueMessages(TaskDiff diff) {
    return Uni.createFrom().item(() -> runFlow(diff, source));
  }
  
  private List<TaskNotification> runFlow(TaskDiff diff, TaskCommentSource source) {
    try {
      final List<TaskNotification> queues = new ArrayList<>();
      final var taskGroupId = diff.getTask().getAssignedRoles().isEmpty() ? "" : diff.getTask().getAssignedRoles().iterator().next();
      for(final var diffValue : diff.getValues()) {
        queues.addAll(processDiffValue(diff, diffValue, taskGroupId));
      }
      return queues.stream().distinct().toList();
    } catch(Exception e) {
      log.error("Failed to resolved flow queues of task diff:\r\n{}\r\n{}", diff, e.getMessage(), e);
      return Collections.emptyList();
    }
  }
  
  
  private List<TaskNotification> processDiffValue(TaskDiff diff, TaskDiffValue diffValue, String taskGroupId) {
    
    final var language = Optional.ofNullable(diff.getTask().getClientLanguage()).orElse(default_locale);
    
    try {
      
      final var run = envir.getBundle().queryFlows()
        .name(flowName)
        .findOne().get()
        .run(DefaultProgramInput.of(Map.of(
            "operation", diffValue.getOp().operationName().toLowerCase(),
            "path", diffValue.getPath(),
            "taskRef",  diff.getTask().getTaskRef(),
            "clientId", diff.getTask().getClientIdentificator(),
            "taskGroupId", taskGroupId,
            "clientLanguage", language,
            "taskSource", source 
        ))).andGetBody();
      
      @SuppressWarnings("unchecked")
      final List<Map<String, Object>> dtMatches = (List<Map<String, Object>>) run.getReturns().get("");
      if(dtMatches == null) {
        return Collections.emptyList();
      }
      
      final List<TaskNotification> queues = new ArrayList<>();
      for(final var match : dtMatches) {
        try {
          final var json = JsonObject
              .mapFrom(match)
              .put("taskId", diff.getTask().getId())
              .put("updaterId", diff.getTask().getUpdaterId())
              .put("customerLocale", language)
              .put("assigneeUser", diff.getTask().getAssignedUser())
              .put("taskGroupIds", findDiffGroupId(diff, diffValue, match))
              .put("assigneeEmail", diff.getTask().getAssignedUserEmail());
          
          final var notification = json.mapTo(TaskNotification.class);
          queues.add(notification);
        } catch(Exception e) {
          log.error("Failed to resolved flow queues of task diff value:\r\n{}\r\n{}\r\n{}", diffValue, match, e.getMessage(), e);            
        }
      }
      
      return queues;
    } catch(Exception e) {
      log.error("Failed to resolved flow queues of task diff value:\r\n{}\r\n{}", diffValue, e.getMessage(), e);
      return Collections.emptyList();
    }
    

  }

  private List<String> findDiffGroupId(TaskDiff diff, TaskDiffValue diffValue, Map<String, Object> match) {
    // in case of task role update diff value contains added or updated role
    if (CHANGE_TYPE_TASK_ROLES_UPDATED.equals(match.get("changeType"))) {
      return List.of(diffValue.getValue());
    }
    // in other cases notify all task's roles
    return diff.getTask().getAssignedRoles().stream().toList();
  }
  
  @JsonSerialize(as = ImmutableTaskNotification.class)
  @JsonDeserialize(as = ImmutableTaskNotification.class)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @Value.Immutable
  public interface TaskNotification {
    @Nullable String getUpdaterId();
    @Nullable String getAssigneeUser();
    @Nullable String getAssigneeEmail();
    String getChangeType();
    String getQueue();

    String getCustomerLocale();
    String getCustomerId();
    String getTaskRef();
    String getTaskId();
    List<String> getTaskGroupIds();
    MessageType getMessageType();
      
    // Locale based message data, locale(fi/sv/en) - "translated message"
    Map<String, String> getMessage();
    Map<String, String> getTitle();
    Map<String, String> getEmail();
    
    enum MessageType { WORKER_MSG, SUOMIFI_MSG }
  }
}
