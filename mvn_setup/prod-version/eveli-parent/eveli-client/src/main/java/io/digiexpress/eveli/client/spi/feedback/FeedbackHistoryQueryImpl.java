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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import io.digiexpress.eveli.client.api.FeedbackClient.FeedbackHistoryEvent;
import io.digiexpress.eveli.client.api.FeedbackClient.FeedbackHistoryQuery;
import io.digiexpress.eveli.client.api.ImmutableFeedbackHistoryEvent;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FeedbackHistoryQueryImpl implements FeedbackHistoryQuery {
  private static final String SELECT_HISTORY_EVENTS = 
"""
SELECT 
  feedback_history.id,
  
  feedback_history.commit_id,
  feedback_history.rating_id,
  feedback_history.category_id,
  feedback_history.reply_id,
  
  feedback_history.json_body_type,
  feedback_history.json_body,
  
  feedback_history.created_on_date,
  feedback_history.created_by
  
FROM feedback_history as feedback_history

""";
  
  
  private final JdbcTemplate jdbc;
  
  @Override
  public List<FeedbackHistoryEvent> findAll() {
    return jdbc.query(SELECT_HISTORY_EVENTS, (ResultSet rs) -> {
      final var result = new ArrayList<FeedbackHistoryEvent>();
      while(rs.next()) {
        result.add(map(rs));
      }    
      return Collections.unmodifiableList(result);
    });
  }
  

  private FeedbackHistoryEvent map(ResultSet rs) throws SQLException {
    return  ImmutableFeedbackHistoryEvent.builder()
        .id(rs.getString("id"))
        
        .commitId(rs.getString("commit_id"))
        .ratingId(rs.getString("rating_id"))
        .categoryId(rs.getString("category_id"))
        .replyId(rs.getString("reply_id"))

        .jsonBodyType(rs.getString("json_body_type"))
        .jsonBody(rs.getString("json_body"))
        
        .createdOnDate(rs.getTimestamp("created_on_date").toInstant().atZone(ZoneId.systemDefault()))
        
        .createdBy(rs.getString("created_by"))
        
        .build();
  }

}
