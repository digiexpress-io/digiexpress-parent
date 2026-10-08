package io.digiexpress.eveli.client.test.metis;

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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.google.genai.common.GoogleGenAiThinkingLevel;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingModel;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.google.genai.Client;

import io.digiexpress.eveli.client.config.EveliAutoConfigMetis;
import io.digiexpress.eveli.client.config.EveliAutoConfigMetisGoogleGenAi;
import io.digiexpress.eveli.client.test.metis.search.StubEmbeddingModel;
import io.resys.metis.api.MetisConfig;

/**
 * The Google modules are on the test classpath, as they are in eveli-app-gcloud, so these
 * cases also prove that deployments without Vertex are unaffected by them.
 */
public class EveliAutoConfigMetisGoogleGenAiTest {

  private final ApplicationContextRunner runner = new ApplicationContextRunner()
      .withUserConfiguration(EveliAutoConfigMetisGoogleGenAi.class)
      .withPropertyValues("eveli.metis.enabled=true");

  @Test
  void noGoogleBeansWhenNeitherKeySelectsGoogle() {
    runner.withPropertyValues("spring.ai.model.chat=none", "spring.ai.model.embedding=none")
        .run(context -> {
          assertThat(context).hasNotFailed();
          assertThat(context).doesNotHaveBean(Client.class);
          assertThat(context).doesNotHaveBean(ChatModel.class);
          assertThat(context).doesNotHaveBean(EmbeddingModel.class);
        });
    runner.withPropertyValues("spring.ai.model.chat=ollama", "spring.ai.model.embedding=ollama")
        .run(context -> {
          assertThat(context).hasNotFailed();
          assertThat(context).doesNotHaveBean(Client.class);
          assertThat(context).doesNotHaveBean(ChatModel.class);
          assertThat(context).doesNotHaveBean(EmbeddingModel.class);
        });
  }

  @Test
  void noGoogleBeansWhenMetisIsOff() {
    new ApplicationContextRunner()
        .withUserConfiguration(EveliAutoConfigMetisGoogleGenAi.class)
        .withPropertyValues("eveli.metis.enabled=false",
            "spring.ai.model.chat=google-genai", "spring.ai.model.embedding=google-genai")
        .run(context -> {
          assertThat(context).hasNotFailed();
          assertThat(context).doesNotHaveBean(Client.class);
          assertThat(context).doesNotHaveBean(ChatModel.class);
          assertThat(context).doesNotHaveBean(EmbeddingModel.class);
        });
  }

  @Test
  void bothKeysSelectGoogleAndTheOptionsCarryTheDefaults() {
    final var client = apiKeyClient();
    runner.withBean(Client.class, () -> client)
        .withPropertyValues("spring.ai.model.chat=google-genai", "spring.ai.model.embedding=google-genai")
        .run(context -> {
          assertThat(context).hasNotFailed();
          assertThat(context).getBean(Client.class).isSameAs(client);

          assertThat(context).hasSingleBean(ChatModel.class);
          assertThat(context.getBean(ChatModel.class)).isInstanceOf(GoogleGenAiChatModel.class);
          final var chatOptions = (GoogleGenAiChatOptions) context.getBean(ChatModel.class).getDefaultOptions();
          assertThat(chatOptions.getModel()).isEqualTo("gemini-3.1-flash-lite");
          assertThat(chatOptions.getTemperature()).isEqualTo(0.2);
          assertThat(chatOptions.getThinkingLevel()).isEqualTo(GoogleGenAiThinkingLevel.MINIMAL);

          assertThat(context).hasSingleBean(EmbeddingModel.class);
          assertThat(context.getBean(EmbeddingModel.class)).isInstanceOf(GoogleGenAiTextEmbeddingModel.class);
          final var embeddingOptions = ((GoogleGenAiTextEmbeddingModel) context.getBean(EmbeddingModel.class)).defaultOptions;
          assertThat(embeddingOptions.getModel()).isEqualTo("gemini-embedding-2");
          assertThat(embeddingOptions.getDimensions()).isEqualTo(1024);
        });
  }

