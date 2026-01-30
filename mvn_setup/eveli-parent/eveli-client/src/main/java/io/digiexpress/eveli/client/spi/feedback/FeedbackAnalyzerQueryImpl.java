package io.digiexpress.eveli.client.spi.feedback;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

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


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.digiexpress.eveli.client.api.FeedbackCategoriesReader;
import io.digiexpress.eveli.client.api.FeedbackClient.FeedbackAnalyzerQuery;
import io.digiexpress.eveli.client.api.FeedbackClient.FeedbackItem;
import io.digiexpress.eveli.client.api.FeedbackClient.SentimentAndSubcategory;
import io.digiexpress.eveli.client.api.FeedbackClient.SimilarFeedback;
import io.digiexpress.eveli.client.api.ImmutableFeedbackItem;
import io.digiexpress.eveli.client.api.ImmutableFeedbackSentimentAndSubcategoryCommand;
import io.digiexpress.eveli.client.api.ImmutableSimilarFeedbackCommand;
import io.digiexpress.eveli.client.config.EveliPropsFeedback;
import io.digiexpress.eveli.client.spi.feedback.AnalyzerApiCallException.HttpErrorDetail;
import io.digiexpress.eveli.client.spi.feedback.AnalyzerApiCallException.HttpValidationError;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class FeedbackAnalyzerQueryImpl implements FeedbackAnalyzerQuery {
  private final FeedbackClientImpl feedbackClient;
  private final EveliPropsFeedback properties;
  private final ObjectMapper mapper;
  private final FeedbackCategoriesReader feedbackCategoriesReader;

  @Override
  public Uni<SentimentAndSubcategory> getOneSentimentAndSubcategoryById(String id) {
    final var categories = feedbackCategoriesReader.getCategories();

    return feedbackClient.queryFeedbacks().findOneById(id)
        .onItem().transformToUni(feedback -> {
          if (feedback.isPresent()) {
            final var command = ImmutableFeedbackSentimentAndSubcategoryCommand.builder()
              .id(id)
              .language(feedback.get().getLocale())
              .text(feedback.get().getContent().getQuestion())
              .mainCategory(feedback.get().getContent().getMain())
              .categories(categories)
              .build();
            return Uni.createFrom().item(executeCall(command, "sentiment-and-subcategory", SentimentAndSubcategory.class));
          }

          // if feedback not found, try finding feedback questionnaire from task
          return feedbackClient.queryQuestionnaire().findOneFromTaskById(id)
          .onItem().transform(questionnaire -> {
            if (questionnaire.isEmpty()) {
              throw new FeedbackAnalyzerException("Feedback questionnaire not found for task id: " + id);
            }
            final var command = ImmutableFeedbackSentimentAndSubcategoryCommand.builder()
              .id(id)
              .language(questionnaire.get().getQuestionnaire().getMetadata().getLanguage())
              .mainCategory(questionnaire.get().getContent().getMain())
              .text(questionnaire.get().getContent().getQuestion())
              .categories(categories)
              .build();
            return executeCall(command, "find-sentiment-and-subcategory", SentimentAndSubcategory.class);              
          });
        
        });
  }

  @Override
  public Uni<Optional<SimilarFeedback>> findOneSimilarFeedbackById(String id) {
    
    return Uni.combine().all().unis(
        feedbackClient.queryFeedbacks().findAll().collect().asList(),
        feedbackClient.queryQuestionnaire().findOneFromTaskById(id)
      )
      .asTuple()
      .onItem().transform(tuple -> {

        final var questionnaire = tuple.getItem2();
        final var feedbacks = tuple.getItem1();
        
        if (feedbacks.isEmpty()) {
          return Optional.empty();
        }

        final List<FeedbackItem> items = new ArrayList<>(feedbacks.stream().map(fb -> ImmutableFeedbackItem.builder()
            .id(fb.getSourceId())
            .language(fb.getLocale())
            .text(fb.getContent().getQuestion())
            .build())
          .toList());

        // if given id is not feedback id, use task id to find feedback questionnaire and add it to items
        if(items.stream().noneMatch(i -> i.getId().equals(id))) {
          
          if (questionnaire.isEmpty()) {
            throw new FeedbackAnalyzerException("Feedback questionnaire not found for task id: " + id);
          }
          items.add(ImmutableFeedbackItem.builder()
            .id(id)
            .language(questionnaire.get().getQuestionnaire().getMetadata().getLanguage())
            .text(questionnaire.get().getContent().getQuestion())
            .build());
        }

        final var command = ImmutableSimilarFeedbackCommand.builder().id(id).entries(items).build();
        final var similarFeedback = executeCall(command, "find-similar", SimilarFeedback.class);
        return Optional.ofNullable(similarFeedback);
        
      });

  }

  private RestClient getRestClient() {
    // this factory is needed to pass request size to analyzer, by default it is not sent and
    // analyzer does not recognize body, resulting in error.
    // See https://github.com/spring-projects/spring-framework/issues/31854#issuecomment-1858817976
    var factory = new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
    return RestClient.builder()
      .requestFactory(factory).build();
  }


  private <R, B> R executeCall(B body, String path, Class<R> responseClass) {
    if (log.isDebugEnabled()) {
      try {
        ObjectWriter ow = new ObjectMapper().writer();
        String json = ow.writeValueAsString(body);
        log.debug("Analyzer call to path {}, body: {}", String.format("%s/%s", properties.getAnalyzer().getEndpointUrl(), path), json);
      }
      catch (Exception e) {
        log.warn("Body json formatting failed, message: {}", e.getMessage(), e);
      }
    }
    final RestClient client = getRestClient();
    return client.post().uri(String.format("%s/%s", properties.getAnalyzer().getEndpointUrl(), path))
      .contentType(MediaType.APPLICATION_JSON)
      .body(body)
      .retrieve()
      .onStatus(code -> code.is4xxClientError(), (request, response) -> {
        HttpValidationError restErrorResponse = null;
        try {
          restErrorResponse = mapper.readValue(response.getBody(), HttpValidationError.class);
        }
        catch (Exception e) {
          String bodyValue = mapper.readValue(response.getBody(), String.class);
          log.error("Error response parsing failed, body: {}, message: {}", bodyValue, e.getMessage(), e);
          restErrorResponse = new HttpValidationError();
          restErrorResponse.setDetail(List.of(HttpErrorDetail.builder()
            .msg("Error in parsing response:" + response.getStatusText())
            .type("Error")
            .build()));
        }
        log.warn("Analyzer call failed with error: {}", restErrorResponse);
        throw new AnalyzerApiCallException("AnalyzerCallFailed", restErrorResponse, response.getStatusCode());
      })
      .body(responseClass);
  }
}
