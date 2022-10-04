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
import io.resys.hdes.client.api.HdesComposer.*;
import io.resys.hdes.client.api.HdesStore.HistoryEntity;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.hdes.client.spi.web.HdesWebConfig;
import io.resys.hdes.spring.composer.ComposerConfigBean;
import io.resys.hdes.spring.composer.controllers.util.VersionEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
  public ComposerState dataModels() {
    return composer.get().await().atMost(timeout);
  }

  @GetMapping(path = "/" + HdesWebConfig.EXPORTS, produces = MediaType.APPLICATION_JSON_VALUE)
  public StoreDump exports() {
    return composer.getStoreDump().await().atMost(timeout);
  }
  
  @PostMapping(path = "/" + HdesWebConfig.COMMANDS, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ComposerEntity<?> commands(@RequestBody String body) throws JsonMappingException, JsonProcessingException {
    final var command = objectMapper.readValue(body, UpdateEntity.class);
    return composer.dryRun(command).await().atMost(timeout);
  }

  @PostMapping(path = "/" + HdesWebConfig.DEBUGS, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public DebugResponse debug(@RequestBody DebugRequest debug) {
    return composer.debug(debug).await().atMost(timeout);
  }

  @PostMapping(path = "/" + HdesWebConfig.IMPORTS, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ComposerState importTag(@RequestBody AstTag entity) {
    return composer.importTag(entity).await().atMost(timeout);
  }

  @PostMapping(path = "/" + HdesWebConfig.RESOURCES, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ComposerState create(@RequestBody CreateEntity entity) {
    return composer.create(entity).await().atMost(timeout);
  }
  @PutMapping(path = "/" + HdesWebConfig.RESOURCES, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ComposerState update(@RequestBody UpdateEntity entity) {
    return composer.update(entity).await().atMost(timeout);
  }
  @DeleteMapping(path = "/" + HdesWebConfig.RESOURCES + "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ComposerState delete(@PathVariable String id) {
    return composer.delete(id).await().atMost(timeout);
  }
  @GetMapping(path = "/" + HdesWebConfig.RESOURCES + "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ComposerEntity<?> get(@PathVariable String id) {
    return composer.get(id).await().atMost(timeout);
  }
  
  @PostMapping(path = "/" + HdesWebConfig.COPYAS, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ComposerState copyAs(@RequestBody CopyAs entity) {
    return composer.copyAs(entity).await().atMost(timeout);
  }
  @GetMapping(path = "/" + HdesWebConfig.HISTORY + "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public HistoryEntity history(@RequestParam("id") String id) {
    return composer.getHistory(id).await().atMost(timeout);
  }
  @GetMapping(path = "/" + HdesWebConfig.VERSION, produces = MediaType.APPLICATION_JSON_VALUE)
  public VersionEntity version() {
    return new VersionEntity(version, timestamp);
  }
}
