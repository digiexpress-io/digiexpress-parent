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

import io.digiexpress.eveli.client.api.TaskClient.ProcessInstance;
import io.digiexpress.eveli.client.api.TaskClient.Task;

public interface PdfClient {

  ProcessQuestionnairePdfBuilder pdfBuilder();
  public enum PdfRequestFields {
    CUSTOMER_NAME,
    CUSTOMER_SSN,
    EXTERNAL_COMMENTS
  }
  
  interface ProcessQuestionnairePdfBuilder {
    /**
     * Process ID is required if process is not given
     * @param processId
     * @return
     */
    ProcessQuestionnairePdfBuilder processId(String processId);
    /**
     * Task ID is required if task is not given
     * @param taskId
     * @return
     */
    ProcessQuestionnairePdfBuilder taskId(String taskId);
    ProcessQuestionnairePdfBuilder process(ProcessInstance process);
    ProcessQuestionnairePdfBuilder task(Task task);
    ProcessQuestionnairePdfBuilder requestFields(PdfRequestFields ...field);
    ProcessQuestionnairePdfBuilder requestFields(Collection<PdfRequestFields> fields);
    byte[] build();
  }
}
