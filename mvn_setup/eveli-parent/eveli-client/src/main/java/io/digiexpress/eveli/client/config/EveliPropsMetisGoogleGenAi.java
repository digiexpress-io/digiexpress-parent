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

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import lombok.Setter;

/**
 * Google Gemini on Vertex AI. Used only when spring.ai.model.chat or spring.ai.model.embedding
 * is {@value #PROVIDER}; the two are selected independently.
 */
@Data @Setter
@ConfigurationProperties(prefix = "eveli.metis.google-genai")
public class EveliPropsMetisGoogleGenAi {

  /** Spring AI's own name for the provider, the value of spring.ai.model.chat / spring.ai.model.embedding. */
  public static final String PROVIDER = "google-genai";
  public static final String DEFAULT_CHAT_MODEL = "gemini-3.1-flash-lite";
  public static final String DEFAULT_EMBEDDING_MODEL = "gemini-embedding-2";

  /** GCP project that is billed for Vertex AI. Required when the provider is selected. */
  private String projectId;
  /**
   * Vertex AI location, one for both models. eu is the EU multi-region endpoint, which keeps
   * processing in the EU and serves gemini-3.1-flash-lite and gemini-embedding-2. Required when
   * the provider is selected.
   */
  private String location;

  private Chat chat = new Chat();
  private Embedding embedding = new Embedding();

  @Data
  public static class Chat {
    /** Metadata generation and feedback classification. */
    private String model = DEFAULT_CHAT_MODEL;
    private Double temperature = 0.2;
    /** Thinking tokens are billed as output; metadata generation does not need them. MINIMAL, LOW, MEDIUM or HIGH. */
    private String thinkingLevel = "MINIMAL";
  }

  @Data
  public static class Embedding {
    private String model = DEFAULT_EMBEDDING_MODEL;
    /** Must equal eveli.metis.search.indexing.embedding-dimension. A reindex fails fast on a mismatch. */
    private Integer dimensions = 1024;
  }
}
