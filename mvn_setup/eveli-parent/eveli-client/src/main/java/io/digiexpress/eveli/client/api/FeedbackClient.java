package io.digiexpress.eveli.client.api;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

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
import java.util.Map;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.dialob.api.questionnaire.Questionnaire;
import io.digiexpress.eveli.client.api.TaskClient.ProcessInstance;
import jakarta.annotation.Nullable;

public interface FeedbackClient {

  Feedback createOneFeedback(CreateFeedbackCommand command, String userId);
  FeedbackRating modifyOneFeedbackRank(UpsertFeedbackRankingCommand command, String userId);
  Feedback modifyOneFeedback(ModifyOneFeedbackCommand commands, String userId);
  
  List<Feedback> deleteAll(DeleteReplyCommand command, String userId);
  FeedbackQuestionnaireQuery queryQuestionnaire();

  CustomerFeedbackQuery queryCustomerFeedbacks();
  FeedbackQuery queryFeedbacks();
  FeedbackRatingQuery queryFeedbackRatings();
  FeedbackTemplateQuery queryTemplate();
  
  FeedbackHistoryQuery queryHistory();

  FeedbackAnalyzerQuery queryFeedbackAnalyzer();


  /**
   * Extract task/questionnaire data and map it to possible feedback 
   */
  interface FeedbackTemplateQuery {
    FeedbackTemplate getOneByTaskId(String taskId, String userId);
    Optional<FeedbackTemplate> findOneByTaskId(String taskId, String userId);
  }
  
  interface CustomerFeedbackQuery {
    List<CustomerFeedback> findAll();
    List<CustomerFeedback> findAllByCustomerId(String customerId);
  }
  
  interface FeedbackRatingQuery {
    List<FeedbackRating> findAllByCustomerId(String customerId);    
    FeedbackRating getOneById(String ratingId);

  }

  /**
   * Query/delete feedback
   */
  interface FeedbackQuery {
    List<Feedback> findAll();
    Feedback getOneById(String id);
    Optional<Feedback> findOneById(String taskIdOrFeedbackId);
  }
  
  /**
   * Get the commands with what the data was created in the first place
   */
  interface FeedbackHistoryQuery {
    List<FeedbackHistoryEvent> findAll();
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
    @Nullable String getReporterNames();
    @Nullable String getCustomerTitle();
    
    String getProcessId();
    String getTaskId();
    
