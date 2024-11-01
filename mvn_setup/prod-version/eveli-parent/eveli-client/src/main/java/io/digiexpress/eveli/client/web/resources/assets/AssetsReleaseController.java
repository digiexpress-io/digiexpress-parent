package io.digiexpress.eveli.client.web.resources.assets;

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
import io.digiexpress.eveli.assets.api.EveliAssetClient.Publication;
import io.digiexpress.eveli.assets.api.EveliAssetComposer;
import io.digiexpress.eveli.assets.api.EveliAssetComposer.AnyAssetTag;
import io.digiexpress.eveli.assets.api.EveliAssetComposer.AssetTagType;
import io.digiexpress.eveli.assets.api.EveliAssetComposer.CreatePublication;
import io.digiexpress.eveli.assets.api.EveliAssetComposer.Deployment;
import io.digiexpress.eveli.assets.api.ImmutableCreatePublication;
import io.digiexpress.eveli.client.api.AuthClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@RestController
@RequiredArgsConstructor
@Slf4j
public class AssetsReleaseController {
  private static final Duration timeout = Duration.ofMillis(10000);
  private final EveliAssetComposer composer;
  private final AuthClient securityClient;

  
  @GetMapping("/releases/")
  public ResponseEntity<List<Entity<Publication>>> getAllReleases() {
    return new ResponseEntity<>(composer.publicationQuery().findAll().await().atMost(timeout), HttpStatus.OK);
  }
  
  @GetMapping("/releases/{name}")
  public ResponseEntity<Entity<Publication>> get(@PathVariable("name") String name) {
    final var workflowRelease = composer.publicationQuery().findOneByName(name).await().atMost(timeout);
    if(workflowRelease.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return new ResponseEntity<>(workflowRelease.get(), HttpStatus.OK);
  }
  
  @PostMapping("/releases/")
  public ResponseEntity<Entity<Publication>> create(
      @RequestBody CreatePublication workflowRelease) {
    
    final var userName = securityClient.getUser().getPrincipal().getUsername();
    final var publicationInit = ImmutableCreatePublication.builder().from(workflowRelease).user(userName).build();
    
    try {
      return new ResponseEntity<>(
          composer.create().publication(publicationInit).await().atMost(timeout),
          HttpStatus.CREATED);
      
    } catch (org.springframework.dao.DataIntegrityViolationException e) {
      log.warn("Data integrity violation in release creation: {}", e.getMostSpecificCause().getMessage());
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Tag already exists");
    }
  }
  
  @GetMapping("/releaseTags/wrench")
  public ResponseEntity<List<AnyAssetTag>> getWrenchTags() {
    return new ResponseEntity<>(composer.anyAssetTagQuery().findAllByType(AssetTagType.WRENCH).await().atMost(timeout), HttpStatus.OK);
  }

  @GetMapping("/releaseTags/workflow")
  public ResponseEntity<List<? extends AnyAssetTag>> getWorkflowTags() {
    return new ResponseEntity<>(composer.anyAssetTagQuery().findAllByType(AssetTagType.WORKFLOW).await().atMost(timeout), HttpStatus.OK);
  }

  @GetMapping("/releaseTags/content")
  public ResponseEntity<List<AnyAssetTag>> getContentTags() {
    return new ResponseEntity<>(composer.anyAssetTagQuery().findAllByType(AssetTagType.STENCIL).await().atMost(timeout), HttpStatus.OK);
  }
  
  @GetMapping("/releaseDownload/{name}")
  public ResponseEntity<Deployment> download(@PathVariable("name") String name) {
    final var workflowRelease = composer.deployment().id(name).build().await().atMost(timeout);
    if(workflowRelease.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return new ResponseEntity<>(workflowRelease.get(), HttpStatus.OK);
  }

}
