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
package io.dialob.test.program;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.dialob.api.form.FormValidationError;
import io.dialob.api.form.ImmutableFormValidationError;
import io.dialob.executor.model.IdUtils;
import io.dialob.executor.model.ImmutableScope;
import io.dialob.program.EvalContext;
import io.dialob.program.GroupBuilder;
import io.dialob.program.ProgramBuilder;
import io.dialob.program.QuestionBuilder;
import io.dialob.program.model.Group;
import io.dialob.program.model.ItemIdMatchers;
import io.dialob.program.model.Program;
import io.dialob.rule.parser.function.FunctionRegistry;

class QuestionBuilderTest {

  @Test
  public void rowGroupItemsWillBePrototypes() {
    final FunctionRegistry functionRegistry = mock(FunctionRegistry.class);
    ProgramBuilder programBuilder = new ProgramBuilder(functionRegistry);
    Program program = programBuilder
      .startProgram()
      .setId("q1")
      .addGroup("questionnaire")
        .root()
        .addItem("g1")
        .build()
      .addGroup("g1")
        .rowgroup()
        .addItem("i1")
        .build()
      .addGroup("g2")
        .group()
        .addItem("i2")
        .build()
      .addQuestion("i1")
        .setType("note")
        .build()
      .addQuestion("i2")
        .setType("note")
        .build()
      .build();

    assertTrue(program.findItemsBy(ItemIdMatchers.idIs(IdUtils.toId("g1.*.i1"))).findFirst().get().isPrototype());
    assertFalse(program.findItemsBy(ItemIdMatchers.idIs(IdUtils.toId("i2"))).findFirst().get().isPrototype());
  }

  @Test
  public void rowGroupCreatesGroupAndGroupPrototype() {
    final FunctionRegistry functionRegistry = mock(FunctionRegistry.class);
    ProgramBuilder programBuilder = new ProgramBuilder(functionRegistry);
    // @formatter:off
    Program program = programBuilder
      .startProgram()
        .setId("q1")
        .addGroup("questionnaire")
          .root()
          .addItem("g1")
          .build()
        .addGroup("g1")
          .rowgroup()
          .addItem("i1")
          .addItem("i2")
          .build()
        .addQuestion("i1")
          .setType("note")
          .build()
        .addQuestion("i2")
          .setType("note")
          .build()
      .build();
    // @formatter:on


    assertTrue(program.findItemsBy(ItemIdMatchers.idIs(IdUtils.toId("g1.*.i1"))).findFirst().get().isPrototype());
    assertTrue(program.findItemsBy(ItemIdMatchers.idIs(IdUtils.toId("g1.*.i2"))).findFirst().get().isPrototype());
    Group g1proto = (Group) program.findItemsBy(ItemIdMatchers.idIs(IdUtils.toId("g1.*"))).findFirst().get();
    assertTrue(g1proto.isPrototype());
    Group g1 = (Group) program.findItemsBy(ItemIdMatchers.idIs(IdUtils.toId("g1"))).findFirst().get();
    assertFalse(g1.isPrototype());

    final ImmutableScope rowScope = ImmutableScope.of(IdUtils.toId("g1.2"), Collections.emptySet());
    EvalContext context = mock(EvalContext.class);
    when(context.mapTo(any(), eq(true))).thenAnswer(invocation -> rowScope.mapTo(invocation.getArgument(0), true));
    Assertions.assertEquals(Arrays.asList(IdUtils.toId("g1.2.i1"), IdUtils.toId("g1.2.i2")), g1proto.getItemsExpression().eval(context));
    verify(context, times(2)).mapTo(any(), eq(true));
    verifyNoMoreInteractions(context);

    reset(context);
    Assertions.assertEquals(Collections.emptyList(), g1.getItemsExpression().eval(context));
    verifyNoMoreInteractions(context);
  }

  @Test
  public void testInvalidDefaultValueValidationWhenItemTypeIsUnset() {
    ProgramBuilder programBuilder = mock(ProgramBuilder.class);
    GroupBuilder hoistingGroupBuilder = mock(GroupBuilder.class);
    Consumer<FormValidationError> errorConsumer = mock(Consumer.class);
    String id = "q1";
    new QuestionBuilder(programBuilder, hoistingGroupBuilder, id)
      .setDefaultValue("err")
      .beforeExpressionCompilation(errorConsumer);

    verify(errorConsumer).accept(eq(ImmutableFormValidationError.builder()
      .itemId("q1")
      .level(FormValidationError.Level.ERROR)
      .message("VALUE_TYPE_NOT_SET")
      .type(FormValidationError.Type.GENERAL)
      .build()));

    verifyNoMoreInteractions(programBuilder,
      hoistingGroupBuilder,
      errorConsumer);
  }

  @Test
  public void testInvalidDefaultValueValidation2() {
    ProgramBuilder programBuilder = mock(ProgramBuilder.class);
    GroupBuilder hoistingGroupBuilder = mock(GroupBuilder.class);
    Consumer<FormValidationError> errorConsumer = mock(Consumer.class);
    String id = "q1";
    new QuestionBuilder(programBuilder, hoistingGroupBuilder, id)
      .setDefaultValue("err")
      .setType("number")
      .beforeExpressionCompilation(errorConsumer);

    verify(errorConsumer).accept(eq(ImmutableFormValidationError.builder()
      .itemId("q1")
      .level(FormValidationError.Level.ERROR)
      .message("INVALID_DEFAULT_VALUE")
      .type(FormValidationError.Type.GENERAL)
      .build()));

    verifyNoMoreInteractions(programBuilder,
      hoistingGroupBuilder,
      errorConsumer);
  }

}
