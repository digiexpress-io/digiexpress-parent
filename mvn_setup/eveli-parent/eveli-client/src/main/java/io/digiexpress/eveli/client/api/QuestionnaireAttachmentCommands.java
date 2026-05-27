package io.digiexpress.eveli.client.api;

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

import io.digiexpress.eveli.client.api.AttachmentCommands.Attachment;
import io.digiexpress.eveli.client.api.PdfClient.PdfRequestFields;

public interface QuestionnaireAttachmentCommands {
  // only used in WRENCH FLOW
  QuestionnaireAttachmentBuilder attachmentBuilder();

  interface QuestionnaireAttachmentBuilder {
    QuestionnaireAttachmentBuilder processId(String processId);
    QuestionnaireAttachmentBuilder docType(String docType);
    QuestionnaireAttachmentBuilder docCategory(String docCategory);
    QuestionnaireAttachmentBuilder taskId(String taskId);
    QuestionnaireAttachmentBuilder questionnaireId(String questionnaireId);
    QuestionnaireAttachmentBuilder fields(PdfRequestFields... inputFields);
    /**
     * 
     * @param pattern - filename pattern with following variables recognized:
     * <li> $TASK_REF -task reference
     * <li> $FORM_NAME - name of form
     * <li> $CLIENT_NAME - name of client
     * Default pattern if not specified is: $FORM_NAME-$TASK_REF.pdf
     * Spaces in resulting filename (e.g. from client name) are replaced with underscores.
     * @return
     */
    QuestionnaireAttachmentBuilder attachmentPattern(String pattern);
    Attachment build();
  }
}
