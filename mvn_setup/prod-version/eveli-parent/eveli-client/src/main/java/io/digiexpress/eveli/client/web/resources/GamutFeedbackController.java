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

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.client.api.AuthClient;
import io.smallrye.mutiny.Uni;
import io.thestencil.iam.api.UserActionsClient;
import io.thestencil.iam.api.UserActionsClient.UserAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("/gamut/feedback")
@RequiredArgsConstructor
public class GamutFeedbackController {

  private final UserActionsClient userActions;
  private final AuthClient auth;
  private final List<String> allowedActions;
  private final String anonUserFirstname;
  private final String anonUserLastname;
  private final String anonEmail;
  private final String anonUserid;
  private final String anonAddress;
  
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
  
  @DeleteMapping(value="/{actionId}")
  public Uni<UserAction> cancelAction(@PathVariable("actionId") String actionId) {
    final var client = auth.getCustomer();
    
    return userActions.cancelUserAction()
      .processId(actionId)
      .userId(client.getPrincipal().getSsn())
      .userName(client.getPrincipal().getUsername())
      .build();
  }
  
  @GetMapping(value="/allowed")
  public List<String> getAllowed() {
    return allowedActions;
  }
  
  public Uni<UserAction> kindOfCreateAction(
      @RequestParam("actionId") String actionId,
      @RequestParam("inputContextId") String inputContextId,
      @RequestParam("inputParentContextId") String inputParentContextId,
      @RequestParam("actionLocale") String actionLocale) {
    

    if(!allowedActions.contains(actionId)) {
      throw new org.springframework.security.access.AccessDeniedException("actionId: " + actionId + ", not allowed!, Allowed: " + allowedActions + "!");
    }
    
    return createUserAction(actionId, actionLocale, inputContextId, inputParentContextId);
  }
  
  
  private Uni<UserAction> createUserAction(
      String actionId, 
      String clientLocale, 
      String inputContextId,
      String inputParentContextId
  ) {
    

    final var create = userActions.createUserAction()
      .actionName(actionId)
      .protectionOrder(false)
      .language(clientLocale);
    	
    return create
      .userName(anonUserFirstname, anonUserLastname)
      .email(anonEmail)
      .address(anonAddress)
      .userId(anonUserid)
      .inputParentContextId(inputParentContextId)
      .inputContextId(inputContextId)
      .build();
  }
}
