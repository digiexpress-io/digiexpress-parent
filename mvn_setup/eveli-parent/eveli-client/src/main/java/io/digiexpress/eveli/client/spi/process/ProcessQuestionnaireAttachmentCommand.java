package io.digiexpress.eveli.client.spi.process;

import java.time.Duration;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import io.digiexpress.eveli.client.api.AttachmentCommands;
import io.digiexpress.eveli.client.api.AttachmentCommands.Attachment;
import io.digiexpress.eveli.client.api.PdfClient;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.ProcessClient.ProcessInstance;
import io.digiexpress.eveli.client.api.QuestionnaireAttachmentCommands;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.Task;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ProcessQuestionnaireAttachmentCommand implements QuestionnaireAttachmentCommands{
  private static final Duration timeout = Duration.ofMillis(10000);
  
  private final AttachmentCommands attachments;
  private final PdfClient pdf;
  private final TaskClient tasks;
  private final ProcessClient processClient;
  @Override
  public QuestionnaireAttachmentBuilder attachmentBuilder() {
    // TODO Auto-generated method stub
    return new QuestionnaireAttachmentBuilder() {
      private String taskId;
      private String processId;
      private String questionnaireId;
      @Override
      public QuestionnaireAttachmentBuilder taskId(String taskId) {
        this.taskId = taskId;
        return this;
      }
      
      @Override
      public QuestionnaireAttachmentBuilder processId(String processId) {
        this.processId = processId;
        return this;
      }
      
      @Override
      public Attachment build() {
        ProcessInstance process;
        if (questionnaireId != null) {
          process = processClient.queryInstances().findOneByQuestionnaireId(questionnaireId).get();
          processId = process.getId().toString();
        }
        else if (processId != null) {
          process = tasks.queryTaskProcesess().getOneById(processId).await().atMost(timeout);
        }
        else {
          if (taskId == null) {
            throw new IllegalStateException("Process or task Id is missing");
          }
          Optional<ProcessInstance> taskProcess = tasks.queryTaskProcesess().findOneByTaskId(taskId).await().atMost(timeout);
          process = taskProcess.get();
          processId = process.getId().toString();
        }
        String formName = process.getFormName();
        Task task = tasks.queryTasks().getOneById(taskId != null ? taskId : process.getTaskId()).await().atMost(timeout);
        String taskRef = task.getTaskRef();
        byte[] content = pdf.pdfBuilder().process(process).task(task).build();

        String attachmentName = "%s-%s.pdf".formatted(StringUtils.firstNonBlank(formName, "NA"), StringUtils.firstNonBlank(taskRef, "NA"));
        return attachments.contentUpload().processId(processId).filename(attachmentName).build(content);
      }

      @Override
      public QuestionnaireAttachmentBuilder questionnaireId(String questionnaireId) {
        this.questionnaireId = questionnaireId;
        return this;
      }
    };
  }

}
