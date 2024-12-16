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
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;

import io.digiexpress.eveli.client.api.FeedbackClient.FeedbackRating;
import io.digiexpress.eveli.client.api.FeedbackClient.FeedbackRatingQuery;
import io.digiexpress.eveli.client.api.ImmutableFeedbackRating;
import io.digiexpress.eveli.client.spi.asserts.ProcessAssert;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FeedbackRatingQueryImpl implements FeedbackRatingQuery {

  private static String SELECT = 
"""
SELECT 
  id,
  category_id,
  reply_id,
  source_id,
  star_rating,
  created_on_date,
  updated_on_date
FROM 
  feedback_approval 
""";

  private final JdbcTemplate jdbc;

  @Override
  public List<FeedbackRating> findAllByCustomerId(String customerId) {
    // find existing record
    return jdbc.query(SELECT + " WHERE source_id = ?", (PreparedStatement ps) -> {
      ps.setString(1, FeedbackRatingBuilderImpl.maskCustomer(customerId));
    }, (ResultSet rs) -> {
      final var result = new ArrayList<FeedbackRating>();
      while(rs.next()) {
        result.add(map(rs));
      }
      return Collections.unmodifiableList(result);
    });
  }
  
  public FeedbackRating getOneById(String ratingId) {
    // find existing record
    return jdbc.query(SELECT + " WHERE id = ?", (PreparedStatement ps) -> {
      ps.setObject(1, UUID.fromString(ratingId));
    }, (ResultSet rs) -> {
      if(rs.next()) {
         return map(rs);
      }
      throw ProcessAssert.fail(() -> "Can't find feedback_rating by id: '" + ratingId + "'!");
    });
  }
  
  private FeedbackRating map(ResultSet rs) throws SQLException {
    return ImmutableFeedbackRating.builder()
    .customerId(rs.getString("source_id"))
    .categoryId(rs.getString("category_id"))
    .replyId(rs.getObject("reply_id") == null ? null : rs.getString("reply_id"))
    .id(rs.getString("id"))
    .rating(rs.getInt("star_rating"))
    .build();
  }
}
