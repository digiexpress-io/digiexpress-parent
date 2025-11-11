package io.digiexpress.eveli.client.web.resources.comms;

import java.time.Duration;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.client.api.PdfClient;
import io.digiexpress.eveli.client.api.PdfClient.PdfRequestFields;
import io.digiexpress.eveli.client.api.PdfClient.ProcessQuestionnairePdfBuilder;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.WorkerAuthClient;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/worker/rest/api")
@Slf4j
@RequiredArgsConstructor
public class PrintoutController {

  PdfClient pdfClient;
  
  private final TaskClient client;
  
  private final WorkerAuthClient auth;

  private static final Duration timeout = Duration.ofMillis(10000);
  
  @PostMapping(value = {"/pdf"}, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<byte[]> printQuestionnaire(@RequestBody PdfRequest body) {
    
    try {
      final var worker = auth.getUser().getPrincipal();
      log.debug("PDF printout request user has roles: {}", worker.getRoles());      
      
      final var task = client.queryTasks().getOneById(body.getTaskId()).await().atMost(timeout);
      
      if(!worker.isAdmin() && !worker.isAccessGranted(task.getAssignedRoles())) {
        log.warn("Task with ID {} not found or no roles access for printout, returning 404", task.getId());
        return ResponseEntity.status(403).build();
      }
      
      ProcessQuestionnairePdfBuilder pdfBuilder = pdfClient.pdfBuilder();
      byte[] content = pdfBuilder.taskId(body.getTaskId()).requestFields(body.fields).build();
      
      log.info("PDF printout request completed for user: {} for printout of task: {} and questionnaire: {}, content size: {}", 
          worker.getUsername(), task.getId(), task.getQuestionnaireId(), content.length);
      
      return ResponseEntity
          .ok()
          .contentType(MediaType.APPLICATION_PDF)
          .contentLength(content.length)
          .body(content);

    } catch (Exception e) {
      log.error("PDF printout request FAILED with cause {}", e);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }
    @Data @Builder @Jacksonized
  public static class PdfRequest {
    private final String taskId;
    private final List<PdfRequestFields> fields;
  }


}
