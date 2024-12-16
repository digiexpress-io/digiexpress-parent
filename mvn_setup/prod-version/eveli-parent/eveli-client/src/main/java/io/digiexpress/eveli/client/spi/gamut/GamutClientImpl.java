package io.digiexpress.eveli.client.spi.gamut;

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

import java.time.ZoneOffset;
import java.util.function.Supplier;

import io.digiexpress.eveli.assets.api.EveliAssetClient.WorkflowTag;
import io.digiexpress.eveli.client.api.AttachmentCommands;
import io.digiexpress.eveli.client.api.CrmClient;
import io.digiexpress.eveli.client.api.GamutClient;
import io.digiexpress.eveli.client.api.ImmutableUserAction;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.ProcessClient.ProcessStatus;
import io.digiexpress.eveli.client.persistence.repositories.CommentRepository;
import io.digiexpress.eveli.client.persistence.repositories.TaskAccessRepository;
import io.digiexpress.eveli.client.persistence.repositories.TaskRepository;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import io.thestencil.client.api.MigrationBuilder.Sites;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GamutClientImpl implements GamutClient {
  private final ProcessClient processInstanceClient;
  private final TaskRepository taskRepository;
  private final CommentRepository commentRepository;
  private final TaskAccessRepository taskAccessRepository;
  
  private final AttachmentCommands attachmentsCommands;
  private final DialobClient dialobCommands;
  private final CrmClient authClient;
  private final ZoneOffset offset;
  
  private final Supplier<Sites> siteEnvir;
  private final Supplier<ProgramEnvir> programEnvir;
  private final Supplier<WorkflowTag> workflowEnvir;


  @Override
  public UserActionFillEventBuilder fillEvent() {
    return new UserActionFillEventBuilderImpl();
  }
  
  @Override
  public UserActionBuilder userActionBuilder() {
    return new UserActionsBuilderImpl(processInstanceClient, dialobCommands, siteEnvir, programEnvir, workflowEnvir, authClient, offset);
  }

  @Override
  public UserActionQuery userActionQuery() {
    return new UserActionsQueryImpl(processInstanceClient, taskRepository, authClient, attachmentsCommands);
  }

  @Override
  public UserMessagesQuery userMessagesQuery() {
    return new UserMessagesQueryImpl(processInstanceClient, commentRepository, taskRepository, taskAccessRepository, authClient);
  }

  @Override
  public UserAttachmentBuilder userAttachmentBuilder() {
    return new UserAttachmentBuilderImpl(processInstanceClient, attachmentsCommands);
  }

  @Override
  public ReplyToBuilder replyToBuilder() {
    return new ReplyToBuilderImpl(processInstanceClient, commentRepository, taskRepository, taskAccessRepository, authClient);
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
        
        processInstanceClient.queryInstances().deleteOneById(actionId);
      
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
            .viewed(true)
            
            // deprecated
            .messagesUri("not-needed")
            .reviewUri("not-needed")
            .formUri("not-needed")
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
}
