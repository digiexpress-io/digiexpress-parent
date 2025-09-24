package io.digiexpress.eveli.client.web.resources.comms;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

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

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import io.dialob.api.form.Form;
import io.dialob.api.questionnaire.Questionnaire;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.TaskCommentSource;
import io.digiexpress.eveli.client.api.WorkerAuthClient;
import io.digiexpress.eveli.dialob.api.DialobClient;
import jakarta.annotation.Nullable;
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

  private final TaskClient client;
  
  private final WorkerAuthClient auth;
  private final DialobClient dialob;
  private final RestTemplate restTemplate;
  private final String serviceUrl;
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
      
      final var process = client.queryTaskProcesess().findOneByTaskId(task.getId()).await().atMost(timeout);
      final var questionnaire = dialob.getQuestionnaireById(task.getQuestionnaireId());
      final var form = dialob.getFormById(questionnaire.getMetadata().getFormId());
      
      
      final PrintoutInput input = PrintoutInput.builder()
          .form(form)
          .session(questionnaire)
          .lang(questionnaire.getMetadata().getLanguage())
          .referenceId(task.getTaskRef())
          
          .customerName(body.getFields().contains(PdfRequestFields.CUSTOMER_NAME) ? task.getClientIdentificator() : null)
          .customerSsn(body.getFields().contains(PdfRequestFields.CUSTOMER_SSN) ? process.get().getUserId() : null)
          .comments(body.getFields().contains(PdfRequestFields.EXTERNAL_COMMENTS) ? task.getComments().stream()
              .filter(e -> Boolean.TRUE.equals(e.getExternal()))
              .map(e -> PrintoutInputComment.builder()
                  .created(e.getCreated())
                  .commentText(e.getCommentText())
                  .source(e.getSource())
                  .build())
              .sorted((a, b) -> a.getCreated().compareTo(b.getCreated()))
              .toList(): Collections.emptyList())
          .build();
      
      
      
      final ResponseEntity<byte[]> printoutResponse = callPrintoutService(restTemplate, input);
      
      log.info("PDF printout request completed for user: {} for printout of task: {} and questionnaire: {}", 
          worker.getUsername(), task.getId(), task.getQuestionnaireId());
      
      return ResponseEntity
          .ok()
          .contentType(MediaType.APPLICATION_PDF)
          .contentLength(printoutResponse.getBody().length)
          .body(printoutResponse.getBody());

    } catch (Exception e) {
      log.error("PDF printout request FAILED with cause {}", e);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      /* just get empty pdf when fast and furious end point testing ...
      return ResponseEntity
          .ok()
          .contentType(MediaType.APPLICATION_PDF)
          .contentLength(0)
          .body(new byte[] {});
          */
    }
  }
  
  private ResponseEntity<byte[]> callPrintoutService(RestTemplate restTemplate, PrintoutInput input) {
    ResponseEntity<byte[]> pdfEntity = null;
    try {
      HttpHeaders printHeaders = new HttpHeaders();
      // ignore this warning about deprecated type, this content will be sent to printout
      // server which expects UTF-8 type defined to decode json correctly.
      printHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
      HttpEntity<?> printRequest = new HttpEntity<>(input, printHeaders);
      log.debug("Calling printout service url  {}", serviceUrl);
      log.debug("body:{}", input);
      pdfEntity = restTemplate.postForEntity(serviceUrl, printRequest, byte[].class);
    } 
    catch(Exception e) {
      log.warn("Error accessing form:", e);
      checkStatus("printout service", 404);
    }   
    checkStatus("printout service", getResponseStatus(pdfEntity));
    return pdfEntity;
  }
  
  private int getResponseStatus(ResponseEntity<byte[]> pdfEntity) {
    return pdfEntity != null ? pdfEntity.getStatusCode().value() : 0;
  }
  
  private void checkStatus(String scope, int status) {
    if (status == 404) {
      throw new IllegalStateException("Printout endpoint not found");
    } 
    else if(status == 403) {
      throw new IllegalStateException("Printout access forbidden");
    } 
    else if (status != 200){
      log.warn("Unknown status: {} for scope: {}", status, scope);
      throw new IllegalStateException("Printout service error!");
    }
  }
  
  @Data @Builder @Jacksonized
  public static class PdfRequest {
    private final String taskId;
    private final List<PdfRequestFields> fields;
  }
  
  public enum PdfRequestFields {
    CUSTOMER_NAME,
    CUSTOMER_SSN,
    EXTERNAL_COMMENTS
  }
  
  
  @Data @Builder
  public static class PrintoutInput {
    private final String lang;
    private final Form form;
    private final Questionnaire session;
    private final String referenceId;
    
    @Nullable private final String customerName;
    @Nullable private final String customerSsn;

    private final List<PrintoutInputComment> comments;
  }
  
  @Data @Builder
  public static class PrintoutInputComment {
    private final ZonedDateTime created;
    private final String commentText;
    private final TaskCommentSource source;
  }
}
