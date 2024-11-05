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

import io.digiexpress.eveli.assets.api.EveliAssetClient;
import io.digiexpress.eveli.client.api.AttachmentCommands;
import io.digiexpress.eveli.client.api.CrmClient;
import io.digiexpress.eveli.client.api.GamutClient;
import io.digiexpress.eveli.client.api.HdesCommands;
import io.digiexpress.eveli.client.api.ProcessCommands.ProcessStatus;
import io.digiexpress.eveli.client.persistence.repositories.CommentRepository;
import io.digiexpress.eveli.client.persistence.repositories.ProcessRepository;
import io.digiexpress.eveli.client.persistence.repositories.TaskAccessRepository;
import io.digiexpress.eveli.client.persistence.repositories.TaskRepository;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.thestencil.iam.api.ImmutableUserAction;
import io.thestencil.iam.api.UserActionsClient.UserAction;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GamutClientImpl implements GamutClient {
  private final ProcessRepository processRepository;
  private final TaskRepository taskRepository;
  private final CommentRepository commentRepository;
  private final TaskAccessRepository taskAccessRepository;
  
  private final AttachmentCommands attachmentsCommands;
  private final DialobClient dialobCommands;
  private final HdesCommands hdesCommands;
  private final EveliAssetClient assetClient;
  private final CrmClient authClient;

  @Override
  public UserActionBuilder userActionBuilder() {
    return new UserActionsBuilderImpl(processRepository, dialobCommands, hdesCommands, assetClient, authClient);
  }

  @Override
  public UserActionQuery userActionQuery() {
    return new UserActionsQueryImpl(taskRepository, processRepository, authClient, hdesCommands, attachmentsCommands);
  }

  @Override
  public UserMessagesQuery userMessagesQuery() {
    return new UserMessagesQueryImpl(processRepository, commentRepository, taskRepository, taskAccessRepository, authClient);
  }

  @Override
  public UserAttachmentBuilder userAttachmentBuilder() {
    return new UserAttachmentBuilderImpl(processRepository, attachmentsCommands);
  }

  @Override
  public ReplyToBuilder replyToBuilder() {
    return new ReplyToBuilderImpl(commentRepository, processRepository, taskRepository, taskAccessRepository, authClient);
  }

  @Override
  public AttachmentDownloadQuery attachmentDownloadQuery() {
    return new AttachmentDownloadQueryImpl(processRepository, attachmentsCommands);
  }

  @Override
  public CancelUserActionBuilder cancelUserActionBuilder() {
    return new CancelUserActionBuilder() {
      
      private String actionId;
      @Override
      public UserAction cancelOne() throws ProcessNotFoundException, ProcessCantBeDeletedException {
        TaskAssert.notNull(actionId, () -> "actionId can't be null!");

        long id = Long.parseLong(actionId);
        final var process = processRepository.findById(Long.parseLong(actionId))
            .orElseThrow(() -> new ProcessNotFoundException("Process not found by id: " + actionId + "!"));
        
        
        if (process.getStatus() != ProcessStatus.ANSWERING && process.getStatus() != ProcessStatus.CREATED) {
          throw new ProcessCantBeDeletedException("Can't delete process with answered questionnaire, id: " + actionId);
        }
        
        processRepository.deleteById(id);
      
        return ImmutableUserAction.builder()
            .id(process.getId().toString())
            .status(process.getStatus().name())
            .created(process.getCreated())
            .updated(process.getUpdated())
            .name(process.getWorkflowName())
            .inputContextId(process.getInputContextId())
            .inputParentContextId(process.getInputParentContextId())
            .formId(process.getQuestionnaire())
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
