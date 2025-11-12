package io.digiexpress.eveli.client.web.resources.comms;

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
