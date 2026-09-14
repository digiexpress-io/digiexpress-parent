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

  @Value.Default
  default double getVectorWeight() {
    return 0.8;
  }

  @Value.Default
  default double getFtsWeight() {
    return 0.2;
  }

  @Value.Default
  default int getRrfK() {
    return 60;
  }

  @Value.Default
  default double getScoreDropOffRatio() {
    return 0.90;
  }

  @Value.Default
  default int getMaxResults() {
    return 8;
  }

  @Value.Default
  default double getMinVectorScore() {
    return 0.30;
  }

  @Value.Default
  default double getMinVectorScoreWithoutKeyword() {
    return 0.45;
  }

  @Value.Default
  default int getMinResultsBeforeFallback() {
    return 1;
  }

  @Value.Default
  default double getTrgmThreshold() {
    return 0.2;
  }

  @Value.Default
  default int getDefaultLimit() {
    return 8;
  }

  @Value.Default
  default int getMaxQueryChars() {
    return 400;
  }

  @Value.Default
  default int getTimeoutSeconds() {
    return 5;
  }
}
