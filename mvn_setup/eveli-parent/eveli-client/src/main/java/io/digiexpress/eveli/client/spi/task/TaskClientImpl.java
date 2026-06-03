package io.digiexpress.eveli.client.spi.task;

import java.time.Duration;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.digiexpress.eveli.client.api.ImmutableTaskArchivePointer;
import io.digiexpress.eveli.client.api.ImmutableTaskDasboard;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskFileClient;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import io.digiexpress.eveli.client.spi.crm.CustomerAccountClientImpl;
import io.digiexpress.eveli.client.spi.dms.DocContainerClient;
import io.digiexpress.eveli.client.spi.task.visitors.AddCustomerCommitViewer;
import io.digiexpress.eveli.client.spi.task.visitors.AddFormToCustomerAssignment;
import io.digiexpress.eveli.client.spi.task.visitors.AddWorkerCommitViewer;
import io.digiexpress.eveli.client.spi.task.visitors.ChangeDocProperties;
import io.digiexpress.eveli.client.spi.task.visitors.CompleteCustomerAssignment;
import io.digiexpress.eveli.client.spi.task.visitors.CreateCustomerAssignment;
import io.digiexpress.eveli.client.spi.task.visitors.CreateOneTask;
import io.digiexpress.eveli.client.spi.task.visitors.CreateOneTaskComment;
import io.digiexpress.eveli.client.spi.task.visitors.CreateProcessVisitor;
import io.digiexpress.eveli.client.spi.task.visitors.DeleteCustomerAssignment;
import io.digiexpress.eveli.client.spi.task.visitors.DeleteOneTask;
import io.digiexpress.eveli.client.spi.task.visitors.FindAllExternalTaskCommentsByReporterIdVisitor;
import io.digiexpress.eveli.client.spi.task.visitors.FindAllTaskByIdsVisitor;
import io.digiexpress.eveli.client.spi.task.visitors.FindAllTaskCommentsByTaskIdVisitor;
import io.digiexpress.eveli.client.spi.task.visitors.FindAllTaskVisitor;
import io.digiexpress.eveli.client.spi.task.visitors.FindAllUnreadTasksVisitor;
import io.digiexpress.eveli.client.spi.task.visitors.FindOneTaskByIdVisitor;
import io.digiexpress.eveli.client.spi.task.visitors.FormAssignmentVisitor;
import io.digiexpress.eveli.client.spi.task.visitors.GetOneTaskByIdVisitor;
import io.digiexpress.eveli.client.spi.task.visitors.GetOneTaskCommentByIdVisitor;
import io.digiexpress.eveli.client.spi.task.visitors.ModifyOneTask;
import io.digiexpress.eveli.client.spi.task.visitors.ModifyProcessVisitor;
import io.digiexpress.eveli.client.spi.task.visitors.PaginateTasksImpl;
import io.digiexpress.eveli.client.spi.task.visitors.TaskDiffVisitor;
import io.digiexpress.eveli.client.spi.task.visitors.TransferTaskVisitor;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class TaskClientImpl implements TaskClient {

  
  private final TaskFileClient taskFilesClient;
  private final DocContainerClient docContainerClient;
  private final TaskStore ctx;
  private final io.resys.limaone.program.Runtime envir;

  
  public TaskStore unwrap() {
    return ctx;
  }
  
  @Override
  public PaginateTasks paginateTasks() {
    return new PaginateTasksImpl(ctx);
  }
  
  @Override
  public QueryFormAssignments queryFormAssignments() {
    return new QueryFormAssignments() {
      
      @Override
      public Multi<FormAssignment> findAll(String taskId) {
        return new FormAssignmentVisitor(envir, ctx, taskId).accept();
      }
    };
  }
  
  @Override
  public QueryTasks queryTasks() {
    return new QueryTasks() {
      private List<String> requireAnyRoles;
      
      @Override
      public QueryTasks requireAnyRoles(List<String> roles) {
        if (roles != null) {
          if(requireAnyRoles == null) {
            requireAnyRoles = new ArrayList<>();
          }
          requireAnyRoles.addAll(roles);
        }
        return this;
      }
      
      @Override
      public Uni<Task> getOneById(String taskId) {
        TaskAssert.notEmpty(taskId, () -> "taskId can't be empty!");
        return ctx.getConfig().accept(new GetOneTaskByIdVisitor(taskId));
      }
      @Override
      public Uni<List<Task>> findAll(List<String> taskIds) {
        TaskAssert.notNull(taskIds, () -> "taskIds can't be empty!");
        return ctx.getConfig().accept(new FindAllTaskByIdsVisitor(taskIds));
      }
      @Override
      public Uni<TaskDiff> getOneTaskDiff(String taskId, String commitId) {
        TaskAssert.notNull(taskId, () -> "taskId can't be empty!");
        TaskAssert.notNull(commitId, () -> "commitId can't be empty!");
        return new TaskDiffVisitor(ctx, taskId, commitId).accept();
      }
      @Override
      public Uni<List<Task>> findAll() {
        return ctx.getConfig().accept(new FindAllTaskVisitor(requireAnyRoles));
      }
      @Override
      public Uni<Optional<Task>> findOneById(String taskId) {
        TaskAssert.notEmpty(taskId, () -> "taskId can't be empty!");
        return ctx.getConfig().accept(new FindOneTaskByIdVisitor(taskId));
      }
    };
  }

  @Override
  public TaskCommandBuilder taskBuilder() {
    final var taskClient = this;
    return new TaskCommandBuilder() {
      private String userId, userEmail;
      @Override
      public TaskCommandBuilder userId(String userId, String userEmail) {
        this.userId = userId;
        this.userEmail = userEmail;
        return this;
      }
      @Override
      public Uni<TaskComment> createTaskComment(CreateTaskCommentCommand command) {
        TaskAssert.notEmpty(userId, () -> "userId can't be empty!");
        return ctx.getConfig().accept(new CreateOneTaskComment(userId, command));
      }
      @Override
      public Uni<Task> createTask(CreateTaskCommand command) {
        TaskAssert.notEmpty(userId, () -> "userId can't be empty!");
        if(command.getQuestionnaireId() == null) {
          return ctx.getConfig().accept(new CreateOneTask(userId, command, null));  
        }
        return new CustomerAccountClientImpl(taskClient).accountQuery().getOneByAnyId(command.getQuestionnaireId())
          .onItem().transformToUni(account -> ctx.getConfig().accept(new CreateOneTask(userId, command, account)));
        
      }
      @Override
      public Uni<Task> modifyTask(String taskId, ModifyTaskCommand command) {
        TaskAssert.notEmpty(userId, () -> "userId can't be empty!");
        return ctx.getConfig().accept(new ModifyOneTask(userId, userEmail, taskId, command));
      }
      @Override
      public Uni<Task> deleteTask(String taskId) {
        TaskAssert.notEmpty(userId, () -> "userId can't be empty!");
        //TaskAssert.notEmpty(userEmail, () -> "userEmail can't be empty!");
        return ctx.getConfig().accept(new DeleteOneTask(userId, userEmail, taskId));
      }
      @Override
      public Uni<Void> addWorkerCommitViewer(String taskId) {
        TaskAssert.notEmpty(userId, () -> "userId can't be empty!");
        TaskAssert.notEmpty(taskId, () -> "taskId can't be empty!");
        
        final var config = ctx.getConfig();
        return config.getClient().grim(ctx.getConfig().getTenantName())
          .find().commitViewersQuery().createdIn(Duration.ofHours(1))
          .usedBy(userId)
          .usedFor(TaskMapper.VIEWER_WORKER)
          .missionId(taskId)
          .findAll().onItem().transformToUni(views -> {
            return ctx.getConfig().accept(new AddWorkerCommitViewer(userId, taskId, views))
                .onItem().transformToUni((task) -> Uni.createFrom().voidItem());            
          });
      }
      @Override
      public Uni<Void> addCustomerCommitViewer(String taskId) {
        TaskAssert.notEmpty(userId, () -> "userId can't be empty!");
        TaskAssert.notEmpty(taskId, () -> "taskId can't be empty!");
        final var config = ctx.getConfig();
        
        return config.getClient().grim(ctx.getConfig().getTenantName())
          .find().commitViewersQuery().createdIn(Duration.ofHours(1))
          .usedBy(userId)
          .usedFor(TaskMapper.VIEWER_CUSTOMER)
          .missionId(taskId)
          .findAll().onItem().transformToUni(views -> {
            return ctx.getConfig().accept(new AddCustomerCommitViewer(userId, taskId, views))
                .onItem().transformToUni((task) -> Uni.createFrom().voidItem());            
          });
      }
      @Override
      public Uni<Task> transferTask(String taskId, TransferTaskCommand command) {
        TaskAssert.notEmpty(userId, () -> "userId can't be empty!");
        TaskAssert.notEmpty(taskId, () -> "taskId can't be empty!");
        return new TransferTaskVisitor(envir, ctx, taskFilesClient, docContainerClient, userId, taskId, command).accept();
      }
      @Override
      public Uni<Task> completeCustomerAssignment(String taskId, CompleteCustomerAssignmentCommand command) {
        TaskAssert.notEmpty(userId, () -> "userId can't be empty!");
        TaskAssert.notEmpty(taskId, () -> "taskId can't be empty!");
        return ctx.getConfig().accept(new CompleteCustomerAssignment(userId, taskId, command));
      }
      @Override
      public Uni<Task> createCustomerAssignment(String taskId, List<CreateCustomerAssignmentCommand> command) {
        TaskAssert.notEmpty(userId, () -> "userId can't be empty!");
        TaskAssert.notEmpty(taskId, () -> "taskId can't be empty!");
        
        return ctx.getConfig().accept(new CreateCustomerAssignment(userId, taskId, command, envir));
      }
      @Override
      public Uni<Task> addFormToCustomerAssignment(String taskId, List<AddFormToCustomerAssignmentCommand> command) {
        TaskAssert.notEmpty(taskId, () -> "taskId can't be empty!");
        
        return ctx.getConfig().accept(new AddFormToCustomerAssignment(userId, taskId, command));
      }
      @Override
      public Uni<Task> deleteCustomerTaskAssignment(String taskId, List<String> assignmentId) {
        TaskAssert.notEmpty(userId, () -> "userId can't be empty!");
        TaskAssert.notEmpty(taskId, () -> "taskId can't be empty!");
        TaskAssert.notNull(assignmentId, () -> "assignmentId can't be null!");
        TaskAssert.isTrue(!assignmentId.isEmpty(), () -> "assignmentId can't be empty!");

        return ctx.getConfig().accept(new DeleteCustomerAssignment(userId, taskId, assignmentId));
      }
      @Override
      public Uni<Task> changeDocProperties(String taskId, ChangeDocPropertiesCommand command) {
        TaskAssert.notEmpty(taskId, () -> "taskId can't be empty!");
        return ctx.getConfig().accept(new ChangeDocProperties(userId, taskId, command));
      }
    };
  }

  @Override
  public QueryTaskComments queryTaskComments() {
    return new QueryTaskComments() {
      @Override
      public Uni<TaskComment> getOneById(String commentId) {
        return ctx.getConfig().accept(new GetOneTaskCommentByIdVisitor(commentId));
      }
      @Override
      public Multi<TaskComment> findAllByTaskId(String taskId) {        
        return ctx.getConfig().accept(new FindAllTaskCommentsByTaskIdVisitor(taskId))
            .map(e -> e.stream())
            .onItem().transformToMulti(Multi.createFrom()::items);
      }
      @Override
      public Multi<TaskComment> findAllByReporterId(String reporterId) {
        return ctx.getConfig().accept(new FindAllExternalTaskCommentsByReporterIdVisitor(reporterId))
            .map(e -> e.stream())
            .onItem().transformToMulti(Multi.createFrom()::items);
      }
    };
  }

  @Override
  public QueryTaskKeywords queryTaskKeywords() {
    return new QueryTaskKeywords() {
      @Override
      public Uni<List<String>> findAllKeywords() {
        final var config = ctx.getConfig();
        final var grim = config.getClient().grim(config.getTenantName());
        final Uni<List<String>> items = grim.find()
            .missionLabelQuery().findAllUnique()
            .map(e -> new ArrayList<>(e.stream()
                .map(x -> x.getLabelValue())
                .collect(Collectors.toSet()))
            );
        return items;
      }
    };
  }
  @Override
  public QueryUnreadUserTasks queryUnreadUserTasks() {
    return new QueryUnreadUserTasks() {
      private String workerId;
      private String customerId;
      private final List<String> roles = new ArrayList<>();
      @Override
      public QueryUnreadUserTasks workerId(String workerId) {
        this.workerId = workerId;
        return this;
      }
      @Override
      public QueryUnreadUserTasks customerId(String customerId) {
        this.customerId = customerId;
        return this;
      }
      @Override
      public QueryUnreadUserTasks requireAnyRoles(List<String> roles) {
        TaskAssert.notEmpty("roles", () -> "roles can't be empty!");
        this.roles.addAll(roles);
        return this;
      }
      @Override
      public Uni<List<String>> findAll() {
        TaskAssert.isTrue(workerId != null || customerId != null, () -> "workerId or customerId both can't be empty!");
        
        if(customerId != null) {
          return ctx.getConfig().accept(new FindAllUnreadTasksVisitor(customerId, roles, TaskMapper.VIEWER_CUSTOMER));          
        }
        return ctx.getConfig().accept(new FindAllUnreadTasksVisitor(workerId, roles, TaskMapper.VIEWER_WORKER));
      }

    };
  }
  @Override
  public QueryTaskDasboard queryTaskDasboard() {
    return new QueryTaskDasboard() {
      private final List<String> requireAnyRoles = new ArrayList<>();
      @Override
      public QueryTaskDasboard requireAnyRoles(List<String> roles) {
        TaskAssert.notEmpty("roles", () -> "roles can't be empty!");
        this.requireAnyRoles.addAll(roles);
        return this;
      }
      @Override
      public Uni<TaskDasboard> findAll() {
        final var config = ctx.getConfig();
        final var grim = config.getClient().grim(config.getTenantName());
        return grim.find().missionStatsQuery().findAllByMissionAttributes(requireAnyRoles)
          .onItem().transform(resp -> {
            if(resp.getStatus() != QueryEnvelopeStatus.OK) {
              throw TaskException.builder("FIND_TASK_DASKBOARD_FAIL")
                .add(grim, resp)
                .build();
            }
            final var result = resp.getObjects();
            if(result == null) {
              throw TaskException.builder("FIND_TASK_DASKBOARD_NO_FOUND")   
                .add(grim, resp)
                .build();
            }
            return ImmutableTaskDasboard.builder().events(result).build();
          });
      }
    };
  }

  @Override
  public DeleteTasks deleteTasks() {

    return new DeleteTasks() {
      private String commitMessage;
      private String commitAuthor;
      @Override
      public DeleteTasks commitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
        return this;
      }
      @Override
      public DeleteTasks commitAuthor(String commitAuthor) {
        this.commitAuthor = commitAuthor;
        return this;
      }
      @Override
      public Uni<TaskArchivePointer> deleteOne(String id) {
        TaskAssert.notEmpty(commitMessage, () -> "commitMessage can't be empty!");
        TaskAssert.notEmpty(commitAuthor, () -> "commitAuthor can't be empty!");
        TaskAssert.notEmpty(id, () -> "id can't be empty!");
        
        final var config = ctx.getConfig();
        final var grim = config.getClient().grim(config.getTenantName());
        
        return grim.find().missionDeleteQuery()
          .commitAuthor(commitAuthor)
          .commitMessage(commitMessage)
          .missionId(Arrays.asList(id))
          .deleteAll()
          .onItem().transform(resp -> {
            final TaskArchivePointer pointer = ImmutableTaskArchivePointer.builder()
                .commit(resp)
                .build();
            return pointer;
          }).collect().last();
      }
    };
  }    

  @Override
  public DeleteProcesses deleteProcesses() {

    return new DeleteProcesses() {
      private String commitMessage;
      private String commitAuthor;
      @Override
      public DeleteProcesses commitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
        return this;
      }
      @Override
      public DeleteProcesses commitAuthor(String commitAuthor) {
        this.commitAuthor = commitAuthor;
        return this;
      }
      @Override
      public Uni<ProcessInstance> deleteOne(String id) {
        TaskAssert.notEmpty(commitMessage, () -> "commitMessage can't be empty!");
        TaskAssert.notEmpty(commitAuthor, () -> "commitAuthor can't be empty!");
        TaskAssert.notEmpty(id, () -> "id can't be empty!");
        
        final var config = ctx.getConfig();
        final var grim = config.getClient().grim(config.getTenantName());
        
        return grim.find().missionProcDeleteQuery()
          .commitAuthor(commitAuthor)
          .commitMessage(commitMessage)
          .procId(id)
          .deleteOne()
          .map(TaskMapper::map);
        };
    };
  }    

  @Override
  public QueryTaskProcesess queryTaskProcesess() {
    return new QueryTaskProcesess() {
      private boolean includeQuestionnaire = false;

      @Override
      public QueryTaskProcesess includeQuestionnaire() {
        this.includeQuestionnaire = true;
        return this;
      }

      @Override
      public Multi<ProcessInstance> findAllInLast6Months() {
        final var startDate = OffsetDateTime.now().minusMonths(6);
        final var config = ctx.getConfig();
        final var grim = config.getClient().grim(config.getTenantName());
        return grim.find().missionProcsQuery()
            .includeFormBody(includeQuestionnaire)
          .findAllOnOrAfter(startDate)
          .map(TaskMapper::map);
      }

      @Override
      public Multi<ProcessInstance> findAllStaleWithoutTasks(OffsetDateTime olderThen) {
        final var config = ctx.getConfig();
        final var grim = config.getClient().grim(config.getTenantName());
        return grim.find().missionProcsQuery()
          .includeFormBody(includeQuestionnaire)
          .findAllOnOrBeforeWithoutMission(olderThen)
          .map(TaskMapper::map);
      }

      @Override
      public Uni<Optional<ProcessInstance>> findOneByTaskId(String taskId) {
        final var config = ctx.getConfig();
        final var grim = config.getClient().grim(config.getTenantName());
        return grim.find().missionProcsQuery()
          .includeFormBody(includeQuestionnaire)
          .findOneByMissionId(taskId)
          .map(optional -> optional.map(TaskMapper::map));
      }
      @Override
      public Uni<ProcessInstance> getOneById(String processId) {
        final var config = ctx.getConfig();
        final var grim = config.getClient().grim(config.getTenantName());
        return grim.find().missionProcsQuery()
          .includeFormBody(includeQuestionnaire)
          .getOneById(processId)
          .map(TaskMapper::map);
      }

      @Override
      public Uni<Optional<ProcessInstance>> findOneById(String processId) {
        final var config = ctx.getConfig();
        final var grim = config.getClient().grim(config.getTenantName());
        return grim.find().missionProcsQuery()
          .includeFormBody(includeQuestionnaire)
          .findOneById(processId)
          .map(optional -> optional.map(TaskMapper::map));
      }

      @Override
      public Multi<ProcessInstance> findAllNotArchivedyUserId(String userId) {
        final var config = ctx.getConfig();
        final var grim = config.getClient().grim(config.getTenantName());
        return grim.find().missionProcsQuery()
          .includeFormBody(includeQuestionnaire)
          .findAllNotArchivedyUserId(userId)
          .map(TaskMapper::map);
      }

      @Override
      public Uni<Optional<ProcessInstance>> findOneByQuestionnaireId(String questionnaireId) {
        final var config = ctx.getConfig();
        final var grim = config.getClient().grim(config.getTenantName());
        return grim.find().missionProcsQuery()
          .includeFormBody(includeQuestionnaire)
          .findOneByQuestionnaireId(questionnaireId)
          .map(optional -> optional.map(TaskMapper::map));
      }

      @Override
      public Multi<ProcessInstance> findAll() {
        final var config = ctx.getConfig();
        final var grim = config.getClient().grim(config.getTenantName());
        return grim.find().missionProcsQuery()
          .includeFormBody(includeQuestionnaire)
          .findAll()
          .map(TaskMapper::map);
      }

      @Override
      public Multi<ProcessInstance> findAllExpired() {
        final var config = ctx.getConfig();
        final var grim = config.getClient().grim(config.getTenantName());
        return grim.find().missionProcsQuery()
          .includeFormBody(includeQuestionnaire)
          .findAllExpired()
          .map(TaskMapper::map);
      }

      @Override
      public Multi<ProcessInstance> findAllAnsweredFrom(OffsetDateTime pickupFrom) {
        final var config = ctx.getConfig();
        final var grim = config.getClient().grim(config.getTenantName());
        return grim.find().missionProcsQuery()
          .includeFormBody(includeQuestionnaire)
          .findAllAnsweredFrom(pickupFrom)
          .map(TaskMapper::map);
      }
    };
  }
  @Override
  public ModifyProcess modifyProcess() {
    return new ModifyProcessVisitor(ctx);
  }
  @Override
  public CreateProcess createProcess() {
    return new CreateProcessVisitor(ctx);
  }
}
