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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import io.digiexpress.eveli.client.api.FeedbackClient.FeedbackRating;
import io.digiexpress.eveli.client.api.FeedbackClient.UpsertFeedbackRankingCommand;
import io.digiexpress.eveli.client.spi.asserts.ProcessAssert;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FeedbackRatingBuilderImpl {
  private final JdbcTemplate jdbc;
  private final FeedbackWithHistory withHistory;
  private final String userId;
  
  public FeedbackRating execute(UpsertFeedbackRankingCommand command) {
    return withHistory.withHistory(history -> {
      final var upserted = upsert(command);
      history.append(command, upserted, userId);
      return upserted;
    });
  }
  
  private FeedbackRating getRatingById(String ratingId) {
    return new FeedbackRatingQueryImpl(jdbc).getOneById(ratingId);
  }
  
  public static String maskCustomer(String customerId) {
    return DigestUtils.md5Hex(customerId).toUpperCase();
  }
  
  private FeedbackRating upsert(UpsertFeedbackRankingCommand command) {
    final var customerId = maskCustomer(userId);
    
    String categoryId = getCategoryId(command).orElse(null);
    Optional<String> replyId = Optional.empty();
    if(categoryId == null) {
      final var replyIdAndCategoryId = getReplyIdAndCategoryId(command)
          .orElseThrow(() -> ProcessAssert.fail(
              () -> "Can't find category and reply by replyId = '" + command.getReplyIdOrCategoryId() + "'"));
      
      categoryId = replyIdAndCategoryId.getCategoryId();
      replyId = Optional.of(replyIdAndCategoryId.getReplyId());
    }
    

    final var ratingIdentifiers = RatingIdentifiers.builder()
      .categoryId(categoryId)
      .customerId(customerId)
      .replyId(replyId)
      .build();

    final var ratingId = getExistingRatingId(ratingIdentifiers);
    
    if(ratingId.isPresent() && command.getRating() != null) {
      final var id = updateRating(ratingId.get(), ratingIdentifiers, command);
      return getRatingById(id);
    
    } else if(ratingId.isPresent() && command.getRating() == null) {
      
      final var result = getRatingById(ratingId.get());
      deleteRating(ratingId.get(), command);
      return result;
    }

    final var id = createRating(ratingIdentifiers, command);
    return getRatingById(id);
  }
  
  private String createRating(RatingIdentifiers ratingIdentifiers, UpsertFeedbackRankingCommand command) {
    final var now = java.sql.Timestamp.from(Instant.now());
    return jdbc.execute((Connection connection) -> connection.prepareStatement(
"""
INSERT INTO feedback_approval
( category_id, 
  reply_id,
  source_id,
  star_rating,
  created_on_date,
  updated_on_date
)
VALUES
(?,?,?,?,?,?)
""", new String[] {"id"} ), 
    (PreparedStatement categeoryStm) -> {
      categeoryStm.setObject(1, UUID.fromString(ratingIdentifiers.getCategoryId()));
      categeoryStm.setObject(2, ratingIdentifiers.getReplyId().map(UUID::fromString).orElse(null));
      categeoryStm.setString(3, ratingIdentifiers.getCustomerId());
      categeoryStm.setInt(4, command.getRating());
      
      categeoryStm.setObject(5, now);
      categeoryStm.setObject(6, now);
     
      categeoryStm.execute();
      final var rs = categeoryStm.getGeneratedKeys();
      rs.next();
      final var createdId = rs.getString(1);
      rs.close();
      return createdId;
   });
  }

  private String updateRating(String id, RatingIdentifiers ratingIdentifiers, UpsertFeedbackRankingCommand command) {
    final var now = java.sql.Timestamp.from(Instant.now());
    return jdbc.execute((Connection connection) -> connection.prepareStatement(
"""
UPDATE feedback_approval
SET 
  updated_on_date = ?,
  star_rating = ?
WHERE
  id = ?
"""), 
    (PreparedStatement categeoryStm) -> {
      categeoryStm.setObject(1, now);
      categeoryStm.setInt(2, command.getRating());
      categeoryStm.setObject(3, UUID.fromString(id));
      categeoryStm.execute();
      return id;
   });
  }
  
  private String deleteRating(String id, UpsertFeedbackRankingCommand command) {
    return jdbc.execute((Connection connection) -> connection.prepareStatement("DELETE FROM feedback_approval WHERE id = ?"), 
    (PreparedStatement categeoryStm) -> {
      categeoryStm.setObject(1, UUID.fromString(id));
      categeoryStm.execute();
      return id;
   });
  }
  
  
  private Optional<String> getExistingRatingId(RatingIdentifiers ratingIdentifier) {

    final var categoryId = ratingIdentifier.getCategoryId();
    final var customerId = ratingIdentifier.getCustomerId();
    final var replyId = ratingIdentifier.getReplyId();
    
    UUID.fromString(categoryId);
    // find existing record
    return jdbc.query(
"""
SELECT 
  id,
  source_id
FROM 
  feedback_approval 
WHERE 
  category_id = ?
  and source_id = ?
  and COALESCE(reply_id::text, '') = COALESCE(?::text, '')
""", (PreparedStatement ps) -> {
      ps.setObject(1, UUID.fromString(categoryId));
      ps.setString(2, customerId);
      ps.setObject(3, replyId.map(UUID::fromString).orElse(null));
    }, (ResultSet rs) -> {
      if(rs.next()) {
        return Optional.of(rs.getString(1));
      }
      return Optional.<String>empty();
    });
  }
  
  
  private Optional<String> getCategoryId(UpsertFeedbackRankingCommand command) {
    // find existing record
    return jdbc.query(
"""
SELECT id FROM feedback_category WHERE id = ?
""", (PreparedStatement ps) -> {
      ps.setObject(1, UUID.fromString(command.getReplyIdOrCategoryId()));
    }, (ResultSet rs) -> {
      if(rs.next()) {
        return Optional.of(rs.getString(1));
      }
      return Optional.<String>empty();
    });
  }
  
  
  private Optional<ReplyIdAndCategoryId> getReplyIdAndCategoryId(UpsertFeedbackRankingCommand command) {
    // find existing record
    return jdbc.query(
"""
SELECT 
  feedback_reply.id,
  feedback_reply.category_id 
FROM 
  feedback_reply 
WHERE feedback_reply.id = ?
""", (PreparedStatement ps) -> {
      ps.setObject(1,  UUID.fromString(command.getReplyIdOrCategoryId()));
    }, (ResultSet rs) -> {
      if(rs.next()) {
        return Optional.of(new ReplyIdAndCategoryId(rs.getString(1), rs.getString(2)));
      }
      return Optional.<ReplyIdAndCategoryId>empty();
    });
  }
  
  
  @Data @RequiredArgsConstructor
  private static class ReplyIdAndCategoryId {
    private final String replyId;
    private final String categoryId;
  }
  
  
  @Data @Builder @RequiredArgsConstructor
  private static class RatingIdentifiers {
    private final Optional<String> replyId;
    private final String categoryId;
    private final String customerId;
  }
}
