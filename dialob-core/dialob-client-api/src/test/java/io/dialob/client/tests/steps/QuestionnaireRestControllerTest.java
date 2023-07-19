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
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import java.util.Iterator;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import io.dialob.api.form.FormValueSet;
import io.dialob.api.form.ImmutableForm;
import io.dialob.api.form.ImmutableFormMetadata;
import io.dialob.api.form.ImmutableFormValueSet;
import io.dialob.api.form.ImmutableFormValueSetEntry;
import io.dialob.api.form.ImmutableVariable;
import io.dialob.api.proto.Action;
import io.dialob.client.tests.steps.support.AbstractWebSocketTests;

public class QuestionnaireRestControllerTest extends AbstractWebSocketTests {


  @Test
  public void testGetQuestionnaires() throws Exception {
    ImmutableForm.Builder formBuilder = ImmutableForm.builder().id("123").rev("321")
      .metadata(ImmutableFormMetadata.builder().label("Kysely").build());
    addQuestionnaire(formBuilder, builder -> builder.addClassName("main-questionnaire"));
    save(formBuilder.build());

    createAndOpenSession("123", "321")
      .expectActivated()
//      .expectActions(actions -> {
//        Assertions.assertThat(actions.getActions())
//          .extracting("type", "item.id", "item.items")
//          .containsOnly(
//            tuple(Action.Type.RESET,   null, null),
//            tuple(Action.Type.ITEM, "questionnaire", asList())
//          );
//      })
      .expectActions(actions -> {
        Assertions.assertThat(actions.getActions())
          .extracting("type", "item.id", "item.items")
          .containsOnly(
            tuple(Action.Type.RESET,   null, null),
            tuple(Action.Type.LOCALE,   null, null),
            tuple(Action.Type.ITEM, "questionnaire", null)
          );
      }).execute();

  }


  @Test
  public void testNextPage() throws Exception {

    ImmutableForm.Builder formBuilder = ImmutableForm.builder().id("testNextPage").rev("321")
      .metadata(ImmutableFormMetadata.builder().label("Kysely").build());

    addQuestionnaire(formBuilder, builder -> builder.addClassName("main-questionnaire").addItems("g1", "g2"));
    addItem(formBuilder, "g1", builder -> builder.type("group").putLabel("en","Group1"));
    addItem(formBuilder, "g2", builder -> builder.type("group").putLabel("en","Group2"));

    save(formBuilder.build());

    // -- doLogin();
    createAndOpenSession("testNextPage")
      .expectActivated()
      .expectActions(actions -> {
        Assertions.assertThat(actions.getActions())
          .extracting("type", "item.id", "item.items")
          .containsOnly(
            tuple(Action.Type.RESET,   null, null),
            tuple(Action.Type.LOCALE,  null, null),
            tuple(Action.Type.ITEM, "questionnaire", asList("g1", "g2")),
            tuple(Action.Type.ITEM, "g1", null)
          );
      }).next()
    .nextPage()
    .expectActions(actions -> {
      assertEquals(3, actions.getActions().size());
      Iterator<Action> i = actions.getActions().iterator();
      Action action = i.next();
      assertEquals(Action.Type.REMOVE_ITEMS, action.getType());
      assertEquals(Arrays.asList("g1"), action.getIds());

      action = i.next();
      assertEquals(Action.Type.ITEM, action.getType());
      assertEquals("g2", action.getItem().getId());

      action = i.next();
      assertEquals(Action.Type.ITEM, action.getType());
      assertEquals("questionnaire", action.getItem().getId());
      assertEquals("g2", action.getItem().getActiveItem());
      assertEquals(asList("g1", "g2"), action.getItem().getItems());
      assertEquals(asList("g1", "g2"), action.getItem().getAvailableItems());
      assertFalse(i.hasNext());

    }).next()
    .nextPage()
    .expectUpdateWithoutActions().next()
    .prevPage()
    .expectActions(actions -> {
      // Previous Page  g1 -> g2
      assertEquals(3, actions.getActions().size());
      Iterator<Action> i = actions.getActions().iterator();
      Action action = i.next();
      assertEquals(Action.Type.REMOVE_ITEMS, action.getType());
      assertEquals(Arrays.asList("g2"), action.getIds());

      action = i.next();
      assertEquals(Action.Type.ITEM, action.getType());
      assertEquals("g1", action.getItem().getId());

      action = i.next();
      assertEquals(Action.Type.ITEM, action.getType());
      assertEquals("questionnaire", action.getItem().getId());
      assertEquals("g1", action.getItem().getActiveItem());
      assertEquals(asList("g1", "g2"), action.getItem().getItems());
      assertEquals(asList("g1", "g2"), action.getItem().getAvailableItems());

      assertFalse(i.hasNext());

    }).execute();
  }

