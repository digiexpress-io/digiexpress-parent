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
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;

import io.digiexpress.eveli.client.api.FeedbackClient.DeleteReplyCommand;
import io.digiexpress.eveli.client.api.FeedbackClient.Feedback;
import io.digiexpress.eveli.client.spi.asserts.ProcessAssert;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FeedbackRatingDeleteBuilderImpl {
  private final JdbcTemplate jdbc;
  private final FeedbackWithHistory withHistory;
  private final String userId;
  
  public List<Feedback> execute(DeleteReplyCommand command) {
    return withHistory.withHistory(history -> {
      final var upserted = deleteAll(command);
      history.append(command, upserted, userId);
      return upserted;
    });
  }
  
  private List<Feedback> deleteAll(DeleteReplyCommand command) {
    
    final var entries = command.getReplyIds().stream()
      .map(id -> new FeedbackQueryImpl(jdbc).findOneById(id))
      .map(e -> e.orElseThrow(() -> ProcessAssert.fail(() -> "Failed to find feedbacks: " + command.getReplyIds() + " for deletion!")))
      .toList();
    
    deleteApproval(entries);
    return entries;
    
  }

  private UUID[] deleteApproval(List<Feedback> entries) {
    final var ids = entries.stream().map(e -> e.getId()).map(UUID::fromString).toList().toArray(new UUID[]{});
    return jdbc.execute((Connection connection) -> {
      
      final var prep = connection.prepareStatement(
"""
DELETE 
FROM feedback_reply
WHERE id = ANY(?::UUID[])
"""
);
      final var array = connection.createArrayOf("UUID", ids);
      prep.setObject(1, array);
      return prep;
    }, 
    (PreparedStatement categeoryStm) -> {
      final var updated = categeoryStm.executeUpdate();
      ProcessAssert.isTrue(updated == ids.length, () -> "Failed to find feedbacks: " + ids + " for deletion!");
      return ids;
   });
  }
  
}
