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
import java.util.Optional;

import io.dialob.api.questionnaire.Questionnaire;
import io.digiexpress.eveli.client.api.FeedbackClient.FeedbackQuestionnaire;
import io.digiexpress.eveli.client.api.FeedbackClient.FeedbackQuestionnaireContent;
import io.digiexpress.eveli.client.api.FeedbackClient.FeedbackQuestionnaireQuery;
import io.digiexpress.eveli.client.api.ImmutableFeedbackQuestionnaireContent;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.ProcessInstance;
import io.digiexpress.eveli.client.api.TaskClient.TaskComment;
import io.digiexpress.eveli.client.config.EveliPropsFeedback;
import io.resys.limaone.spi.dialob.FormDb.AnswerAndFormItem;
import io.resys.limaone.spi.dialob.FormDb.FormInstance;
import io.resys.limaone.spi.dialob.model.FormInstanceImpl;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RequiredArgsConstructor
@Slf4j
public class FeedbackQuestionnaireQueryImpl implements FeedbackQuestionnaireQuery {

  private final TaskClient taskClient;
  private final EveliPropsFeedback configProps;
  
  @Override
  public Uni<Optional<FeedbackQuestionnaire>> findOneFromTaskById(String taskIdOrRef) {
    return Uni.combine().all().unis(
        taskClient.queryTasks().getOneById(taskIdOrRef),
        taskClient.queryTaskProcesess().includeQuestionnaire().findOneByTaskId(taskIdOrRef)
      )
      .asTuple()
      .onItem().transform(tuple -> {
        
        final var task = tuple.getItem1();
        final var comments = task.getComments();

        final var process = tuple.getItem2();
        if(process.isEmpty()) {
          return Optional.empty();
        }

        final var processQuestionnaire = Optional.ofNullable(process.get().getQuestionnaire());
        if(processQuestionnaire.isEmpty()) {
          return Optional.empty();
        }

        final var questionnaire = new FormInstanceImpl(
            processQuestionnaire.get().mapTo(Questionnaire.class),
            false, Optional.empty());
        return Optional.of(new FeedbackQuestionnaireImpl(task, process.get(), comments, questionnaire, configProps));
      });
    

  }

  @RequiredArgsConstructor
  public static class FeedbackQuestionnaireImpl implements FeedbackQuestionnaire {
    private final TaskClient.Task task;
    private final ProcessInstance process;
    private final List<TaskComment> comments;
    private final FormInstance questionnaire;
    private final EveliPropsFeedback configProps;
    
    
    private Optional<AnswerAndFormItem> mainCategory;
    private Optional<AnswerAndFormItem> subCategory;
    
    private Optional<AnswerAndFormItem> title;
    private Optional<AnswerAndFormItem> question;

