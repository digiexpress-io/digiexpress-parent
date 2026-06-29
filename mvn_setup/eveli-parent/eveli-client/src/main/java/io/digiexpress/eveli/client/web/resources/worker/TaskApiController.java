package io.digiexpress.eveli.client.web.resources.worker;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.hash.Hashing;

import io.dialob.api.proto.Actions;
import io.digiexpress.eveli.client.api.AttachmentCommands;
import io.digiexpress.eveli.client.api.GamutClient.UserActionAttachment;
import io.digiexpress.eveli.client.api.GamutClient.UserActionFillEvent;
import io.digiexpress.eveli.client.api.GamutClient.UserActionNotAllowedException;
import io.digiexpress.eveli.client.api.GamutClient.UserAttachmentUploadInit;
import io.digiexpress.eveli.client.api.GamutClient.WorkflowNotFoundException;
import io.digiexpress.eveli.client.api.TaskAuditClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.FormAssignment;
import io.digiexpress.eveli.client.api.TaskClient.Task;
import io.digiexpress.eveli.client.api.TaskClient.TaskCommentSource;
import io.digiexpress.eveli.client.api.TaskClient.TaskDasboard;
import io.digiexpress.eveli.client.api.TaskClient.TaskPriority;
import io.digiexpress.eveli.client.api.TaskClient.TaskStatus;
import io.digiexpress.eveli.client.api.WorkerAuthClient;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import io.digiexpress.eveli.client.spi.dialob.DialobCreateEventPublisher;
import io.digiexpress.eveli.client.spi.dialob.DialobFillEventPublisher;
import io.digiexpress.eveli.client.spi.gamut.UserAttachmentBuilderImpl;
import io.digiexpress.eveli.client.spi.mq.MqEventPublisher;
import io.digiexpress.eveli.client.spi.task.TaskViewerPublisher;
import io.resys.limaone.program.ImmutableParticipant;
import io.resys.limaone.program.ImmutableParticipantId;
import io.resys.limaone.program.ImmutableWorkflowDefaultProps;
import io.resys.limaone.program.WorkflowProgram;
import io.resys.limaone.program.WorkflowProgram.WorkflowFormResult;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;


