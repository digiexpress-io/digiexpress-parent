/*
 * Copyright © 2015 - 2021 ReSys (info@dialob.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dialob.client.tests.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.tuple;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import io.dialob.api.form.FormItem;
import io.dialob.api.form.ImmutableForm;
import io.dialob.api.form.ImmutableFormMetadata;
import io.dialob.api.form.ImmutableFormValueSet;
import io.dialob.api.form.ImmutableFormValueSetEntry;
import io.dialob.api.proto.Action;
import io.dialob.api.proto.ValueSetEntry;
import io.dialob.client.tests.steps.support.AbstractWebSocketTests;

public class QuestionnaireLocaleUpdateTest extends AbstractWebSocketTests {

  @SuppressWarnings({ "unused", "deprecation" })
  @Test
  public void updateFormLocaleOnline() throws Exception {

    ImmutableForm.Builder updateFormOnlineBuilder = ImmutableForm.builder();
    Consumer<ImmutableForm.Builder> initializer = formBuilder -> {
      FormItem formItemBean = addQuestionnaire(formBuilder, builder -> builder.addClassName("main-questionnaire").addItems("g1") );
      addItem(formBuilder, "g1", builder -> builder.type("group").putLabel("en", "Group").putLabel("fi","Ryhmä").addItems("q1","g2"));
      addItem(formBuilder, "g2", builder -> builder.type("group").putLabel("en", "Group 2").putLabel("fi","Ryhmä 2").addItems("q2"));
      addItem(formBuilder, "q1", builder -> builder.type("text").putLabel("en", "Question").putLabel("fi","Kysymys").putDescription("en","Hard one").putDescription("fi","Vaikea"));
      addItem(formBuilder, "q2", builder -> builder.type("text").putLabel("en", "Question 2").putLabel("fi","Kysymys 2").activeWhen("language = 'fi'"));
      formBuilder.addValueSets(ImmutableFormValueSet.builder()
        .id("vs1")
        .addEntries(
          ImmutableFormValueSetEntry.builder()
            .id("choice-1")
            .putLabel("en","Choice 1")
            .putLabel("fi","Valinta 1")
            .build(),
          ImmutableFormValueSetEntry.builder()
            .id("choice-2")
            .putLabel("en","Choice 2")
            .putLabel("fi","Valinta 2")
            .build()
        ).build());
      formBuilder.metadata(ImmutableFormMetadata.builder().languages(Arrays.asList("en","fi")).label("Kysely").build());
    };
    initializer.accept(updateFormOnlineBuilder);
    updateFormOnlineBuilder
      .id("updateFormLocaleOnline")
      .rev("1")
      .build();

    final ImmutableForm form1 = updateFormOnlineBuilder.build();
    save(form1);

    createAndOpenSession("updateFormLocaleOnline")
      .expectActivated()
      .expectActions(actions -> {
        assertThat(actions.getActions())
          .extracting("type",                     "item.id", "item.type", "item.label", "item.description","value").containsExactlyInAnyOrder(
          tuple(Action.Type.RESET,           null,            null,             null,     null, null),
          tuple(Action.Type.LOCALE,          null,            null,             null,     null, "en"),
          tuple(Action.Type.ITEM, "questionnaire", "questionnaire",         "Kysely",     null, null),
          tuple(Action.Type.ITEM,            "g1",         "group",          "Group",     null, null),
          tuple(Action.Type.ITEM,            "g2",         "group",          "Group 2",     null, null),
          tuple(Action.Type.ITEM,            "q1",          "text",       "Question",     "Hard one", null),
          tuple(Action.Type.VALUE_SET,       null,            null,             null,             null, null)
        );
        assertThat(actions.getActions()).extracting(action -> action.getValueSet() == null ? null : action.getValueSet().getEntries().stream().map(ValueSetEntry::getValue).collect(Collectors.toList()))
          .containsOnly(
            null,
            null,
            null,
            null,
            null,
            Arrays.asList("Choice 1", "Choice 2")
          );
      })
      .next().setLocale("fi")
      .expectActions(actions -> {
        assertThat(actions.getActions())  // We'll get a full form because FormUpdatedEvent trigger session eviction from cache
          .extracting("type",                     "item.id", "item.type", "item.label", "item.description","value").containsExactlyInAnyOrder(
          tuple(Action.Type.LOCALE,          null,            null,             null,     null, "fi"),
          tuple(Action.Type.ITEM,            "q1",          "text",       "Kysymys",     "Vaikea", null),
          tuple(Action.Type.ITEM,            "q2",          "text",       "Kysymys 2",    null, null),
          tuple(Action.Type.ITEM,            "g1",         "group",          "Ryhmä",     null, null),
          tuple(Action.Type.ITEM,            "g2",         "group",          "Ryhmä 2",     null, null),
          tuple(Action.Type.VALUE_SET,       null,            null,             null,             null, null)
        );
        assertThat(actions.getActions()).extracting(action -> action.getValueSet() == null ? null : action.getValueSet().getEntries().stream().map(ValueSetEntry::getValue).collect(Collectors.toList()))
          .containsOnly(
            null,
            null,
            null,
            null,
            null,
            Arrays.asList("Valinta 1", "Valinta 2")
          );
      })
      .next().setLocale("en")
      .expectActions(actions -> {
        assertThat(actions.getActions())  // We'll get a full form because FormUpdatedEvent trigger session eviction from cache
          .extracting("type",                     "item.id", "item.type", "item.label", "item.description","value").containsExactlyInAnyOrder(
          tuple(Action.Type.REMOVE_ITEMS,    null,            null,             null,     null, null),
          tuple(Action.Type.LOCALE,          null,            null,             null,     null, "en"),
          tuple(Action.Type.ITEM,            "q1",          "text",       "Question",     "Hard one", null),
          tuple(Action.Type.ITEM,            "g1",         "group",          "Group",     null, null),
          tuple(Action.Type.ITEM,            "g2",         "group",          "Group 2",     null, null),
          tuple(Action.Type.VALUE_SET,       null,            null,             null,     null, null)
        );
        assertThat(actions.getActions()).extracting(action -> action.getValueSet() == null ? null : action.getValueSet().getEntries().stream().map(ValueSetEntry::getValue).collect(Collectors.toList()))
          .containsOnly(
            null,
            null,
            null,
            null,
            null,
            Arrays.asList("Choice 1", "Choice 2")
          );
      })
      .next()
      .answerQuestion("q1", "vastaus 2")
      .expectUpdateWithoutActions()
      // Do not trigger model updates. Other's will receive this same 'answerQuestion' action. (Propably not the best way to respond..)

      .execute();
  }

}