    public Optional<AnswerAndFormItem> getMainCat() {
      if(mainCategory != null) {
        return mainCategory;
      }
      this.mainCategory = findAnswer(configProps.getCategoryMain()); 
      return this.mainCategory;
    }
    public Optional<AnswerAndFormItem> getSubCat() {
      if(subCategory != null) {
        return subCategory;
      }
      this.subCategory = findAnswer(configProps.getCategorySub());
      return this.subCategory;
    }
    public Optional<AnswerAndFormItem> getTitle() {
      if(title != null) {
        return title;
      }
      this.title = findAnswer(configProps.getQuestionTitle());
      return this.title;
    }
    public Optional<AnswerAndFormItem> getQuestion() {
      if(question != null) {
        return question;
      }
      this.question = findAnswer(configProps.getQuestion());
      return this.question;
    }
    public Optional<AnswerAndFormItem> findAnswer(List<String> answerId) {
      return questionnaire.getAnswerAndFormItems().stream()
        .filter(a -> answerId.contains(a.getAnswer().getId()))
        .findFirst();
    }
    @Override
    public boolean getEnabled() {
      return configProps.getForms().contains(process.getFormName());
    }
    @Override
    public String getLabelKey() {
      return getMainCat().map(e -> e.getAnswer().getValue()).map(Object::toString).orElse("-");
    }
    @Override
    public String getLabelValue() {
      return getMainCat().map(e -> e.getValueSetLabel().orElse("-")).orElse("-");
    }
    @Override
    public String getSubLabelKey() {
      return getSubCat().map(e -> e.getAnswer().getValue()).map(Object::toString).orElse("-");
    }
    @Override
    public String getSubLabelValue() {
      return getSubCat().map(e -> e.getValueSetLabel().orElse(null)).orElse("-");
    }
    @Override
    public FeedbackQuestionnaireContent getContent() {
      
      final String mainCat = getMainCat()
          .map(proxy -> getSelectionAnswer(proxy))
          .orElse("- no main category: '" + String.join(",", configProps.getCategoryMain()) + "' -");
      
      return ImmutableFeedbackQuestionnaireContent.builder()
          .title(getTitle().map(e -> getStringAnswer(e)).orElse("- no title -"))
          .main(mainCat)
          .sub(getSubCat().map(e -> getSelectionAnswer(e)).orElse(""))
          .question(getQuestion().map(e -> getStringAnswer(e)).orElse("- no question -"))
          .build(); 
    }
    
    
    private String getStringAnswer(AnswerAndFormItem proxyAnswer) {
      try {
        final var answer = Optional.ofNullable(proxyAnswer.getAnswer().getValue()).map(Object::toString).orElse("-not-answered-");
        return answer;
      } catch(Exception e) {
        log.error("Failed to resolve value in dialob answer, tried to parse text, error: {}!", e.getMessage(), e);
        return "failed-to-resolve-string";
      }
    }
    private String getSelectionAnswer(AnswerAndFormItem proxyAnswer) {
      try {
        final var answer = proxyAnswer.getValueSetLabel().orElse("");
        return answer;
      } catch(Exception e) {
        log.error("Failed to resolve value in dialob answer, tried to parse selection, error: {}!", e.getMessage(), e);
        return "failed-to-resolve-selection";
      }
    }
    
    @Override
    public String getCustomerTitle() {
      final var title = getTitle();
      if(title.isEmpty()) {
        return null;
      }
      
      final var answer = title.get().getAnswer().getValue();
      if(answer == null) {
        return null;
      }

      return answer.toString();
    }
    
    @Override    
    public List<String> getReplys() {
      return comments.stream()
        .filter(reply -> Boolean.TRUE.equals(reply.getExternal()))
        .sorted((s1, s2) -> s1.getCreated().compareTo(s2.getCreated()))
        .map(this::formatReply)
        .toList();
    }
    @Override    
    public String getReporterNames() {
      final var isUsernameAllowed = findAnswer(configProps.getUsernameAllowed())
        .map(a -> a.getAnswer().getValue())
        .filter(a -> a != null)
        .map(a -> a.toString().toLowerCase().trim())
        .orElse("false").equals("true");
      if(!isUsernameAllowed) {
        return null;
      }
      
      
      
      
      final var usernames = this.configProps.getUsername().stream()
        .map(name -> this.questionnaire.context(name))
        .filter(e -> e != null)
        .map(e -> e.toString().trim())
        .filter(e -> !e.isEmpty())
        .toList();
      
      final var reporterNames = String.join(",", usernames);
      
      if(reporterNames.isBlank()) {
        return null;
      }
      
      return reporterNames;
    }
    @Override
    public Questionnaire getQuestionnaire() {
      return this.questionnaire.getQuestionnaire();
    }
    @Override
    public ProcessInstance getProcessInstance() {
      return process;
    }
    
    private String formatReply(TaskComment comment) {
      return new StringBuilder().append(comment.getCommentText()).toString();
    }
    @Override
    public String getTaskRef() {
      return task.getTaskRef();
    } 
  }
}
