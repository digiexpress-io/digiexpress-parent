package io.digiexpress.eveli.client.api;

import io.digiexpress.eveli.client.api.AttachmentCommands.Attachment;

public interface QuestionnaireAttachmentCommands {
  
  QuestionnaireAttachmentBuilder attachmentBuilder();

  public interface QuestionnaireAttachmentBuilder {
    QuestionnaireAttachmentBuilder processId(String processId);
    QuestionnaireAttachmentBuilder taskId(String taskId);
    QuestionnaireAttachmentBuilder questionnaireId(String questionnaireId);
    Attachment build();
  }
}
