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

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.client.api.CrmClient;
import io.digiexpress.eveli.client.api.DialobCommands;
import io.digiexpress.eveli.client.api.GamutClient;
import io.digiexpress.eveli.client.api.GamutClient.AttachmentUploadUrlException;
import io.digiexpress.eveli.client.api.GamutClient.ProcessCantBeDeletedException;
import io.digiexpress.eveli.client.api.GamutClient.ProcessNotFoundException;
import io.digiexpress.eveli.client.api.GamutClient.ReplayToInit;
import io.digiexpress.eveli.client.api.GamutClient.UserActionNotAllowedException;
import io.digiexpress.eveli.client.api.GamutClient.UserAttachmentUploadInit;
import io.digiexpress.eveli.client.api.GamutClient.WorkflowNotFoundException;
import io.digiexpress.eveli.client.api.HdesCommands;
import io.digiexpress.eveli.client.api.ImmutableInitProcessAuthorization;
import io.thestencil.iam.api.ImmutableAuthorizationAction;
import io.thestencil.iam.api.UserActionsClient.Attachment;
import io.thestencil.iam.api.UserActionsClient.AttachmentDownloadUrl;
import io.thestencil.iam.api.UserActionsClient.AuthorizationAction;
import io.thestencil.iam.api.UserActionsClient.UserAction;
import io.thestencil.iam.api.UserActionsClient.UserMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("/gamut/user-actions")
@RequiredArgsConstructor
public class GamutUserActionsController {
  
  private final GamutClient gamutClient;
  private final CrmClient authClient;
  private final DialobCommands dialob;
  private final HdesCommands hdes;

  @GetMapping(value="fill/{sessionId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> fillProxyGet(@PathVariable("sessionId") String sessionId) {
    return dialob.proxy().fillGet(sessionId);
  }
  @PostMapping(value="/fill/{sessionId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> fillProxyPost(@PathVariable("sessionId") String sessionId, @RequestBody String body) {
    return dialob.proxy().fillPost(sessionId, body);
  }
  @GetMapping(value="/review/{sessionId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> reviewProxyGet(@PathVariable("sessionId") String sessionId) {
    return dialob.proxy().reviewGet(sessionId);
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
  @PostMapping(value="{actionId}/messages", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UserMessage> createMessage(@PathVariable("actionId") String actionId, @RequestBody ReplayToInit raw) {
    try {
      return ResponseEntity.ok(gamutClient.replyToBuilder().actionId(actionId).from(raw).createOne());
    } catch (ProcessNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }
  
  @PostMapping(value="{actionId}/attachments", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<Attachment>> createAttachments(@PathVariable("actionId") String actionId, @RequestBody List<UserAttachmentUploadInit> raw) {
    try {
      return new ResponseEntity<>(gamutClient.userAttachmentBuilder().actionId(actionId).addAll(raw).createMany(), HttpStatus.CREATED);
    } catch (ProcessNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    } catch (AttachmentUploadUrlException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }
  
  @GetMapping(value="{actionId}/attachments/{filename}", produces = MediaType.APPLICATION_JSON_VALUE)
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
    final var allowed = hdes.processAuthorizationQuery().get(ImmutableInitProcessAuthorization.builder()
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
  
  
  @GetMapping(value="/")
  public ResponseEntity<List<UserAction>> findAllUserActions() {
    return ResponseEntity.ok(gamutClient.userActionQuery().findAll());
  }
  
  @GetMapping(value="/")
  public ResponseEntity<UserAction> kindOfCreateAction(
      @RequestParam("actionId") String actionId,
      @RequestParam("inputContextId") String inputContextId,
      @RequestParam("inputParentContextId") String inputParentContextId,
      @RequestParam("actionLocale") String actionLocale
  ) {

    try {
      return ResponseEntity.ok(gamutClient.userActionBuilder()
          .actionId(actionId)
          .clientLocale(actionLocale)
          .inputContextId(inputContextId)
          .inputParentContextId(inputParentContextId)
          .createOne());
    } catch(UserActionNotAllowedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (WorkflowNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }
}
