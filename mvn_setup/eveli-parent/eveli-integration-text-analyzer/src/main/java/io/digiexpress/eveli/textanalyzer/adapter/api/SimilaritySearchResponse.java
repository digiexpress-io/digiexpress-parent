package io.digiexpress.eveli.textanalyzer.adapter.api;

/*-
 * #%L
 * eveli-integration-text-analyzer
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import java.time.OffsetDateTime;
import java.util.List;

import lombok.Data;

@Data
public class SimilaritySearchResponse {
  OffsetDateTime timestamp;
  String modelVersion;
  String modelId;
  List<ProcessedEntry> entries;
  @Data
  public static class ProcessedEntry {
    String id;
    String language;
    String text;
    List<SimilarityResult> similarities;
  }
  @Data
  public static class SimilarityResult {
    String id;
    float similarityScore;
    String text;
    String language;
  }
}
