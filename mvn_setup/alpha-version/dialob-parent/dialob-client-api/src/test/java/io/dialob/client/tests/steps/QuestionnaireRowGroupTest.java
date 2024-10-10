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

import static io.dialob.api.proto.Action.Type.ADD_ROW;
import static java.util.Arrays.asList;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;

import io.dialob.api.form.ImmutableForm;
import io.dialob.api.form.ImmutableFormMetadata;
import io.dialob.api.form.ImmutableValidation;
import io.dialob.api.proto.Action;
import io.dialob.client.tests.steps.support.AbstractWebSocketTests;

public class QuestionnaireRowGroupTest extends AbstractWebSocketTests {



  @Test
  public void shouldAddAndRemoveRows() throws Exception {
    ImmutableForm.Builder formBuilder1 = ImmutableForm.builder()
      .id("testGetQuestionnaires-123")
      .rev("1")
      .metadata(ImmutableFormMetadata.builder().label("Kysely").build());


    Consumer<ImmutableForm.Builder> initializer = formBuilder -> {
      addQuestionnaire(formBuilder, builder -> builder.addClassName("main-questionnaire").addItems("p1"));
      addItem(formBuilder, "p1", builder -> builder.type("group").putLabel("en","Sivu").addItems("g1"));
      addItem(formBuilder, "g1", builder -> builder.type("rowgroup").putLabel("en","Ryhma").addItems("q1", "q2", "q3"));
      addItem(formBuilder, "q1", builder -> builder.type("text").putLabel("en","Kysymys 1"));
      addItem(formBuilder, "q2", builder -> builder.type("text").putLabel("en","Kysymys 2").addValidations(
        ImmutableValidation.builder().message(ImmutableMap.of("en","error")).rule("answer = \"wrong answer\"").build()
      ));
      addItem(formBuilder, "q3", builder -> builder.type("text").putLabel("en","Kysymys 3").activeWhen("q2 = \"correct answer\""));
      formBuilder.metadata(ImmutableFormMetadata.builder().label("Kysely").build());
    };
    initializer.accept(formBuilder1);
    shouldFindForm(formBuilder1.build());

    createAndOpenSession("testGetQuestionnaires-123")
      .expectActivated()
      .expectActions(actions -> {
        Assertions.assertThat(actions.getActions())
          .extracting("type", "item.id", "item.items")
          .containsOnly(
            tuple(Action.Type.RESET,   null, null),
            tuple(Action.Type.LOCALE,  null, null),
            tuple(Action.Type.ITEM, "questionnaire", asList("p1")),
            tuple(Action.Type.ITEM, "g1", null),
            tuple(Action.Type.ITEM, "p1", asList("g1"))
          );
      })
      .next()
      .addRow("g1")
      .expectActions(actions -> {
        Assertions.assertThat(actions.getActions())
          .extracting("type", "item.id", "item.items", "item.label")
          .containsOnly(
            tuple(Action.Type.ITEM, "g1.0.q1", null, "Kysymys 1"),
            tuple(Action.Type.ITEM, "g1.0.q2", null, "Kysymys 2"),
            tuple(Action.Type.ITEM, "g1.0", asList("g1.0.q1", "g1.0.q2", "g1.0.q3"), "Ryhma"),
            tuple(Action.Type.ITEM, "g1", asList("g1.0"), "Ryhma")
          );
      }).next()
      .addRow("g1")
      .expectActions(actions -> {
        Assertions.assertThat(actions.getActions())
          .extracting("type", "item.id", "item.items")
          .containsOnly(
            tuple(Action.Type.ITEM, "g1.1.q1", null),
            tuple(Action.Type.ITEM, "g1.1.q2", null),
            tuple(Action.Type.ITEM, "g1.1", asList("g1.1.q1", "g1.1.q2", "g1.1.q3")),
            tuple(Action.Type.ITEM, "g1", asList("g1.0", "g1.1"))
          );
      }).next()
      .deleteRow("g1.0")
      .expectActions(actions -> {
        Assertions.assertThat(actions.getActions())
          .extracting("type", "ids", "item.id", "item.items")
          .containsOnly(
            tuple(Action.Type.REMOVE_ITEMS, Arrays.asList("g1.0","g1.0.q2","g1.0.q1"), null, null),
            tuple(Action.Type.ITEM, null,       "g1", asList("g1.1"))
          );
      }).next()
      .answerQuestion("g1.1.q2","wrong answer")
      .expectActions(actions -> {
        Assertions.assertThat(actions.getActions())
          .extracting("type","item.id","error.id", "item.allowedActions").
          containsOnly(
            tuple(Action.Type.ITEM,"questionnaire",null, Set.of(Action.Type.ANSWER)),
            tuple(Action.Type.ERROR,null,"g1.1.q2", null)
          );
      }).next()
      .addRow("g1")
      .expectActions(actions -> {
        Assertions.assertThat(actions.getActions())
          .extracting("type", "item.id", "item.items")
          .containsOnly(
            tuple(Action.Type.ITEM, "g1.2.q1", null),
            tuple(Action.Type.ITEM, "g1.2.q2", null),
            tuple(Action.Type.ITEM, "g1.2", asList("g1.2.q1", "g1.2.q2", "g1.2.q3")),
            tuple(Action.Type.ITEM, "g1", asList("g1.1","g1.2"))
          );
      }).next()
      .deleteRow("g1.1")
      .expectActions(actions -> {
        Assertions.assertThat(actions.getActions())
          .extracting("type", "ids", "item.id", "item.items", "error.id", "error.code", "item.allowedActions")
          .containsOnly(
            tuple(Action.Type.REMOVE_ITEMS, Arrays.asList("g1.1","g1.1.q2","g1.1.q1"), null, null, null, null, null),
            tuple(Action.Type.REMOVE_ERROR,     null,                                      null, null, "g1.1.q2", "q2_error1", null),
            tuple(Action.Type.ITEM,             null,"questionnaire", asList("p1"), null, null, Set.of(Action.Type.ANSWER, Action.Type.COMPLETE)),
            tuple(Action.Type.ITEM,         null,                                      "g1", asList("g1.2"), null, null, Set.of(ADD_ROW))
          );
      }).next()
      .addRow("g1")
      .expectActions(actions -> {
        Assertions.assertThat(actions.getActions())
          .extracting("type", "item.id", "item.items")
          .containsOnly(
            tuple(Action.Type.ITEM, "g1.3.q1", null),
            tuple(Action.Type.ITEM, "g1.3.q2", null),
            tuple(Action.Type.ITEM, "g1.3", asList("g1.3.q1", "g1.3.q2", "g1.3.q3")),
            tuple(Action.Type.ITEM, "g1", asList("g1.2","g1.3"))
          );
      }).next()
      .answerQuestion("g1.2.q2","correct answer")
      .expectActions(actions -> {
        Assertions.assertThat(actions.getActions())
          .extracting("type", "item.id", "item.items")
          .containsOnly(
            tuple(Action.Type.ITEM, "g1.2.q3", null)
          );
      })
      .finallyAssert(webSocketMessage -> {
    }).execute();
  }


}
