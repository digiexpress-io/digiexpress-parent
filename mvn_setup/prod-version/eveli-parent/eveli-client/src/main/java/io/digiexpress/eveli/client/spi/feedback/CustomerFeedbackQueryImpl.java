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

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;

import io.digiexpress.eveli.client.api.FeedbackClient.CustomerFeedback;
import io.digiexpress.eveli.client.api.FeedbackClient.CustomerFeedbackQuery;
import io.digiexpress.eveli.client.api.FeedbackClient.Feedback;
import io.digiexpress.eveli.client.api.FeedbackClient.FeedbackRating;
import io.digiexpress.eveli.client.api.ImmutableCustomerFeedback;
import io.digiexpress.eveli.client.api.ImmutableFeedback;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomerFeedbackQueryImpl implements CustomerFeedbackQuery {
  private final JdbcTemplate jdbc;
  
  @Override
  public List<CustomerFeedback> findAllByCustomerId(String customerId) {
    final var ratings = new FeedbackRatingQueryImpl(jdbc)
      .findAllByCustomerId(customerId).stream()
      .collect(Collectors.toMap(e -> e.getReplyId(), e -> e));

    return new FeedbackQueryImpl(jdbc)
      .findAll().stream()
      .map(entry -> map(entry, ratings.get(entry.getId()))).toList();
  }

  @Override
  public List<CustomerFeedback> findAll() {
    return new FeedbackQueryImpl(jdbc)
    .findAll()
    .stream()
    .map(entry -> map(entry, null)).toList();
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
