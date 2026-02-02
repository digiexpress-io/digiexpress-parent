package io.digiexpress.eveli.client.spi.dialob;

import java.util.ArrayList;

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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import io.digiexpress.eveli.client.api.GamutClient.UserAction;
import io.digiexpress.eveli.client.api.ImmutableAddFormToCustomerAssignmentCommand;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.AddFormToCustomerAssignmentCommand;
import io.digiexpress.eveli.client.api.TaskClient.ProcessInstance;
import io.digiexpress.eveli.client.api.TaskClient.TaskAssignmentStatus;
import io.digiexpress.eveli.client.api.TaskClient.TaskCommentSource;
import io.digiexpress.eveli.client.api.TaskClient.TaskCustomerAssignment;
import io.digiexpress.eveli.client.spi.gamut.ImmutableInitUserAction;
import io.digiexpress.eveli.client.spi.gamut.UserActionsBuilderImpl;
import io.digiexpress.eveli.client.spi.mq.MqEventPublisher;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.digiexpress.thena.cockpit.client.api.CockpitAware.CockpitAwareProvider;
import io.digiexpress.thena.cockpit.client.api.CockpitAware.CockpitIdSupplier;
import io.resys.thena.api.entities.grim.GrimProcess.GrimProcessStatus;
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
  private final DialobClient dialobClient;
  private final EveliEnvirClient envir;
  private final MqEventPublisher mqEventPublisher;
  private final Optional<CockpitAwareProvider> cockpitAwareProvider;
  
  
  @Data
  @AllArgsConstructor
  public static class CreateProcessAndFormEvent {
    private final String taskId;
  }
  
  @Data
  @AllArgsConstructor
  public static class DeleteProcessAndFormEvent {
    private final String taskId;
    private final String processId;
    private final String userId;
  }
  

  // Async create
  public void publishCreateEvent(TaskClient.Task task) {
    if(task.isNewCustomerAssignment()) {
      publisher.publishEvent(new CreateProcessAndFormEvent(task.getId()));
    }
  }
  @Async
  @EventListener
  public CompletableFuture<?> handleCreateFormAndProcess(CreateProcessAndFormEvent event) {
    return Uni.combine().all().unis(
          taskClient.queryTasks().getOneById(event.getTaskId()),
          taskClient.queryTaskProcesess().findOneByTaskId(event.getTaskId())
        ).asTuple()
        .onItem().transformToUni(this::createFormsAndProcesses)
        .onItem().transformToUni(this::syncWithTask)
        .onItem().invoke(task -> {
          
          mqEventPublisher.publishMqEvent(task, TaskCommentSource.FRONTDESK);
        })
        .subscribeAsCompletionStage()
        .toCompletableFuture();
  }
  
  
  // Async delete
  public void publishDeleteAssignmentEvent(TaskClient.Task task, String assignmentId, String userId) {
    final var processId = task.getCustomerAssignments().stream()
      .filter(assigment -> assigment.getId().equals(assignmentId))
      .map(assignment -> assignment.getProcessId())
      .findFirst();
    
    if(processId.isPresent()) {
      publisher.publishEvent(new DeleteProcessAndFormEvent(task.getId(), processId.get(), userId));
    }
  }
  
  @Async
  @EventListener
  public CompletableFuture<?> handleDeleteProcessAndForm(DeleteProcessAndFormEvent event) {
    return taskClient.modifyProcess()
        .commitAuthor(event.getProcessId())
        .commitMessage("Invalidating process")
        .id(event.getProcessId())
        .merge((current, merger) -> merger.status(GrimProcessStatus.REJECTED).build())
        .build()
        .subscribeAsCompletionStage()
        .toCompletableFuture();
  }
  
  
  
  // Support
  private Uni<TaskClient.Task> syncWithTask(TaskUpdate taskUpdate) {        
    if(taskUpdate.getValues().isEmpty()) {
      return Uni.createFrom().item(taskUpdate.getTask());
    }
    
    final var updates = taskUpdate.getValues().stream()
        .filter(update -> update.getItem1().isPresent())
        .map(update -> {
          final AddFormToCustomerAssignmentCommand command = ImmutableAddFormToCustomerAssignmentCommand.builder()
              .assignmentId(update.getItem1().get().getId())
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
  
  private Uni<TaskUpdate> createFormsAndProcesses(Tuple2<TaskClient.Task, Optional<ProcessInstance>> task) {
    final var assignments = task.getItem1().getCustomerAssignments().stream()
        .filter(e -> e.getStatus() == TaskAssignmentStatus.NEW)
        .toList();
    
    final var isMainOptional = task.getItem1().getCustomerAssignments().isEmpty() && assignments.isEmpty();
    final var isMainCreated = task.getItem2().isPresent() || isMainOptional;
    
    if(assignments.isEmpty() && isMainCreated) {
      return Uni.createFrom().item(new TaskUpdate(task.getItem1(), task.getItem2(), Collections.emptyList()));
    }
    
    final String ssn = task.getItem2().map(proc -> proc.getUserId()).orElse(task.getItem1().getClientIdentificator());

    
    final var requests = new ArrayList<>(assignments.stream().map(assignment -> 
        new UserActionsBuilderImpl(dialobClient, envir.withCockpitIdSupplier(getCockpitIdForFrontoffice(task)), taskClient)
          .inputContextId("_")
          .inputParentContextId("_")
          .customerAssignment(true)
          .externalUserActionInit(ImmutableInitUserAction.builder()
              .identity(ssn)
              .workflowName(assignment.getServiceName())
              .protectionOrder(false)
              .build())
          .actionId(assignment.getExternalId())
          .taskId(task.getItem1().getId())
          .clientLocale(assignment.getLocale())
          .createOne().onItem().transform(action -> Tuple2.of(Optional.of(assignment), action))
    ).toList());
    
    if(!isMainCreated) {
   
      
      final var mainRequest = getCockpitIdForBackoffice().apply()
          .onItem().transformToUni((cockpitId) -> taskClient.createProcess()
            .anon(false)
            .taskId(task.getItem1().getId())
            .userId(ssn)
            .cockpitId(cockpitId.orElse(null))
            .anon(false)
            .customerAssignment(false)
            
            .workflowName("_")
            .articleName("_")
            .parentArticleName("_")
  
            .expiresInSeconds(null)
            .expiresAt(null)
            
            .flowName(null)
            .formName(null)
            
            .formTagName(null)
            .stencilTagName(null)
            .wrenchTagName(null)
            .commitAuthor(DialobCreateEventPublisher.class.getSimpleName())
            .commitMessage("creating main proc")
            .build())
          .onItem().transform(UserActionsBuilderImpl::map)
          .onItem().transform(action -> Tuple2.of(Optional.<TaskCustomerAssignment>empty(), action));
  
      requests.add(mainRequest);
    }
    
    return Uni.join().all(requests).usingConcurrencyOf(4).andCollectFailures()
        .onItem().transform(values -> new TaskUpdate(task.getItem1(), task.getItem2(), values));
  } 
  
  @Data @RequiredArgsConstructor
  private static class TaskUpdate {
    private final TaskClient.Task task;
    private final Optional<ProcessInstance> process;
    private final List<Tuple2<Optional<TaskCustomerAssignment>, UserAction>> values;
  }

  
  private CockpitIdSupplier getCockpitIdForFrontoffice(Tuple2<TaskClient.Task, Optional<ProcessInstance>> task) {
    return () -> Uni.createFrom().item(task.getItem2().map(p -> p.getCockpitId()));
  }
  
  private CockpitIdSupplier getCockpitIdForBackoffice() {
    if(cockpitAwareProvider.isEmpty()) {
      return () -> Uni.createFrom().item(Optional.empty());
    }
    final CockpitAwareProvider provider = cockpitAwareProvider.get();
    final Uni<Optional<String>> cockpitId = provider.get()
        .onItem().transform(container -> container.map(e -> e.getConfig().getId()));
    return () -> cockpitId;
  }
}
