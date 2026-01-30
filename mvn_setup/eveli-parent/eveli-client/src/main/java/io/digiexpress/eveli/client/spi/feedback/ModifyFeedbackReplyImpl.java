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
import java.util.UUID;

import org.flywaydb.core.internal.jdbc.JdbcUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import io.digiexpress.eveli.client.api.FeedbackClient.Feedback;
import io.digiexpress.eveli.client.api.FeedbackClient.ModifyFeedbackCommandType;
import io.digiexpress.eveli.client.api.FeedbackClient.ModifyOneFeedbackCommand;
import io.digiexpress.eveli.client.api.FeedbackClient.ModifyOneFeedbackReplyCommand;
import io.digiexpress.eveli.client.spi.asserts.ProcessAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ModifyFeedbackReplyImpl {
  private final JdbcTemplate jdbc;
  private final FeedbackWithHistory withHistory;
  private final String userId;

  public Uni<Feedback> apply(ModifyOneFeedbackCommand command) {
    final var beforeUpdate = new FeedbackQueryImpl(jdbc, withHistory).findOneById(command.getId());
    final var replyId = beforeUpdate.get().getId();
    
    return withHistory.withHistory(history -> {
      return jdbc.execute((Connection connection) -> {
        
        connection.setAutoCommit(false);
        connection.beginRequest();
        try {
      
          final var updated = applyCommand(command, replyId);
          history.append(command, updated, userId);
          return updated;
      
        } catch(Exception e) {
          connection.rollback();
          throw ProcessAssert.fail(e);
        } finally {
          JdbcUtils.closeConnection(connection);
        }
        
      });      
    });
  }


  private Feedback applyCommand(ModifyOneFeedbackCommand command, String replyId) {
    
    if(command.getCommandType() == ModifyFeedbackCommandType.MODIFY_ONE_FEEDBACK_REPLY) {
      final var category = getOrCreateCategory((ModifyOneFeedbackReplyCommand) command);
      return modifyReply((ModifyOneFeedbackReplyCommand) command, replyId, category);
    }
  
    throw ProcessAssert.fail(() -> "Unknown modify feedback command: " + command + "!");
  }
  
  
  private Feedback modifyReply(ModifyOneFeedbackReplyCommand command, String replyId, String category) {
    final var now = java.sql.Timestamp.from(Instant.now());
    final var updatedRows = jdbc.execute((Connection connection) -> connection.prepareStatement(
"""
UPDATE feedback_reply
SET 
  updated_on_date = ?,
  updated_by = ?,
  reply_text = ?,
  customer_question = ?,
  localized_label = ?,
  localized_sub_label = ?,
  category_id = ?,
  customer_title = ?
WHERE
  id = ?
"""), 
    (PreparedStatement statement) -> {
      
      statement.setTimestamp(1, now);
      statement.setString(2, userId);
      statement.setString(3, command.getReply());
      statement.setObject(4, command.getQuestion());
      
      statement.setString(5, command.getLabelValue());
      statement.setString(6, command.getSubLabelValue());
      statement.setObject(7, UUID.fromString(category));
      statement.setObject(8, command.getCustomerTitle());
      
      
      statement.setObject(9, UUID.fromString(replyId));

      return statement.executeUpdate();
    }); 
    
    ProcessAssert.isTrue(updatedRows == 1, () -> "Failed to update reply with command: " + command + "!");
    final var afterUpdate = new FeedbackQueryImpl(jdbc).findOneById(command.getId()).get();
    return afterUpdate;
  }
 

  
  private String getOrCreateCategory(ModifyOneFeedbackReplyCommand command) {
     final var beforeMod = new FeedbackQueryImpl(jdbc).getOneById(command.getId());
     ProcessAssert.notEmpty(command.getLabelKey(), () -> "labelKey can't be empty!");
     ProcessAssert.notEmpty(userId, () -> "user id can't be empty!");
     
     final var labelKey = command.getLabelKey().trim().toUpperCase();
     final var labelSubKey = command.getSubLabelKey() == null || command.getSubLabelKey().isBlank() ? null : command.getSubLabelKey().trim().toUpperCase();
     
     jdbc.execute((Connection connection) -> connection.prepareStatement(
 """
 INSERT INTO feedback_category
 ( label, 
   sub_label, 
   origin,  
   created_by_user_id,
   created_on_date,
   updated_on_date
 )
 VALUES
 (?, ?, ?, ?, ?, ?)

 ON CONFLICT DO NOTHING
 """), (PreparedStatement categeoryStm) -> {
   
   categeoryStm.setString(1, labelKey);
   categeoryStm.setString(2, labelSubKey);
   categeoryStm.setString(3, beforeMod.getOrigin());
   categeoryStm.setString(4, userId);
   categeoryStm.setObject(5, java.sql.Timestamp.from(Instant.now()));
   categeoryStm.setObject(6, java.sql.Timestamp.from(Instant.now()));

   categeoryStm.execute();
   return null;
 });
     

     // find inserted or existing record
     return jdbc.query(
 """
 SELECT id FROM feedback_category WHERE label = ? and COALESCE(sub_label, '') = COALESCE(?, '')
 """, (PreparedStatement ps) -> {
     ps.setString(1, labelKey);
     ps.setString(2, labelSubKey);
   }, (ResultSet rs) -> {
     while(rs.next()) {
       return rs.getString(1);
     }
     throw ProcessAssert.fail(() -> "can't find category by label = '" + labelKey + "'");
   });

   }
}