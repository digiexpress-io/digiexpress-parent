package io.digiexpress.eveli.client.api;

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

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.dialob.api.questionnaire.Questionnaire;
import jakarta.annotation.Nullable;

public interface FeedbackClient {

  
  Feedback createOneFeedback(CreateFeedbackCommand command);
  FeedbackRating modifyOneFeedbackRank(UpsertFeedbackRankingCommand command);
  
  FeedbackQuery queryFeedbacks();
  FeedbackTemplateQuery queryTemplate();  

  
  /**
   * Extract task/questionnaire data and map it to possible feedback 
   */
  interface FeedbackTemplateQuery {
    FeedbackTemplate getOneByTaskId(String taskId, String userId);
  }

  /**
   * Query/delete feedback
   */
  interface FeedbackQuery {
    List<Feedback> findAll();    
    List<Feedback> deleteById(List<String> feedbackId);
    Feedback getOneById(long id);
  }
  
  
  
  
  /**
   * Command to create feedback
   */
  @JsonSerialize(as = ImmutableCreateFeedbackCommand.class)
  @JsonDeserialize(as = ImmutableCreateFeedbackCommand.class)
  @Value.Immutable
  interface CreateFeedbackCommand {
    String getLabelKey();
    String getLabelValue();
    
    @Nullable String getSubLabelKey();
    @Nullable String getSubLabelValue();
    
    String getProcessId();
    String getUserId();
    
    String getOrigin();
    String getContent();
    String getLocale();
  }
  
  
  /**
   * Command to rank the feedback, aka thumbs down/up
   */
  @JsonSerialize(as = ImmutableUpsertFeedbackRankingCommand.class)
  @JsonDeserialize(as = ImmutableUpsertFeedbackRankingCommand.class)
  @Value.Immutable
  interface UpsertFeedbackRankingCommand {
    String getReplyId();
    String getCustomerId();
    
    @Nullable Integer getRating(); // null is remove vote 
  }
  
  
  /**
   * Represents customer questionnaire + worker answer + rantings by customers
   */
  @JsonSerialize(as = ImmutableFeedback.class)
  @JsonDeserialize(as = ImmutableFeedback.class)
  @Value.Immutable
  interface Feedback {
    String getId();
    
    String getLabelKey();
    String getLabelValue();
    
    @Nullable String getSubLabelKey();
    @Nullable String getSubLabelValue();
    
    String getSourceId();
    String getOrigin();
    
    String getUpdatedBy();
    String getCreatedBy();
    
    String getContent();
    String getLocale();
    
    
    int getThumbsUpCount(); // round rating to thumbs up
    int getThumbsDownCount(); // round rating to thumbs down
  }
  
  @JsonSerialize(as = ImmutableFeedbackRating.class)
  @JsonDeserialize(as = ImmutableFeedbackRating.class)
  @Value.Immutable
  interface FeedbackRating {
    String getId();
    String getFeedbackId();
    String getCustomerId(); //obscure id for customer, should not be able to identify the person
    int getRating(); // score 1-5
  }
  
  
  /**
   * Transient questionnaire + task extract
   */
  @JsonSerialize(as = ImmutableFeedbackTemplate.class)
  @JsonDeserialize(as = ImmutableFeedbackTemplate.class)
  @Value.Immutable
  interface FeedbackTemplate {
    String getLabelKey();
    String getLabelValue();
    
    @Nullable String getSubLabelKey();
    @Nullable String getSubLabelValue();
    
    String getProcessId();
    String getOrigin();
    String getContent();
    String getLocale();
    
    String getUserId();
    
    Questionnaire getQuestionnaire();
    List<String> getReplys(); 
  }

}
