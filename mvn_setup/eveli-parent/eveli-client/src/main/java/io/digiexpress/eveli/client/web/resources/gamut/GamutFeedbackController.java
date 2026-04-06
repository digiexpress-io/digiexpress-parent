package io.digiexpress.eveli.client.web.resources.gamut;

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
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
// TODO:: should be public forms and not feedback
@RequestMapping("/portal/feedback")
@RequiredArgsConstructor
public class GamutFeedbackController {
  private final GamutClient gamutClient;
  private final io.resys.limaone.program.Runtime runtime;
  private final DialobFillEventPublisher publisher;
  private final GamutAuthClient authClient;
  
  @GetMapping(value="fill/{sessionId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Uni<?> fillProxyGet(@PathVariable("sessionId") String sessionId) {
    return runtime.getProperties().getFormDb().withTenant().createFormFill().formInstanceId(sessionId).build()
        .onItem().transform(questionnaire -> questionnaire.unwrap());
  }
  @PostMapping(value="/fill/{sessionId}", produces = MediaType.APPLICATION_JSON_VALUE)
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
  
  @DeleteMapping(value="/{actionId}")
  public Uni<ResponseEntity<UserAction>> cancelAction(@PathVariable("actionId") String actionId) {
    final var customer = authClient.getParticipant();    
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
        .customer(authClient.getParticipant())
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
    
    return gamutClient.userActionBuilder()
        .actionId(actionId)
        .cockpitId(cockpitId)
        .participant(authClient.getParticipant())
        .clientLocale(actionLocale)
        .inputContextId(inputContextId)
        .inputParentContextId(inputParentContextId)
        .createOne()
        .onItem().transform(action -> new ResponseEntity<>(action, HttpStatus.OK))
        .onFailure().recoverWithItem(ignore -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }
}
