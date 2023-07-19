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

import static org.assertj.core.groups.Tuple.tuple;

import java.util.Arrays;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import io.dialob.api.form.ImmutableForm;
import io.dialob.api.form.ImmutableFormMetadata;
import io.dialob.api.form.ImmutableVariable;
import io.dialob.api.proto.Action;
import io.dialob.client.tests.steps.support.AbstractWebSocketTests;

public class QuestionnaireAsyncFunctionExecutorTest extends AbstractWebSocketTests {
  
  // registry function
  public static String testFunction(String input) {
    return "got it " + input;
  }

  
  @Test
  public void shouldEvaluateFunctionAsynchronously() throws Exception {
    getRegistry().configureFunction("testFunction", QuestionnaireAsyncFunctionExecutorTest.class, true);

    ImmutableForm.Builder formBuilder = ImmutableForm.builder()
      .id("shouldEvaluateFunctionAsynchronously")
      .rev("321")
      .metadata(ImmutableFormMetadata.builder().label("Kysely").build());

    addQuestionnaire(formBuilder, builder -> builder.addClassName("main-questionnaire").addItems("g1"));
    addItem(formBuilder, "g1", builder -> builder.type("group").putLabel("en","Group1").addItems("question1","note1","note2"));
    addItem(formBuilder, "note1", builder -> builder.type("note").putLabel("en","{testResult}"));
    addItem(formBuilder, "note2", builder -> builder.type("note").putLabel("en","Things got Weird").activeWhen("testFunction(question1) = \"got it Weird\""));
    addItem(formBuilder, "question1", builder -> builder.type("text").putLabel("en","Question 1"));

    formBuilder.addVariables(ImmutableVariable.builder()
      .name("testResult")
      .expression("testFunction(question1)").build());

    save(formBuilder.build());


    // @formatter: off
    createAndOpenSession("shouldEvaluateFunctionAsynchronously")
//      .expectActivated()
      .expectActions(actions -> {
        Assertions.assertThat(actions.getActions())
          .extracting("type","item.id","item.label")
          .containsOnly(
            tuple(Action.Type.RESET,  null,             null),
            tuple(Action.Type.LOCALE, null,             null),
            tuple(Action.Type.ITEM,   "questionnaire", "Kysely"),
            tuple(Action.Type.ITEM,   "g1",            "Group1"),
            tuple(Action.Type.ITEM,   "question1",     "Question 1")
          );
      })
    .next()
    .answerQuestion("question1", "Weird")
    .expectUpdateWithoutActions()
    .expectActions(actions -> {
      Assertions.assertThat(actions.getActions())
        .extracting("type","item.id","item.label")
        .containsOnly(
          tuple(Action.Type.ITEM, "note2", "Things got Weird"),
          tuple(Action.Type.ITEM, "note1", "got it Weird")
        );
      }).next()
    .answerQuestion("question1", "not more Weird")
    .expectUpdateWithoutActions()
    .expectActions("expect 1", actions -> {
      Assertions.assertThat(actions.getActions())
        .extracting("type","item.id",        "item.label", "ids")
        .containsOnly(
          tuple(Action.Type.REMOVE_ITEMS,        null,                    null, Arrays.asList("note2")),
          tuple(Action.Type.ITEM,     "note1", "got it not more Weird",         null)
        );
    })
    .execute();
    // @formatter: on

  }
}
