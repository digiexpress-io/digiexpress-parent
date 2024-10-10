package io.resys.hdes.spring.composer;

/*-
 * #%L
 * hdes-spring-composer
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

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.spi.HdesComposerImpl;
import io.resys.hdes.spring.composer.ComposerAutoConfiguration.SpringIdeTokenSupplier;
import io.resys.hdes.spring.composer.controllers.HdesComposerRouter;
import io.resys.hdes.spring.composer.controllers.IdeController;
import io.resys.hdes.spring.composer.controllers.RedirectController;
import io.resys.hdes.spring.composer.controllers.util.ControllerUtil;

@Configuration
@ConditionalOnProperty(name = "wrench.assets.ide", havingValue = "true", matchIfMissing = true)
public class ComposerIdeConfig {
  private static final Logger LOGGER = LoggerFactory.getLogger(ComposerIdeConfig.class);

  @Value("${server.servlet.context-path:}")
  private String contextPath;
  
  @Bean
  public IdeController hdesIdeController(ComposerConfigBean composerConfig, Optional<SpringIdeTokenSupplier> token) {
    final var config = ControllerUtil.ideOnClasspath(contextPath);
    LOGGER.debug("Hdes IDE Controller: " + config.getMainJs());
    return new IdeController(composerConfig, config, token);
  }
  @Bean
  public RedirectController hdesRedirectController(ComposerConfigBean composerConfig) {
    LOGGER.debug("Hdes Composer Index Redirect: UP");
    return new RedirectController(composerConfig);
  }
  @Bean
  public HdesComposerRouter hdesComposerController(HdesClient client, ObjectMapper objectMapper) {
    LOGGER.debug("Hdes Composer Router: UP");
    return new HdesComposerRouter(new HdesComposerImpl(client), objectMapper);
  }
  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
    LOGGER.debug("Hdes Composer Jackson Modules: UP");
    return builder -> builder.modules(new GuavaModule(), new JavaTimeModule(), new Jdk8Module());
  }
}
