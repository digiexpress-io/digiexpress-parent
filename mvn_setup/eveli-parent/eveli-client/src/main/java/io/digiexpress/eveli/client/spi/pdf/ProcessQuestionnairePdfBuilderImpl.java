package io.digiexpress.eveli.client.spi.pdf;

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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;

import io.digiexpress.eveli.client.api.PdfClient.PdfNotCompletedException;
import io.digiexpress.eveli.client.api.PdfClient.PdfNotFoundException;
import io.digiexpress.eveli.client.api.PdfClient.PdfRequestFields;
import io.digiexpress.eveli.client.api.PdfClient.ProcessQuestionnairePdfBuilder;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.ProcessInstance;
import io.digiexpress.eveli.client.api.TaskClient.Task;
import io.digiexpress.eveli.client.api.TaskClient.TaskComment;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert.TaskException;
import io.resys.limaone.program.TagomiProgram;
import io.resys.limaone.program.TagomiProgram.PdfStatus;
import io.resys.limaone.spi.dialob.FormDb;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class ProcessQuestionnairePdfBuilderImpl implements ProcessQuestionnairePdfBuilder {
  private static final Duration timeout = Duration.ofMillis(10000);
  private static final DateTimeFormatter dateTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
  private static final String taskProps = "task";

  private final TaskClient taskClient;
  private final FormDb formDb;
  private final io.resys.limaone.program.Runtime runtime;
  private final String serviceName;
  private final String defaultLocale;

  private String processId;
  private String taskId;
  private String questionnaireId;
  private Task task;
  private ProcessInstance process;
  private final Set<PdfRequestFields> requestedFields = EnumSet.noneOf(PdfRequestFields.class);
  private String docType;
  private String docCategory;

  @Override
  public ProcessQuestionnairePdfBuilder processId(String processId) {
    this.processId = processId;
    return this;
  }
  @Override
  public ProcessQuestionnairePdfBuilder taskId(String taskId) {
    this.taskId = taskId;
    return this;
  }
  @Override
  public ProcessQuestionnairePdfBuilder process(ProcessInstance process) {
    this.process = process;
    return this;
  }
  @Override
  public ProcessQuestionnairePdfBuilder task(Task task) {
    this.task = task;
    return this;
  }
  @Override
  public ProcessQuestionnairePdfBuilder questionnaireId(String questionnaireId) {
    this.questionnaireId = questionnaireId;
    return this;
  }
  @Override
  public ProcessQuestionnairePdfBuilder requestFields(PdfRequestFields... field) {
    if (field != null) {
      for (final var f : field) {
        if (f != null) {
          requestedFields.add(f);
        }
      }
    }
    return this;
  }
  @Override
  public ProcessQuestionnairePdfBuilder requestFields(Collection<PdfRequestFields> fields) {
    if (fields != null) {
      fields.stream().filter(f -> f != null).forEach(requestedFields::add);
    }
    return this;
  }
  @Override
  public ProcessQuestionnairePdfBuilder docType(String dt) {
    this.docType = dt;
    return this;
  }
  @Override
  public ProcessQuestionnairePdfBuilder docCategory(String dc) {
    this.docCategory = dc;
    return this;
  }

  @Override
  public byte[] build() {
    TaskAssert.notEmpty(serviceName, () -> "eveli.tagomi.task-pdf-service-name can't be empty!");
    visitProcess();
    visitTask();

    final var formInstanceId = visitFormInstanceId();
    final var zone = ZoneId.systemDefault();
    final var flatData = formDb.withTenant().formInstanceFlatDataQuery()
        .instanceId(formInstanceId)
        .timeZone(zone.getId())
        .getOneSync();

    switch (flatData.getStatus()) {
      case NOT_COMPLETED -> throw new PdfNotCompletedException("questionnaire: " + formInstanceId + " is not completed!");
      case NOT_FOUND -> throw new PdfNotFoundException("questionnaire: " + formInstanceId + " not found!");
      case ERROR -> throw new TaskException(flatData.getMessage().orElse("flat data query FAILED for questionnaire: " + formInstanceId));
      case COMPLETED -> { }
    }

    final var props = flatData.getBody().orElseThrow();
    props.put(taskProps, visitTaskProps(zone));

    final var program = runtime.getBundle().queryTagomis().name(serviceName).getOne();
    final var locale = visitLocale(program, props);
    final var workerPool = runtime.getProperties().getWorkerPool();
    final var workerTimeout = runtime.getProperties().getWorkerPoolMaxTimeout();
    final var result = program.run(locale, props)
        .runSubscriptionOn(workerPool)
        .await().atMost(workerTimeout);
    if (result.getStatus() != PdfStatus.OK) {
      throw new TaskException("PDF rendering FAILED for printout: " + serviceName + ", locale: " + locale + ", cause: " + result.getStatusMessage());
    }
    return Base64.getDecoder().decode(result.getBodyBase64());
  }

  private void visitProcess() {
    if (process != null) {
      return;
    }
    final var workerPool = Infrastructure.getDefaultWorkerPool();
    if (processId != null) {
      process = taskClient.queryTaskProcesess().getOneById(processId)
          .runSubscriptionOn(workerPool)
          .await().atMost(timeout);
    } else if (taskId != null) {
      process = taskClient.queryTaskProcesess().findOneByTaskId(taskId)
          .runSubscriptionOn(workerPool)
          .await().atMost(timeout).orElse(null);
    } else if (task != null) {
      process = taskClient.queryTaskProcesess().findOneByTaskId(task.getId())
          .runSubscriptionOn(workerPool)
          .await().atMost(timeout).orElse(null);
    }
  }

  private void visitTask() {
    if (task != null) {
      return;
    }
    if (taskId == null) {
      TaskAssert.notNull(process, () -> "taskId can't be null!");
      taskId = process.getTaskId();
    }
    task = taskClient.queryTasks().getOneById(taskId)
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .await().atMost(timeout);
  }

  private String visitFormInstanceId() {
    final String formInstanceId;
    if (questionnaireId != null && !questionnaireId.isBlank()) {
      formInstanceId = questionnaireId;
    } else if (task.getQuestionnaireId() != null) {
      formInstanceId = task.getQuestionnaireId();
    } else if (process != null) {
      formInstanceId = process.getQuestionnaireId();
    } else {
      formInstanceId = null;
    }
    if (formInstanceId == null) {
      throw new PdfNotFoundException("task: " + task.getId() + " has no questionnaire!");
    }
    return formInstanceId;
  }

  private JsonObject visitTaskProps(ZoneId zone) {
    final var json = new JsonObject();
    json.put("id", task.getId());
    json.put("taskRef", task.getTaskRef());
    json.put("subject", task.getSubject());
    json.put("status", task.getStatus() == null ? null : task.getStatus().name());
    json.put("priority", task.getPriority() == null ? null : task.getPriority().name());
    json.put("created", visitDateTime(task.getCreated(), zone));
    json.put("updated", visitDateTime(task.getUpdated(), zone));
    json.put("completed", visitDateTime(task.getCompleted(), zone));
    json.put("dueDate", task.getDueDate() == null ? null : task.getDueDate().toString());
    json.put("assignedUser", task.getAssignedUser());
    json.put("keyWords", new JsonArray(task.getKeyWords()));
    json.put("formName", process == null ? null : process.getFormName());
    json.put("docType", docType);
    json.put("docCategory", docCategory);

    final var fields = new JsonArray();
    requestedFields.forEach(field -> fields.add(field.name()));
    json.put("requestedFields", fields);

    if (requestedFields.contains(PdfRequestFields.CUSTOMER_NAME)) {
      json.put("customerName", task.getClientIdentificator());
    }
    if (requestedFields.contains(PdfRequestFields.CUSTOMER_SSN)) {
      json.put("customerSsn", process == null ? null : process.getUserId());
    }
    if (requestedFields.contains(PdfRequestFields.EXTERNAL_COMMENTS)) {
      final var comments = new JsonArray();
      task.getComments().stream()
        .filter(comment -> Boolean.TRUE.equals(comment.getExternal()))
        .sorted(Comparator.comparing(TaskComment::getCreated))
        .forEach(comment -> comments.add(new JsonObject()
            .put("created", visitDateTime(comment.getCreated(), zone))
            .put("userName", comment.getUserName())
            .put("source", comment.getSource() == null ? null : comment.getSource().name())
            .put("commentText", comment.getCommentText())));
      json.put("comments", comments);
    }
    return json;
  }

  private String visitLocale(TagomiProgram program, JsonObject props) {
    final var language = props.getJsonObject("metadata", new JsonObject()).getString("language");
    if (language != null && program.getLocales().stream().anyMatch(locale -> locale.equalsIgnoreCase(language))) {
      return language;
    }
    TaskAssert.notEmpty(defaultLocale, () -> "Printout: " + serviceName + " has no page for questionnaire language: " + language +
        ", known locales: " + program.getLocales() + " and eveli.tagomi.task-pdf-default-locale can't be empty!");
    log.warn("Printout: {} has no page for questionnaire language: {}, using default locale: {}, known locales: {}",
        serviceName, language, defaultLocale, program.getLocales());
    return defaultLocale;
  }

  private static String visitDateTime(ZonedDateTime value, ZoneId zone) {
    return value == null ? null : dateTime.format(value.withZoneSameInstant(zone));
  }
}
