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

import java.time.Duration;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.digiexpress.eveli.assets.api.EveliAssetClient.Entity;
import io.digiexpress.eveli.assets.api.EveliAssetClient.WorkflowTag;
import io.digiexpress.eveli.assets.api.EveliAssetComposer;
import io.digiexpress.eveli.assets.api.EveliAssetComposer.CreateWorkflowTag;
import io.digiexpress.eveli.assets.api.ImmutableCreateWorkflowTag;
import io.digiexpress.eveli.client.api.AuthClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AssetsWorkflowTagController {

  private final EveliAssetComposer composer;
  private static final Duration timeout = Duration.ofMillis(10000);
  private final AuthClient securityClient;

  
  @GetMapping("/workflowReleases/")
  public ResponseEntity<List<Entity<WorkflowTag>>> getAllWorkflows() {
    return new ResponseEntity<>(composer.workflowTagQuery().findAll().await().atMost(timeout), HttpStatus.OK);
  }


  @PostMapping("/workflowReleases/")
  public ResponseEntity<Entity<WorkflowTag>> createSnapshot(@RequestBody CreateWorkflowTag workflowRelease) {
    try {
      final var snapshotRelease = ImmutableCreateWorkflowTag.builder()
        .from(workflowRelease)
        .user(securityClient.getUser().getPrincipal().getUserName())
        .build();
      
      return new ResponseEntity<>(composer.create().workflowTag(snapshotRelease).await().atMost(timeout), HttpStatus.CREATED);
      
    } catch (Exception e) {
      log.warn("Data integrity violation in snapshot release creation: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tag already exists");
    }
  }
  
  @GetMapping("/workflowReleases/{name}")
  public ResponseEntity<Entity<WorkflowTag>> get(@PathVariable("name") String name) {
    final var tags = composer.workflowTagQuery().findAll().await().atMost(timeout);
    final var workflowRelease = tags.stream().filter(e -> e.getBody().getName().equals(name)).findFirst();
    if(workflowRelease.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return new ResponseEntity<>(workflowRelease.get(), HttpStatus.OK);
  }
  
}
