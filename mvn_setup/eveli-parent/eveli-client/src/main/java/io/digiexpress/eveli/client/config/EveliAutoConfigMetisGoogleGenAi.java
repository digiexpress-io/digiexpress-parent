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

import java.util.Arrays;
import java.util.Locale;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.google.genai.GoogleGenAiEmbeddingConnectionDetails;
import org.springframework.ai.google.genai.common.GoogleGenAiThinkingLevel;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingModel;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingOptions;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

import com.google.genai.Client;

import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * Gemini on Vertex AI for Metis, without the Spring AI Google starters. Their embedding
 * connection bean is unconditional and they use a second selector key, so the beans are
 * created here instead, selected by Spring AI's own spring.ai.model.chat and
 * spring.ai.model.embedding keys. Ollama's auto-configuration turns itself off for any value
 * other than ollama, so chat and embedding can be served by different providers.
 * <p>
 * Authentication uses Application Default Credentials; on GKE that is Workload Identity.
 * GOOGLE_APPLICATION_CREDENTIALS pointing at a service-account key works the same way,
 * locally or anywhere else.
 */
@Slf4j
@Configuration
@ConditionalOnBooleanProperty(value = "eveli.metis.enabled", havingValue = true, matchIfMissing = false)
@ConditionalOnClass(name = {
    "com.google.genai.Client",
    "org.springframework.ai.google.genai.GoogleGenAiChatModel" })
@EnableConfigurationProperties(value = { EveliPropsMetisGoogleGenAi.class })
public class EveliAutoConfigMetisGoogleGenAi {

  private static final String CHAT_KEY = "spring.ai.model.chat";
  private static final String EMBEDDING_KEY = "spring.ai.model.embedding";

  /** Tests supply their own Client in API-key mode so they need no Application Default Credentials. */
  @Bean
  @ConditionalOnMissingBean
  @Conditional(GoogleGenAiSelected.class)
  public Client metisGoogleGenAiClient(EveliPropsMetisGoogleGenAi props) {
    if (isBlank(props.getProjectId()) || isBlank(props.getLocation())) {
      throw new IllegalStateException(
          "spring.ai.model.chat or spring.ai.model.embedding is " + EveliPropsMetisGoogleGenAi.PROVIDER
              + " but eveli.metis.google-genai.project-id and eveli.metis.google-genai.location are not both set.");
    }
    log.info("Metis Vertex AI client, project: {}, location: {}", props.getProjectId(), props.getLocation());
    return Client.builder()
        .project(props.getProjectId())
        .location(props.getLocation())
        .vertexAI(true)
        .build();
  }

  @Bean
  @ConditionalOnProperty(name = CHAT_KEY, havingValue = EveliPropsMetisGoogleGenAi.PROVIDER)
  public ChatModel metisGoogleGenAiChatModel(
      Client client,
      EveliPropsMetisGoogleGenAi props,
      ObjectProvider<RetryTemplate> retryTemplate,
      ObjectProvider<ObservationRegistry> observationRegistry) {
    final var chat = props.getChat();
    final var builder = GoogleGenAiChatModel.builder()
        .genAiClient(client)
        .defaultOptions(GoogleGenAiChatOptions.builder()
            .model(chat.getModel())
            .temperature(chat.getTemperature())
            .thinkingLevel(thinkingLevel(chat.getThinkingLevel()))
            .build());
    retryTemplate.ifAvailable(builder::retryTemplate);
    observationRegistry.ifAvailable(builder::observationRegistry);
    return builder.build();
  }

  @Bean
  @ConditionalOnProperty(name = EMBEDDING_KEY, havingValue = EveliPropsMetisGoogleGenAi.PROVIDER)
  @ConditionalOnClass(name = "org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingModel")
  public EmbeddingModel metisGoogleGenAiEmbeddingModel(
      Client client,
      EveliPropsMetisGoogleGenAi props,
      ObjectProvider<RetryTemplate> retryTemplate,
      ObjectProvider<ObservationRegistry> observationRegistry) {
    final var embedding = props.getEmbedding();
    return new GoogleGenAiTextEmbeddingModel(
        GoogleGenAiEmbeddingConnectionDetails.builder().genAiClient(client).build(),
        GoogleGenAiTextEmbeddingOptions.builder()
            .model(embedding.getModel())
            .dimensions(embedding.getDimensions())
            .build(),
        retryTemplate.getIfAvailable(() -> RetryUtils.DEFAULT_RETRY_TEMPLATE),
        observationRegistry.getIfAvailable(() -> ObservationRegistry.NOOP));
  }

  private static GoogleGenAiThinkingLevel thinkingLevel(String value) {
    if (isBlank(value)) {
      return null;
    }
    try {
      return GoogleGenAiThinkingLevel.valueOf(value.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException("eveli.metis.google-genai.chat.thinking-level is '" + value
          + "', expected one of: " + Arrays.toString(GoogleGenAiThinkingLevel.values()), e);
    }
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  static class GoogleGenAiSelected extends AnyNestedCondition {
    GoogleGenAiSelected() {
      super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnProperty(name = CHAT_KEY, havingValue = EveliPropsMetisGoogleGenAi.PROVIDER)
    static class Chat {
    }

    @ConditionalOnProperty(name = EMBEDDING_KEY, havingValue = EveliPropsMetisGoogleGenAi.PROVIDER)
    static class Embedding {
    }
  }
}
