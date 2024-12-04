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

import org.immutables.value.Value;

import io.dialob.api.form.Form;
import io.dialob.api.questionnaire.Questionnaire;
import io.digiexpress.eveli.client.api.FeedbackClient.FeedbackTemplate;
import io.digiexpress.eveli.client.api.FeedbackClient.FeedbackTemplateQuery;
import io.digiexpress.eveli.client.api.ImmutableFeedbackTemplate;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.TaskComment;
import io.digiexpress.eveli.client.spi.asserts.ProcessAssert;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FeedbackTemplateQueryImpl implements FeedbackTemplateQuery {

  private final TaskClient taskClient;
  private final ProcessClient processClient;
  private final QuestionnaireCategoryExtractor extractor;
  
  @FunctionalInterface
  public interface QuestionnaireCategoryExtractor {
    Optional<QuestionnaireCategoryExtract> apply(Questionnaire q, Form form);
  }

  @Value.Immutable
  public interface QuestionnaireCategoryExtract {
    String getLabelKey();
    String getLabelValue();
    
    @Nullable String getSubLabelKey();
    @Nullable String getSubLabelValue();
    @Nullable String getContent();
  }
  
  
  @Override
  public FeedbackTemplate getOneByTaskId(String taskId, String userId) {
    final var task = taskClient.queryTasks().getOneById(Long.parseLong(taskId));
    final var comments = taskClient.queryComments().findAllByTaskId(task.getId());
    
    final var process = processClient.queryInstances().findOneByTaskId(task.getId());
    ProcessAssert.isTrue(process.isPresent(), () -> "Process must be synced!");
    
    final var processQuestionnaire = processClient.queryProcessQuestionnaire().findOneByTaskId(task.getId());
    ProcessAssert.isTrue(processQuestionnaire.isPresent(), () -> "Questionnaire must be synced!");
    
    final var questionnaire = processQuestionnaire.get().mapTo(Questionnaire.class);
    
    
    final var replys = comments.stream()
      .filter(reply -> Boolean.TRUE.equals(reply.getExternal()))
      .sorted((s1, s2) -> s1.getCreated().compareTo(s2.getCreated()))
      .map(this::formatReply)
      .toList();
    
    final var extract = extractor.apply(questionnaire, null);
    
    return ImmutableFeedbackTemplate.builder()
        .addAllReplys(replys)
        .questionnaire(questionnaire)
        .processId(process.get().getId().toString())
        .content(formatContent(extract))
        
        .locale(questionnaire.getMetadata().getLanguage())
        .origin(questionnaire.getClass().getSimpleName().toUpperCase())

        .labelKey(extract.map(e -> e.getLabelKey()).orElse("-"))
        .labelValue(extract.map(e -> e.getLabelValue()).orElse("-"))
        
        .subLabelKey(extract.map(e -> e.getSubLabelKey()).orElse("-"))
        .subLabelValue(extract.map(e -> e.getSubLabelValue()).orElse("-"))
        
        .userId(userId)
        
        .build();
  }
  
  
  private String formatContent(Optional<QuestionnaireCategoryExtract> extract) {
    if(extract.isPresent() && extract.get().getContent() != null) {
      return extract.get().getContent();
    }
    
    
    return new StringBuilder()
        .append("## ").append(extract.map(e -> e.getLabelValue()).orElse("- ")).append("  ")
        .append(System.lineSeparator())
        .append(System.lineSeparator())

        .append("## ").append(extract.map(e -> e.getSubLabelValue()).orElse("- ")).append("  ")
        .append(System.lineSeparator())
        .append(System.lineSeparator())
        
        .toString();
  }
  
  private String formatReply(TaskComment comment) {
    return new StringBuilder()
      .append(comment.getCommentText()).append("  ")
      .append(System.lineSeparator())
      .append(System.lineSeparator())
      .toString();
    
  }
  
}
