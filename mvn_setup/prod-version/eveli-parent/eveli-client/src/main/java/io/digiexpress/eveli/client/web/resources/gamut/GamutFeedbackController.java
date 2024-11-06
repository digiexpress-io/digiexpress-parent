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

import io.digiexpress.eveli.client.api.GamutClient;
import io.digiexpress.eveli.client.api.GamutClient.ProcessCantBeDeletedException;
import io.digiexpress.eveli.client.api.GamutClient.ProcessNotFoundException;
import io.digiexpress.eveli.client.api.GamutClient.UserActionNotAllowedException;
import io.digiexpress.eveli.client.api.GamutClient.WorkflowNotFoundException;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.thestencil.iam.api.UserActionsClient.UserAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("/portal/feedback")
@RequiredArgsConstructor
public class GamutFeedbackController {
  private final GamutClient gamutClient;
  private final DialobClient dialob;
  private final List<String> allowedActions;
  
  @GetMapping(value="fill/{sessionId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> fillProxyGet(@PathVariable("sessionId") String sessionId) {
    return dialob.createProxy().sessionGet(sessionId);
  }
  @PostMapping(value="/fill/{sessionId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> fillProxyPost(@PathVariable("sessionId") String sessionId, @RequestBody String body) {
    return dialob.createProxy().sessionPost(sessionId, body);
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
  @GetMapping(value="/allowed")
  public List<String> getAllowed() {
    return allowedActions;
  }
  
  public ResponseEntity<UserAction> kindOfCreateAction(
      @RequestParam("actionId") String actionId,
      @RequestParam("inputContextId") String inputContextId,
      @RequestParam("inputParentContextId") String inputParentContextId,
      @RequestParam("actionLocale") String actionLocale) {
    

    if(!allowedActions.contains(actionId)) {
      throw new org.springframework.security.access.AccessDeniedException("actionId: " + actionId + ", not allowed!, Allowed: " + allowedActions + "!");
    }
    
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
