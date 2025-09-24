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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import io.digiexpress.eveli.client.api.GamutClient.UserAction;
import io.digiexpress.eveli.client.api.ImmutableAddFormToCustomerAssignmentCommand;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.AddFormToCustomerAssignmentCommand;
import io.digiexpress.eveli.client.api.TaskClient.TaskAssignmentStatus;
import io.digiexpress.eveli.client.api.TaskClient.TaskCommentSource;
import io.digiexpress.eveli.client.api.TaskClient.TaskCustomerAssignment;
import io.digiexpress.eveli.client.spi.gamut.ImmutableInitUserAction;
import io.digiexpress.eveli.client.spi.gamut.UserActionsBuilderImpl;
import io.digiexpress.eveli.client.spi.mq.MqEventPublisher;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DialobCreateEventPublisher {
  private final ApplicationEventPublisher publisher;
  private final TaskClient taskClient;
  private final ProcessClient processClient;
  private final DialobClient dialobClient;
  private final EveliEnvirClient envir;
  private final MqEventPublisher mqEventPublisher;
  
  
  @Data
  @AllArgsConstructor
  public static class CreateProcessAndFormEvent {
    private final String taskId;
  }
  
  public void publishCreateEvent(TaskClient.Task task) {
    if(task.isNewCustomerAssignment()) {
      publisher.publishEvent(new CreateProcessAndFormEvent(task.getId()));      
    }
  }
  
  @Async
  @EventListener
  public CompletableFuture<?> handleFillCompleted(CreateProcessAndFormEvent event) {
    return taskClient.queryTasks().getOneById(event.getTaskId())
        .onItem().transformToUni(this::createFormsAndProcesses)
        .onItem().transformToUni(this::syncWithTask)
        .onItem().invoke(task -> {
          
          mqEventPublisher.publishMqEvent(task, TaskCommentSource.FRONTDESK);
        })
        .subscribeAsCompletionStage()
        .toCompletableFuture();
  }
  
  
  
  private Uni<TaskClient.Task> syncWithTask(TaskUpdate taskUpdate) {
    if(taskUpdate.getValues().isEmpty()) {
      return Uni.createFrom().item(taskUpdate.getTask());
    }
    
    final var updates = taskUpdate.getValues().stream().map(update -> {
      
      final AddFormToCustomerAssignmentCommand command = ImmutableAddFormToCustomerAssignmentCommand.builder()
          .assignmentId(update.getItem1().getId())
          .taskId(taskUpdate.getTask().getId())
          .taskVersion(taskUpdate.getTask().getVersion())
          .processId(update.getItem2().getId())
          .questionnaireId(update.getItem2().getFormId())
          .build();
      
      return command;
    }).toList();
    
    return taskClient.taskBuilder()
        .userId(DialobCreateEventPublisher.class.getSimpleName(), null)
        .addFormToCustomerAssignment(taskUpdate.getTask().getId(), updates);
  }
  
  private Uni<TaskUpdate> createFormsAndProcesses(TaskClient.Task task) {
    final var assignments = task.getCustomerAssignments().stream()
        .filter(e -> e.getStatus() == TaskAssignmentStatus.NEW)
        .toList();
    if(assignments.isEmpty()) {
      return Uni.createFrom().item(new TaskUpdate(task, Collections.emptyList()));
    }

    final var requests = assignments.stream().map(assignment -> 
      new UserActionsBuilderImpl(processClient, dialobClient, envir)
          .inputContextId("_")
          .inputParentContextId("_")
          .customerAssignment(true)
          .externalUserActionInit(ImmutableInitUserAction.builder()
              .identity(task.getClientIdentificator())
              .workflowName(assignment.getServiceName())
              .protectionOrder(false)
              .build())
          .actionId(assignment.getExternalId())
          .taskId(task.getId())
          .clientLocale(assignment.getLocale())
          .createOne().onItem().transform(action -> Tuple2.of(assignment, action))
      ).toList();
    
    return Uni.join().all(requests).usingConcurrencyOf(4).andCollectFailures()
        .onItem().transform(values -> new TaskUpdate(task, values));
  } 
  
  @Data @RequiredArgsConstructor
  private static class TaskUpdate {
    private final TaskClient.Task task;
    private final List<Tuple2<TaskCustomerAssignment, UserAction>> values;
  }

}
