package io.digiexpress.eveli.client.web.resources.worker;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.client.api.PdfClient;
import io.digiexpress.eveli.client.api.PdfClient.PdfNotCompletedException;
import io.digiexpress.eveli.client.api.PdfClient.PdfNotFoundException;
import io.digiexpress.eveli.client.api.PdfClient.PdfRequestFields;
import io.digiexpress.eveli.client.api.PdfClient.QuestionnairePdfRequest;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.WorkerAuthClient;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/worker/rest/api")
@Slf4j
@RequiredArgsConstructor
public class PdfApiController {

  private final PdfClient pdfClient;
  private final TaskClient taskClient;
  private final WorkerAuthClient securityClient;
  private static final Duration timeout = Duration.ofMillis(10000);

  @PostMapping("/tasks/{taskId}/pdf")
  public ResponseEntity<byte[]> printQuestionnaire(
      @PathVariable String taskId,
      @RequestBody(required = false) QuestionnairePdfRequest request) {

    final var authentication = securityClient.getUser();
    log.debug("Questionnaire pdf POST API call for task id: {} from user {}", taskId, authentication.getPrincipal().getUsername());
    final var found = taskClient.queryTasks().findOneById(taskId)
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .await().atMost(timeout);
    if (found.isEmpty()) {
      log.warn("Questionnaire pdf for task id: {} FAILED: task not found", taskId);
      return ResponseEntity.notFound().build();
    }
    final var task = found.get();
    if (!isAuthorized(task, authentication)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    final var questionnaireId = request == null ? null : request.getQuestionnaireId();
    final List<PdfRequestFields> fields = request == null ? Collections.emptyList() : request.getFields();
    try {
      final var content = pdfClient.pdfBuilder()
          .task(task)
          .questionnaireId(questionnaireId)
          .requestFields(fields)
          .build();
      log.info("Questionnaire pdf created for task id: {}, questionnaire: {}, fields: {}, size: {}, user {}",
          taskId, questionnaireId == null ? task.getQuestionnaireId() : questionnaireId, fields, content.length,
          authentication.getPrincipal().getUsername());
      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_PDF)
          .contentLength(content.length)
          .body(content);
    } catch (PdfNotCompletedException e) {
      log.warn("Questionnaire pdf for task id: {} FAILED: {}", taskId, e.getMessage());
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    } catch (PdfNotFoundException e) {
      log.warn("Questionnaire pdf for task id: {} FAILED: {}", taskId, e.getMessage());
      return ResponseEntity.notFound().build();
    }
  }

  private boolean isAuthorized(TaskClient.Task task, WorkerAuthClient.User authentication) {
    log.debug("Checking task {} access for user {}", task.getId(), authentication.getPrincipal().getUsername());
    final List<String> roles = authentication.getPrincipal().getRoles();
    if (!authentication.getPrincipal().isAdmin() && !authentication.getPrincipal().isAccessGranted(task.getAssignedRoles())) {
      log.warn("Access to task {} disabled for roles {}", task.getId(), roles);
      return false;
    }
    log.debug("Check for task {} access PASSED", task.getId());
    return true;
  }
}
