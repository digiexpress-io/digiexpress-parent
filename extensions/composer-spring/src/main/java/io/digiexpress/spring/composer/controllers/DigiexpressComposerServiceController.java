package io.digiexpress.spring.composer.controllers;

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

import java.time.Duration;

import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.client.api.Client;
import io.digiexpress.client.api.Composer;
import io.digiexpress.client.api.ComposerEntity.CreateMigration;
import io.digiexpress.client.api.ComposerEntity.DefinitionState;
import io.digiexpress.client.api.ComposerEntity.HeadState;
import io.digiexpress.client.api.ComposerEntity.MigrationState;
import io.digiexpress.client.spi.ComposerImpl;
import io.digiexpress.spring.composer.config.UiConfigBean;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(UiConfigBean.REST_SPRING_CTX_PATH_EXP)
public class DigiexpressComposerServiceController {
  
  private final Client client;
  private final Composer composer;
  private static final Duration timeout = Duration.ofMillis(100000);
  
  
  @Data @Jacksonized
  public static class HeadCreate {
    
  }
  
  public DigiexpressComposerServiceController(ObjectMapper objectMapper, ApplicationContext ctx, Client client) {
    super();
    this.client = client;
    this.composer = new ComposerImpl(client);
    
    final var servicePath = ctx.getEnvironment().getProperty(UiConfigBean.REST_SPRING_CTX_PATH);
    final var uiPath = ctx.getEnvironment().getProperty(UiConfigBean.UI_SPRING_CTX_PATH);    
    final var uiEnabled = ctx.getEnvironment().getProperty(UiConfigBean.UI_ENABLED);
    
    final var logBuilder = new StringBuilder()
      .append("Digiexpress, Composer Service: UP").append(System.lineSeparator())
      .append("service paths:").append(System.lineSeparator())
      .append("  - GET, user interface, ").append("enabled: ").append(uiEnabled).append(", path: '").append(uiPath).append("' ").append(System.lineSeparator())
      .append("  - restapi: ").append(servicePath).append(System.lineSeparator())
    ;
    log.info(logBuilder.toString());
  }
  @PostMapping(path = "/" + UiConfigBean.API_MIGRATE, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public MigrationState migrate(@RequestBody CreateMigration entity) {
    return composer.create().migrate(entity).await().atMost(Duration.ofMillis(90000));
  }
  @GetMapping(path = "/" + UiConfigBean.API_HEAD, produces = MediaType.APPLICATION_JSON_VALUE)
  public HeadState head() {
    return composer.query().head().await().atMost(timeout);
  }
  @GetMapping(path = "/" + UiConfigBean.API_DEF + "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public DefinitionState def(@PathVariable String id) {
    return composer.query().definition(id).await().indefinitely();
  }
  @PostMapping(path = "/" + UiConfigBean.API_HEAD, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public HeadState headCreate(HeadCreate create) {
    return client.tenant().create()
        .onItem().transformToUni((_created) -> composer.query().head())
        .await().atMost(timeout);
  }
}
