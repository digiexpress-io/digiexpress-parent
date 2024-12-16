package io.digiexpress.eveli.client.web.resources.gamut;

import java.time.Duration;

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

import java.util.List;
import java.util.Map;

import org.immutables.value.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.eveli.client.api.CrmClient;
import io.digiexpress.eveli.client.api.GamutClient;
import io.digiexpress.eveli.client.api.GamutClient.Attachment;
import io.digiexpress.eveli.client.api.GamutClient.AttachmentDownloadUrl;
import io.digiexpress.eveli.client.api.GamutClient.AttachmentUploadUrlException;
import io.digiexpress.eveli.client.api.GamutClient.ProcessCantBeDeletedException;
import io.digiexpress.eveli.client.api.GamutClient.ProcessNotFoundException;
import io.digiexpress.eveli.client.api.GamutClient.ReplayToInit;
import io.digiexpress.eveli.client.api.GamutClient.UserAction;
import io.digiexpress.eveli.client.api.GamutClient.UserActionNotAllowedException;
import io.digiexpress.eveli.client.api.GamutClient.UserAttachmentUploadInit;
import io.digiexpress.eveli.client.api.GamutClient.UserMessage;
import io.digiexpress.eveli.client.api.GamutClient.WorkflowNotFoundException;
import io.digiexpress.eveli.client.api.ImmutableInitProcessAuthorization;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.dialob.api.DialobClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("/portal/secured/actions")
@RequiredArgsConstructor
public class GamutUserActionsController {
  private final ApplicationEventPublisher publisher;
  private static final Duration timeout = Duration.ofSeconds(15);
  private final GamutClient gamutClient;
  private final CrmClient authClient;
  private final DialobClient dialob;
  private final ProcessClient hdes;
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableAuthorizationAction.class)
  @JsonDeserialize(as = ImmutableAuthorizationAction.class)
  interface AuthorizationAction {
    List<String> getUserRoles();
    List<String> getAllowedProcessNames();
  }
  

  @GetMapping(value="/fill/{sessionId}")
  public ResponseEntity<String> fillProxyGet(@PathVariable("sessionId") String sessionId) {
    return dialob.createProxy().sessionGet(sessionId);
  }
  @PostMapping(value="/fill/{sessionId}")
  public ResponseEntity<String> fillProxyPost(@PathVariable("sessionId") String sessionId, @RequestBody String body) {
    final var resp = dialob.createProxy().sessionPost(sessionId, body);
    
    if(resp.getStatusCode().is2xxSuccessful()) {
      final var event = gamutClient.fillEvent()
        .requestBody(body)
        .responseBody(resp.getBody())
        .sessionId(sessionId)
        .create();
      publisher.publishEvent(event);
    }
    return resp; 
  }
  @GetMapping(value="/review/{sessionId}")
  public ResponseEntity<?> reviewProxyGet(@PathVariable("sessionId") String sessionId) {
    final var session = dialob.getQuestionnaireById(sessionId);
    final var form = dialob.getFormById(session.getMetadata().getFormId());
    return ResponseEntity.ok(Map.of("session", session, "form", form));
  }

  @Transactional // ends up pulling task -> task comment -> reply-to-comment -> all the access entities
  @GetMapping(value="/messages")
  public ResponseEntity<List<UserMessage>> getMessages() {
    return ResponseEntity.ok(gamutClient.userMessagesQuery().findAllByUserId());
  }
  @Transactional // ends up pulling task -> task comment -> reply-to-comment -> all the access entities
  @GetMapping(value="{actionId}/messages")
  public ResponseEntity<List<UserMessage>> getMessages(@PathVariable("actionId") String actionId) {
    try {
      return ResponseEntity.ok(gamutClient.userMessagesQuery().findAllByActionId(actionId));
    } catch (ProcessNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }
  
  @Transactional
  @PostMapping(value="{actionId}/messages")
  public ResponseEntity<UserMessage> createMessage(@PathVariable("actionId") String actionId, @RequestBody ReplayToInit raw) {
    try {
      return ResponseEntity.ok(gamutClient.replyToBuilder().actionId(actionId).from(raw).createOne());
    } catch (ProcessNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }
  
  @PostMapping(value="{actionId}/attachments")
  public ResponseEntity<List<Attachment>> createAttachments(@PathVariable("actionId") String actionId, @RequestBody List<UserAttachmentUploadInit> raw) {
    try {
      return new ResponseEntity<>(gamutClient.userAttachmentBuilder().actionId(actionId).addAll(raw).createMany(), HttpStatus.CREATED);
    } catch (ProcessNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    } catch (AttachmentUploadUrlException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }
  
  @GetMapping(value="{actionId}/attachments/{filename}")
  public ResponseEntity<AttachmentDownloadUrl> getAttachment(
      @PathVariable("actionId") String actionId, 
      @PathVariable("filename") String filename) {
    
    try {
      return new ResponseEntity<>(gamutClient.attachmentDownloadQuery().actionId(actionId).filename(filename).getOne(), HttpStatus.OK);
    } catch (ProcessNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    
  }

  @GetMapping(value="/authorizations")
  public ResponseEntity<AuthorizationAction> getAuthorizations() {
    final var customer = authClient.getCustomer().getPrincipal();
    
    final var person = customer.getRepresentedPerson();
    final var company = customer.getRepresentedCompany();
    if(person == null && company == null) {
      return ResponseEntity.ok(null); // Nobody is represented
    }
    final var roles = authClient.getCustomerRoles().getRoles();
    final var allowed = hdes.queryAuthorization().get(ImmutableInitProcessAuthorization.builder()
        .addAllUserRoles(roles)
        .build());
    
    return  ResponseEntity.ok(ImmutableAuthorizationAction.builder()
        .addAllUserRoles(roles)
        .allowedProcessNames(allowed.getAllowedProcessNames())
        .build());
  }

  @DeleteMapping(value="/{actionId}")
  public ResponseEntity<UserAction> cancelAction(@PathVariable("actionId") String actionId) {
    try {
      return new ResponseEntity<>(gamutClient.cancelUserActionBuilder().actionId(actionId).cancelOne(), HttpStatus.OK);
    } catch (ProcessNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    } catch (ProcessCantBeDeletedException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }
  
  

  @GetMapping
  public ResponseEntity<?> kindOfCreateActionOrGet(
      @RequestParam(name = "id", required = false) String actionId,
      @RequestParam(name = "inputContextId", required = false) String inputContextId,
      @RequestParam(name = "inputParentContextId", required = false) String inputParentContextId,
      @RequestParam(name = "locale", required = false) String actionLocale
  ) {
    
    if(actionId == null) {
      return ResponseEntity.ok(gamutClient.userActionQuery().findAll());
    }

    try {
      return ResponseEntity.ok(gamutClient.userActionBuilder()
          .actionId(actionId)
          .clientLocale(actionLocale)
          .inputContextId(inputContextId)
          .inputParentContextId(inputParentContextId)
          .createOne().await().atMost(timeout));
    } catch(UserActionNotAllowedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (WorkflowNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }
}
