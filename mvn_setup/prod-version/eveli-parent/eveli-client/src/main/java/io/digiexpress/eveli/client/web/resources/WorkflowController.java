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
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.client.api.PortalClient;
import io.digiexpress.eveli.client.api.WorkflowCommands.Workflow;
import io.digiexpress.eveli.client.api.WorkflowCommands.WorkflowInit;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class WorkflowController {

  private final PortalClient client;

  @GetMapping("/workflows/")
  @Transactional
  public ResponseEntity<List<Workflow>> getAllWorkflows() {
    return new ResponseEntity<>(client.workflow().query().findAll(), HttpStatus.OK);
  }
  
  @PostMapping("/workflows/")
  @Transactional
  public ResponseEntity<Workflow> create(@RequestBody WorkflowInit workflow) {
    return new ResponseEntity<>(client.workflow().create(workflow), HttpStatus.CREATED);
  }
  
  @GetMapping("/workflows/{id}")
  @Transactional
  public ResponseEntity<Workflow> get(@PathVariable("id") String id) {
    final var workflow = client.workflow().query().get(id);
    if(workflow.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return new ResponseEntity<>(workflow.get(), HttpStatus.OK);
  }
  
  @PutMapping("/workflows/{id}")
  @Transactional
  public ResponseEntity<Workflow> save(@PathVariable("id") String id, @RequestBody Workflow workflow) {
    final var entity = client.workflow().update(id, workflow);
    if(entity.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return new ResponseEntity<>(entity.get(), HttpStatus.OK);
  }
  
  @GetMapping(path="/workflowAssets")
  public ResponseEntity<List<String>> allFlows() {
    return ResponseEntity.status(HttpStatus.OK).body(client.hdes().query().findFlowNames());
  }
}
