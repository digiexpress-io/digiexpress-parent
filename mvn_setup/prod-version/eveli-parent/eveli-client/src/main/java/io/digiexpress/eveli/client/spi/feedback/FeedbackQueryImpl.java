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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;

import io.digiexpress.eveli.client.api.FeedbackClient;
import io.digiexpress.eveli.client.api.FeedbackClient.Feedback;
import io.digiexpress.eveli.client.api.ImmutableFeedback;
import io.digiexpress.eveli.client.spi.asserts.ProcessAssert;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FeedbackQueryImpl implements FeedbackClient.FeedbackQuery {
  private static final String SELECT_REPLY = 
"""
SELECT 
  feedback_reply.id,
  feedback_reply.category_id,
  feedback_reply.content,
  feedback_reply.locale,
  feedback_reply.source_id,
    
  feedback_reply.localized_label,
  feedback_reply.localized_sub_label,
  feedback_reply.created_on_date,
  feedback_reply.updated_on_date,
  feedback_reply.updated_by,
  feedback_reply.created_by,
  
  
  feedback_category.label as label_key,
  feedback_category.sub_label as sub_label_key,
  feedback_category.origin,
  
  (SELECT count(*) FROM feedback_approval WHERE reply_id = feedback_reply.id AND star_rating = 5) as thumbs_up_count,
  (SELECT count(*) FROM feedback_approval WHERE reply_id = feedback_reply.id AND star_rating = 1) as thumbs_down_count  
  
FROM feedback_reply as feedback_reply
LEFT JOIN feedback_category ON (feedback_category.id = feedback_reply.category_id)
""";
  
  
  private final JdbcTemplate jdbc;
  
  @Override
  public List<Feedback> findAll() {
    return jdbc.query(SELECT_REPLY, (ResultSet rs) -> {
      final var result = new ArrayList<Feedback>();
      while(rs.next()) {
        result.add(map(rs));
      }    
      return Collections.unmodifiableList(result);
    });
  }
  
  @Override
  public Feedback getOneById(String id) {
    return jdbc.query(SELECT_REPLY + " WHERE feedback_reply.id = ?", (PreparedStatement ps) -> ps.setObject(1, UUID.fromString(id)), (ResultSet rs) -> {
      if(rs.next()) {
        return map(rs);
      }    
      throw ProcessAssert.fail(() -> "can't find feedback reply by id = '" + id + "'");
    });
  }
  
  private Feedback map(ResultSet rs) throws SQLException {
    return  ImmutableFeedback.builder()
        .id(rs.getString("id"))
        .categoryId(rs.getString("category_id"))
        .labelKey(rs.getString("label_key"))
        .subLabelKey(rs.getString("sub_label_key"))
        .origin(rs.getString("origin"))
        .thumbsUpCount(rs.getInt("thumbs_up_count"))
        .thumbsDownCount(rs.getInt("thumbs_down_count"))
        
        .labelValue(rs.getString("localized_label"))
        .subLabelValue(rs.getString("localized_sub_label"))
        .sourceId(rs.getString("source_id"))
        .createdBy(rs.getString("created_by"))
        .updatedBy(rs.getString("updated_by"))
        .updatedOnDate(rs.getString("updated_on_date"))
        .content(rs.getString("content"))
        .locale(rs.getString("locale"))
        .build();
  }

  @Override
  public Optional<Feedback> findOneById(String taskIdOrFeedbackId) {
    return jdbc.query(SELECT_REPLY + 
"""
WHERE feedback_reply.id = ? OR feedback_reply.source_id = ?
""", (PreparedStatement ps) -> {
      
      try {
        ps.setObject(1, UUID.fromString(taskIdOrFeedbackId));
      } catch(IllegalArgumentException e) {
        ps.setObject(1, null);
      }
      ps.setString(2, taskIdOrFeedbackId);
      
    }, (ResultSet rs) -> {
      if(rs.next()) {
        return Optional.of(map(rs));
      }    
      return Optional.empty();
    });
  }
}
