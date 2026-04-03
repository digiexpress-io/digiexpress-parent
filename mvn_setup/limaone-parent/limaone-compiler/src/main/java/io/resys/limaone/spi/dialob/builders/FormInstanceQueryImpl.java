package io.resys.limaone.spi.dialob.builders;

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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormItem;
import io.dialob.api.form.FormValueSet;
import io.dialob.api.form.FormValueSetEntry;
import io.dialob.api.proto.ValueSet;
import io.dialob.api.proto.ValueSetEntry;
import io.dialob.api.questionnaire.Answer;
import io.dialob.api.questionnaire.Questionnaire;
import io.resys.limaone.spi.dialob.FormDb.FormInstance;
import io.resys.limaone.spi.dialob.FormDb.FormInstanceQuery;
import io.resys.limaone.spi.dialob.FormDbImpl.FormDbProps;
import io.resys.limaone.spi.dialob.model.FormInstanceImpl;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FormInstanceQueryImpl implements FormInstanceQuery {
  public static String LOOKUP = "__MOD_";
  
  private final FormDbProps db;
  private boolean includeForm = false;

  @Override
  public FormInstanceQuery includeForm(boolean includeForm) {
    this.includeForm = includeForm;
    return this;
  }
  
  @Override
  public Uni<Optional<FormInstance>> findOne(String questionnaireId) {
    Objects.requireNonNull(questionnaireId, () -> "questionnaireId must be defined");
    
    final var questionnaireUni = db.getFormHttp()
      .httpQuery()
      .uri(uri -> uri.append("questionnaires").append(questionnaireId).build())
      .method(Questionnaire.class)
      .findOneObject();
    
    if(!includeForm) {
      return questionnaireUni
        .onItem().transformToUni(questionnaire -> {
          if(questionnaire.isEmpty()) { 
            return Uni.createFrom().item(Optional.empty());
          }
          final FormInstance instance = new FormInstanceImpl(questionnaire.get(), includeForm, Optional.empty());
          return Uni.createFrom().item(Optional.of(instance));
        });
    }
    
    return questionnaireUni.map(e -> e.get())
        .onItem().transformToUni(questionnaire -> new FormQueryImpl(db)
            .formId(questionnaire.getMetadata().getFormId())
            .findOne()
            .onItem().transform(form -> Tuple2.of(form, questionnaire)))
        .onItem().transform(tuple -> Optional.of(join(tuple.getItem2(), tuple.getItem1()))); 
  }

  @Override
  public Optional<FormInstance> findOneSync(String questionnaireId) {
    final var workerTimeout = Duration.ofMinutes(1);
    final var workerPool = Infrastructure.getDefaultWorkerPool();
    return this.findOne(questionnaireId)
      .runSubscriptionOn(workerPool)
      .await().atMost(workerTimeout);
  }

  @Override
  public Uni<FormInstance> getOne(String questionnaireId) {
    Objects.requireNonNull(questionnaireId, () -> "questionnaireId must be defined");
    
    final var questionnaireUni = db.getFormHttp()
      .httpQuery()
      .uri(uri -> uri.append("questionnaires").append(questionnaireId).build())
      .method(Questionnaire.class)
      .getOneObject();
    
    if(!includeForm) {
      return questionnaireUni
          .onItem().transform(questionnaire -> new FormInstanceImpl(questionnaire, includeForm, Optional.empty()));
    }
    
    return questionnaireUni
        .onItem().transformToUni(questionnaire -> new FormQueryImpl(db)
            .formId(questionnaire.getMetadata().getFormId())
            .findOne()
            .onItem().transform(form -> Tuple2.of(form, questionnaire)))
        .onItem().transform(tuple -> join(tuple.getItem2(), tuple.getItem1())); 
  }
  
  public FormInstance join(Questionnaire questionnaire, Optional<Form> rawForm) {
    final var form = rawForm.orElseThrow(() -> new RuntimeException("Form query failed to find any results"));
    final var valuesets = form.getValueSets().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    
    // re-map answers with form data
    final var answers = questionnaire.getAnswers().stream()
    .map(answer -> {
      var meta = form.getData().get(answer.getId());      
      if (meta == null) {
        // rowgroup element similar to 'rowgroup1.1.text2' has meta info as last element in dot split id.
        meta = Stream.of(answer.getId().split("\\."))
          .reduce((first, second) -> second)
          .map(lastField->form.getData().get(lastField)).orElse(null);
      }
      // rowgroup row similar 'rowgroup1.1' has no metadata, check for meta not being null
      final var valueset = meta == null || meta.getValueSetId() == null ? null : valuesets.get(meta.getValueSetId());

  
      final Optional<FormValueSetEntry> valuesetEntry = valueset == null ? Optional.empty() : valueset.getEntries().stream().filter(entry -> entry.getId().equals(answer.getValue())).findFirst();    
      return new AnswerAndValueSet(answer, meta, Optional.ofNullable(valueset), valuesetEntry);
    })
    .collect(Collectors.toList());
    
    
    // group answers by value set id
    final var answerValueSets = answers.stream()
        .filter(a -> a.valueSet().isPresent() && a.valueSetEntry().isPresent())
        .collect(Collectors.groupingBy(a -> a.valueSet().get().getId()));
    
    // apply corrections to value set 
    final var correctedValueSets = new ArrayList<ValueSet>();
    for(final var valueSet : questionnaire.getValueSets()) {
      final var mods = answerValueSets.get(valueSet.getId());
      final ValueSet merged = merge(valueSet, mods, questionnaire);
      correctedValueSets.add(merged);
    }
    
    // add answer meta data
    try {
      for(final var answer : answers) {
        correctedValueSets.add(new ValueSet.Builder()
            .id(LOOKUP + answer.item().getId())
            .addEntries(new ValueSetEntry.Builder()
                .key(LOOKUP)
                .value(JsonObject.mapFrom(answer.item()).encode())
                .build())
            .build());
      }
    } catch(Exception e) {
      // ignore failure on purpose
    }
    return new FormInstanceImpl(
        new Questionnaire.Builder().from(questionnaire).valueSets(correctedValueSets).build(), 
        includeForm, rawForm);
  }
  
  private ValueSet merge(ValueSet valueset, List<AnswerAndValueSet> valuesetCorrections, Questionnaire questionnaire) {
    if(valuesetCorrections == null) {
      return valueset;
    }
    final var entries = Optional.ofNullable(valueset.getEntries()).orElse(Collections.emptyList());
    final var correction = new ValueSet.Builder().id(valueset.getId()).entries(entries);
    
    final var existing = entries.stream().map(e -> e.getKey()).toList();
    for(final var corrections : valuesetCorrections) {
      final var key = corrections.answer().getValue().toString();
      if(existing.contains(key)) {
        continue;
      }
      
      final var labels = corrections.valueSetEntry().get().getLabel();
      
      for(final var label : labels.entrySet()) {
        correction.addEntries(new ValueSetEntry.Builder()
            .key(label.getKey())
            .value(label.getValue())
            .build());
      }
    }
    
    return correction.build();
  }

  @Override
  public FormInstance getOneSync(String questionnaireId) {
    final var workerTimeout = Duration.ofMinutes(1);
    final var workerPool = Infrastructure.getDefaultWorkerPool();
    return this.getOne(questionnaireId)
      .runSubscriptionOn(workerPool)
      .await().atMost(workerTimeout);
  }
  
  public record AnswerAndValueSet(
      Answer answer,
      FormItem item,
      Optional<FormValueSet> valueSet,
      Optional<FormValueSetEntry> valueSetEntry) {}
}
