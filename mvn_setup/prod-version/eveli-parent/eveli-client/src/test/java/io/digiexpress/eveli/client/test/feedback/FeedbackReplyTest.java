package io.digiexpress.eveli.client.test.feedback;

import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;

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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import io.digiexpress.eveli.client.api.FeedbackClient;
import io.digiexpress.eveli.client.api.FeedbackClient.ModifyFeedbackCommandType;
import io.digiexpress.eveli.client.api.ImmutableCreateFeedbackCommand;
import io.digiexpress.eveli.client.api.ImmutableModifyOneFeedbackReplyCommand;



@SpringBootTest
public class FeedbackReplyTest extends FeedbackEnvirSetup {

  @Autowired SetupTask setupTasks;
  @Autowired FeedbackClient feedbackClient;
  
  @Container @ServiceConnection static PostgreSQLContainer<?> CONTAINER = new PostgreSQLContainer<>("postgres:17");
  @BeforeEach void beforeAll() { CONTAINER.start(); }
  @AfterEach void afterAll() throws SQLException { CONTAINER.stop(); }
  
  @Test
  void testReplyUpdate() {
    final var taskId = setupTasks.generateOneTask();
    final var template = feedbackClient.queryTemplate().getOneByTaskId(taskId, "");
    final var feedback = feedbackClient.createOneFeedback(ImmutableCreateFeedbackCommand.builder()
        .content(template.getContent())
        
        .labelKey(template.getLabelKey())
        .labelValue(template.getLabelValue())
        
        .subLabelKey(template.getSubLabelKey())
        .subLabelValue(template.getSubLabelValue())
        
        .locale(template.getLocale())
        .origin(template.getOrigin())
        
        .processId(template.getProcessId())
        .reporterNames(template.getReporterNames())
        
        .reply("Proletariat John here, replying to you")
        .build(), "user-john");
    
    Assertions.assertEquals(1, feedbackClient.queryFeedbacks().findAll().size());
    
    {
      final var updatedReply = feedbackClient.modifyOneFeedback(
          ImmutableModifyOneFeedbackReplyCommand.builder()
          .id(feedback.getId())
          .commandType(ModifyFeedbackCommandType.MODIFY_ONE_FEEDBACK_REPLY)
          .reply("This is my updated reply from John")
          .build(), "JOHN");
      
      Assertions.assertNotNull(updatedReply, "Can't find modified feedback reply!");
      
      final var afterUpdate = feedbackClient.queryFeedbacks().findAll().stream()
        .filter(e -> e.getId().equals(feedback.getId()))
        .findFirst()
        .get();
      
      Assertions.assertEquals("This is my updated reply from John", afterUpdate.getReplyText());
    }
  }
}
