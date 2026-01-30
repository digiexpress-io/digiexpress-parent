package io.digiexpress.eveli.client.spi.feedback;

/*-
 * #%L
 * eveli-client
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


import io.digiexpress.eveli.client.api.FeedbackClient.FeedbackAnalyzerQuery;
import io.digiexpress.eveli.client.api.FeedbackClient.SentimentAndSubcategory;
import io.digiexpress.eveli.client.api.FeedbackClient.SentimentPolarity;
import io.digiexpress.eveli.client.api.FeedbackClient.SimilarFeedback;
import io.digiexpress.eveli.client.api.ImmutableSentiment;
import io.digiexpress.eveli.client.api.ImmutableSentimentAndSubcategory;
import io.digiexpress.eveli.client.api.ImmutableSimilarFeedback;
import io.digiexpress.eveli.client.api.ImmutableSubcategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class FeedbackAnalyzerQueryDummyImpl implements FeedbackAnalyzerQuery {

  @Override
  public SentimentAndSubcategory getOneSentimentAndSubcategoryById(String id) {
    final var sentiment = ImmutableSentiment.builder()
      .id("1")
      .sentiment(SentimentPolarity.unknown)
      .confidence(0.0f)
      .timestamp(OffsetDateTime.now())
      .modelId("dummy-model")
      .modelVersion("1.0")
      .build();

    final var subcategory = ImmutableSubcategory.builder()
      .id("1")
      .subcategory("unknown")
      .confidence(0.0f)
      .timestamp(OffsetDateTime.now())
      .modelId("dummy-model")
      .modelVersion("1.0")
      .build();

    return ImmutableSentimentAndSubcategory.builder()
      .sentiment(sentiment)
      .subcategory(subcategory)
      .build();
  }

  @Override
  public Optional<SimilarFeedback> findOneSimilarFeedbackById(String id) {
    return Optional.of(ImmutableSimilarFeedback.builder()
      .id("1")
      .modelId("dummy-model")
      .modelVersion("1.0")
      .timestamp(OffsetDateTime.now())
      .build());
  }
}
