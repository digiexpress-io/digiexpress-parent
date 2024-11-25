package io.digiexpress.eveli.client.web.resources.assets;

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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.digiexpress.eveli.assets.api.EveliAssetClient.Entity;
import io.digiexpress.eveli.assets.api.EveliAssetClient.Workflow;
import io.digiexpress.eveli.assets.api.EveliAssetClient.WorkflowTag;
import io.digiexpress.eveli.assets.api.EveliAssetComposer;
import io.digiexpress.eveli.assets.api.EveliAssetComposer.CreateWorkflow;
import io.digiexpress.eveli.assets.api.EveliAssetComposer.CreateWorkflowTag;
import io.digiexpress.eveli.assets.api.EveliAssetComposer.WorkflowMutator;
import io.digiexpress.eveli.assets.api.ImmutableCreateWorkflowTag;
import io.digiexpress.eveli.client.api.AuthClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@RestController
@RequestMapping("/worker/rest/api/assets/workflows")
@RequiredArgsConstructor
@Slf4j
public class AssetsWorkflowController {

  private final AuthClient authClient;
  private final EveliAssetComposer composer;
  private static final Duration timeout = Duration.ofMillis(10000);
  
  @GetMapping
  public ResponseEntity<List<Entity<Workflow>>> findAllWorkflows() {
    final var wks = composer.workflowQuery().findAll().await().atMost(timeout);
    return new ResponseEntity<>(wks, HttpStatus.OK);
  }
  
  @PostMapping
  public ResponseEntity<Entity<Workflow>> create(@RequestBody CreateWorkflow workflow) {
    return new ResponseEntity<>(composer.create().workflow(workflow).await().atMost(timeout), HttpStatus.CREATED);
  }
  
  @GetMapping("/{id}")
  public ResponseEntity<Entity<Workflow>> get(@PathVariable("id") String id) {
    final var workflow = composer.workflowQuery().findOneByName(id)
        .await().atMost(timeout);

    if(workflow.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return new ResponseEntity<>(workflow.get(), HttpStatus.OK);
  }
  
  @PutMapping("/{id}")
  @Transactional
  public ResponseEntity<Entity<Workflow>> save(@PathVariable("id") String id, @RequestBody WorkflowMutator workflow) {
    final var previousWorkflow = composer.workflowQuery().findOneById(id)
        .await().atMost(timeout);

    if(previousWorkflow.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    final var entity = composer.update().workflow(workflow).await().atMost(timeout);
    return new ResponseEntity<>(entity, HttpStatus.OK);
  }
  
  
  
  @GetMapping("/tags")
  public ResponseEntity<List<Entity<WorkflowTag>>> findAllTags() {
    return new ResponseEntity<>(composer.workflowTagQuery().findAll().await().atMost(timeout), HttpStatus.OK);
  }
  @PostMapping("/tags")
  public ResponseEntity<Entity<WorkflowTag>> createOneTag(@RequestBody CreateWorkflowTag workflowRelease) {
    try {
      final var snapshotRelease = ImmutableCreateWorkflowTag.builder()
        .from(workflowRelease)
        .user(authClient.getUser().getPrincipal().getUsername())
        .build();
      
      return new ResponseEntity<>(composer.create().workflowTag(snapshotRelease).await().atMost(timeout), HttpStatus.CREATED);
      
    } catch (Exception e) {
      log.warn("Data integrity violation in snapshot release creation: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tag already exists");
    }
  }
  @GetMapping("/tags/{id}")
  public ResponseEntity<Entity<WorkflowTag>> getOneTagById(@PathVariable("id") String name) {
    final var tags = composer.workflowTagQuery().findAll().await().atMost(timeout);
    final var workflowRelease = tags.stream().filter(e -> e.getBody().getName().equals(name)).findFirst();
    if(workflowRelease.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return new ResponseEntity<>(workflowRelease.get(), HttpStatus.OK);
  }
}
