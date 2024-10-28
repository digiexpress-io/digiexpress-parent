package io.digiexpress.eveli.client.web.resources;

/*-
 * #%L
 * quarkus-stencil-user-actions
 * %%
 * Copyright (C) 2021 - 2022 Copyright 2021 ReSys OÜ
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
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.smallrye.mutiny.Uni;
import io.thestencil.iam.api.IAMClient;
import io.thestencil.iam.api.IAMClient.RepresentedCompany;
import io.thestencil.iam.api.IAMClient.RepresentedPerson;
import io.thestencil.iam.api.IAMClient.UserQueryResult;
import io.thestencil.iam.api.UserActionsClient;
import io.thestencil.iam.api.UserActionsClient.Attachment;
import io.thestencil.iam.api.UserActionsClient.AttachmentDownloadUrl;
import io.thestencil.iam.api.UserActionsClient.AuthorizationAction;
import io.thestencil.iam.api.UserActionsClient.UserAction;
import io.thestencil.iam.api.UserActionsClient.UserMessage;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("/gamut/user-actions")
@RequiredArgsConstructor
public class GamutUserActionsController {

  private final IAMClient iam;
  private final UserActionsClient userActions;
  
  @GetMapping(value="fill/{sessionId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Uni<String> fillProxyGet(@PathVariable("sessionId") String sessionId) {
    return userActions.fill()
    .path("session/dialob/" + sessionId)
    .method(io.vertx.core.http.HttpMethod.GET)
    .build()
    .onItem().transform(b -> b.toString());
  }
  @PostMapping(value="/fill/{sessionId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Uni<String> fillProxyPost(@PathVariable("sessionId") String sessionId, @RequestBody String body) {
    return userActions.fill()
    .path("session/dialob/"+ sessionId)
    .method(io.vertx.core.http.HttpMethod.POST)
    .body(io.vertx.mutiny.core.buffer.Buffer.buffer(body))
    .build()
    .onItem().transform(b -> b.toString());
  }
  @GetMapping(value="/review/{sessionId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Uni<String> reviewProxyGet(@PathVariable("sessionId") String sessionId) {
    return userActions.review().path("api/questionnaires/" + sessionId).build()
        .onItem().transform(b -> b.toString());
  }

  @GetMapping(value="{actionId}/messages")
  public Uni<List<UserMessage>> getMessages(@PathVariable("actionId") String actionId) {
    return iam.userQuery().get().onItem().transformToUni(client -> 
      userActions.markUser()
        .processId(actionId)
        .userId(client.getUser().getSsn())
        .userName(client.getUser().getUsername())
        .build()
    );
  }
  @PostMapping(value="{actionId}/messages", consumes = MediaType.APPLICATION_JSON_VALUE)
  public Uni<UserMessage> createMessage(@PathVariable("actionId") String actionId, @RequestBody String raw) {
    final var body = new JsonObject(raw);
    
    return iam.userQuery().get().onItem().transformToUni(client -> 
      userActions.replyTo()
        .processId(actionId)
        .userId(client.getUser().getSsn())
        .userName(client.getUser().getUsername())
        .replyToId(body.getString("replyToId"))
        .text(body.getString("text"))
        .build()
    );
  }

  @PostMapping(value="{actionId}/attachments", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public Uni<List<Attachment>> createAttachments(@PathVariable("actionId") String actionId, @RequestBody String raw) {
    final List<Map<String, String>> files = new JsonArray(raw).getList();
       
    return iam.userQuery().get().onItem().transformToUni(client ->
        userActions.attachment()
        .userId(client.getUser().getSsn())
        .userName(client.getUser().getUsername())
        .processId(actionId)
        .call(b -> files.stream()
            .forEach(item -> b.data(
                item.get("fileName"), 
                item.get("fileType")
                )))
        .build().collect().asList()
      );  
  }
  @GetMapping(value="{actionId}/attachments/{attachmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Uni<AttachmentDownloadUrl> getAttachment(@PathVariable("actionId") String actionId, @PathVariable("attachmentId") String attachmentId) {
    return iam.userQuery().get().onItem().transformToUni(client ->
      userActions.attachmentDownload()
        .userId(client.getUser().getSsn())
        .userName(client.getUser().getUsername())
        .processId(actionId)
        .attachmentId(attachmentId)
        .build()
      );
  }
  
  

  @GetMapping(value="/authorizations")
  public Uni<AuthorizationAction> getAuthorizations(@RequestHeader("cookie") String id) {
    
    return iam.userQuery().get().onItem().transformToUni(client -> {
      final RepresentedPerson person = client.getUser().getRepresentedPerson();
      final RepresentedCompany company = client.getUser().getRepresentedCompany();
      if(person == null && company == null) {
        return null; // Nobody is represented
      }
      
      final var query = person != null ? 
          iam.personRolesQuery().id(id).get() : 
          iam.companyRolesQuery().id(id).get();
      
      return query
          .onItem().transformToUni(roleData -> userActions
              .authorizationActionQuery()
              .userRoles(roleData.getUserRoles().getRoles())
              .get());
    });
  }
  
  @GetMapping(value="/")
  public Uni<List<UserAction>> findAllUserActions(@RequestHeader("cookie") String id) {
    return iam.userQuery().get().onItem().transformToUni(client -> {
      final var user = client.getUser();
      
      if(user.getRepresentedCompany() != null || user.getRepresentedPerson() != null) {
        final var query = user.getRepresentedPerson() != null ? 
            iam.personRolesQuery().id(id).get() : 
            iam.companyRolesQuery().id(id).get();
        
        final Uni<AuthorizationAction> authorizations = query
            .onItem().transformToUni(roleData -> userActions
                .authorizationActionQuery()
                .userRoles(roleData.getUserRoles().getRoles())
                .get());
        
        final var personNames = user.getRepresentedPerson() == null ? null : getRepresentativeName(user.getRepresentedPerson().getName());
        
        final var workflows = userActions.queryUserAction()
          .userId(user.getRepresentedPerson() != null ? user.getRepresentedPerson().getPersonId() : user.getRepresentedCompany().getCompanyId())
          .userName(personNames != null ? personNames[1] + " " + personNames[0]: user.getRepresentedCompany().getName())
          .representativeUserName(client.getUser().getUsername())
          .list().collect().asList();
        
        return Uni.combine().all().unis(workflows, authorizations)
        .asTuple().onItem().transform(tuple -> {
          final var validNames = tuple.getItem2().getAllowedProcessNames();
          log.debug("Allowed process names: {}" , validNames, tuple.getItem1().stream().map(wk -> wk.getId() + "/" + wk.getName() + "/").collect(Collectors.toList()));
          return tuple.getItem1().stream().filter(wk -> validNames.contains(wk.getName())).collect(Collectors.toList());
        });
      }
      
      
      return userActions.queryUserAction()
          .userId(user.getSsn())
          .userName(user.getUsername())
          .list().collect().asList();
    });
  }
  

  
  @DeleteMapping(value="/{actionId}")
  public Uni<UserAction> cancelAction(@PathVariable("actionId") String actionId) {
    return iam.userQuery().get().onItem().transformToUni(client ->
    
      userActions.cancelUserAction()
      .processId(actionId)
      .userId(client.getUser().getSsn())
      .userName(client.getUser().getUsername())
      .build());
  }
  
  @GetMapping(value="/")
  public Uni<UserAction> kindOfCreateAction(
      @RequestParam("actionId") String actionId,
      @RequestParam("inputContextId") String inputContextId,
      @RequestParam("inputParentContextId") String inputParentContextId,
      @RequestParam("actionLocale") String actionLocale,
      @RequestHeader("cookie") String id
  ) {
    
    return iam.userQuery().get().onItem().transformToUni(client -> {
      
      final RepresentedPerson person = client.getUser().getRepresentedPerson();
      final RepresentedCompany company = client.getUser().getRepresentedCompany();
      if(person == null && company == null) {
        return createUserAction(
            actionId, client, actionLocale, inputContextId, inputParentContextId
         ); // Nobody is represented
      }
              
      final var query = person != null ? 
          iam.personRolesQuery().id(id).get() : 
          iam.companyRolesQuery().id(id).get();
      
      final Uni<AuthorizationAction> authorizations = query
          .onItem().transformToUni(roleData -> userActions
              .authorizationActionQuery()
              .userRoles(roleData.getUserRoles().getRoles())
              .get());
      return authorizations.onItem().transformToUni(auth -> {
        if(auth.getAllowedProcessNames().contains(actionId)) {
          return createUserAction(actionId, client, actionLocale, inputContextId, inputParentContextId); // User allowed
        }
        
        log.error("User blocked from accessing process: {} because they are not authorized!", actionId);
        return Uni.createFrom().nullItem(); 
      });
    }); 
  }
  

  
  private String[] getRepresentativeName(String name) {
    final var splitAt = name.indexOf(" ");
    if(splitAt <= 0) {
      return new String[] {" ", name.trim()};
    }
    return new String[] {name.substring(0, splitAt).trim(), name.substring(splitAt).trim()};
  }
  
  private Uni<UserAction> createUserAction(
      String actionId, 
      UserQueryResult client, String clientLocale, 
      String inputContextId,
      String inputParentContextId
      ) {
	  final var user = client.getUser();
    final var person = user.getRepresentedPerson();
    final var company = user.getRepresentedCompany();
    final var create = userActions.createUserAction()
      .actionName(actionId)
      .protectionOrder(user.getProtectionOrder())
      .language(clientLocale);
    
    if(person != null) {
      final var representativeName = getRepresentativeName(person.getName());
      final var representativeFirstName = representativeName[1];  
      final var representativeLastName = representativeName[0];
      
      return create
        .inputParentContextId(inputParentContextId)
        .inputContextId(inputContextId)
        .userName(representativeFirstName, representativeLastName)
        .userId(person.getPersonId())
        .representative(user.getFirstName(), user.getLastName(), user.getSsn())
        .build();
    } else if(company != null) {
      return create
        .inputParentContextId(inputParentContextId)
        .inputContextId(inputContextId)
        .companyName(company.getName())
        .userId(company.getCompanyId())
        .representative(user.getFirstName(), user.getLastName(), user.getSsn())
        .build();
    }
	
    return create
      .userName(user.getFirstName(), user.getLastName())
      .email(user.getContact().getEmail())
      .address(user.getContact().getAddressValue())
      .userId(user.getSsn())
      .inputParentContextId(inputParentContextId)
      .inputContextId(inputContextId)
      .build();
  }
}
