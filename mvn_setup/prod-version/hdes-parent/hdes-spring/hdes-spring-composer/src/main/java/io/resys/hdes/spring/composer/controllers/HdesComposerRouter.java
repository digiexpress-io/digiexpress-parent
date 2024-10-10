package io.resys.hdes.spring.composer.controllers;

/*-
 * #%L
 * hdes-spring-bundle-editor
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.resys.hdes.client.api.HdesComposer;
import io.resys.hdes.client.api.HdesComposer.ComposerEntity;
import io.resys.hdes.client.api.HdesComposer.ComposerState;
import io.resys.hdes.client.api.HdesComposer.CopyAs;
import io.resys.hdes.client.api.HdesComposer.CreateEntity;
import io.resys.hdes.client.api.HdesComposer.DebugRequest;
import io.resys.hdes.client.api.HdesComposer.DebugResponse;
import io.resys.hdes.client.api.HdesComposer.StoreDump;
import io.resys.hdes.client.api.HdesComposer.UpdateEntity;
import io.resys.hdes.client.api.HdesStore.HistoryEntity;
import io.resys.hdes.client.api.ImmutableDiffRequest;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.hdes.client.api.ast.AstTagSummary;
import io.resys.hdes.client.api.diff.TagDiff;
import io.resys.hdes.client.spi.web.HdesWebConfig;
import io.resys.hdes.spring.composer.ComposerConfigBean;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;


@RestController
@RequestMapping(ComposerConfigBean.REST_CTX_PATH)
public class HdesComposerRouter {
  private final HdesComposer composer;
  private final ObjectMapper objectMapper;
  private static final Duration timeout = Duration.ofMillis(10000);

  @Value("${app.version}")
  private String version;

  @Value("${build.timestamp}")
  private String timestamp;

  public HdesComposerRouter(HdesComposer composer, ObjectMapper objectMapper) {
    super();
    this.composer = composer;
    this.objectMapper = objectMapper;
  }

  @GetMapping(path = "/" + HdesWebConfig.MODELS, produces = MediaType.APPLICATION_JSON_VALUE)
  public ComposerState dataModels(@RequestHeader(value = "Branch-Name", required = false) String branchName) {
    return composer.withBranch(branchName).get().await().atMost(timeout);
  }

  @GetMapping(path = "/" + HdesWebConfig.EXPORTS, produces = MediaType.APPLICATION_JSON_VALUE)
  public StoreDump exports() {
    return composer.getStoreDump().await().atMost(timeout);
  }

  @PostMapping(path = "/" + HdesWebConfig.COMMANDS, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ComposerEntity<?> commands(@RequestBody String body, @RequestHeader(value = "Branch-Name", required = false) String branchName) throws JsonMappingException, JsonProcessingException {
    final var command = objectMapper.readValue(body, UpdateEntity.class);
    return composer.withBranch(branchName).dryRun(command).await().atMost(timeout);
  }

  @PostMapping(path = "/" + HdesWebConfig.DEBUGS, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public DebugResponse debug(@RequestBody DebugRequest debug, @RequestHeader(value = "Branch-Name", required = false) String branchName) {
    return composer.withBranch(branchName).debug(debug).await().atMost(timeout);
  }

  @PostMapping(path = "/" + HdesWebConfig.IMPORTS, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ComposerState importTag(@RequestBody AstTag entity) {
    return composer.importTag(entity).await().atMost(timeout);
  }

  @PostMapping(path = "/" + HdesWebConfig.RESOURCES, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ComposerState create(@RequestBody CreateEntity entity, @RequestHeader(value = "Branch-Name", required = false) String branchName) {
    return composer.withBranch(branchName).create(entity).await().atMost(timeout);
  }

  @PutMapping(path = "/" + HdesWebConfig.RESOURCES, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ComposerState update(@RequestBody UpdateEntity entity, @RequestHeader(value = "Branch-Name", required = false) String branchName) {
    return composer.withBranch(branchName).update(entity).await().atMost(timeout);
  }

  @DeleteMapping(path = "/" + HdesWebConfig.RESOURCES + "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ComposerState delete(@PathVariable String id, @RequestHeader(value = "Branch-Name", required = false) String branchName) {
    return composer.withBranch(branchName).delete(id).await().atMost(timeout);
  }

  @GetMapping(path = "/" + HdesWebConfig.RESOURCES + "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ComposerEntity<?> get(@PathVariable String id, @RequestHeader(value = "Branch-Name", required = false) String branchName) {
    return composer.withBranch(branchName).get(id).await().atMost(timeout);
  }

  @PostMapping(path = "/" + HdesWebConfig.COPYAS, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ComposerState copyAs(@RequestBody CopyAs entity, @RequestHeader(value = "Branch-Name", required = false) String branchName) {
    return composer.withBranch(branchName).copyAs(entity).await().atMost(timeout);
  }

  @GetMapping(path = "/" + HdesWebConfig.HISTORY + "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public HistoryEntity history(@RequestParam("id") String id) {
    return composer.getHistory(id).await().atMost(timeout);
  }

  @GetMapping(path = "/" + HdesWebConfig.DIFF, produces = MediaType.APPLICATION_JSON_VALUE)
  public TagDiff diff(@RequestParam("baseId") String baseId, @RequestParam("targetId") String targetId) {
    final var request = ImmutableDiffRequest.builder().baseId(baseId).targetId(targetId).build();
    return composer.diff(request).await().atMost(timeout);
  }

  @GetMapping(path = "/" + HdesWebConfig.SUMMARY + "/{tagId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public AstTagSummary summary(@PathVariable("tagId") String tagId) {
    return composer.summary(tagId).await().atMost(timeout);
  }

  @GetMapping(path = "/" + HdesWebConfig.VERSION, produces = MediaType.APPLICATION_JSON_VALUE)
  public VersionEntity version() {
    return new VersionEntity(version, timestamp);
  }
  
  @Data
  @RequiredArgsConstructor
  public static class VersionEntity {
    private final String version;
    private final String built;
  }

}
