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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.tuple;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormItem;
import io.dialob.api.form.ImmutableForm;
import io.dialob.api.form.ImmutableFormItem;
import io.dialob.api.form.ImmutableFormMetadata;
import io.dialob.api.proto.Action;
import io.dialob.client.spi.event.ImmutableFormUpdatedEvent;
import io.dialob.client.tests.steps.support.AbstractWebSocketTests;

//
// NOTE! This tests fails randomly, due race condition between actions sent over websocket.
//
public class QuestionnaireOnlineUpdateTest extends AbstractWebSocketTests {

  @SuppressWarnings({ "unused", "deprecation" })
  @Test
  public void updateFormOnline() throws Exception {

    ImmutableForm.Builder updateFormOnlineBuilder = ImmutableForm.builder();
    Consumer<ImmutableForm.Builder> initializer = formBuilder -> {
      FormItem formItemBean = addQuestionnaire(formBuilder, builder -> builder.addClassName("main-questionnaire").addItems("g1") );
      addItem(formBuilder, "g1", builder -> builder.type("group").putLabel("en", "Ryhma").addItems("q1"));
      addItem(formBuilder, "q1", builder -> builder.type("text").putLabel("en", "Kysymys"));
      formBuilder.metadata(ImmutableFormMetadata.builder().label("Kysely").build());
    };
    initializer.accept(updateFormOnlineBuilder);
    updateFormOnlineBuilder
      .id("updateFormOnline")
      .rev("1")
      .build();

    final ImmutableForm form1 = updateFormOnlineBuilder.build();
    save(form1);

    createAndOpenSession("updateFormOnline")
      .expectActivated()
      .expectActions(actions -> {
        assertThat(actions.getActions())
          .extracting("type",                     "item.id", "item.type", "item.label", "item.items").containsOnly(
          tuple(Action.Type.RESET,              null,            null,             null,          null),
          tuple(Action.Type.LOCALE,             null,            null,             null,          null),
          tuple(Action.Type.ITEM,    "questionnaire", "questionnaire",         "Kysely",  asList("g1")),
          tuple(Action.Type.ITEM,               "q1",          "text",        "Kysymys",          null),
          tuple(Action.Type.ITEM,               "g1",         "group",          "Ryhma",  asList("q1"))
        );
      })
      .next()
      .answerQuestion("q1", "vastaus")
      .expectActions(actions -> {
        assertThat(actions.getActions()).isNullOrEmpty();

        // Update Form and notify about it
        ImmutableForm.Builder updateFormOnlineBuilder2 = ImmutableForm.builder()
          .id("updateFormOnline")
          .rev("2");
        initializer.accept(updateFormOnlineBuilder2);

        FormItem formItemBean = form1.getData().get("g1");
        formItemBean = ImmutableFormItem.builder().from(formItemBean).items(asList("q1", "q2")).build();
        updateFormOnlineBuilder2.putData(formItemBean.getId(), formItemBean);
        addItem(updateFormOnlineBuilder2, "q2", builder -> builder.type("text").putLabel("en", "Kysymys 2"));
        
        Form formDocument2 = save(updateFormOnlineBuilder2.build());
        publishEvent(ImmutableFormUpdatedEvent.builder()
          .source("sourc3")
          .formId("updateFormOnline")
          .revision("2")
          .build());

      })
      .expectPassivation()
      .next()
      .answerQuestion("q1", "vastaus")
      .expectActions(actions -> {
        assertThat(actions.getActions())  // We'll get a full form because FormUpdatedEvent trigger session eviction from cache
          .extracting("type",                     "item.id", "item.type", "item.label", "item.items", "item.value").containsOnly(
                tuple(Action.Type.RESET,           null,            null,             null,             null,             null),
                tuple(Action.Type.LOCALE,          null,            null,             null,             null,             null),
                tuple(Action.Type.ITEM, "questionnaire", "questionnaire",         "Kysely",     asList("g1"),             null),
                tuple(Action.Type.ITEM,            "q1",          "text",        "Kysymys",             null,        "vastaus"),
                tuple(Action.Type.ITEM,            "q2",          "text",      "Kysymys 2",             null,             null),
                tuple(Action.Type.ITEM,            "g1",         "group",          "Ryhma",asList("q1", "q2"),            null)
        );
      })
      .next()
      .answerQuestion("q1", "vastaus 2")
      .expectUpdateWithoutActions()
      // Do not trigger model updates. Other's will receive this same 'answerQuestion' action. (Propably not the best way to respond..)
      .next().execute();
  }

}
