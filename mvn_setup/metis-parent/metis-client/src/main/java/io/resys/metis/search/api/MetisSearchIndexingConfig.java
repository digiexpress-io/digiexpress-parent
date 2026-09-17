package io.resys.metis.search.api;

/*-
 * #%L
 * metis-client
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

import org.immutables.value.Value;

@Value.Immutable
public interface MetisSearchIndexingConfig {

  /** Expected width of metis_search_index.embedding. bge-m3 is 1024. A mismatch fails the reindex job. */
  @Value.Default
  default int getEmbeddingDimension() {
    return 1024;
  }

  /** Documents processed in parallel. 1 is optimal for CPU-only Ollama. */
  @Value.Default
  default int getConcurrency() {
    return 1;
  }

  /** Upper bound for metadata plus embedding of one document, from when that document starts. */
  @Value.Default
  default int getDocumentTimeoutSeconds() {
    return 600;
  }

  /** How long a running job may report no progress before another instance may reclaim it. */
  @Value.Default
  default int getAbandonedAfterSeconds() {
    return 3600;
  }

  /** Truncation for the text handed to the metadata model. */
  @Value.Default
  default int getMaxLlmContextChars() {
    return 600;
  }

  /** Truncation for the indexed page body. */
  @Value.Default
  default int getMaxPageChars() {
    return 800;
  }

  /** A topic linking more than this many workflows is treated as a generic listing page. */
  @Value.Default
  default int getGenericTopicThreshold() {
    return 4;
  }

  /** Folded into the content hash. Bump when the prompt changes so a normal reindex reprocesses every document. */
  @Value.Default
  default String getMetadataPromptVersion() {
    return "v1";
  }
}
