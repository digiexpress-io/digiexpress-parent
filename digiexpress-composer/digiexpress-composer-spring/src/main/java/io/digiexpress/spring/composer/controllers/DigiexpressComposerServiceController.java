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
import java.util.Map;

import org.immutables.value.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.spring.composer.config.UiConfigBean;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(UiConfigBean.REST_SPRING_CTX_PATH_EXP)
public class DigiexpressComposerServiceController {
  
  private final ObjectMapper objectMapper;
  private static final Duration timeout = Duration.ofMillis(10000);

  
  @Value.Immutable @JsonSerialize(as = ImmutableInitSession.class) @JsonDeserialize(as = ImmutableInitSession.class)
  public interface InitSession {
    String getFormId();
    String getLanguage();
    Map<String, Object> getContextValues();
  }
  
  public DigiexpressComposerServiceController(ObjectMapper objectMapper, ApplicationContext ctx) {
    super();
    this.objectMapper = objectMapper;
    
    final var servicePath = ctx.getEnvironment().getProperty(UiConfigBean.REST_SPRING_CTX_PATH);
    final var uiPath = ctx.getEnvironment().getProperty(UiConfigBean.UI_SPRING_CTX_PATH);    
    final var uiEnabled = ctx.getEnvironment().getProperty(UiConfigBean.UI_ENABLED);
    
    final var log = new StringBuilder()
    .append("Digiexpress, Composer Service: UP").append(System.lineSeparator())
    .append("service paths:").append(System.lineSeparator())
    .append("  - GET, html").append(uiPath).append(": ").append("user interface, enabled: ").append(uiEnabled).append(System.lineSeparator())
    ;
    
    DigiexpressComposerServiceController.log.info(log.toString());
  }
}