  @Test
  public void shouldInterpolateValueSetEntryy() throws Exception {

    ImmutableForm.Builder formBuilder = ImmutableForm.builder().id("shouldInterpolateValueSetEntryy").rev("321")
      .metadata(ImmutableFormMetadata.builder().label("Kysely").build());

    addQuestionnaire(formBuilder, builder -> builder.addClassName("main-questionnaire").addItems("g1"));
    addItem(formBuilder, "g1", builder -> builder.type("group").putLabel("en","Group1").addItems("selection1","note1"));
    addItem(formBuilder, "selection1", builder -> builder.type("text").valueSetId("vs1"));
    addItem(formBuilder, "note1", builder -> builder.type("note").putLabel("en","Your selection is {selection1:lowercase}"));

    FormValueSet formValueSetBean = ImmutableFormValueSet.builder().id("vs1").addEntries(
      ImmutableFormValueSetEntry.builder().id("e1").putLabel("en", "Selectino 1").build(),
      ImmutableFormValueSetEntry.builder().id("e2").putLabel("en", "Selectino 2").build(),
      ImmutableFormValueSetEntry.builder().id("e3").putLabel("en", "Selectino 3").build()
    ).build();
    formBuilder.addValueSets(formValueSetBean);
    
    save(formBuilder.build());

    // -- doLogin();
    createAndOpenSession("shouldInterpolateValueSetEntryy")
      .expectActivated()
      .expectActions(actions -> {
        Assertions.assertThat(actions.getActions())
          .extracting("type", "item.id", "item.items")
          .containsOnly(
            tuple(Action.Type.RESET,   null, null),
            tuple(Action.Type.LOCALE,          null,            null),
            tuple(Action.Type.ITEM, "selection1", null),
            tuple(Action.Type.ITEM, "questionnaire", asList("g1")),
            tuple(Action.Type.ITEM, "g1", asList("selection1","note1")),
            tuple(Action.Type.VALUE_SET, null, null)
          );
      }).next()
      .answerQuestion("selection1","e2")
      .expectActions(actions -> {
        Assertions.assertThat(actions.getActions())
          .extracting("type", "item.id", "item.label")
          .containsOnly(
            tuple(Action.Type.ITEM, "note1", "Your selection is selectino 2")
          );
      }).next()
      .answerQuestion("selection1","e1")
      .expectActions(actions -> {
        Assertions.assertThat(actions.getActions())
          .extracting("type", "item.id", "item.label")
          .containsOnly(
            tuple(Action.Type.ITEM, "note1", "Your selection is selectino 1")
          );
      }).next()
      .answerQuestion("selection1",null)
      .expectActions(actions -> {
        Assertions.assertThat(actions.getActions())
          .extracting("type", "ids")
          .containsOnly(
            tuple(Action.Type.REMOVE_ITEMS, Arrays.asList("note1"))
          );
      })
      .execute();

  }

  @Test
  public void shouldHandleBigDecimalVariables() throws Exception {

    ImmutableForm.Builder formBuilder = ImmutableForm.builder().id("shouldHandleBigDecimalVariables").rev("321")
      .metadata(ImmutableFormMetadata.builder().label("bigD").build());

    addQuestionnaire(formBuilder, builder -> builder.addClassName("main-questionnaire").addItems("g1"));
    addItem(formBuilder, "g1", builder -> builder.type("group").putLabel("en","Group1").addItems("value1","note1"));
    addItem(formBuilder, "value1", builder -> builder.type("number").defaultValue("4"));
    addItem(formBuilder, "note1", builder -> builder.type("note").putLabel("en","Your value is {var1}"));

    formBuilder.addVariables(ImmutableVariable.builder()
      .name("var1")
      .expression("value1 * 0.5").build());

    save(formBuilder.build());

    // -- doLogin();
    createAndOpenSession("shouldHandleBigDecimalVariables")
      .expectActivated()
      .expectActions(actions -> {
        Assertions.assertThat(actions.getActions())
          .extracting("type", "item.id", "item.items")
          .containsOnly(
            tuple(Action.Type.RESET,   null, null),
            tuple(Action.Type.LOCALE,   null, null),
            tuple(Action.Type.ITEM, "questionnaire", asList("g1")),
            tuple(Action.Type.ITEM, "value1", null),
            tuple(Action.Type.ITEM, "g1", asList("value1","note1")),
            tuple(Action.Type.ITEM, "note1", null)

          );

      }).next()
      .answerQuestion("value1","10")
      .expectActions(actions -> {
        Assertions.assertThat(actions.getActions())
          .extracting("type", "item.id", "item.label")
          .containsOnly(
            tuple(Action.Type.ITEM, "note1", "Your value is 5")
          );
      }).next()
      .answerQuestion("value1","1")
      .expectActions(actions -> {
        Assertions.assertThat(actions.getActions())
          .extracting("type", "item.id", "item.label")
          .containsOnly(
            tuple(Action.Type.ITEM, "note1", "Your value is 0.5")
          );
      }).next()
      .answerQuestion("value1",null)
      .expectActions(actions -> {
        Assertions.assertThat(actions.getActions())
          .extracting("type", "item.id", "item.label")
          .containsOnly(
            tuple(Action.Type.ITEM, "note1", "Your value is 2")
          );
      })
      .execute();

  }

}
