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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.assets.api.EveliAssetComposer;
import io.digiexpress.eveli.assets.api.EveliAssetComposer.Deployment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@RestController
@RequiredArgsConstructor
@RequestMapping("assets/deployments")
@Slf4j
public class AssetsDeploymentController {
  private static final Duration timeout = Duration.ofMillis(10000);
  private final EveliAssetComposer composer;
  
  @GetMapping("/{name}")
  public ResponseEntity<Deployment> download(@PathVariable("name") String name) {
    final var workflowRelease = composer.deployment().id(name).build().await().atMost(timeout);
    if(workflowRelease.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return new ResponseEntity<>(workflowRelease.get(), HttpStatus.OK);
  }

}
