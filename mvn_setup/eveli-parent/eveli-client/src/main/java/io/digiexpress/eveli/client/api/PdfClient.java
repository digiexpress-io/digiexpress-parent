package io.digiexpress.eveli.client.api;

import java.util.Collection;

public interface PdfClient {

  ProcessQuestionnairePdfBuilder pdfBuilder();
  public enum PdfRequestFields {
    CUSTOMER_NAME,
    CUSTOMER_SSN,
    EXTERNAL_COMMENTS
  }
  
  interface ProcessQuestionnairePdfBuilder {
    ProcessQuestionnairePdfBuilder processId(String processId);
    ProcessQuestionnairePdfBuilder taskId(String taskId);
    ProcessQuestionnairePdfBuilder requestFields(PdfRequestFields ...field);
    ProcessQuestionnairePdfBuilder requestFields(Collection<PdfRequestFields> fields);
    byte[] build();
  }
}
