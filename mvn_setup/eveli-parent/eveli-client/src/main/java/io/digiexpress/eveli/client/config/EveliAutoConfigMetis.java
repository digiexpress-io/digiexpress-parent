package io.digiexpress.eveli.client.config;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import io.digiexpress.eveli.client.web.resources.worker.MetisApiController;
import io.resys.metis.api.ImmutableMetisConfig;
import io.resys.metis.api.MetisClient;
import io.resys.metis.api.MetisConfig;
import io.resys.metis.search.api.MetisSearchClient;
import io.resys.metis.spi.MetisClientImpl;
import io.resys.metis.spi.ai.EmbeddingService;
import io.resys.metis.spi.ai.StructuredChatService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@ConditionalOnBooleanProperty(value = "eveli.metis.enabled", havingValue = true, matchIfMissing = false)
@EnableConfigurationProperties(value = { EveliPropsMetis.class, EveliPropsMetisSearch.class })
public class EveliAutoConfigMetis {

  @Bean
  public MetisConfig metisConfig(
      Environment environment,
      ObjectProvider<EmbeddingModel> embeddingModels,
      ObjectProvider<ChatClient.Builder> chatClients,
      ObjectProvider<ChatModel> chatModels) {
    final var embeddingModel = embeddingModels.getIfAvailable();
    final var chatClient = chatClients.getIfAvailable();
    if (embeddingModel == null || chatClient == null) {
      throw new IllegalStateException(
          "eveli.metis.enabled is true but Spring AI did not create EmbeddingModel / ChatClient beans. "
              + "Set spring.ai.model.chat and spring.ai.model.embedding to a provider (for example ollama), not none.");
    }
    final var chatModel = chatModels.getIfAvailable();
    return ImmutableMetisConfig.builder()
        .provider(environment.getProperty("spring.ai.model.embedding", ""))
        .chatModelId(firstNonBlank(
            modelIdFromAiBean(chatModel),
            environment.getProperty("spring.ai.ollama.chat.options.model"),
            environment.getProperty("spring.ai.openai.chat.options.model")))
        .embeddingModelId(firstNonBlank(
            modelIdFromAiBean(embeddingModel),
            environment.getProperty("spring.ai.ollama.embedding.options.model"),
            environment.getProperty("spring.ai.openai.embedding.options.model"),
            embeddingModel.getClass().getSimpleName()))
        .build();
  }

  private static String modelIdFromAiBean(Object bean) {
    if (bean == null) {
      return "";
    }
    try {
      final var options = bean.getClass().getMethod("getDefaultOptions").invoke(bean);
      return modelName(options);
    } catch (ReflectiveOperationException ignored) {
      return "";
    }
  }

  private static String modelName(Object options) {
    if (options == null) {
      return "";
    }
    try {
      final var value = options.getClass().getMethod("getModel").invoke(options);
      return value instanceof String id ? id : "";
    } catch (ReflectiveOperationException ignored) {
      return "";
    }
  }

  private static String firstNonBlank(String... values) {
    if (values == null) {
      return "";
    }
    for (final var value : values) {
      if (value != null && !value.isBlank()) {
        return value;
      }
    }
    return "";
  }

  @Bean
  public EmbeddingService metisEmbeddingService(
      EmbeddingModel embeddingModel,
      EveliPropsMetisSearch searchProps) {
    log.info("Metis query embed concurrency: {}", searchProps.resolvedQueryEmbedConcurrency());
    return new EmbeddingService(
        embeddingModel,
        Duration.ofSeconds(2),
        searchProps.resolvedQueryEmbedConcurrency());
  }

  @Bean
  public StructuredChatService metisChatService(ChatClient.Builder chatClientBuilder) {
    return new StructuredChatService(chatClientBuilder.build());
  }

  @Bean
  public MetisClient metisClient(MetisConfig config, ObjectProvider<MetisSearchClient> search) {
    final var siteSearch = search.getIfAvailable();
    log.info("Metis enabled, provider: {}, chat model: {}, embedding model: {}, semantic site search: {}",
        config.getProvider(), config.getChatModelId(), config.getEmbeddingModelId(),
        siteSearch == null ? "disabled" : "enabled");
    return new MetisClientImpl(config, siteSearch);
  }

  @Bean
  public MetisApiController metisApiController(MetisClient metis) {
    return new MetisApiController(metis);
  }
}
