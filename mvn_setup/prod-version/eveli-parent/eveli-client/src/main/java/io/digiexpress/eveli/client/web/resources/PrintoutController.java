package io.digiexpress.eveli.client.web.resources;

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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import io.dialob.api.form.Form;
import io.dialob.api.questionnaire.Questionnaire;
import io.digiexpress.eveli.client.api.PortalClient;
import io.digiexpress.eveli.client.api.TaskCommands;
import io.digiexpress.eveli.client.config.PortalConfigBean;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
public class PrintoutController {

  private final PortalClient client;
  private final PortalConfigBean config;
  private final RestTemplate restTemplate;
  
  @GetMapping(value = {"/pdf"}, produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> printQuestionnaire(
      @RequestParam(required = false, value = "taskId")String taskId, 
      @RequestParam(required = false, value = "questionnaireId")String questionnaireId,
      Authentication authentication) {
    
    Optional<TaskCommands.Task> task = Optional.empty();
    List<String> roles = Collections.emptyList();
    
    try {
      log.info("PDF printout request from user: {} for printout of task: {} and questionnaire: {}", 
          authentication != null ? authentication.getName() : "UNAUTHENTICATED", taskId, questionnaireId);
      if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
        roles = getRoles(authentication);
      }
      log.debug("PDF printout request user has roles: {}", roles);
      task = client.task().find(taskId, roles);
      
      
      
      if (task.isEmpty()) {
        log.warn("Task with ID {} not found or no roles access for printout, returning 404", taskId);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      else {
        TaskCommands.Task taskModel = task.get();
        if (verifyLink(questionnaireId, taskModel)) {
          Questionnaire questionnaire = client.dialob().get(questionnaireId);
          Form form = client.dialob().getForm(questionnaire.getMetadata().getFormId());
          PrintoutInput input = new PrintoutInput();
          input.setForm(form);
          input.setSession(questionnaire);
          input.setLang(questionnaire.getMetadata().getLanguage());
          ResponseEntity<byte[]> printoutResponse = callPrintoutService(restTemplate, input);
          log.info("PDF printout request completed for user: {} for printout of task: {} and questionnaire: {}", 
              authentication != null ? authentication.getName() : "UNAUTHENTICATED", taskId, questionnaireId);
          return new ResponseEntity<>(printoutResponse.getBody(), HttpStatus.OK);
        }
        else {
          log.warn("Task with ID {} has no questionnaire {} for printout, returning 404", taskId, questionnaireId);
          return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
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
      log.debug("Calling printout service url  {}", config.getPrintoutServiceUrl());
      log.debug("body:{}", input);
      pdfEntity = restTemplate.postForEntity(config.getPrintoutServiceUrl(), printRequest, byte[].class);
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

  private boolean verifyLink(String questionnaireId, TaskCommands.Task taskModel) {
    boolean linkFound = false;
    for (var link : taskModel.getTaskLinks()) {
      log.debug("Found link with id {}, key: {}, link: {}", link.getId(), link.getLinkKey(), link.getLinkAddress());
      if ("questionnaireId".equals(link.getLinkKey())) {
        if (questionnaireId.equals(link.getLinkAddress())) {
          log.debug("Found link match with id {}, key: {}, link: {}", link.getId(), link.getLinkKey(), link.getLinkAddress());
          linkFound = true;
        }
      }
    }
    return linkFound;
  }
  
  private List<String> getRoles(Authentication authentication) {
    List<String> roles = authentication.getAuthorities().stream().map(auth->auth.getAuthority()).collect(Collectors.toList());
    return roles;
  }
  
  @Data
  public static class PrintoutInput {
    String lang;
    Form form;
    Questionnaire session;
  }

}
