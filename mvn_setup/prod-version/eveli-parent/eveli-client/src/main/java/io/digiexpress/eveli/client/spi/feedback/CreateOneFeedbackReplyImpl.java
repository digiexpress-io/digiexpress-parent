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
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

import org.flywaydb.core.internal.jdbc.JdbcUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import io.digiexpress.eveli.client.api.FeedbackClient.CreateFeedbackCommand;
import io.digiexpress.eveli.client.api.FeedbackClient.Feedback;
import io.digiexpress.eveli.client.spi.asserts.ProcessAssert;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateOneFeedbackReplyImpl {
  private final JdbcTemplate jdbc;
  private final FeedbackWithHistory withHistory;
  
  public Feedback apply(CreateFeedbackCommand command) {
    return withHistory.withHistory(history -> {
      final var id = jdbc.execute((Connection conn) -> doInConnection(conn, command));
      final var created = new FeedbackQueryImpl(jdbc).getOneById(id);
      history.append(command, created);
      return created;
    });
    
    
  }

  private final String doInConnection(Connection connection, CreateFeedbackCommand command) throws SQLException {
    connection.setAutoCommit(false);
    connection.beginRequest();
    try {
      
      final var categoryId = getOrCreateCategory(connection, command);
      final var feedback = createReply(categoryId, command);
      
      connection.commit();
      connection.endRequest();
      
      return feedback;
    } catch(Exception e) {
      connection.rollback();
      throw ProcessAssert.fail(e);
    } finally {
      JdbcUtils.closeConnection(connection);
    }
  }
  
  
 private String createReply(String categoryId, CreateFeedbackCommand command) {
   final var now = java.sql.Timestamp.from(Instant.now());
   return jdbc.execute((Connection connection) -> connection.prepareStatement(
"""

INSERT INTO feedback_reply
( category_id, 
  content, 
  locale,
  localized_label,
  localized_sub_label,
  source_id,
  created_on_date,
  updated_on_date,
  updated_by,
  created_by,
  reporter_names,
  reply_text
)
VALUES
(?,?,?,?,?,?,?,?,?,?,?,?)

""", new String[] {"id"} ), 
   (PreparedStatement categeoryStm) -> {
     categeoryStm.setObject(1, UUID.fromString(categoryId));
     categeoryStm.setString(2, command.getContent());
     categeoryStm.setString(3, command.getLocale());
     categeoryStm.setString(4, command.getLabelValue().trim());
     categeoryStm.setObject(5, command.getSubLabelValue().isBlank() ? null : command.getSubLabelValue().trim());
     categeoryStm.setString(6, command.getProcessId());
     
     categeoryStm.setObject(7, now);
     categeoryStm.setObject(8, now);
     
     categeoryStm.setString(9, command.getUserId());
     categeoryStm.setString(10, command.getUserId());
     categeoryStm.setString(11, command.getReporterNames());
     categeoryStm.setString(12, command.getReply());

    
     categeoryStm.execute();
     final var rs = categeoryStm.getGeneratedKeys();
     rs.next();
     final var id = rs.getString(1);
     rs.close();
     return id;
  });
   
   
   
 }
  
  
 private String getOrCreateCategory(Connection parent, CreateFeedbackCommand command) throws SQLException {
    ProcessAssert.notEmpty(command.getLabelKey(), () -> "labelKey can't be empty!");
    ProcessAssert.notEmpty(command.getOrigin(), () -> "origin can't be empty!");
    ProcessAssert.notEmpty(command.getProcessId(), () -> "processId can't be empty!");
    ProcessAssert.notEmpty(command.getUserId(), () -> "user id can't be empty!");
    
    final var labelKey = command.getLabelKey().trim().toUpperCase();
    final var labelSubKey = command.getSubLabelKey().isBlank() ? null : command.getSubLabelKey().trim().toUpperCase();
    
    
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
  categeoryStm.setString(3, command.getOrigin());
  categeoryStm.setString(4, command.getUserId());
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
