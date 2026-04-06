package io.digiexpress.eveli.client.web.resources.gamut;

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
import java.util.Optional;

import org.immutables.value.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.eveli.client.api.FeedbackClient;
import io.digiexpress.eveli.client.api.FeedbackClient.CustomerFeedback;
import io.digiexpress.eveli.client.api.FeedbackClient.UpsertFeedbackRankingCommand;
import io.digiexpress.eveli.client.api.GamutAuthClient;
import io.digiexpress.eveli.client.api.GamutClient;
import io.digiexpress.eveli.client.api.GamutClient.AttachmentDownloadUrl;
import io.digiexpress.eveli.client.api.GamutClient.ReplayToInit;
import io.digiexpress.eveli.client.api.GamutClient.UserAction;
import io.digiexpress.eveli.client.api.GamutClient.UserActionAttachment;
import io.digiexpress.eveli.client.api.GamutClient.UserAttachmentUploadInit;
import io.digiexpress.eveli.client.api.GamutClient.UserMessage;
import io.digiexpress.eveli.client.spi.dialob.DialobFillEventPublisher;
import io.resys.limaone.spi.program.input.DefaultAuthProgramInput;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("/portal/secured/actions")
@RequiredArgsConstructor
public class GamutUserActionsController {
  private final DialobFillEventPublisher publisher;
  private final GamutClient gamutClient;
  private final GamutAuthClient authClient;
  private final io.resys.limaone.program.Runtime runtime;
  private final FeedbackClient feedback;
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableAuthorizationAction.class)
  @JsonDeserialize(as = ImmutableAuthorizationAction.class)
  interface AuthorizationAction {
    List<String> getUserRoles();
    List<String> getAllowedProcessNames();
  }
  
  @GetMapping(value="/fill/{sessionId}")
  public Uni<?> fillProxyGet(@PathVariable("sessionId") String sessionId) {
    return runtime.getProperties().getFormDb().withTenant().createFormFill().formInstanceId(sessionId).build()
        .onItem().transform(questionnaire -> questionnaire.unwrap());
  }
  
  @PostMapping(value="/fill/{sessionId}")
  public Uni<?> fillProxyPost(@PathVariable("sessionId") String sessionId, @RequestBody String body) {
    return runtime.getProperties().getFormDb().withTenant().createFormFill().formInstanceId(sessionId).actions(body)
        .onCompletion(completion -> {
          return gamutClient.fillEvent()
              .requestBody(body)
              .responseBody(completion)
              .sessionId(sessionId)
              .create()
              .onItem().invoke(event -> publisher.publishEvent(event));
        })
        .build()
        .onItem().transform(questionnaire -> questionnaire.unwrap());
  }
  
  @GetMapping(value="/review/{sessionId}")
  public Uni<Map<String, Object>> reviewProxyGet(@PathVariable("sessionId") String sessionId) {
    return runtime.getProperties().getFormDb().withTenant()
        .formInstanceQuery().includeForm(true)
        .getOne(sessionId)
        .onItem()
        .transform(prop -> Map.of("session", prop.getQuestionnaire(), "form", prop.getForm().get()));
    
  }

  @GetMapping(value="/messages")
  public Multi<UserMessage> getMessages() {
    final var customer = authClient.getParticipant();
    return gamutClient.userMessagesQuery().findAllByUserId(customer);
  }
  
  @PutMapping(path = "/feedback")
  public ResponseEntity<?> updateFeedback(@RequestBody UpsertFeedbackRankingCommand upsert) {
    final var isValid = upsert.getRating() == null || upsert.getRating() == 1 || upsert.getRating() == 5;
    if(isValid) {
      return ResponseEntity.ok(feedback.modifyOneFeedbackRank(upsert, authClient.getCustomer().getPrincipal().getUsername()));      
    }
    return ResponseEntity.badRequest().body(Map.of(
        "errorCode", "invalid-body"));
  }
  
  @GetMapping(path = "/feedback")
  public Multi<CustomerFeedback> findAllFeedback(@RequestParam(name = "locale") String locale) {
    return feedback.queryCustomerFeedbacks().findAllByCustomerId(authClient.getCustomer().getPrincipal().getUsername());
  }
  
  @GetMapping(value="{actionId}/messages")
  public Multi<UserMessage> getMessages(@PathVariable("actionId") String actionId) {
    final var customer = authClient.getParticipant();
    return gamutClient.userMessagesQuery().findAllByActionId(customer, actionId);
  }
  
  @PutMapping(value="{actionId}/views")
  public Uni<ResponseEntity<?>> markUserActionViewed(@PathVariable("actionId") String actionId) {
    final var customer = authClient.getParticipant();
    return gamutClient.userActionViewBuilder()
        .actionId(actionId).customer(customer).create()
        .onItem().transform(junk -> ResponseEntity.ok().build());
  }
  
  @PostMapping(value="{actionId}/messages")
  public Uni<ResponseEntity<UserMessage>> createMessage(@PathVariable("actionId") String actionId, @RequestBody ReplayToInit raw) {
    final var customer = authClient.getParticipant();
    return gamutClient.replyToBuilder().actionId(actionId).customer(customer).from(raw).createOne()
        .map(msg -> ResponseEntity.ok(msg))
        .onFailure().recoverWithItem(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }
  
  @PostMapping(value="{actionId}/attachments")
  public Uni<ResponseEntity<List<UserActionAttachment>>> createAttachments(
      @PathVariable("actionId") String actionId, 
      @RequestBody List<UserAttachmentUploadInit> raw) {
    
    final var customer = authClient.getCustomer();
    return gamutClient.userAttachmentBuilder().customer(customer).actionId(actionId).addAll(raw).createMany()
        .collect().asList().onItem().transform(entries -> new ResponseEntity<>(entries, HttpStatus.CREATED))
        .onFailure().recoverWithItem(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
        
  }
  
  @GetMapping(value="{actionId}/attachments/{filename}")
  public Uni<ResponseEntity<AttachmentDownloadUrl>> getAttachment(
      @PathVariable("actionId") String actionId, 
      @PathVariable("filename") String filename) {
    
      return gamutClient.attachmentDownloadQuery()
          .actionId(actionId).filename(filename).getOne().map(body -> new ResponseEntity<>(body, HttpStatus.OK))    
          .onFailure().recoverWithItem(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
  }
  
  @DeleteMapping(value="/{actionId}")
  public Uni<ResponseEntity<UserAction>> cancelAction(@PathVariable("actionId") String actionId) {
    final var customer = authClient.getParticipant();
    
    return gamutClient.cancelUserActionBuilder()
        .actionId(actionId).customer(customer).cancelOne().map(body -> new ResponseEntity<>(body, HttpStatus.OK))
        .onFailure().recoverWithItem(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
  }

  @GetMapping(value="/authorizations")
  public ResponseEntity<AuthorizationAction> getAuthorizations(
      @RequestParam(name = "cockpitId", required = false) String cockpitId
  ) {
    
    final var customer = authClient.getCustomer().getPrincipal();
    final var person = customer.getRepresentedPerson();
    final var company = customer.getRepresentedCompany();
    if(person == null && company == null) {
      return ResponseEntity.ok(null); // Nobody is represented
    }
    
    final var user = this.authClient.getParticipant();
    final var runtime = this.runtime.withTenant(Optional.ofNullable(cockpitId));
    final var locale = user.getLanguage();
    final var article = runtime.getBundle().queryArticles().findOne().orElse(null);
    if(article == null) {
      return ResponseEntity.ok(null); // Nobody is represented
    }
    
    final var input = DefaultAuthProgramInput.builder().runtime(runtime).user(user).locale(locale).build();
    final var result = article.run(input);
    
    return ResponseEntity.ok(ImmutableAuthorizationAction.builder()
        .addAllUserRoles(result.getRoles())
        .allowedProcessNames(result.getAllowed())
        .build());
  }

  @GetMapping
  public Uni<ResponseEntity<?>> kindOfCreateActionOrGet(
      @RequestParam(name = "id", required = false) String actionId,
      @RequestParam(name = "cockpitId", required = false) String cockpitId,
      @RequestParam(name = "inputContextId", required = false) String inputContextId,
      @RequestParam(name = "inputParentContextId", required = false) String inputParentContextId,
      @RequestParam(name = "locale", required = false) String actionLocale
  ) {
    
    if(actionId == null) {
      return gamutClient.userActionQuery()
          .customer(authClient.getParticipant())
          .cockpitId(cockpitId)
          .findAll().collect().asList().map(ResponseEntity::ok);
    }

    return gamutClient.userActionBuilder()
        .actionId(actionId)
        .cockpitId(cockpitId)
        .clientLocale(actionLocale)
        .inputContextId(inputContextId)
        .inputParentContextId(inputParentContextId)
        .participant(authClient.getParticipant())
        .createOne().map(ResponseEntity::ok)
        .onFailure().recoverWithItem(() -> ResponseEntity.notFound().build())
        // make the type as "<?>"
        .map(e -> e);
  }
}
