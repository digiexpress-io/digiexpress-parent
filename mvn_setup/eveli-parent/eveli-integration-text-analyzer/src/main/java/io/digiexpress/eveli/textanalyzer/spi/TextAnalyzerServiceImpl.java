package io.digiexpress.eveli.textanalyzer.spi;

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

import java.util.Objects;
import java.util.Optional;

import io.digiexpress.eveli.textanalyzer.adapter.api.FeedbackAnalyzerClient;
import io.digiexpress.eveli.textanalyzer.adapter.api.SentimentSubcategoryRequest;
import io.digiexpress.eveli.textanalyzer.adapter.api.SentimentSubcategoryResponse;
import io.digiexpress.eveli.textanalyzer.adapter.api.SimilaritySearchRequest;
import io.digiexpress.eveli.textanalyzer.adapter.api.SimilaritySearchRequest.Entry;
import io.digiexpress.eveli.textanalyzer.adapter.api.SimilaritySearchResponse;
import io.digiexpress.eveli.textanalyzer.api.ImmutableTextSentimentAndSubcategory;
import io.digiexpress.eveli.textanalyzer.api.ImmutableTextSimilarityItem;
import io.digiexpress.eveli.textanalyzer.api.ImmutableTextSimilarityItems;
import io.digiexpress.eveli.textanalyzer.api.ImmutableTextSimilarityScoreItem;
import io.digiexpress.eveli.textanalyzer.api.TextAnalyzerService;
import io.digiexpress.eveli.textanalyzer.api.TextCategoryItem;
import io.digiexpress.eveli.textanalyzer.api.TextItems;
import io.digiexpress.eveli.textanalyzer.api.TextSentimentAndSubcategory;
import io.digiexpress.eveli.textanalyzer.api.TextSimilarityItems;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TextAnalyzerServiceImpl implements TextAnalyzerService{

  private final FeedbackAnalyzerClient client;

  @Override
  public TextSentimentAndSubcategory findSentimentAndSubcategory(TextCategoryItem request) {
    SentimentSubcategoryRequest clientRequest = new SentimentSubcategoryRequest();
    clientRequest.setId(request.getId());
    clientRequest.setLanguage(Objects.requireNonNullElse(request.getLanguage(), "fi"));
    clientRequest.setMainCategory(request.getMainCategory());
    clientRequest.setText(request.getText());
    SentimentSubcategoryResponse response = client.findSentimentAndSubcategory(clientRequest);
    TextSentimentAndSubcategory result = ImmutableTextSentimentAndSubcategory.builder()
        .id(response.getSentiment().getId())
        .sentiment(response.getSentiment().getSentiment())
        .sentimentConfidence(response.getSentiment().getConfidence())
        .subcategory(response.getSubcategory().getSubcategory())
        .subcategoryConfidence(response.getSubcategory().getConfidence())
        .addAllSubcategoryMatches(response.getSubcategory().getMatches())
        .build();
    return result;
  }

  @Override
  public TextSimilarityItems findSimilar(TextItems request) {
    SimilaritySearchRequest clientRequest = new SimilaritySearchRequest();
    clientRequest.setId(request.getId());
    clientRequest.setEntries(request.getItems().stream().map(item -> {
      Entry entry = new Entry();
      entry.setId(item.getId());
      entry.setLanguage(Optional.ofNullable(item.getLanguage()).or(()->Optional.of("fi")));
      entry.setText(item.getText());
      return entry;
    }).toList());
    SimilaritySearchResponse clientResponse = client.findSimilar(clientRequest);
    TextSimilarityItems response = ImmutableTextSimilarityItems.builder()
        .items(clientResponse.getEntries().stream().map(entry-> {
          return ImmutableTextSimilarityItem.builder()
              .id(entry.getId())
              .language(entry.getLanguage())
              .text(entry.getText())
              .similarityScoreItems(entry.getSimilarities().stream().map(similarity-> {
                return ImmutableTextSimilarityScoreItem.builder()
                    .id(similarity.getId())
                    .language(similarity.getLanguage())
                    .similarityScore(similarity.getSimilarityScore())
                    .text(similarity.getText())
                    .build();
              }).toList())
              .build();
        }).toList())
        .build();
    return response;
  }

}
