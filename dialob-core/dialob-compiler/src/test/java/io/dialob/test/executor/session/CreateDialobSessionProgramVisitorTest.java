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
package io.dialob.test.executor.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.dialob.api.proto.ActionsFactory;
import io.dialob.executor.ActiveDialobSessionUpdater;
import io.dialob.executor.CreateDialobSessionProgramVisitor;
import io.dialob.executor.DialobSessionUpdater;
import io.dialob.executor.model.DialobSession;
import io.dialob.executor.model.IdUtils;
import io.dialob.executor.model.ImmutableItemIdPartial;
import io.dialob.executor.model.ItemId;
import io.dialob.executor.model.ItemState;
import io.dialob.program.DialobProgram;
import io.dialob.program.DialobSessionEvalContextFactory;
import io.dialob.program.EvalContext;
import io.dialob.program.ProgramBuilder;
import io.dialob.program.expr.arith.ArrayReducerOperator;
import io.dialob.program.expr.arith.ImmutableArrayReducerOperator;
import io.dialob.program.expr.arith.ImmutableCollectRowFieldsOperator;
import io.dialob.program.expr.arith.ImmutableConstant;
import io.dialob.program.expr.arith.ImmutableRowItemsExpression;
import io.dialob.program.expr.arith.Operators;
import io.dialob.program.model.ImmutableFormItem;
import io.dialob.program.model.ImmutableGroup;
import io.dialob.program.model.Program;
import io.dialob.rule.parser.api.ValueType;
import io.dialob.rule.parser.function.FunctionRegistry;

class CreateDialobSessionProgramVisitorTest {

  @Test
  public void prototypeItemsAreCollectedToPrototypes() {
    CreateDialobSessionProgramVisitor createDialobSessionProgramVisitor = new CreateDialobSessionProgramVisitor("tenant",
      "session", "en",
      null,
      (id, item) -> Optional.empty(), valueSetId -> Collections.emptyList(), new HashMap(), null, null, null);
    Program program = Mockito.mock(Program.class);
    createDialobSessionProgramVisitor.startProgram(program);
    createDialobSessionProgramVisitor.visitItems().ifPresent(itemVisitor -> {
      itemVisitor.visitItem(ImmutableFormItem.builder()
        .id(IdUtils.toId("proto"))
        .type("note")
        .isPrototype(true)
        .build());
    });
    createDialobSessionProgramVisitor.end();
    DialobSession dialobSession = createDialobSessionProgramVisitor.getDialobSession();
    assertTrue(dialobSession.findPrototype(IdUtils.toId("proto")).isPresent());

  }


  @Test
  public void prototshouldCreateGroupForRowGroupAndPrototypeGroupForRows() {
    CreateDialobSessionProgramVisitor createDialobSessionProgramVisitor = new CreateDialobSessionProgramVisitor("tenant",
      "session", "en",
      null,
      (id, item) -> Optional.empty(), valueSetId -> Collections.emptyList(), new HashMap<>(), null, null, null);
    Program program = Mockito.mock(Program.class);
    createDialobSessionProgramVisitor.startProgram(program);
    createDialobSessionProgramVisitor.visitItems().ifPresent(itemVisitor -> {
      itemVisitor.visitItem(ImmutableGroup.builder()
        .id(IdUtils.toId("rg"))
        .type("rowgroup")
        .isPrototype(false)
        .itemsExpression(ImmutableConstant.builder().valueType(ValueType.arrayOf(ValueType.STRING)).value(Arrays.asList("a", "b")).build())
        .build());
      itemVisitor.visitItem(ImmutableGroup.builder()
        .id(IdUtils.toId("rg.*"))
        .type("row")
        .isPrototype(true)
        .itemsExpression(ImmutableRowItemsExpression.builder().itemIds(Arrays.asList(IdUtils.toId("rg.*.q1"))).build()).build());
    });
    createDialobSessionProgramVisitor.end();
    DialobSession dialobSession = createDialobSessionProgramVisitor.getDialobSession();
    assertTrue(dialobSession.getItemState(Operators.ref("rg")).isPresent());
    assertTrue(dialobSession.findPrototype(IdUtils.toId("rg.*")).isPresent());
  }

