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

import io.digiexpress.eveli.textanalyzer.api.Sentiment;
import lombok.Data;

@Data
public class SentimentResponse {

  @Data
  public static class SentenceSentiment {
    String text;
    Sentiment sentiment;
    Object scores;
  }
  String id;
  Sentiment sentiment;
  float confidence;
  List<SentenceSentiment> sentences;
  OffsetDateTime timestamp;
  String modelVersion;
  String modelId;
}
