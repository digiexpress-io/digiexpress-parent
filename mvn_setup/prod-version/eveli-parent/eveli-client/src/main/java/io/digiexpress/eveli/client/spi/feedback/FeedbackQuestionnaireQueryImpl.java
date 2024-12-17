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
import io.digiexpress.eveli.client.api.FeedbackClient.FeedbackQuestionnaireQuery;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.ProcessClient.ProcessInstance;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.Task;
import io.digiexpress.eveli.client.api.TaskClient.TaskComment;
import io.digiexpress.eveli.client.config.EveliPropsFeedback;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.dialob.api.DialobClient.ProxyAnswer;
import io.digiexpress.eveli.dialob.spi.QuestionnaireWrapperImpl;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FeedbackQuestionnaireQueryImpl implements FeedbackQuestionnaireQuery {

  private final TaskClient taskClient;
  private final DialobClient dialobClient;
  private final ProcessClient processClient;
  private final EveliPropsFeedback configProps;
  
  @Override
  public Optional<FeedbackQuestionnaire> findOneFromTaskById(String taskId) {
    final var task = taskClient.queryTasks().getOneById(Long.parseLong(taskId));
    final var comments = taskClient.queryComments().findAllByTaskId(task.getId());

    final var process = processClient.queryInstances().findOneByTaskId(task.getId());
    if(process.isEmpty()) {
      return Optional.empty();
    }

    final var processQuestionnaire = processClient.queryProcessQuestionnaire().findOneByTaskId(task.getId());
    if(processQuestionnaire.isEmpty()) {
      return Optional.empty();
    }

    final var questionnaire = processQuestionnaire.get().mapTo(Questionnaire.class);
    return Optional.of(new FeedbackQuestionnaireImpl(task, dialobClient, process.get(), comments, questionnaire, configProps));
  }

  @RequiredArgsConstructor
  public static class FeedbackQuestionnaireImpl implements FeedbackQuestionnaire {
    @SuppressWarnings("unused")
    private final Task task;
    private final DialobClient dialobClient;
    private final ProcessInstance process;
    private final List<TaskComment> comments;
    private final Questionnaire questionnaire;
    private final EveliPropsFeedback configProps;
    
    
    private Optional<ProxyAnswer> mainCategory;
    private Optional<ProxyAnswer> subCategory;
    
    private Optional<ProxyAnswer> title;
    private Optional<ProxyAnswer> question;

    public Optional<ProxyAnswer> getMainCat() {
      if(mainCategory != null) {
        return mainCategory;
      }
      this.mainCategory = findAnswer(configProps.getCategoryMain()); 
      return this.mainCategory;
    }
    public Optional<ProxyAnswer> getSubCat() {
      if(subCategory != null) {
        return subCategory;
      }
      this.subCategory = findAnswer(configProps.getCategorySub());
      return this.subCategory;
    }
    public Optional<ProxyAnswer> getTitle() {
      if(title != null) {
        return title;
      }
      this.title = findAnswer(configProps.getQuestionTitle());
      return this.title;
    }
    public Optional<ProxyAnswer> getQuestion() {
      if(question != null) {
        return question;
      }
      this.question = findAnswer(configProps.getQuestion());
      return this.question;
    }
    public Optional<ProxyAnswer> findAnswer(List<String> answerId) {
      return questionnaire.getAnswers().stream()
        .filter(a -> answerId.contains(a.getId())).map(a -> dialobClient.proxyAnswer(questionnaire, a))
        .findFirst();
    }
    
    
    @Override
    public boolean getEnabled() {
      return configProps.getForms().contains(process.getFormName());
    }
    @Override
    public String getLabelKey() {
      return getMainCat().map(e -> e.getAnswer().getValue().toString()).orElse("-");
    }
    @Override
    public String getLabelValue() {
      return getMainCat().map(e -> e.getValueSetLabel().orElse("-")).orElse("-");
    }
    @Override
    public String getSubLabelKey() {
      return getSubCat().map(e -> e.getAnswer().getValue()).map(e -> e.toString()).orElse("-");
    }
    @Override
    public String getSubLabelValue() {
      return getSubCat().map(e -> e.getValueSetLabel().orElse(null)).orElse("-");
    }
    @Override
    public String getContent() {
      return new StringBuilder()
          .append(getTitle().map(e -> formatText(e)).orElse("- no title -"))
          .append(formatSelection(getMainCat().get()))
          .append(getSubCat().map(e -> formatSelection(e)).orElse(""))
          .append(getQuestion().map(e -> formatText(e)).orElse("- no question -"))
          .toString();
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
      
      
      final var wrapper = new QuestionnaireWrapperImpl(this.questionnaire);
      
      final var usernames = this.configProps.getUsername().stream()
        .map(name -> wrapper.context(name))
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
      return questionnaire;
    }
    @Override
    public ProcessInstance getProcessInstance() {
      return process;
    }
    
    private String formatReply(TaskComment comment) {
      return new StringBuilder().append(comment.getCommentText()).toString();
    } 
    private String formatText(ProxyAnswer proxyAnswer) {
      final var lang = questionnaire.getMetadata().getLanguage();
      final var question = proxyAnswer.getFormItem().getLabel().get(lang);
      final var answer = proxyAnswer.getAnswer().getValue().toString();
      return new StringBuilder()
        .append("#### ").append(question).append("  ").append(System.lineSeparator())
        .append(answer).append("  ").append(System.lineSeparator()).append(System.lineSeparator())
      .toString();
    }
    private String formatSelection(ProxyAnswer proxyAnswer) {
      final var lang = questionnaire.getMetadata().getLanguage();
      final var question = proxyAnswer.getFormItem().getLabel().get(lang);
      final var answer = proxyAnswer.getValueSetLabel().orElse("");
      
      return new StringBuilder()
        .append("#### ").append(question).append("  ").append(System.lineSeparator())
        .append(answer).append("  ").append(System.lineSeparator()).append(System.lineSeparator())
      .toString();
    }
  }
}
