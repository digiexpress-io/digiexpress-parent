package io.digiexpress.eveli.client.api;

import java.util.Collection;

import io.digiexpress.eveli.client.api.ProcessClient.ProcessInstance;
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
