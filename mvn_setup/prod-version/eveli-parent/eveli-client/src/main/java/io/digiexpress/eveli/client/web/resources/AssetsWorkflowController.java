package io.digiexpress.eveli.client.web.resources;

import java.time.Duration;

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
import java.util.function.Supplier;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.assets.api.EveliAssetClient.Entity;
import io.digiexpress.eveli.assets.api.EveliAssetClient.Workflow;
import io.digiexpress.eveli.assets.api.EveliAssetComposer;
import io.digiexpress.eveli.assets.api.EveliAssetComposer.CreateWorkflow;
import io.digiexpress.eveli.assets.api.EveliAssetComposer.WorkflowMutator;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import lombok.RequiredArgsConstructor;



@RestController
@RequiredArgsConstructor
public class AssetsWorkflowController {

  private final EveliAssetComposer composer;
  private final Supplier<ProgramEnvir> programEnvir;
  private static final Duration timeout = Duration.ofMillis(10000);
  
  @GetMapping("/workflows/")
  public ResponseEntity<List<Entity<Workflow>>> getAllWorkflows() {
    final var wks = composer.workflowQuery().findAll().await().atMost(timeout);
    return new ResponseEntity<>(wks, HttpStatus.OK);
  }
  
  @PostMapping("/workflows/")
  public ResponseEntity<Entity<Workflow>> create(@RequestBody CreateWorkflow workflow) {
    return new ResponseEntity<>(composer.create().workflow(workflow).await().atMost(timeout), HttpStatus.CREATED);
  }
  
  @GetMapping("/workflows/{id}")
  public ResponseEntity<Entity<Workflow>> get(@PathVariable("id") String id) {
    final var workflow = composer.workflowQuery().findOneByName(id)
        .await().atMost(timeout);

    if(workflow.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return new ResponseEntity<>(workflow.get(), HttpStatus.OK);
  }
  
  @PutMapping("/workflows/{id}")
  @Transactional
  public ResponseEntity<Entity<Workflow>> save(@PathVariable("id") String id, @RequestBody WorkflowMutator workflow) {
    final var previousWorkflow = composer.workflowQuery().findOneByName(id)
        .await().atMost(timeout);

    if(previousWorkflow.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    final var entity = composer.update().workflow(workflow).await().atMost(timeout);
    return new ResponseEntity<>(entity, HttpStatus.OK);
  }
  
  @GetMapping(path="/workflowAssets/")
  public ResponseEntity<List<String>> allFlows() {
    return ResponseEntity.status(HttpStatus.OK).body(programEnvir.get().getFlowsByName().keySet().stream().toList());
  }
}