  @Test
  public void shouldGetAllItemsById() {
    FunctionRegistry functionRegistry = Mockito.mock(FunctionRegistry.class);
    CreateDialobSessionProgramVisitor createDialobSessionProgramVisitor = new CreateDialobSessionProgramVisitor(
      "tenant",
      "session",
      "en",
      null,
      (id, item) -> Optional.empty(),
      valueSetId -> Collections.emptyList(),
      new HashMap<>(),
      null,
      null,
      null);
    Program program = new ProgramBuilder(functionRegistry)
      .startProgram()
        .setId("test")
        .addRoot().addItem("rg").build()
        .addRowGroup("rg").addItem("q1").addItem("q2").addItem("q3").build()
        .addQuestion("q1").setType("number").build()
        .addQuestion("q2").setType("decimal").build()
        .addQuestion("q3").setType("number").build()
      .build();
    DialobProgram dialobProgram = DialobProgram.createDialobProgram(program);

    // TODO session is created based on Program, but execution is done against DialobProgram??
    program.accept(createDialobSessionProgramVisitor);
    DialobSession dialobSession = createDialobSessionProgramVisitor.getDialobSession();
    DialobSessionEvalContextFactory contextFactory = new DialobSessionEvalContextFactory(functionRegistry, Clock.systemDefaultZone(), null);
    DialobSessionUpdater sessionUpdater = new ActiveDialobSessionUpdater(contextFactory, dialobProgram, dialobSession);
    sessionUpdater.dispatchActions(Arrays.asList(ActionsFactory.addRow("rg")), true);
    sessionUpdater.dispatchActions(Arrays.asList(ActionsFactory.addRow("rg")), true);
    sessionUpdater.dispatchActions(Arrays.asList(ActionsFactory.addRow("rg")), true);
    sessionUpdater.dispatchActions(Arrays.asList(ActionsFactory.answer("rg.0.q1", 1)), false);
    sessionUpdater.dispatchActions(Arrays.asList(ActionsFactory.answer("rg.0.q2", 1)), false);
    sessionUpdater.dispatchActions(Arrays.asList(ActionsFactory.answer("rg.2.q1", 2.0)), false);
    sessionUpdater.dispatchActions(Arrays.asList(ActionsFactory.answer("rg.2.q2", 2.0)), false);

    assertTrue(dialobSession.getItemState(Operators.ref("rg.0")).isPresent());
    assertTrue(dialobSession.getItemState(Operators.ref("rg")).isPresent());
    assertTrue(dialobSession.findPrototype(IdUtils.toId("rg.*")).isPresent());



    assertEquals(1, dialobSession.getItemState(IdUtils.toId("rg.0.q1")).get().getValue());
    assertEquals(BigDecimal.valueOf(1.0), dialobSession.getItemState(IdUtils.toId("rg.0.q2")).get().getValue());

    EvalContext context = contextFactory.createDialobSessionEvalContext(dialobSession, event -> {}, false);

//    ItemId test = context.mapTo(IdUtils.toId("q2"), true);
    Optional<ItemState> test = context.findPrototype(IdUtils.toId("q2"));

    assertEquals(BigDecimal.valueOf(3.0), ImmutableArrayReducerOperator.of(
      ArrayReducerOperator.DECIMAL_SUM,
      ImmutableCollectRowFieldsOperator.of(IdUtils.toId("rg.*.q2"),
        ValueType.DECIMAL)
    ).eval(context));
    assertEquals(3, ImmutableArrayReducerOperator.of(
      ArrayReducerOperator.INTEGER_SUM,
      ImmutableCollectRowFieldsOperator.of(IdUtils.toId("rg.*.q1"),
        ValueType.INTEGER)
    ).eval(context));
    assertNull(ImmutableArrayReducerOperator.of(
      ArrayReducerOperator.INTEGER_SUM,
      ImmutableCollectRowFieldsOperator.of(IdUtils.toId("rg.*.q3"),
        ValueType.INTEGER)
    ).eval(context));
    assertEquals(BigDecimal.valueOf(1.0), ImmutableArrayReducerOperator.of(
      ArrayReducerOperator.DECIMAL_MIN,
      ImmutableCollectRowFieldsOperator.of(IdUtils.toId("rg.*.q2"),
        ValueType.DECIMAL)
    ).eval(context));
    assertEquals(1, ImmutableArrayReducerOperator.of(
      ArrayReducerOperator.INTEGER_MIN,
      ImmutableCollectRowFieldsOperator.of(IdUtils.toId("rg.*.q1"),
        ValueType.INTEGER)
    ).eval(context));
    assertNull(ImmutableArrayReducerOperator.of(
      ArrayReducerOperator.INTEGER_MIN,
      ImmutableCollectRowFieldsOperator.of(IdUtils.toId("rg.*.q3"),
        ValueType.INTEGER)
    ).eval(context));
    assertEquals(BigDecimal.valueOf(2.0), ImmutableArrayReducerOperator.of(
      ArrayReducerOperator.DECIMAL_MAX,
      ImmutableCollectRowFieldsOperator.of(IdUtils.toId("rg.*.q2"),
        ValueType.DECIMAL)
    ).eval(context));
    assertEquals(2, ImmutableArrayReducerOperator.of(
      ArrayReducerOperator.INTEGER_MAX,
      ImmutableCollectRowFieldsOperator.of(IdUtils.toId("rg.*.q1"),
        ValueType.INTEGER)
    ).eval(context));
    assertNull(ImmutableArrayReducerOperator.of(
      ArrayReducerOperator.INTEGER_MAX,
      ImmutableCollectRowFieldsOperator.of(IdUtils.toId("rg.*.q3"),
        ValueType.INTEGER)
    ).eval(context));
    assertEquals(2, ImmutableArrayReducerOperator.builder()
      .reducer(ArrayReducerOperator.ANSWER_COUNT)
      .arrayExpression(ImmutableCollectRowFieldsOperator.of(IdUtils.toId("rg.*.q1"), ValueType.INTEGER))
      .placeholderValue(0)
      .build().eval(context));
    assertEquals(0, ImmutableArrayReducerOperator.builder()
      .reducer(ArrayReducerOperator.ANSWER_COUNT)
      .arrayExpression(ImmutableCollectRowFieldsOperator.of(IdUtils.toId("rg.*.q3"), ValueType.INTEGER))
      .placeholderValue(0)
      .build().eval(context));
  }


