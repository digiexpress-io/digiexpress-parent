package io.digiexpress.eveli.client.web.resources.gamut;

import java.time.Duration;
import java.util.Collections;

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

import io.digiexpress.eveli.client.api.GamutAuthClient;
import io.digiexpress.eveli.client.api.GamutClient;
import io.digiexpress.eveli.client.api.GamutClient.UserAction;
import io.digiexpress.eveli.client.spi.dialob.DialobFillEventPublisher;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
// TODO:: should be public forms and not feedback
@RequestMapping("/portal/feedback")
@RequiredArgsConstructor
public class GamutFeedbackController {
  private static final Duration timeout = Duration.ofSeconds(15);
  private final GamutClient gamutClient;
  private final DialobClient dialob;
  private final DialobFillEventPublisher publisher;
  private final GamutAuthClient authClient;
  
  @GetMapping(value="fill/{sessionId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> fillProxyGet(@PathVariable("sessionId") String sessionId) {
    ResponseEntity<String> responseEntity = dialob.createProxyClient().sessionGet(sessionId);
    return ResponseEntity.status(responseEntity.getStatusCode()).body(responseEntity.getBody());
  }
  @PostMapping(value="/fill/{sessionId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Uni<ResponseEntity<String>> fillProxyPost(@PathVariable("sessionId") String sessionId, @RequestBody String body) {
    final var resp = dialob.createProxyClient().sessionPost(sessionId, body);
    
    if(resp.getStatusCode().is2xxSuccessful()) {
      return gamutClient.fillEvent()
        .requestBody(body)
        .responseBody(resp.getBody())
        .sessionId(sessionId)
        .create()
        .onItem().invoke(publisher::publishEvent)
        .map(ignore -> ResponseEntity.status(resp.getStatusCode()).body(resp.getBody()));
      
    }
    return Uni.createFrom().item(ResponseEntity.status(resp.getStatusCode()).body(resp.getBody()));
  }
  
  @DeleteMapping(value="/{actionId}")
  public Uni<ResponseEntity<UserAction>> cancelAction(@PathVariable("actionId") String actionId) {
    final var customer = authClient.getCustomer();    
    return gamutClient.cancelUserActionBuilder()
        .actionId(actionId).customer(customer).cancelOne()
        .map(body -> new ResponseEntity<>(body, HttpStatus.OK))
        .onFailure().recoverWithItem(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
  }
  
  @GetMapping
  public List<UserAction> getActions() {
    return Collections.emptyList(); // not keeping unauthenticated actions
  }
  
  @GetMapping(value="/{actionId}")
  public Uni<ResponseEntity<UserAction>> getActionById(
      @PathVariable("actionId") String actionId, 
      @RequestParam(name = "cockpitId", required = false) String cockpitId) {
    
    return gamutClient.userActionQuery()
        .cockpitId(cockpitId)
        .customer(authClient.getCustomer(), authClient.getCustomerRoles())
        .findOneAnonById(actionId)
        .onItem().transform(action -> {
          if(action.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
          }
          return new ResponseEntity<>(action.get(), HttpStatus.OK);
        });
  }
  
  @PostMapping
  public Uni<ResponseEntity<UserAction>> kindOfCreateAction(
      @RequestParam("actionId") String actionId,
      @RequestParam(name = "cockpitId", required = false) String cockpitId,
      @RequestParam("inputContextId") String inputContextId,
      @RequestParam("inputParentContextId") String inputParentContextId,
      @RequestParam("actionLocale") String actionLocale) {
    
    final var customer = authClient.getCustomer();
    final var customerRoles = authClient.getCustomerRoles();
    
    return gamutClient.userActionMetaQuery().actionId(actionId).cockpitId(cockpitId).locale(actionLocale)
      .getOne().onItem().transformToUni(meta -> {
        if(!Boolean.TRUE.equals(meta.getTopicLink().getAnon())) {
          throw new org.springframework.security.access.AccessDeniedException("action: " + meta + ", not allowed!");
        }
          return gamutClient.userActionBuilder()
            .actionId(actionId)
            .cockpitId(cockpitId)
            .anon(true)
            .customer(customer)
            .customerRoles(customerRoles)
            .clientLocale(actionLocale)
            .inputContextId(inputContextId)
            .inputParentContextId(inputParentContextId)
            .createOne();
    })
    .map(ResponseEntity::ok)
    .onFailure().recoverWithItem(() -> ResponseEntity.notFound().build())
    // make the type as "<?>"
    .map(e -> e);
  }
}
