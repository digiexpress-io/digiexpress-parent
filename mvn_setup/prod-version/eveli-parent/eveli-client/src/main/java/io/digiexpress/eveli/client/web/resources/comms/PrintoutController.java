package io.digiexpress.eveli.client.web.resources.comms;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import io.dialob.api.form.Form;
import io.dialob.api.questionnaire.Questionnaire;
import io.digiexpress.eveli.client.api.AuthClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.dialob.api.DialobClient;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController("/rest/api/worker")
@Slf4j
@RequiredArgsConstructor
public class PrintoutController {

  private final TaskClient client;
  private final AuthClient auth;
  private final DialobClient dialob;
  private final RestTemplate restTemplate;
  private final String serviceUrl;
  
  @GetMapping(value = {"/pdf"}, produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> printQuestionnaire(
      @RequestParam(required = false, value = "taskId")Long taskId, 
      @RequestParam(required = false, value = "questionnaireId")String questionnaireId) {
    
    try {
      final var worker = auth.getUser().getPrincipal();
      
      
      log.debug("PDF printout request user has roles: {}", worker.getRoles());
      final var task = client.queryTasks().getOneById(taskId);
      
      if(!worker.isAdmin() && !worker.isAccessGranted(task.getAssignedRoles())) {
        log.warn("Task with ID {} not found or no roles access for printout, returning 404", taskId);
        return ResponseEntity.status(403).build();
      }

      if (verifyLink(questionnaireId, task)) {
        Questionnaire questionnaire = dialob.getQuestionnaireById(questionnaireId);
        Form form = dialob.getFormById(questionnaire.getMetadata().getFormId());
        PrintoutInput input = new PrintoutInput();
        input.setForm(form);
        input.setSession(questionnaire);
        input.setLang(questionnaire.getMetadata().getLanguage());
        ResponseEntity<byte[]> printoutResponse = callPrintoutService(restTemplate, input);
        log.info("PDF printout request completed for user: {} for printout of task: {} and questionnaire: {}", 
            worker.getUsername(), taskId, questionnaireId);
        return new ResponseEntity<>(printoutResponse.getBody(), HttpStatus.OK);
      }
      else {
        log.warn("Task with ID {} has no questionnaire {} for printout, returning 404", taskId, questionnaireId);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      
    }
    catch (Exception e) {
      log.error("PDF printout request FAILED with cause {}", e);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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
    return pdfEntity != null ? pdfEntity.getStatusCodeValue() : 0;
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

  private boolean verifyLink(String questionnaireId, TaskClient.Task taskModel) {
    boolean linkFound = false;
    if (questionnaireId.equals(taskModel.getQuestionnaireId())) {
      linkFound = true;
    }
    return linkFound;
  }

  @Data
  public static class PrintoutInput {
    String lang;
    Form form;
    Questionnaire session;
  }

}
