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
import io.digiexpress.eveli.client.api.GamutAuthClient;
import io.digiexpress.eveli.client.api.GamutClient;
import io.digiexpress.eveli.client.api.ImmutableUserAction;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.ProcessClient.ProcessStatus;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import io.digiexpress.eveli.client.spi.mq.MqEventPublisher;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.resys.thena.api.entities.grim.GrimProcess.GrimProcessType;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GamutClientImpl implements GamutClient {
  private final ProcessClient processInstanceClient;
  private final TaskClient taskClient;
  private final MqEventPublisher mqEventPublisher;
  private final AttachmentCommands attachmentsCommands;
  private final DialobClient dialobCommands;
  private final GamutAuthClient authClient;
  private final EveliEnvirClient envir;


  @Override
  public UserActionFillEventBuilder fillEvent() {
    return new UserActionFillEventBuilderImpl();
  }
  
  @Override
  public UserActionBuilder userActionBuilder() {
    return new UserActionsBuilderImpl(processInstanceClient, dialobCommands, envir);
  }

  @Override
  public UserActionQuery userActionQuery() {
    return new UserActionsQueryImpl(processInstanceClient, taskClient, authClient, attachmentsCommands);
  }

  @Override
  public UserMessagesQuery userMessagesQuery() {
    return new UserMessagesQueryImpl(processInstanceClient, taskClient, authClient);
  }

  @Override
  public UserAttachmentBuilder userAttachmentBuilder() {
    return new UserAttachmentBuilderImpl(processInstanceClient, attachmentsCommands);
  }

  @Override
  public ReplyToBuilder replyToBuilder() {
    return new ReplyToBuilderImpl(processInstanceClient, taskClient, authClient, mqEventPublisher);
  }

  @Override
  public AttachmentDownloadQuery attachmentDownloadQuery() {
    return new AttachmentDownloadQueryImpl(processInstanceClient, attachmentsCommands);
  }

  @Override
  public CancelUserActionBuilder cancelUserActionBuilder() {
    return new CancelUserActionBuilder() {
      
      private String actionId;
      @Override
      public UserAction cancelOne() throws ProcessNotFoundException, ProcessCantBeDeletedException {
        TaskAssert.notNull(actionId, () -> "actionId can't be null!");

        final var process = processInstanceClient.queryInstances().findOneById(actionId)
            .orElseThrow(() -> new ProcessNotFoundException("Process not found by id: " + actionId + "!"));
                
        if (process.getStatus() != ProcessStatus.ANSWERING && process.getStatus() != ProcessStatus.CREATED) {
          throw new ProcessCantBeDeletedException("Can't delete process with answered questionnaire, id: " + actionId);
        }
        
        processInstanceClient.queryInstances().deleteOneById(process.getId());
      
        return ImmutableUserAction.builder()
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
            .build();
      }
      
      @Override
      public CancelUserActionBuilder actionId(String actionId) {
        TaskAssert.notNull(actionId, () -> "actionId can't be null!");
        this.actionId = actionId;
        return this;
      }
    };
  }

  @Override
  public UserActionMetaQuery userActionMetaQuery() {
    return new UserActionMetaQueryImpl(envir);
  }

  @Override
  public UserActionViewBuilder userActionViewBuilder() {

    return new UserActionViewBuilder() {
      private String actionId;
      @Override
      public UserActionViewBuilder actionId(String actionId) {
        TaskAssert.notNull(actionId, () -> "actionId can't be null!");
        this.actionId = actionId;
        return this;
      }
      @Override
      public Uni<Void> create() {
        final var action = userActionQuery().findOneById(actionId);
        if(action.isEmpty()) {
          return Uni.createFrom().voidItem();        
        }
        final var taskId = action.get().getTaskId();
        if(taskId == null) {
          return Uni.createFrom().voidItem();
        }
        
        final var customerId = authClient.getCustomer().getCustomerId();
        return taskClient.taskBuilder()
            .userId(customerId.getSafeId(), null)
            .addCustomerCommitViewer(taskId);
      }
    };
  }
}