  @Test
  void propertiesOverrideTheModelOptions() {
    runner.withBean(Client.class, EveliAutoConfigMetisGoogleGenAiTest::apiKeyClient)
        .withPropertyValues("spring.ai.model.chat=google-genai", "spring.ai.model.embedding=google-genai",
            "eveli.metis.google-genai.chat.model=gemini-3.1-flash",
            "eveli.metis.google-genai.chat.thinking-level=low",
            "eveli.metis.google-genai.embedding.dimensions=768")
        .run(context -> {
          assertThat(context).hasNotFailed();
          final var chatOptions = (GoogleGenAiChatOptions) context.getBean(ChatModel.class).getDefaultOptions();
          assertThat(chatOptions.getModel()).isEqualTo("gemini-3.1-flash");
          assertThat(chatOptions.getThinkingLevel()).isEqualTo(GoogleGenAiThinkingLevel.LOW);
          assertThat(((GoogleGenAiTextEmbeddingModel) context.getBean(EmbeddingModel.class)).defaultOptions
              .getDimensions()).isEqualTo(768);
        });
  }

  @Test
  void metisReportsTheGoogleProviderAndModelIds() {
    runner.withUserConfiguration(EveliAutoConfigMetis.class)
        .withBean(Client.class, EveliAutoConfigMetisGoogleGenAiTest::apiKeyClient)
        .withBean(ChatClient.Builder.class, () -> Mockito.mock(ChatClient.Builder.class, Mockito.RETURNS_DEEP_STUBS))
        .withPropertyValues("spring.ai.model.chat=google-genai", "spring.ai.model.embedding=google-genai",
            // Left over from an Ollama setup, as in application-dev.yml; must not name the models.
            "spring.ai.ollama.chat.options.model=llama3.2",
            "spring.ai.ollama.embedding.options.model=bge-m3")
        .run(context -> {
          assertThat(context).hasNotFailed();
          final var config = context.getBean(MetisConfig.class);
          assertThat(config.getProvider()).isEqualTo("google-genai");
          assertThat(config.getChatModelId()).isEqualTo("gemini-3.1-flash-lite");
          assertThat(config.getEmbeddingModelId()).isEqualTo("gemini-embedding-2");
        });
  }

  @Test
  void selectingGoogleWithoutAProjectFailsWithAMetisMessage() {
    runner.withPropertyValues("spring.ai.model.chat=google-genai", "spring.ai.model.embedding=google-genai",
            "eveli.metis.google-genai.location=europe-north1")
        .run(context -> {
          assertThat(context).hasFailed();
          assertThat(context.getStartupFailure())
              .rootCause()
              .isInstanceOf(IllegalStateException.class)
              .hasMessageContaining("eveli.metis.google-genai.project-id");
        });
  }

  @Test
  void anUnknownThinkingLevelFailsWithTheAllowedValues() {
    runner.withBean(Client.class, EveliAutoConfigMetisGoogleGenAiTest::apiKeyClient)
        .withPropertyValues("spring.ai.model.chat=google-genai", "eveli.metis.google-genai.chat.thinking-level=turbo")
        .run(context -> {
          assertThat(context).hasFailed();
          assertThat(context.getStartupFailure())
              .hasStackTraceContaining("eveli.metis.google-genai.chat.thinking-level is 'turbo'")
              .hasStackTraceContaining("MINIMAL");
        });
  }

  /** Chat on Vertex, embeddings from Ollama. The stub stands in for Ollama's EmbeddingModel. */
  @Test
  void chatOnGoogleWithEmbeddingsFromAnotherProvider() {
    final var ollamaEmbeddings = new StubEmbeddingModel();
    runner.withBean(Client.class, EveliAutoConfigMetisGoogleGenAiTest::apiKeyClient)
        .withBean(EmbeddingModel.class, () -> ollamaEmbeddings)
        .withPropertyValues("spring.ai.model.chat=google-genai", "spring.ai.model.embedding=ollama")
        .run(context -> {
          assertThat(context).hasNotFailed();
          assertThat(context.getBean(ChatModel.class)).isInstanceOf(GoogleGenAiChatModel.class);
          assertThat(context).hasSingleBean(EmbeddingModel.class);
          assertThat(context.getBean(EmbeddingModel.class)).isSameAs(ollamaEmbeddings);
        });
  }

  /** API-key mode needs no Application Default Credentials and makes no call at construction. */
  private static Client apiKeyClient() {
    return Client.builder().apiKey("test-key").build();
  }
}
