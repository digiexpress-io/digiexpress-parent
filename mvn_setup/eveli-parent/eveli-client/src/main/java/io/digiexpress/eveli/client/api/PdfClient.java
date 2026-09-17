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

import java.util.Collection;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.eveli.client.api.TaskClient.ProcessInstance;
import io.digiexpress.eveli.client.api.TaskClient.Task;
import jakarta.annotation.Nullable;

public interface PdfClient {

  ProcessQuestionnairePdfBuilder pdfBuilder();

  enum PdfRequestFields {
    CUSTOMER_NAME,
    CUSTOMER_SSN,
    EXTERNAL_COMMENTS
  }

  interface ProcessQuestionnairePdfBuilder {
    ProcessQuestionnairePdfBuilder processId(String processId);
    ProcessQuestionnairePdfBuilder taskId(String taskId);
    ProcessQuestionnairePdfBuilder process(ProcessInstance process);
    ProcessQuestionnairePdfBuilder task(Task task);
    ProcessQuestionnairePdfBuilder questionnaireId(String questionnaireId);
    ProcessQuestionnairePdfBuilder requestFields(PdfRequestFields ...field);
    ProcessQuestionnairePdfBuilder requestFields(Collection<PdfRequestFields> fields);
    ProcessQuestionnairePdfBuilder docType(String dt);
    ProcessQuestionnairePdfBuilder docCategory(String dc);
    byte[] build();
  }

  @JsonSerialize(as = ImmutableQuestionnairePdfRequest.class)
  @JsonDeserialize(as = ImmutableQuestionnairePdfRequest.class)
  @Value.Immutable
  interface QuestionnairePdfRequest {
    @Nullable String getQuestionnaireId();
    List<PdfRequestFields> getFields();
  }

  public static class PdfNotCompletedException extends RuntimeException {
    private static final long serialVersionUID = -7523524077568331982L;
    public PdfNotCompletedException(String message) {
      super(message);
    }
  }

  public static class PdfNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 8635913148318841936L;
    public PdfNotFoundException(String message) {
      super(message);
    }
  }
}
