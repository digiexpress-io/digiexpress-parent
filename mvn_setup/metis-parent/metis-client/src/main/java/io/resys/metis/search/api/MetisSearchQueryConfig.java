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
public interface MetisSearchQueryConfig {

  /** Weight of the semantic ranking in reciprocal rank fusion. */
  @Value.Default
  default double getVectorWeight() {
    return 0.8;
  }

  /** Weight of the keyword ranking in reciprocal rank fusion. */
  @Value.Default
  default double getFtsWeight() {
    return 0.2;
  }

  /** Reciprocal rank fusion constant. Higher values flatten the influence of the top ranks. */
  @Value.Default
  default int getRrfK() {
    return 60;
  }

  /** Drop results scoring below this fraction of the best score for the same query. */
  @Value.Default
  default double getScoreDropOffRatio() {
    return 0.90;
  }

  /** Caller cannot ask for more than this. */
  @Value.Default
  default int getMaxResults() {
    return 8;
  }

  /** When even the best vector match is below this, the query has no semantic answer. */
  @Value.Default
  default double getMinVectorScore() {
    return 0.30;
  }

  /** Floor when primary full-text search is empty. Gibberish typically scores 0.30–0.45. */
  @Value.Default
  default double getMinVectorScoreWithoutKeyword() {
    return 0.45;
  }

  /** Below this number of keyword hits the trigram similarity fallback is used. */
  @Value.Default
  default int getMinResultsBeforeFallback() {
    return 1;
  }

  /** Minimum trigram similarity for that fallback. */
  @Value.Default
  default double getTrgmThreshold() {
    return 0.2;
  }

  /** Default number of results when the caller does not pass a limit. */
  @Value.Default
  default int getDefaultLimit() {
    return 8;
  }

  /** Public queries longer than this are truncated. */
  @Value.Default
  default int getMaxQueryChars() {
    return 400;
  }

  /** Upper bound in seconds for answering one search. Query embedding gets two seconds less. */
  @Value.Default
  default int getTimeoutSeconds() {
    return 5;
  }
}
