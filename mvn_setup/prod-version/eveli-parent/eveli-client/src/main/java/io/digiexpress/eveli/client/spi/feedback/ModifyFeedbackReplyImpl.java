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
import java.time.Instant;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;

import io.digiexpress.eveli.client.api.FeedbackClient.Feedback;
import io.digiexpress.eveli.client.api.FeedbackClient.ModifyFeedbackCommandType;
import io.digiexpress.eveli.client.api.FeedbackClient.ModifyOneFeedbackCommand;
import io.digiexpress.eveli.client.api.FeedbackClient.ModifyOneFeedbackReplyCommand;
import io.digiexpress.eveli.client.spi.asserts.ProcessAssert;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ModifyFeedbackReplyImpl {
  private final JdbcTemplate jdbc;
  private final FeedbackWithHistory withHistory;
  private final String userId;

  public Feedback apply(ModifyOneFeedbackCommand command) {
    final var beforeUpdate = new FeedbackQueryImpl(jdbc).findOneById(command.getId());
    final var replyId = beforeUpdate.get().getId();
    
    return withHistory.withHistory(history -> {
      final var updated = applyCommand(command, replyId);
      history.append(command, updated, userId);
      return updated;
    });
  }

  private Feedback applyCommand(ModifyOneFeedbackCommand command, String replyId) {
    
    if(command.getCommandType() == ModifyFeedbackCommandType.MODIFY_ONE_FEEDBACK_REPLY) {
      return modifyReply((ModifyOneFeedbackReplyCommand) command, replyId);
    }
  
    throw ProcessAssert.fail(() -> "Unknown modify feedback command: " + command + "!");
  }
  
  
  private Feedback modifyReply(ModifyOneFeedbackReplyCommand command, String replyId) {
    final var now = java.sql.Timestamp.from(Instant.now());
    final var updatedRows = jdbc.execute((Connection connection) -> connection.prepareStatement(
"""
UPDATE feedback_reply
SET 
  updated_on_date = ?,
  updated_by = ?,
  reply_text = ?
WHERE
  id = ?
"""), 
    (PreparedStatement statement) -> {
      statement.setTimestamp(1, now);
      statement.setString(2, userId);
      statement.setString(3, command.getReply());
      statement.setObject(4, UUID.fromString(replyId));
      return statement.executeUpdate();
    }); 
    
    ProcessAssert.isTrue(updatedRows == 1, () -> "Failed to update reply with command: " + command + "!");
    final var afterUpdate = new FeedbackQueryImpl(jdbc).findOneById(command.getId()).get();
    return afterUpdate;
  }
  
}

  


