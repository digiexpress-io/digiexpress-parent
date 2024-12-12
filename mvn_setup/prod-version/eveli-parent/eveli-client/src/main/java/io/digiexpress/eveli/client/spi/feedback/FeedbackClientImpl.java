package io.digiexpress.eveli.client.spi.feedback;

import java.util.List;

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


import org.springframework.jdbc.core.JdbcTemplate;

import io.digiexpress.eveli.client.api.FeedbackClient;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.config.EveliPropsFeedback;
import io.digiexpress.eveli.dialob.api.DialobClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FeedbackClientImpl implements FeedbackClient {
  private final TaskClient taskClient;
  private final ProcessClient processClient;
  private final DialobClient dialobClient;
  private final JdbcTemplate jdbc;
  private final FeedbackWithHistory feedbackWithHistory;
  private final EveliPropsFeedback configProps;
  
  @Override
  public Feedback createOneFeedback(CreateFeedbackCommand command, String userId) {
    return new CreateOneFeedbackReplyImpl(jdbc, feedbackWithHistory, userId).apply(command);
  }

  @Override
  public FeedbackRating modifyOneFeedbackRank(UpsertFeedbackRankingCommand command, String userId) {
    return new FeedbackRatingBuilderImpl(jdbc, feedbackWithHistory, userId).execute(command);
  }

  @Override
  public FeedbackQuery queryFeedbacks() {
    return new FeedbackQueryImpl(jdbc);
  }
  
  @Override
  public FeedbackQuestionnaireQuery queryQuestionnaire() {
    return new FeedbackQuestionnaireQueryImpl(taskClient, dialobClient, processClient, configProps);
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
  public List<Feedback> deleteAll(DeleteReplyCommand command, String userId) {
    return new FeedbackRatingDeleteBuilderImpl(jdbc, feedbackWithHistory, userId).execute(command);
  }

  @Override
  public Feedback modifyOneFeedback(ModifyFeedbackCommand commands, String userId) {
    return new ModifyFeedbackReplyImpl(jdbc, feedbackWithHistory, userId).apply(commands);
  }

}
