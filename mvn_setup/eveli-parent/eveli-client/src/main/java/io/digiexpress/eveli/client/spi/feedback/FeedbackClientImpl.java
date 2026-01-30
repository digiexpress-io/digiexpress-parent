package io.digiexpress.eveli.client.spi.feedback;

import org.springframework.jdbc.core.JdbcTemplate;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import io.digiexpress.eveli.client.api.FeedbackCategoriesReader;
import io.digiexpress.eveli.client.api.FeedbackClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.config.EveliPropsFeedback;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FeedbackClientImpl implements FeedbackClient {
  private final TaskClient taskClient;
  private final DialobClient dialobClient;
  private final JdbcTemplate jdbc;
  private final FeedbackWithHistory feedbackWithHistory;
  private final EveliPropsFeedback configProps;
  private final ObjectMapper objectMapper;
  private final FeedbackCategoriesReader feedbackCategoriesReader;
  
  @Override
  public Uni<Feedback> createOneFeedback(CreateFeedbackCommand command, String userId) {
    return new CreateOneFeedbackReplyImpl(jdbc, feedbackWithHistory, userId).apply(command);
  }

  @Override
  public Uni<FeedbackRating> modifyOneFeedbackRank(UpsertFeedbackRankingCommand command, String userId) {
    return new FeedbackRatingBuilderImpl(jdbc, feedbackWithHistory, userId).execute(command);
  }

  @Override
  public Uni<Feedback> modifyOneFeedback(ModifyOneFeedbackCommand commands, String userId) {
    return new ModifyFeedbackReplyImpl(jdbc, feedbackWithHistory, userId).apply(commands);
  }

  @Override
  public FeedbackQuery queryFeedbacks() {
    return new FeedbackQueryImpl(jdbc, feedbackWithHistory);
  }
  
  @Override
  public FeedbackQuestionnaireQuery queryQuestionnaire() {
    return new FeedbackQuestionnaireQueryImpl(taskClient, dialobClient, configProps);
  }

  @Override
  public FeedbackTemplateQuery queryTemplate() {
    return new FeedbackTemplateQueryImpl(queryQuestionnaire());
  }
  
  @Override
  public FeedbackHistoryQuery queryHistory() {
    return new FeedbackHistoryQueryImpl(jdbc);
  }
  
  @Override
  public CustomerFeedbackQuery queryCustomerFeedbacks() {
    return new CustomerFeedbackQueryImpl(jdbc, feedbackWithHistory);
  }

  @Override
  public FeedbackRatingQuery queryFeedbackRatings() {
    return new FeedbackRatingQueryImpl(jdbc);
  }

  @Override
  public FeedbackAnalyzerQuery queryFeedbackAnalyzer() {
    final var analyzerQueryImpl = new FeedbackAnalyzerQueryImpl(this, configProps, objectMapper, feedbackCategoriesReader);
    final var analyzerQueryDummyImpl = new FeedbackAnalyzerQueryDummyImpl();
    return configProps.getAnalyzer().getEnabled() ? analyzerQueryImpl : analyzerQueryDummyImpl;
  }

}
