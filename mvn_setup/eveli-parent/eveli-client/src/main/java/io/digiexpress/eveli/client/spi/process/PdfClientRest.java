package io.digiexpress.eveli.client.spi.process;

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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dialob.api.form.Form;
import io.dialob.api.questionnaire.Questionnaire;
import io.digiexpress.eveli.client.api.PdfClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.ProcessInstance;
import io.digiexpress.eveli.client.api.TaskClient.Task;
import io.digiexpress.eveli.client.api.TaskClient.TaskCommentSource;
import io.resys.limaone.spi.dialob.FormDb;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class PdfClientRest implements PdfClient {

  private final TaskClient client;  
  private final FormDb dialob;
  private final RestTemplate restTemplate;
  private final String serviceUrl;
  private final ObjectMapper om;
  
  @Override
  public ProcessQuestionnairePdfBuilder pdfBuilder() {
    return new ProcessQuestionnairePdfRestBuilder();
  }
  
  public class ProcessQuestionnairePdfRestBuilder implements PdfClient.ProcessQuestionnairePdfBuilder {
    private static final Duration timeout = Duration.ofMillis(10000);
    
    private String processId;
    private String taskId;
    private TaskClient.Task task;
    private ProcessInstance process;
    private List<PdfRequestFields> requestedFields = new ArrayList<>();
    private String docType;
    private String docCategory;
    
    @Override
    public byte[] build() {
      try {
        initObjects();
        final var questionnaire = dialob.withTenant().formInstanceQuery()
            .includeForm(true)
            .getOneSync(process.getQuestionnaireId());
        
        
        final PrintoutInput input = PrintoutInput.builder()
            .form(questionnaire.getForm().get())
            .session(questionnaire.getQuestionnaire())
            .lang(questionnaire.metadata().getLanguage())
            .referenceId(task.getTaskRef())
            .docType(docType)
            .docCategory(docCategory)
            .customerName(requestedFields.contains(PdfRequestFields.CUSTOMER_NAME) ? task.getClientIdentificator() : null)
            .customerSsn(requestedFields.contains(PdfRequestFields.CUSTOMER_SSN) ? process.getUserId() : null)
            .comments(requestedFields.contains(PdfRequestFields.EXTERNAL_COMMENTS) ? task.getComments().stream()
                .filter(e -> Boolean.TRUE.equals(e.getExternal()))
                .map(e -> PrintoutInputComment.builder()
                    .created(e.getCreated())
                    .commentText(e.getCommentText())
                    .source(e.getSource())
                    .build())
                .sorted((a, b) -> a.getCreated().compareTo(b.getCreated()))
                .toList(): Collections.emptyList())
            .build();
        
        return callPrintoutService(restTemplate, input);
      } 
      catch (Exception e) {
        log.warn("PDF printout request FAILED with cause {}", e);
        throw new RuntimeException(e);
  
      }
    }
  
    private void initObjects() {
      if (process == null) {
        if (processId != null) {
          process = client.queryTaskProcesess().getOneById(processId).await().atMost(timeout);
        }
        else if (taskId != null) {
          process = client.queryTaskProcesess().findOneByTaskId(taskId).await().atMost(timeout).get();
        }
        else if (task != null) {
          process = client.queryTaskProcesess().findOneByTaskId(task.getId()).await().atMost(timeout).get();
        }
      }
      if (task == null) {
        if (taskId == null) {
          taskId = process.getTaskId();
        }
        task = client.queryTasks().getOneById(taskId).await().atMost(timeout);
      }
    }

    private byte[] callPrintoutService(RestTemplate restTemplate, PrintoutInput input) {
      ResponseEntity<byte[]> pdfEntity = null;
      try {
        HttpHeaders printHeaders = new HttpHeaders();
        // ignore this warning about deprecated type, this content will be sent to printout
        // server which expects UTF-8 type defined to decode json correctly.
        printHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity<?> printRequest = new HttpEntity<>(input, printHeaders);
        log.debug("Calling printout service url  {}", serviceUrl);
        if (log.isDebugEnabled()) {
          log.debug("body:{}",om.writeValueAsString(input));
        }
        pdfEntity = restTemplate.postForEntity(serviceUrl, printRequest, byte[].class);
      } 
      catch(Exception e) {
        log.warn("Error accessing form:", e);
        checkStatus("printout service", 404);
      }   
      checkStatus("printout service", getResponseStatus(pdfEntity));
      return pdfEntity.getBody();
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
    public ProcessQuestionnairePdfBuilder requestFields(PdfRequestFields... field) {
      for (var f : field) {
        this.requestedFields.add(f);
      }
      return this;
    }

    @Override
    public ProcessQuestionnairePdfBuilder requestFields(Collection<PdfRequestFields> fields) {
      this.requestedFields.addAll(fields);
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
    public ProcessQuestionnairePdfBuilder docType(String dt) {
      this.docType = dt;
      return this;
    }
    @Override
    public ProcessQuestionnairePdfBuilder docCategory(String dc) {
      this.docCategory = dc;
      return this;
    }

  }

  
  @Data @Builder
  public static class PrintoutInput {
    private final String lang;
    private final Form form;
    private final Questionnaire session;
    private final String referenceId;
    
    @Nullable private final String docType;
    @Nullable private final String docCategory;
    
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
