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

import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.client.api.PortalClient;
import io.digiexpress.eveli.client.api.ProcessAuthorizationCommands;
import io.digiexpress.eveli.client.api.ProcessCommands;
import io.digiexpress.eveli.client.config.PortalConfigBean;
import io.digiexpress.eveli.client.iam.PortalAccessValidator;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
/**
 * Rest controller to handle internal requests from portal.
 */
public class PortalProcessController extends ProcessBaseController {

  private final PortalAccessValidator validator;
  private PortalConfigBean config;
  public PortalProcessController(PortalClient client, PortalAccessValidator validator, PortalConfigBean config) {
    super(client);
    this.validator = validator;
    this.config = config;
  }
  
  @Transactional
  @GetMapping("/processesSearch")
  public ResponseEntity<?> searchProcesses(
      @RequestParam(name="workflow.name", defaultValue="") String name, 
      @RequestParam(name="status", required=false) List<String> status,
      @RequestParam(name="userId", defaultValue="") String userId, 
      Pageable pageable,
      @AuthenticationPrincipal Jwt principal) {

    validator.validateUserAccess(principal, userId);
    return super.searchProcesses(name, status, userId, pageable);
  }
  
  @PostMapping("/processes/")
  @Transactional
  public ResponseEntity<ProcessCommands.Process> create(@RequestBody ProcessCommands.InitProcess request,
      @AuthenticationPrincipal Jwt principal) {
    String identity = request.getIdentity();
    if (identity == null) {
      log.warn("Access violation by user: {}, missing request identity {}", validator.getUserName(principal), identity);
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }
    else if (principal == null && identity.equals(config.getPortalAnonymousUserId())) {
      log.info("Anonymous process creation {}", request);
    }
    else {
      validator.validateUserAccess(principal, identity);
    }
    return new ResponseEntity<>(client.process().create(request), HttpStatus.CREATED);
  }
  
  @PostMapping("/processesAuthorizations/")
  @Transactional
  public ResponseEntity<ProcessAuthorizationCommands.ProcessAuthorization> processesAuthorizations(
      @RequestBody ProcessAuthorizationCommands.InitProcessAuthorization request,
      @AuthenticationPrincipal Jwt principal) {
    return new ResponseEntity<>(client.processAuthorization().query().get(request), HttpStatus.OK);
  }
  
  
  @GetMapping("/processes/{id}")
  @Transactional
  public ResponseEntity<ProcessCommands.Process> get(@PathVariable("id") String id,
      @AuthenticationPrincipal Jwt principal) {
    final var process = client.process().query().get(id);
    
    if(process.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    else {
      validator.validateProcessAccess(process.get(), principal);
    }
    return new ResponseEntity<>(process.get(), HttpStatus.OK);
  }
  
  @DeleteMapping("/processes/{id}")
  @Transactional
  public ResponseEntity<ProcessCommands.Process> delete(@PathVariable("id") String id,
      @AuthenticationPrincipal Jwt principal) {
    validator.validateProcessIdAccess(id, principal);
    client.process().delete(id);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
