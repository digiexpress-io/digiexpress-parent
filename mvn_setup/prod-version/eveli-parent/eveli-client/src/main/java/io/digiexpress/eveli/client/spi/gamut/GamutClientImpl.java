package io.digiexpress.eveli.client.spi.gamut;

import io.digiexpress.eveli.client.api.AttachmentCommands;
import io.digiexpress.eveli.client.api.CrmClient;
import io.digiexpress.eveli.client.api.GamutClient;
import io.digiexpress.eveli.client.api.ImmutableUserAction;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.ProcessClient.ProcessStatus;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GamutClientImpl implements GamutClient {
  private final ProcessClient processInstanceClient;
  private final TaskClient taskClient;
  
  private final AttachmentCommands attachmentsCommands;
  private final DialobClient dialobCommands;
  private final CrmClient authClient;
  private final EveliEnvirClient envir;


  @Override
  public UserActionFillEventBuilder fillEvent() {
    return new UserActionFillEventBuilderImpl();
  }
  
  @Override
  public UserActionBuilder userActionBuilder() {
    return new UserActionsBuilderImpl(processInstanceClient, dialobCommands, authClient, envir);
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
    return new ReplyToBuilderImpl(processInstanceClient, taskClient, authClient);
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

  @Override
  public UserActionMetaQuery userActionMetaQuery() {
    return new UserActionMetaQueryImpl(envir);
  }
}