    String getOrigin();
    FeedbackQuestionnaireContent getContent();
    String getReply();
    String getLocale();
    @Nullable String getQuestion();
  }
  
  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.PROPERTY,
      property = "commandType", 
      visible = true
      )
  @JsonSubTypes({
    @Type(value = ImmutableModifyOneFeedbackReplyCommand.class, name = "MODIFY_ONE_FEEDBACK_REPLY")
  })
  interface ModifyOneFeedbackCommand {
    String getId();
    ModifyFeedbackCommandType getCommandType();
  }
  
  enum ModifyFeedbackCommandType {
    MODIFY_ONE_FEEDBACK_REPLY
  }
  
  /**
   * Command to for worker to change feedback
   */
  @JsonSerialize(as = ImmutableModifyOneFeedbackReplyCommand.class)
  @JsonDeserialize(as = ImmutableModifyOneFeedbackReplyCommand.class)
  @Value.Immutable
  interface ModifyOneFeedbackReplyCommand extends ModifyOneFeedbackCommand {
    String getReply();
    String getQuestion();
    String getLabelKey();
    String getLabelValue();
    String getCustomerTitle();
    @Nullable String getSubLabelKey();
    @Nullable String getSubLabelValue();

    @Value.Default
    @Override default ModifyFeedbackCommandType getCommandType() { return ModifyFeedbackCommandType.MODIFY_ONE_FEEDBACK_REPLY; }
  }
  
  
  /**
   * Command to rank the feedback, aka thumbs down/up
   */
  @JsonSerialize(as = ImmutableUpsertFeedbackRankingCommand.class)
  @JsonDeserialize(as = ImmutableUpsertFeedbackRankingCommand.class)
  @Value.Immutable
  interface UpsertFeedbackRankingCommand {
    String getReplyIdOrCategoryId();
    
    @Nullable Integer getRating(); // null is remove vote 
  }
  
  
  /**
   * Command to rank the feedback, aka thumbs down/up
   */
  @JsonSerialize(as = ImmutableDeleteReplyCommand.class)
  @JsonDeserialize(as = ImmutableDeleteReplyCommand.class)
  @Value.Immutable
  interface DeleteReplyCommand {
    List<String> getReplyIds();
  }
  
  
  /**
   * Represents customer questionnaire + worker answer + rantings by customers
   */
  @JsonSerialize(as = ImmutableFeedback.class)
  @JsonDeserialize(as = ImmutableFeedback.class)
  @Value.Immutable
  interface Feedback {
    String getId();
    String getCategoryId();
    
    String getLabelKey();
    String getLabelValue();
    
    @Nullable String getCustomerTitle();
    @Nullable String getReporterNames(); // nullable if there is no user consent, separated by ','
    
    String getSourceId();
    String getOrigin();
    
    String getUpdatedBy();
    String getUpdatedOnDate();
    String getCreatedBy();
    
    FeedbackQuestionnaireContent getContent();
    String getReplyText();
    String getLocale();

    
    int getThumbsUpCount(); // round rating to thumbs up
    int getThumbsDownCount(); // round rating to thumbs down
  }
  
  /**
   * Based on logged in users
   */
  @JsonSerialize(as = ImmutableCustomerFeedback.class)
  @JsonDeserialize(as = ImmutableCustomerFeedback.class)
  @Value.Immutable
  interface CustomerFeedback {
    Feedback getFeedback();
    @Nullable FeedbackRating getRating();
  }
  
  
  @JsonSerialize(as = ImmutableFeedbackRating.class)
  @JsonDeserialize(as = ImmutableFeedbackRating.class)
  @Value.Immutable
  interface FeedbackRating {
    String getId();
    @Nullable String getReplyId();
    String getCategoryId();
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
    
    @Nullable String getCustomerTitle();
    @Nullable String getSubLabelKey();
    @Nullable String getSubLabelValue();
    
    String getProcessId();
    String getTaskId();
    String getOrigin();
    FeedbackQuestionnaireContent getContent();
    String getLocale();
    String getUserId();
    
    Questionnaire getQuestionnaire();
    List<String> getReplys(); 
    @Nullable String getReporterNames();
  }

  
  @JsonSerialize(as = ImmutableFeedbackHistoryEvent.class)
  @JsonDeserialize(as = ImmutableFeedbackHistoryEvent.class)
  @Value.Immutable
  interface FeedbackHistoryEvent {
    String getId();
    String getCommitId();
    String getCategoryId();
    @Nullable String getRatingId();
    @Nullable String getReplyId();
    String getJsonBodyType();
    String getJsonBody();
    ZonedDateTime getCreatedOnDate();
    String getCreatedBy();
  }
  
  
  interface FeedbackQuestionnaireQuery {
    Optional<FeedbackQuestionnaire> findOneFromTaskById(String taskId);
  }
  
  interface FeedbackQuestionnaire {
    boolean getEnabled();
    
    String getLabelKey();
    String getLabelValue();
    
    String getSubLabelKey();
    String getSubLabelValue();
    FeedbackQuestionnaireContent getContent();

    List<String> getReplys();
    String getTaskRef();
    
    Questionnaire getQuestionnaire();
    ProcessInstance getProcessInstance();
    @Nullable String getReporterNames();
    @Nullable String getCustomerTitle();
  }
  
  @JsonSerialize(as = ImmutableFeedbackQuestionnaireContent.class)
  @JsonDeserialize(as = ImmutableFeedbackQuestionnaireContent.class)
  @Value.Immutable
  interface FeedbackQuestionnaireContent {
    String getTitle(); 
    @Nullable String getMain(); 
    @Nullable String getSub(); 
    @Nullable String getQuestion();
  }

  interface FeedbackAnalyzerQuery {
    SentimentAndSubcategory getSentimentAndSubcategoryById(String id);
    Optional<SimilarFeedback> findSimilarFeedbackById(String id);
  }

  @JsonSerialize(as = ImmutableFeedbackSentimentAndSubcategoryCommand.class)
  @JsonDeserialize(as = ImmutableFeedbackSentimentAndSubcategoryCommand.class)
  @Value.Immutable
  interface FeedbackSentimentAndSubcategoryCommand {
    String getId();
    String getLanguage();
    String getText();
    JsonNode getCategories();
    String getMainCategory();
  }

  @JsonSerialize(as = ImmutableSentimentAndSubcategory.class)
  @JsonDeserialize(as = ImmutableSentimentAndSubcategory.class)
  @Value.Immutable
  interface SentimentAndSubcategory {
    Sentiment getSentiment();
    Subcategory getSubcategory();
  }

  @JsonSerialize(as = ImmutableSentiment.class)
  @JsonDeserialize(as = ImmutableSentiment.class)
  @Value.Immutable
  interface Sentiment {
    String getId();
    SentimentPolarity getSentiment();
    Float getConfidence();
    List<SentenceSentiment> getSentences();
    OffsetDateTime getTimestamp();
    String getModelVersion();
    String getModelId();
  }

  @JsonSerialize(as = ImmutableSentenceSentiment.class)
  @JsonDeserialize(as = ImmutableSentenceSentiment.class)
  @Value.Immutable
  interface SentenceSentiment {
    String getText();
    SentimentPolarity getSentiment();
    Map<String, Float> getScores();
  }

  enum SentimentPolarity {
    positive,
    negative,
    neutral,
    mixed,
    unknown
  }

  @JsonSerialize(as = ImmutableSubcategory.class)
  @JsonDeserialize(as = ImmutableSubcategory.class)
  @Value.Immutable
  interface Subcategory {
    String getId();
    String getSubcategory();
    Float getConfidence();
    List<String> getMatches();
    Map<String, Float> getScores();
    OffsetDateTime getTimestamp();
    String getModelVersion();
    String getModelId();
  }

  @JsonSerialize(as = ImmutableSimilarFeedbackCommand.class)
  @JsonDeserialize(as = ImmutableSimilarFeedbackCommand.class)
  @Value.Immutable
  interface SimilarFeedbackCommand {
    String getId();
    List<FeedbackItem> getEntries();
  }

  @JsonSerialize(as = ImmutableFeedbackItem.class)
  @JsonDeserialize(as = ImmutableFeedbackItem.class)
  @Value.Immutable
  interface FeedbackItem {
    String getId();
    String getLanguage();
    String getText();
  }

  @JsonSerialize(as = ImmutableSimilarFeedback.class)
  @JsonDeserialize(as = ImmutableSimilarFeedback.class)
  @Value.Immutable
  interface SimilarFeedback {
    String getId();
    String getModelId();
    String getModelVersion();
    OffsetDateTime getTimestamp();
    List<ProcessedFeedbackItem> getEntries();
  }

  @JsonSerialize(as = ImmutableProcessedFeedbackItem.class)
  @JsonDeserialize(as = ImmutableProcessedFeedbackItem.class)
  @Value.Immutable
  interface ProcessedFeedbackItem {
    String getId();
    String getLanguage();
    String getText();
    List<Similarity> getSimilarities();
  }

  @JsonSerialize(as = ImmutableSimilarity.class)
  @JsonDeserialize(as = ImmutableSimilarity.class)
  @Value.Immutable
  interface Similarity {
    String getId();
    String getLanguage();
    String getText();
    Float getSimilarityScore();
  }

}
