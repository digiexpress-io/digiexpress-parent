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

import java.util.Optional;

import io.digiexpress.eveli.client.api.FeedbackClient.FeedbackQuestionnaire;
import io.digiexpress.eveli.client.api.FeedbackClient.FeedbackQuestionnaireQuery;
import io.digiexpress.eveli.client.api.FeedbackClient.FeedbackTemplate;
import io.digiexpress.eveli.client.api.FeedbackClient.FeedbackTemplateQuery;
import io.digiexpress.eveli.client.api.ImmutableFeedbackTemplate;
import io.digiexpress.eveli.client.spi.asserts.ProcessAssert;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FeedbackTemplateQueryImpl implements FeedbackTemplateQuery {
  private final FeedbackQuestionnaireQuery query;

  @Override
  public Optional<FeedbackTemplate> findOneByTaskId(String taskId, String userId) {
    final var questionnaire = query.findOneFromTaskById(taskId);
    if(questionnaire.isPresent()) {
      return Optional.of(map(questionnaire.get(), taskId, userId));  
    }
    return Optional.empty();
  }
  
  @Override
  public FeedbackTemplate getOneByTaskId(String taskId, String userId) {
    final var questionnaire = query.findOneFromTaskById(taskId);
    ProcessAssert.isTrue(questionnaire.isPresent(), () -> "Process must be synced and form enabled for feedback!");
    return map(questionnaire.get(), taskId, userId);
  }
  
  
  public FeedbackTemplate map(FeedbackQuestionnaire questionnaire, String taskId, String userId) {
    return ImmutableFeedbackTemplate.builder()
        .addAllReplys(questionnaire.getReplys())
        .questionnaire(questionnaire.getQuestionnaire())
        .processId(questionnaire.getProcessInstance().getId().toString())

        .reporterNames(questionnaire.getReporterNames())
        
        .locale(questionnaire.getQuestionnaire().getMetadata().getLanguage())
        .origin(questionnaire.getClass().getSimpleName().toUpperCase())

        .labelKey(questionnaire.getLabelKey())
        .labelValue(questionnaire.getLabelValue())
        
        .subLabelKey(questionnaire.getSubLabelKey())
        .subLabelValue(questionnaire.getSubLabelValue())
        
        .userId(userId)
        .content(questionnaire.getContent())
        
        .build();
  }

}
