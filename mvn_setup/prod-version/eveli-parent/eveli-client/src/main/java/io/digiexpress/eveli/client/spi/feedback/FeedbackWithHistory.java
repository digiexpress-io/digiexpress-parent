package io.digiexpress.eveli.client.spi.feedback;

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

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.eveli.client.api.FeedbackClient.CreateFeedbackCommand;
import io.digiexpress.eveli.client.api.FeedbackClient.DeleteReplyCommand;
import io.digiexpress.eveli.client.api.FeedbackClient.Feedback;
import io.digiexpress.eveli.client.api.FeedbackClient.FeedbackRating;
import io.digiexpress.eveli.client.api.FeedbackClient.ModifyFeedbackCommand;
import io.digiexpress.eveli.client.api.FeedbackClient.UpsertFeedbackRankingCommand;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FeedbackWithHistory {
  private final TransactionTemplate tx;
  private final JdbcTemplate jdbc;
  private final ObjectMapper om;
  
  public <T> T withHistory(ExecuteWithHistory<T> executeWithHistory) {
    final var builder = new FeedbackWithHistoryBuilderImpl(jdbc, om);
    return tx.execute((TransactionStatus status) -> {
      final var result = executeWithHistory.execute(builder);
      builder.close();
      return result;
    });
    
  }
  
  @FunctionalInterface
  public interface ExecuteWithHistory<T> {
    T execute(FeedbackWithHistoryBuilder builder);
  }
  
  public interface FeedbackWithHistoryBuilder {
    FeedbackWithHistoryBuilder append(UpsertFeedbackRankingCommand command, FeedbackRating rating, String userId);
    FeedbackWithHistoryBuilder append(CreateFeedbackCommand command, Feedback feedback, String userId);
    FeedbackWithHistoryBuilder append(ModifyFeedbackCommand command, Feedback feedback, String userId);
    FeedbackWithHistoryBuilder append(DeleteReplyCommand command, List<Feedback> feedback, String userId);
    void close();
  }
}
