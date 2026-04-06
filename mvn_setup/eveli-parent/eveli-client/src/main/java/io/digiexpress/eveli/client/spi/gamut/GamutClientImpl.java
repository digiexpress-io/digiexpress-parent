package io.digiexpress.eveli.client.spi.gamut;

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

import io.digiexpress.eveli.client.api.AttachmentCommands;
import io.digiexpress.eveli.client.api.GamutClient;
import io.digiexpress.eveli.client.api.ImmutableUserAction;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import io.digiexpress.eveli.client.spi.mq.MqEventPublisher;
import io.resys.limaone.program.ProgramInput.Participant;
import io.resys.thena.api.entities.grim.GrimProcess.GrimProcessStatus;
import io.resys.thena.api.entities.grim.GrimProcess.GrimProcessType;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GamutClientImpl implements GamutClient {
  private final TaskClient taskClient;
  private final MqEventPublisher mqEventPublisher;
  private final AttachmentCommands attachmentsCommands;
  private final io.resys.limaone.program.Runtime envir;


  @Override
  public UserActionFillEventBuilder fillEvent() {
    return new UserActionFillEventBuilderImpl();
  }
  
  @Override
  public UserActionBuilder userActionBuilder() {
    return new UserActionsBuilderImpl(envir, taskClient);
  }

  @Override
  public UserActionQuery userActionQuery() {
    return new UserActionsQueryImpl(envir, taskClient, attachmentsCommands);
  }

  @Override
  public UserMessagesQuery userMessagesQuery() {
    return new UserMessagesQueryImpl(taskClient);
  }

  @Override
  public UserAttachmentBuilder userAttachmentBuilder() {
    return new UserAttachmentBuilderImpl(taskClient, attachmentsCommands);
  }

  @Override
  public ReplyToBuilder replyToBuilder() {
    return new ReplyToBuilderImpl(taskClient, mqEventPublisher);
  }

  @Override
  public AttachmentDownloadQuery attachmentDownloadQuery() {
    return new AttachmentDownloadQueryImpl(taskClient, attachmentsCommands);
  }

  @Override
  public CancelUserActionBuilder cancelUserActionBuilder() {
    return new CancelUserActionBuilder() {
      
      private Participant customer;
      private String actionId;
      
      @Override
      public Uni<UserAction> cancelOne() {
        TaskAssert.notNull(actionId, () -> "actionId can't be null!");
        TaskAssert.notNull(customer, () -> "customer can't be null!");

        return taskClient.queryTaskProcesess().findOneById(actionId).onItem().transformToUni(found -> {
          
          final var process = found.orElseThrow(() -> new ProcessNotFoundException("Process not found by id: " + actionId + "!"));
                  
          if (process.getStatus() != GrimProcessStatus.ANSWERING && process.getStatus() != GrimProcessStatus.CREATED) {
            throw new ProcessCantBeDeletedException("Can't delete process with answered questionnaire, id: " + actionId);
          }
          
          return taskClient
            .deleteProcesses()
            .commitAuthor(customer.getPartId().getHashId())
            .commitMessage("Customer cancellation")
            .deleteOne(process.getId().toString())
            .map(ignore -> ImmutableUserAction.builder()
                .id(process.getId().toString())
                .status(process.getStatus().name())
                .created(process.getCreated())
                .updated(process.getUpdated())
                .name(process.getWorkflowName())
                .inputContextId(process.getArticleName())
                .inputParentContextId(process.getParentArticleName())
                .formId(process.getQuestionnaireId())
                .formInProgress(true)
                .assigned(process.getType() == GrimProcessType.CUSTOMER_ASSIGNMENT ? true : false)
                .viewed(true)
                .cockpitId(process.getCockpitId())
                .build());
        });
      }
      
      @Override
      public CancelUserActionBuilder actionId(String actionId) {
        TaskAssert.notNull(actionId, () -> "actionId can't be null!");
        this.actionId = actionId;
        return this;
      }
      @Override
      public CancelUserActionBuilder customer(Participant customer) {
        TaskAssert.notNull(customer, () -> "customer can't be null!");
        this.customer = customer;
        return this;
      }
    };
  }
  @Override
  public UserActionViewBuilder userActionViewBuilder() {
    return new UserActionViewBuilder() {
      private String actionId;
      private Participant customer;
      @Override
      public UserActionViewBuilder actionId(String actionId) {
        TaskAssert.notNull(actionId, () -> "actionId can't be null!");
        this.actionId = actionId;
        return this;
      }
      @Override
      public UserActionViewBuilder customer(Participant customer) {
        TaskAssert.notNull(customer, () -> "customer can't be null!");
        this.customer = customer;
        return this;
      }
      @Override
      public Uni<Void> create() {
        TaskAssert.notNull(customer, () -> "customer can't be null!");
        TaskAssert.notNull(actionId, () -> "actionId can't be null!");
        
        return userActionQuery().customer(customer).findOneById(actionId)
          .onItem().transformToUni(action -> {
          
            if(action.isEmpty()) {
              return Uni.createFrom().voidItem();        
            }
            final var taskId = action.get().getTaskId();
            if(taskId == null) {
              return Uni.createFrom().voidItem();
            }
            
            final var customerId = customer.getPartId();
            return taskClient.taskBuilder()
                .userId(customerId.getHashId(), null)
                .addCustomerCommitViewer(taskId);
        });
      }
    };
  }
}
