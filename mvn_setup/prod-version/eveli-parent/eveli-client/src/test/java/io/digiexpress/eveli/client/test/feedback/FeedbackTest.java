package io.digiexpress.eveli.client.test.feedback;

import java.util.Arrays;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.digiexpress.eveli.client.api.FeedbackClient;
import io.digiexpress.eveli.client.api.ImmutableCreateFeedbackCommand;
import io.digiexpress.eveli.client.api.ImmutableDeleteReplyCommand;
import io.digiexpress.eveli.client.api.ImmutableUpsertFeedbackRankingCommand;



@SpringBootTest
public class FeedbackTest extends FeedbackEnirSetup {


  @Autowired SetupTask setupTasks;
  @Autowired FeedbackClient feedbackClient;

  @Test
  void run() {
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
        .userId("super-user")

        .build());
    
    
    final var queryFeedback = feedbackClient.queryFeedbacks().findAll().stream().filter(e -> e.getId().equals(feedback.getId())).findFirst();
    Assertions.assertTrue(queryFeedback.isPresent(), "Can't find created feedback");
    Assertions.assertTrue(feedbackClient.queryFeedbacks().findAll().size() == 1, "Can't find created feedback");
    final var queryFeedbackById = feedbackClient.queryFeedbacks().findOneById(taskId);
    Assertions.assertTrue(queryFeedbackById.isPresent(), "Can't find created feedback");
    
    
    
    // rate feedback as thumbs down    
    {
      final var feedbackRating = feedbackClient.modifyOneFeedbackRank(
          ImmutableUpsertFeedbackRankingCommand.builder()
          .customerId("BOB")
          .rating(1)
          .replyIdOrCategoryId(feedback.getId())
          .build());
      
      Assertions.assertNotNull(feedbackRating, "Can't find created feedback rating");
      
      final var ratedFeedback = feedbackClient.queryFeedbacks().findAll().stream()
        .filter(e -> e.getId().equals(feedback.getId()))
        .findFirst()
        .get();
      
      Assertions.assertEquals(1, ratedFeedback.getThumbsDownCount());
      Assertions.assertEquals(0, ratedFeedback.getThumbsUpCount());
    }
    
    // rate feedback as thumbs up
    {
      final var feedbackRating = feedbackClient.modifyOneFeedbackRank(
          ImmutableUpsertFeedbackRankingCommand.builder()
          .customerId("BOB")
          .rating(5)
          .replyIdOrCategoryId(feedback.getId())
          .build());
      
      Assertions.assertNotNull(feedbackRating, "Can't find created feedback rating");
      
      final var ratedFeedback = feedbackClient.queryFeedbacks().findAll().stream()
        .filter(e -> e.getId().equals(feedback.getId()))
        .findFirst()
        .get();
      
      Assertions.assertEquals(0, ratedFeedback.getThumbsDownCount());
      Assertions.assertEquals(1, ratedFeedback.getThumbsUpCount());
    }
    
    // remove rating
    {
      final var feedbackRating = feedbackClient.modifyOneFeedbackRank(
          ImmutableUpsertFeedbackRankingCommand.builder()
          .customerId("BOB")
          .rating(null)
          .replyIdOrCategoryId(feedback.getId())
          .build());
      
      Assertions.assertNotNull(feedbackRating, "Can't find created feedback rating");
      
      final var ratedFeedback = feedbackClient.queryFeedbacks().findAll().stream()
        .filter(e -> e.getId().equals(feedback.getId()))
        .findFirst()
        .get();
      
      Assertions.assertEquals(0, ratedFeedback.getThumbsDownCount());
      Assertions.assertEquals(0, ratedFeedback.getThumbsUpCount());
    }
    
    final var history = feedbackClient.queryHistory().findAll();
    Assertions.assertEquals(4, history.size());
    
    
    feedbackClient.deleteAll(ImmutableDeleteReplyCommand.builder()
        .userId("userId")
        .replyIds(Arrays.asList(taskId))
        .build());
    
    
    Assertions.assertEquals(0, feedbackClient.queryFeedbacks().findAll().size());
    
  }
}
