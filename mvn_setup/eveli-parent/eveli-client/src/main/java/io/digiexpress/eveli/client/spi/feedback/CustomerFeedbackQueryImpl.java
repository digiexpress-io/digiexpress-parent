package io.digiexpress.eveli.client.spi.feedback;

import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;

import io.digiexpress.eveli.client.api.FeedbackClient.CustomerFeedback;
import io.digiexpress.eveli.client.api.FeedbackClient.CustomerFeedbackQuery;
import io.digiexpress.eveli.client.api.FeedbackClient.Feedback;
import io.digiexpress.eveli.client.api.FeedbackClient.FeedbackRating;
import io.digiexpress.eveli.client.api.ImmutableCustomerFeedback;
import io.digiexpress.eveli.client.api.ImmutableFeedback;
import io.smallrye.mutiny.Multi;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomerFeedbackQueryImpl implements CustomerFeedbackQuery {
  private final JdbcTemplate jdbc;
  private final FeedbackWithHistory feedbackWithHistory;
  
  @Override
  public Multi<CustomerFeedback> findAllByCustomerId(String customerId) {
    final var ratings = new FeedbackRatingQueryImpl(jdbc)
      .findAllByCustomerId(customerId).stream()
      .collect(Collectors.toMap(e -> e.getReplyId(), e -> e));

    return new FeedbackQueryImpl(jdbc, feedbackWithHistory)
      .findAll()
      .map(entry -> map(entry, ratings.get(entry.getId())));
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
