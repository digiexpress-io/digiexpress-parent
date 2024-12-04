package io.digiexpress.eveli.client.spi.feedback;

import java.util.Arrays;
import java.util.List;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormItem;
import io.dialob.api.questionnaire.Answer;
import io.dialob.api.questionnaire.Questionnaire;
import io.digiexpress.eveli.client.spi.feedback.FeedbackTemplateQueryImpl.QuestionnaireCategoryExtract;
import io.digiexpress.eveli.client.spi.feedback.FeedbackTemplateQueryImpl.QuestionnaireCategoryExtractor;
import io.digiexpress.eveli.dialob.spi.DialobClientImpl;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class QuestionnaireCategoryExtractorImpl implements QuestionnaireCategoryExtractor {

  private List<String> main = Arrays.asList("mainList");
  private List<String> sub = Arrays.asList("cityServiceGroup", "preschoolEducationGroup", "cityServiceMainList");
  
  private final ObjectMapper objectMapper;
  
  @Override
  public Optional<QuestionnaireCategoryExtract> apply(Questionnaire q, Form form) {
    final var mainAnswer = q.getAnswers().stream().filter(a -> main.contains(a.getId())).map(a -> proxyAnswer(q, a)).findFirst();
    final var subAnswer = q.getAnswers().stream().filter(a -> sub.contains(a.getId())).map(a -> proxyAnswer(q, a)).findFirst();
    
    if(mainAnswer.isEmpty()) {
      return Optional.empty();      
    }

    

    
    return Optional.of(ImmutableQuestionnaireCategoryExtract.builder()
      .labelKey(mainAnswer.get().getAnswer().getValue().toString())
      .labelValue(mainAnswer.get().getValueSetLabel().orElse(""))
      
      .subLabelKey(subAnswer.map(e -> e.getAnswer().getValue().toString()).orElse(null))
      .subLabelValue(subAnswer.map(e -> e.getValueSetLabel().orElse(null)).orElse(null))
      
      .content(new StringBuilder()
          .append(formatFormItem(mainAnswer.get(), q))
          .append(subAnswer.map(e -> formatFormItem(e, q)).orElse(""))
          .toString())
      
      .build());
  }
  
  
  private String formatFormItem(ProxyAnswer proxyAnswer, Questionnaire q) {
    final var lang = q.getMetadata().getLanguage();
    final var question = proxyAnswer.getFormItem().getLabel().get(lang);
    final var answer = proxyAnswer.getValueSetLabel().orElse("");
    
    return new StringBuilder()
    .append("## ").append(question).append(": ").append(answer).append(false).append("  ").append(System.lineSeparator()).append(System.lineSeparator())

    .toString();
  }
  
  private ProxyAnswer proxyAnswer(Questionnaire q, Answer answer) {
    final var formItem = DialobClientImpl.LOOKUP(answer, q, objectMapper);
    final var valueSetLabel = Optional.ofNullable(formItem.getValueSetId())
      .map(vsId -> {
        
        final var v = q.getValueSets().stream().filter(vs -> vs.getId().equals(vsId))
            .findFirst();

        if(v.isEmpty()) {
          return null;
        }
        
        final var entry = v.get().getEntries().stream()
            .filter(e -> e.getKey().equals(q.getMetadata().getLanguage()))
            .findFirst();
        
        return entry.get().getValue();
      });
    
    return ProxyAnswer.builder()
        .valueSetLabel(valueSetLabel)
        .formItem(formItem)
        .answer(answer)
        .build();
  }


  @Data @Builder
  private static class ProxyAnswer {
    private final Answer answer;
    private final FormItem formItem;
    private final Optional<String> valueSetLabel;
   }
}
