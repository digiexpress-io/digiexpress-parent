package io.digiexpress.eveli.client.spi.feedback;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;

import io.digiexpress.eveli.client.api.FeedbackClient.CustomerFeedback;
import io.digiexpress.eveli.client.api.FeedbackClient.CustomerFeedbackQuery;
import io.digiexpress.eveli.client.api.FeedbackClient.Feedback;
import io.digiexpress.eveli.client.api.FeedbackClient.FeedbackRating;
import io.digiexpress.eveli.client.api.ImmutableCustomerFeedback;
import io.digiexpress.eveli.client.api.ImmutableFeedback;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomerFeedbackQueryImpl implements CustomerFeedbackQuery {
  private final JdbcTemplate jdbc;
  private final FeedbackWithHistory feedbackWithHistory;
  
  @Override
  public Multi<CustomerFeedback> findAllByCustomerId(String customerId) {
    return Uni.combine().all().unis(
        new FeedbackQueryImpl(jdbc, feedbackWithHistory).findAll().collect().asList(),
        new FeedbackRatingQueryImpl(jdbc).findAllByCustomerId(customerId).collect().asList()
      )
      .asTuple()
      .onItem().transformToMulti(tuple -> {
        final var ratings = tuple.getItem2().stream().collect(Collectors.toMap(e -> e.getReplyId(), e -> e));
        return Multi.createFrom().items(tuple.getItem1().stream().map(entry -> map(entry, ratings.get(entry.getId()))));
      });
  }

  @Override
  public Multi<CustomerFeedback> findAll() {
    return new FeedbackQueryImpl(jdbc, feedbackWithHistory)
      .findAll()
      .map(entry -> map(entry, null));
  }

  
  private CustomerFeedback map(Feedback entry, FeedbackRating rating) {
    final var feedback = ImmutableFeedback.builder()
        .from(entry)
        // no need to leak internal ids/user-ids
        .createdBy("")
        .updatedBy("")
        .sourceId("")
        .origin("") 
        .build();
    
    return ImmutableCustomerFeedback.builder()
        .feedback(feedback)
        .rating(rating)
        .build();
  }
}