  @Test
  public void shouldCreateI() {
    CreateDialobSessionProgramVisitor.InitialValueResolver initialValueResolver = Mockito.mock(CreateDialobSessionProgramVisitor.InitialValueResolver.class);

    Program program = Mockito.mock(Program.class);
    final ItemId itemId = IdUtils.toId("rg");

    final ImmutableGroup rowgroup = ImmutableGroup.builder()
      .id(itemId)
      .type("rowgroup")
      .isPrototype(false)
      .itemsExpression(ImmutableConstant.builder().valueType(ValueType.arrayOf(ValueType.STRING)).value(Arrays.asList("a", "b")).build())
      .valueType(ValueType.arrayOf(ValueType.INTEGER))
      .build();

    final ImmutableGroup row = ImmutableGroup.builder()
      .id(ImmutableItemIdPartial.of(Optional.of(itemId)))
      .type("row")
      .isPrototype(true)
      .itemsExpression(ImmutableRowItemsExpression.builder().itemIds(Arrays.asList(IdUtils.toId("rg.*.q1"))).build())
      .build();


    Mockito.when(initialValueResolver.apply(itemId, rowgroup)).thenReturn(Optional.of(Arrays.asList(1,2,3)));


    CreateDialobSessionProgramVisitor createDialobSessionProgramVisitor = new CreateDialobSessionProgramVisitor("tenant",
      "session", "en",
      null,
      initialValueResolver, valueSetId -> Collections.emptyList(), new HashMap(), null, null, null);

    createDialobSessionProgramVisitor.startProgram(program);
    createDialobSessionProgramVisitor.visitItems().ifPresent(itemVisitor -> {
      itemVisitor.visitItem(rowgroup);
      itemVisitor.visitItem(row);
      itemVisitor.end();

    });
    createDialobSessionProgramVisitor.end();
    DialobSession dialobSession = createDialobSessionProgramVisitor.getDialobSession();
    final ItemState rg = dialobSession.getItemState(Operators.ref("rg")).get();
    assertEquals(Arrays.asList(1,2,3), rg.getValue());
    assertTrue(dialobSession.findPrototype(IdUtils.toId("rg.*")).isPresent());

    verify(initialValueResolver).apply(itemId, rowgroup);
    verifyNoMoreInteractions(initialValueResolver);
  }

}
