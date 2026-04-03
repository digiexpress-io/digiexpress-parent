package io.resys.limaone.spi.dialob.model;

/*-
 * #%L
 * limaone-compiler
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

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormItem;
import io.dialob.api.form.FormValueSetEntry;
import io.dialob.api.proto.ValueSetEntry;
import io.dialob.api.questionnaire.Answer;
import io.dialob.api.questionnaire.ContextValue;
import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.api.questionnaire.VariableValue;
import io.resys.limaone.spi.dialob.FormDb.AnswerAndFormItem;
import io.resys.limaone.spi.dialob.FormDb.FormInstance;
import io.resys.limaone.spi.dialob.builders.FormInstanceQueryImpl;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class FormInstanceImpl implements FormInstance {
  private final Questionnaire questionnaire;
  private final boolean formLoaded;
  private final Optional<Form> form;
  private static final Comparator<ValueSetEntry> langComparator = new Comparator<ValueSetEntry>() {
    @Override
    public int compare(ValueSetEntry o1, ValueSetEntry o2) {
      return o1.getKey().compareTo(o2.getKey());
    }
  };
  
  private List<AnswerAndFormItem> lookups;
  
  
  @Override
  public Questionnaire getQuestionnaire() {
    return questionnaire;
  }

  @Override
  public Optional<String> getLookupPrefix() {
    if (!formLoaded) {
      return Optional.empty();
    }
    return Optional.of(FormInstanceQueryImpl.LOOKUP);
  }

  @Override
  public boolean isFormLoaded() {
    return formLoaded;
  }

  @Override
  public FormItem getFormItem(Answer answer) {
    try {
      final var valueset = questionnaire.getValueSets().stream().filter(p -> p.getId().equals(FormInstanceQueryImpl.LOOKUP + answer.getId())).findFirst().get();
      final var formItem = valueset.getEntries().iterator().next().getValue();
      
      return new JsonObject(formItem).mapTo(FormItem.class);
    } catch(Exception e) {
      // ignore failure on purpose
      log.error("Failed to resolve form: {} item for answer: {}, error: {}", 
          questionnaire.getMetadata().getLabel(), 
          answer.getId(),
          e.getMessage(), 
          e);
      return null;
    }
  }

  @Override
  public Optional<String> encodeFormPrettily() {
    if(!this.formLoaded) {
      return Optional.empty();
    }
    
    final var encodeFormPrettily = new DialobFormPrettyPrinter().printForm(JsonObject.mapFrom(this.form.get()));
    return Optional.of(encodeFormPrettily);
  }

  @Override
  public Answer answer(String name) {
    return findEntry(questionnaire.getAnswers(), name);
  }
  
  @Override
  public String text(String name) {
    Answer answer = answer(name);
    if(answer == null || answer.getValue() == null) {
      return "";
    }
    return answer.getValue().toString();
  }

  @Override
  public BigDecimal decimal(String name) {
    Answer answer = findEntry(questionnaire.getAnswers(), name);
    if (answer == null || answer.getValue() == null) {
      return BigDecimal.ZERO; 
    }
    assert answer.getType().equals("INTEGER") || answer.getType().equals("DECIMAL");
    return new BigDecimal(answer.getValue().toString());
  }
  
  @Override
  public boolean bool(String name) {
    Answer answer = findEntry(questionnaire.getAnswers(), name);
    if (answer == null || answer.getValue() == null) {
      return false;
    }
    assert answer.getType().equals("BOOLEAN");
    return Boolean.TRUE.equals(answer.getValue());
  }
  
  @Override
  public Object variable(String name) {
    VariableValue var = findVariable(questionnaire.getVariableValues(), name);
    return var.getValue();
  }
  
  @Override
  public Object context(String name) {
    ContextValue result = findContext(questionnaire.getContext(), name);
    return result == null ? null : result.getValue();
  }
  
  private Answer findEntry(List<Answer> list, String entryKey) {
    return list.stream().filter(it -> it.getId().equals(entryKey)).findFirst().orElse(null);
  }
  
  private VariableValue findVariable(List<VariableValue> variableValues, String key) {
    return variableValues.stream().filter(it -> it.getId().equals(key)).findFirst().orElse(null);
  }
  
  private ContextValue findContext(List<ContextValue> contextValues, String key) {
    return contextValues.stream().filter(it -> it.getId().equals(key)).findFirst().orElse(null);
  }

  public Optional<Form> getForm() {
    return form;
  }
  
  
  @Override
  public List<AnswerAndFormItem> getAnswerAndFormItems() {
    if(this.lookups != null) {
      return this.lookups;
    }
    this.lookups = questionnaire.getAnswers().stream().map(this::proxyAnswer).toList();
    return this.lookups;
  }
  
  private AnswerAndFormItem proxyAnswer(Answer answer) {
    final Questionnaire q = this.questionnaire;
    final var formItem = getFormItem(answer);
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
        
        if(entry.isEmpty()) {
          final String anyValue = v.get().getEntries().stream()
              .sorted(langComparator)
              .findFirst()
              .map(e -> e.getValue())
              .orElse("locale value missing for: " + answer.getValue());
          return anyValue;
        }
        
        return entry.get().getValue();
      });
    
    return ImmutableAnswerAndFormItem.builder()
        .valueSetLabel(valueSetLabel)
        .formItem(formItem)
        .answer(answer)
        .build();
  }
  
  @AllArgsConstructor
  @Value @Builder
  public static class ImmutableAnswerAndFormItem implements AnswerAndFormItem {
    Answer answer;
    FormItem formItem;
    Optional<String> valueSetLabel;
    Optional<FormValueSetEntry> valueSetEntry;
  }
}
