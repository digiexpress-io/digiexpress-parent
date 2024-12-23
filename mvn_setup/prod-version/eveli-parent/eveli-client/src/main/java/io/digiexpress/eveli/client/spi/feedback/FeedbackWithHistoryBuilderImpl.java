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

import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;

import io.digiexpress.eveli.client.api.FeedbackClient.CreateFeedbackCommand;
import io.digiexpress.eveli.client.api.FeedbackClient.DeleteReplyCommand;
import io.digiexpress.eveli.client.api.FeedbackClient.Feedback;
import io.digiexpress.eveli.client.api.FeedbackClient.FeedbackRating;
import io.digiexpress.eveli.client.api.FeedbackClient.ModifyOneFeedbackCommand;
import io.digiexpress.eveli.client.api.FeedbackClient.UpsertFeedbackRankingCommand;
import io.digiexpress.eveli.client.persistence.entities.FeedbackHistoryEntity;
import io.digiexpress.eveli.client.spi.feedback.FeedbackWithHistory.FeedbackWithHistoryBuilder;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FeedbackWithHistoryBuilderImpl implements FeedbackWithHistoryBuilder {
  private final JdbcTemplate jdbc;
  private final ObjectMapper objectMapper;
  
  private final List<FeedbackHistoryEntity> events = new ArrayList<>();
  private final ZonedDateTime now = ZonedDateTime.now();
  private final String commitId = Hashing
      .murmur3_128()
      .hashString(now.toString(), StandardCharsets.UTF_8)
      .toString();;
  
  @Override
  public FeedbackWithHistoryBuilder append(UpsertFeedbackRankingCommand command, FeedbackRating rating, String customerId) {
    final var event = new FeedbackHistoryEntity()
        .setId(null)// auto generated by db
        .setCommitId(commitId)
        .setCategoryId(rating.getCategoryId())
        .setCreatedBy(customerId)
        .setCreatedOnDate(now)
        .setJsonBody(toJson(command))
        .setJsonBodyType(UpsertFeedbackRankingCommand.class.getSimpleName())
        .setRatingId(rating.getId())
        .setReplyId(rating.getReplyId());
    events.add(event);
    return this;
  }

  @Override
  public FeedbackWithHistoryBuilder append(CreateFeedbackCommand command, Feedback feedback, String userId) {
    final var event = new FeedbackHistoryEntity()
        .setId(null)// auto generated by db
        .setCommitId(commitId)
        .setCategoryId(feedback.getCategoryId())
        .setCreatedBy(userId)
        .setCreatedOnDate(now)
        .setJsonBody(toJson(command))
        .setJsonBodyType(CreateFeedbackCommand.class.getSimpleName())
        .setRatingId(null)
        .setReplyId(feedback.getId());
    events.add(event);
    return this;
  }

  @Override
  public FeedbackWithHistoryBuilder append(DeleteReplyCommand command, List<Feedback> allFeedback, String userId) {

    for(final var feedback : allFeedback) {
      final var event = new FeedbackHistoryEntity()
          .setId(null)// auto generated by db
          .setCommitId(commitId)
          .setCategoryId(feedback.getCategoryId())
          .setCreatedBy(userId)
          .setCreatedOnDate(now)
          .setJsonBody(toJson(command))
          .setJsonBodyType(DeleteReplyCommand.class.getSimpleName())
          .setRatingId(null)
          .setReplyId(feedback.getId());
      events.add(event);
    }
    return this;
  }


  @Override
  public FeedbackWithHistoryBuilder append(ModifyOneFeedbackCommand command, Feedback feedback, String userId) {
    final var event = new FeedbackHistoryEntity()
        .setId(null)// auto generated by db
        .setCommitId(commitId)
        .setCategoryId(feedback.getCategoryId())
        .setCreatedBy(userId)
        .setCreatedOnDate(now)
        .setJsonBody(toJson(command))
        .setJsonBodyType(CreateFeedbackCommand.class.getSimpleName())
        .setRatingId(null)
        .setReplyId(feedback.getId());
    events.add(event);
    return this;
  }
  
  @Override
  public void close() {
    if(events.isEmpty()) {
      return;
    }
    
    jdbc.batchUpdate(
"""
INSERT INTO feedback_history
( 
  commit_id,
  rating_id,
  category_id,
  reply_id,
  
  json_body_type,
  
  created_on_date,
  created_by,
  
  json_body
)
VALUES
(?,?,?,?,?,?,?,?::json)

""", new BatchPreparedStatementSetter() {
      @Override
      public void setValues(PreparedStatement ps, int i) throws SQLException {
        final var history = events.get(i);
        ps.setString(1, history.getCommitId());
        ps.setString(2, history.getRatingId());
        ps.setString(3, history.getCategoryId());
        ps.setString(4, history.getReplyId());
        ps.setString(5, history.getJsonBodyType());
        
        ps.setObject(6, java.sql.Timestamp.from(history.getCreatedOnDate().toInstant()));
        ps.setString(7, history.getCreatedBy());
        ps.setObject(8, history.getJsonBody(), java.sql.Types.OTHER);
      }
      @Override
      public int getBatchSize() {
        return events.size();
      }
    });
    
    events.clear();
  }
  
  private String toJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch(Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

}