/*
 * Task controller for frontdesk UI
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/worker/rest/api/tasks")
@Slf4j
public class TaskApiController {    
  private final DialobFillEventPublisher dialobFillEventPublisher;
  private final DialobCreateEventPublisher dialobCreateEventPublisher;
  private final AttachmentCommands attachmentCommands;
  private final WorkerAuthClient securityClient;
  private final TaskClient taskClient;
  private final MqEventPublisher mqEventPublisher;
  private final TaskViewerPublisher viewerPublisher;
  private final TaskAuditClient taskAuditClient;
  private final io.resys.limaone.program.Runtime envirClient;
  
  
  @GetMapping
  public Uni<Page<Task>> taskSearch(
      @RequestParam(name="subject", defaultValue = "") String subject, 
      @RequestParam(name="additionalInfo", defaultValue = "") String additionalInfo, 
      @RequestParam(name="clientIdentificator", defaultValue = "") String clientIdentificator, 
      @RequestParam(name="assignedUser", defaultValue = "") String assignedUser, 
      @RequestParam(name="assignedRoles", defaultValue = "") String searchRole,
      @RequestParam(name="dueDate", required = false) String dueDate,
      @RequestParam(name="status", required = false) List<TaskStatus> status,
      @RequestParam(name="priority", required = false) List<TaskPriority> priority,
      Pageable pageable) {
    
    final var worker = securityClient.getUser();
    
    final var query = taskClient.paginateTasks()
        .subject(subject)
        .clientIdentificator(clientIdentificator)
        .additionalInfo(additionalInfo)
        .assignedUser(assignedUser)
        .role(searchRole)
        .dueDate(dueDate)
        .status(status)
        .priority(priority)
        .page(pageable);
    
    if (worker.getPrincipal().isAdmin()) {
      return query.findAll();
    }
    return query.requireAnyRoles(worker.getPrincipal().getRoles()).findAll();
  }
  
  @GetMapping("/all")
  public Uni<List<Task>> all() {
    final var worker = securityClient.getUser();
    if (worker.getPrincipal().isAdmin()) {
      return taskClient.queryTasks().findAll();
    }
    return taskClient.queryTasks().requireAnyRoles(worker.getPrincipal().getRoles()).findAll();
  }


  @GetMapping("/{id}")
  public Uni<ResponseEntity<Task>> getTaskById(@PathVariable("id") String id) {
    final var worker = securityClient.getUser();
    
    return taskClient.queryTasks().getOneById(id).onItem().transform(task -> {
      if (worker.getPrincipal().isAdmin()) {
        viewerPublisher.publicTaskViewedByWorkerEvent(task, worker);
        return ResponseEntity.ok(task);
      }
      final var isWorkerInAssignedRoles = worker.getPrincipal().isAccessGranted(task.getAssignedRoles());
      if(isWorkerInAssignedRoles) {
        viewerPublisher.publicTaskViewedByWorkerEvent(task, worker);
        return ResponseEntity.ok(task);
      }

      // alarm clocks
      return ResponseEntity.status(403).build();
    });
 
  }
  
  @GetMapping("/{id}/form-assignments")
  public Multi<FormAssignment> getPossibleCustomerTaskAssignments(@PathVariable("id") String id) {
    return taskClient.queryFormAssignments().findAll(id);
  }

  @PostMapping("/{id}/form-assignments")
  public Uni<Task> createCustomerTaskAssignments(@PathVariable("id") String id, @RequestBody List<TaskClient.CreateCustomerAssignmentCommand> commands) {
    final var worker = securityClient.getUser().getPrincipal();
    return taskClient.taskBuilder()
        .userId(worker.getUsername(), worker.getEmail())
        .createCustomerAssignment(id, commands)
        .onItem().invoke(modifiedTask -> dialobCreateEventPublisher.publishCreateEvent(modifiedTask));
  }
  
  @DeleteMapping("/{id}/form-assignments")
  public Uni<Task> deleteCustomerTaskAssignment(
      @PathVariable("id") String id, @RequestBody DeleteCustomerTaskAssignment command) {
    final var worker = securityClient.getUser().getPrincipal();
    return taskClient.taskBuilder()
        .userId(worker.getUsername(), worker.getEmail())
        .deleteCustomerTaskAssignment(id, command.getAssignmentIds())
        .onItem().invoke(modifiedTask -> {
          
          for(final var assignmentId : command.getAssignmentIds()) {
            dialobCreateEventPublisher.publishDeleteAssignmentEvent(modifiedTask, assignmentId, worker.getEmail());
          }
        });
  }


  @PostMapping
  public Uni<ResponseEntity<TaskClient.Task>> createTask(@RequestBody TaskClient.CreateTaskCommand command) {
    final var worker = securityClient.getUser().getPrincipal();
    return taskClient.taskBuilder()
        .userId(worker.getUsername(), worker.getEmail())
        .createTask(command)
        .onItem().invoke(newTask -> {
          mqEventPublisher.publishMqEvent(newTask, TaskCommentSource.FRONTDESK);
        })
        .onItem().invoke(modifiedTask -> dialobCreateEventPublisher.publishCreateEvent(modifiedTask))
        .onItem().transform(newTask -> {
          return new ResponseEntity<>(newTask, HttpStatus.CREATED);
        });
  }
  
  @PutMapping("/{id}")
  public Uni<ResponseEntity<TaskClient.Task>> saveTask(@PathVariable("id") String id, @RequestBody TaskClient.ModifyTaskCommand command) {
    final var worker = securityClient.getUser().getPrincipal();
    return taskClient.taskBuilder()
      .userId(worker.getUsername(), worker.getEmail())
      .modifyTask(id, command)
      .onItem().invoke(modifiedTask -> mqEventPublisher.publishMqEvent(modifiedTask, TaskCommentSource.FRONTDESK))
      .onItem().invoke(modifiedTask -> dialobCreateEventPublisher.publishCreateEvent(modifiedTask))
      .onItem().transform(modifiedTask -> {
        return new ResponseEntity<>(modifiedTask, HttpStatus.OK);
      });
  }
  
  @PutMapping("/{id}/transfers")
  public Uni<ResponseEntity<TaskClient.Task>> saveTask(@PathVariable("id") String id, @RequestBody TaskClient.TransferTaskCommand command) {
    final var worker = securityClient.getUser().getPrincipal();
    return taskClient.taskBuilder()
      .userId(worker.getUsername(), worker.getEmail())
      .transferTask(id, command)
      .onItem().invoke(modifiedTask -> mqEventPublisher.publishMqEvent(modifiedTask, TaskCommentSource.FRONTDESK))
      .onItem().transform(modifiedTask -> {
        return new ResponseEntity<>(modifiedTask, HttpStatus.OK);
      });
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Uni<TaskClient.Task> deleteTask(@PathVariable("id") String id) {
    final var worker = securityClient.getUser().getPrincipal();
    return taskClient.taskBuilder()
        .userId(worker.getUsername(), worker.getEmail())
        .deleteTask(id);
  }
  
  @GetMapping(value="/unread")
  public Uni<List<String>> getUnreadTasks() {
    final var worker = securityClient.getUser().getPrincipal();
    
    if (worker.isAdmin()) {
      return taskClient.queryUnreadUserTasks()
          .workerId(worker.getUsername())
          .findAll();
    } 
    return taskClient.queryUnreadUserTasks()
        .workerId(worker.getUsername())
        .requireAnyRoles(worker.getRoles())
        .findAll();
  }
  
  @GetMapping(value="/dashboard")
  public Uni<TaskDasboard> getDashboard() {
    final var worker = securityClient.getUser().getPrincipal();
    
    return taskClient.queryTaskDasboard().requireAnyRoles(worker.getRoles()).findAll();
  }

  
  @GetMapping(value="/in-house")
  public List<WorkflowProgram> findAllInhouse() {
    
    final var inhouse = new ArrayList<WorkflowProgram>();
    this.envirClient.getBundle().queryWorkflows().forEach(wk -> {
      if(wk.getInHouse()) {
        inhouse.add(wk);
      }
    });
    return inhouse;
  }
  
  @GetMapping(value = "/in-house/{id}")
  public WorkflowFormResult getInHouseSession(
      @PathVariable("id") String id,
      @RequestParam("locale") String clientLocale,
      @RequestParam(name = "tenantId", required = false) String tenantId
  ) throws UserActionNotAllowedException, WorkflowNotFoundException {
    
    final var worker = securityClient.getUser().getPrincipal();
    
    TaskAssert.notNull(id, () -> "id can't be null!");
    TaskAssert.notNull(clientLocale, () -> "clientLocale can't be null!");
    

    final var props = ImmutableWorkflowDefaultProps.builder().locale(clientLocale).build();    
    final var wk = envirClient.withTenant(Optional.ofNullable(tenantId))
        .getBundle()
        .queryWorkflows()
        .name(id)
        .locale(clientLocale)
        .findOne();
    
    if(wk.isEmpty()) {
      throw new WorkflowNotFoundException(new StringBuilder()
        .append("Can't find stencil service for locale: '").append(clientLocale).append("'!")
        .toString());
    }
    
    final var customer = ImmutableParticipant.builder()
        .identity(worker.getSub())
        .partId(ImmutableParticipantId.builder().realId(id).hashId(Hashing
            .murmur3_128()
            .hashString(worker.getSub(), StandardCharsets.UTF_8)
            .toString()).build())
        .username(worker.getUsername())
        
        .anon(false)
        .protectionOrder(false)
        .build();
    
    final var wkResult = wk.get().runForm(customer, props);
    
    if(wkResult.getAccessAllowed()) {
      return wkResult;
    }
    throw new UserActionNotAllowedException("Process: " + customer + " blocked!");
  }
 

  @GetMapping(value="/in-house/{sessionId}/actions")
  public Uni<?> getInHouseSessionGet(@PathVariable("sessionId") String sessionId) {
    return envirClient.getProperties().getFormDb().withTenant().createFormFill().formInstanceId(sessionId).build()
        .onItem().transform(questionnaire -> questionnaire.unwrap());
  }
  
  @PostMapping(value="/in-house/{sessionId}/actions")
  public Uni<?> getInHouseSessionPost(@PathVariable("sessionId") String sessionId, @RequestBody String body) {
    return envirClient.getProperties().getFormDb().withTenant().createFormFill().formInstanceId(sessionId).actions(body)
        .onCompletion(completion -> {
          final var event = UserActionFillEvent.builder()
              .requestBody(body)
              .responseBody(completion)
              .sessionId(sessionId)
              .build();
          return Uni.createFrom().item(event).onItem().invoke(i -> dialobFillEventPublisher.publishEvent(i));
        })
        .build()
        .onItem().transform(questionnaire -> questionnaire.unwrap());
  }
  @PostMapping(value="/in-house/{sessionId}/attachments")
  public Uni<ResponseEntity<List<UserActionAttachment>>> createAttachments(
      @PathVariable("actionId") String actionId, 
      @RequestBody List<UserAttachmentUploadInit> raw) {

    return new UserAttachmentBuilderImpl(this.taskClient, attachmentCommands)
        .actionId(actionId)
        .addAll(raw)
        .createMany()
        
        .collect().asList().onItem().transform(entries -> new ResponseEntity<>(entries, HttpStatus.CREATED))
        .onFailure().recoverWithItem(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
        
  }
  
  @GetMapping(value="/in-house/{sessionId}/review")
  public Uni<Map<String, Object>> getInHouseReviewGet(@PathVariable("sessionId") String sessionId) {
    return envirClient.getProperties().getFormDb().withTenant()
        .formInstanceQuery().includeForm(true)
        .getOne(sessionId)
        .onItem()
        .transform(prop -> Map.of("session", prop.getQuestionnaire(), "form", prop.getForm().get()));
    
  }
  
  @GetMapping(value="/{id}/comments")
  public Multi<TaskClient.TaskComment> getTaskComments(@PathVariable("id") String id)
  {
    final var authentication = securityClient.getUser();    
    return taskClient.queryTaskComments().findAllByTaskId(id)
        .onItem().call(comments -> taskClient.taskBuilder()
            .userId(authentication.getPrincipal().getUsername(), null)
            .addWorkerCommitViewer(id));
  }
  
  @PostMapping(value="/{id}/comments")
  public Uni<TaskClient.TaskComment> createComment(@RequestBody TaskClient.CreateTaskCommentCommand command) 
  {
    final var worker = securityClient.getUser().getPrincipal();
    return taskClient.taskBuilder()
        .userId(worker.getUsername(), worker.getEmail())
        .createTaskComment(command)
        .onItem().invoke(newComment -> {
          mqEventPublisher.publishMqEvent(newComment.getTaskId(), newComment.getVersion(), TaskCommentSource.FRONTDESK);
        });
  }
  
  @GetMapping(value="/{id}/reviews")
  public Uni<ResponseEntity<?>> getTaskFormReview(@PathVariable("id") String id)
  {
    return taskClient.queryTasks().getOneById(id)
    .onItem().transformToUni(task -> {
      
      if(task.getQuestionnaireId() == null) {
        return Uni.createFrom().item(ResponseEntity.notFound().build());
      }
      return envirClient.getProperties().getFormDb().withTenant()
        .formInstanceQuery()
        .includeForm(true)
        .getOne(task.getQuestionnaireId())
        .onItem().transform(instance -> Map.of(
            "form", instance.getForm().get(),
            "session", instance.getQuestionnaire()
        )).map(result -> new ResponseEntity<>(result, HttpStatus.OK));
    });
  }
  
  @GetMapping(value="/{id}/review-actions")
  public Uni<ResponseEntity<?>> getTaskFormReviewActions(@PathVariable("id") String id)
  {
    return taskClient.queryTasks().findAll(Arrays.asList(id))
    .onItem().transformToUni(tasks -> {
      
      final var questionnaireId = Optional.ofNullable(tasks.isEmpty() ? null : tasks.iterator().next())
          .map(task -> task.getQuestionnaireId())
          .orElse(id);
      
      if(questionnaireId == null) {
        return Uni.createFrom().item(ResponseEntity.notFound().build());
      }
      return envirClient.getProperties().getFormDb().withTenant().formFillReview()
          .formInstanceId(questionnaireId).build()
          .onItem().transform(actions -> new ResponseEntity<>(actions, HttpStatus.OK));

    });
  }
  
  @PostMapping(value="/{id}/review-actions")
  public Uni<ResponseEntity<?>> getTaskFormReviewActions(@PathVariable("id") String id, @RequestBody Actions action) {
    
    return taskClient.queryTasks().findAll(Arrays.asList(id))
    .onItem().transformToUni(tasks -> {
      final var questionnaireId = Optional.ofNullable(tasks.isEmpty() ? null : tasks.iterator().next())
          .map(task -> task.getQuestionnaireId())
          .orElse(id);
      
      if(questionnaireId == null) {
        return Uni.createFrom().item(ResponseEntity.notFound().build());
      }
      return envirClient.getProperties().getFormDb().withTenant().formFillReview()
          .formInstanceId(questionnaireId).navigateTo(action).build()
          .onItem().transform(actions -> new ResponseEntity<>(actions, HttpStatus.OK));   
    });
  }
  
  @GetMapping(value="/{id}/audits")
  public Uni<ResponseEntity<?>> getTaskDebug(@PathVariable("id") String id)
  {
    return taskAuditClient.createTaskAuditQuery().findOneTask(id)
    .onItem().transform(task -> {
      
      if(task.isPresent()) {
        return new ResponseEntity<>(task.get(), HttpStatus.OK);
      }
      return ResponseEntity.notFound().build();
      
    });
  }

  @GetMapping("/keywords")
  public Uni<KeyWordsResponse> getKeyWords() {
    return taskClient.queryTaskKeywords().findAllKeywords()
        .onItem().transform(resp -> new KeyWordsResponse(resp));
  }
  
  @Data
  @AllArgsConstructor
  @Builder
  @Jacksonized
  public static class DeleteCustomerTaskAssignment { List<String> assignmentIds; }  
  
  @Data
  @AllArgsConstructor
  public static class KeyWordsResponse { List<String> keyWords; }
  
}
