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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.client.api.PortalClient;
import io.digiexpress.eveli.client.api.ProcessCommands;
import io.digiexpress.eveli.client.iam.PortalAccessValidator;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
/**
 * Rest controller to handle internal requests from portal.
 */
public class PortalProcessAnonymousController extends ProcessBaseController {

  private final PortalAccessValidator validator;
  private final String anonymousUserId;
  public PortalProcessAnonymousController(PortalClient client, PortalAccessValidator validator, String anonymousUserId) {
    super(client);
    this.validator = validator;
    this.anonymousUserId = anonymousUserId;
  }
  
  @PostMapping("/anonymous/processes/")
  @Transactional
  public ResponseEntity<ProcessCommands.Process> create(@RequestBody ProcessCommands.InitProcess request,
      @AuthenticationPrincipal Jwt principal) {
    String identity = request.getIdentity();
    if (identity == null) {
      log.warn("Access violation by anonymous, missing request identity {}", identity);
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }
    else if (principal == null && identity.equals(anonymousUserId)) {
      log.info("Anonymous process creation {}", request);
    }
    return new ResponseEntity<>(client.process().create(request), HttpStatus.CREATED);
  }
  
  @GetMapping("/anonymous/processes/{id}")
  @Transactional
  public ResponseEntity<ProcessCommands.Process> get(@PathVariable("id") String id,
      @AuthenticationPrincipal Jwt principal) {
    final var process = client.process().query().get(id);
    
    if(process.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return new ResponseEntity<>(process.get(), HttpStatus.OK);
  }
  
  @DeleteMapping("/anonymous/processes/{id}")
  @Transactional
  public ResponseEntity<ProcessCommands.Process> delete(@PathVariable("id") String id,
      @AuthenticationPrincipal Jwt principal) {
    validator.validateProcessAnonymousAccess(id, anonymousUserId);
    client.process().delete(id);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
