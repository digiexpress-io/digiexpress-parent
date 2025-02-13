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

import static io.dialob.executor.command.CommandFactory.updateDisabled;
import static io.dialob.executor.command.CommandFactory.updateGroupItems;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import io.dialob.executor.command.Command;
import io.dialob.executor.command.CommandFactory;
import io.dialob.executor.command.EventMatcher;
import io.dialob.executor.model.IdUtils;
import io.dialob.executor.model.ImmutableItemRef;
import io.dialob.executor.model.ItemId;
import io.dialob.program.DependencyResolverVisitor;
import io.dialob.program.expr.ImmutableNotOnPageExpression;
import io.dialob.program.expr.arith.ImmutableConstant;
import io.dialob.program.expr.arith.ImmutableIsActiveOperator;
import io.dialob.program.expr.arith.ImmutableIsAnyInvalidAnswersOperator;
import io.dialob.program.expr.arith.ImmutableIsDisabledOperator;
import io.dialob.program.expr.arith.Operators;
import io.dialob.program.model.ImmutableFormItem;
import io.dialob.program.model.ImmutableGroup;
import io.dialob.program.model.Program;
import io.dialob.rule.parser.api.ValueType;

class DependencyResolverVisitorTest {

  public static final ValueType STRING_ARRAY_VALUE_TYPE = ValueType.arrayOf(ValueType.STRING);
  @Mock
  private Program program;

  protected ItemId ref(String itemId) {
    return (ImmutableItemRef) IdUtils.toId(itemId);
  }


  @BeforeEach
  public void setup() {
    program = mock(Program.class);
  }


  @Test
  public void shouldBePossibleToExecuteAgainstEmptyProgram() {
    DependencyResolverVisitor visitor = createDependencyResolverVisitor();
    Program program = mock(Program.class);
    visitor.startProgram(program);

    visitor.end();
    assertTrue(visitor.getInputUpdates().isEmpty());
    assertTrue(visitor.getItemCommands().isEmpty());
    verifyNoMoreInteractions(program);
  }


  @Test
  public void justQuestionnaireShouldPass() {
    DependencyResolverVisitor visitor = createDependencyResolverVisitor();
    Program program = mock(Program.class);
    visitor.startProgram(program);

    visitor.visitItems().ifPresent(itemVisitor -> {
      itemVisitor.visitItem(ImmutableGroup.builder()
        .id(IdUtils.toId("questionnaire"))
        .type("questionnaire")
        .isPrototype(false)
        .itemsExpression(ImmutableConstant.builder().valueType(ValueType.arrayOf(ValueType.STRING)).value(asList("page1", "page2")).build())
        .build());
      itemVisitor.end();
    });

    visitor.end();
    assertTrue(visitor.getInputUpdates().isEmpty());
    assertThat(visitor.getItemCommands()).hasSize(1)
      .containsEntry(ref("questionnaire"), asList(
        updateGroupItems(ref("questionnaire"), ImmutableConstant.builder().valueType(ValueType.arrayOf(ValueType.STRING)).value(asList("page1", "page2")).build()))
      );
    verifyNoMoreInteractions(program);
  }

  protected DependencyResolverVisitor createDependencyResolverVisitor() {
    return new DependencyResolverVisitor();
  }

  @Test
  @Disabled
  public void questionnaireWithPagesAndGroups() {
    DependencyResolverVisitor visitor = createDependencyResolverVisitor();
    Program program = mock(Program.class);
    visitor.startProgram(program);

    visitor.visitItems().ifPresent(itemVisitor -> {
      itemVisitor.visitItem(ImmutableGroup.builder()
        .id(IdUtils.toId("questionnaire"))
        .type("questionnaire")
        .itemsExpression(ImmutableConstant.builder().valueType(ValueType.arrayOf(ValueType.STRING)).value(asList("page1", "page2")).build())
        .isInvalidAnswersExpression(Operators.not(ImmutableIsAnyInvalidAnswersOperator.builder().build()))
        .build());
      itemVisitor.visitItem(ImmutableGroup.builder()
        .id(IdUtils.toId("page1"))
        .type("page")
        .itemsExpression(ImmutableConstant.builder().valueType(ValueType.arrayOf(ValueType.STRING)).value(asList("page1group1", "page1group2")).build())
        .disabledExpression(ImmutableNotOnPageExpression.of(ref("page1")))
        .build());
      itemVisitor.visitItem(ImmutableGroup.builder()
        .id(IdUtils.toId("page2"))
        .type("page")
        .itemsExpression(stringArray("page2group1", "page2group2"))
        .disabledExpression(ImmutableNotOnPageExpression.of(ref("page2")))
        .build());

      itemVisitor.visitItem(ImmutableGroup.builder()
        .id(IdUtils.toId("page1group1"))
        .type("group")
        .itemsExpression(ImmutableConstant.builder().valueType(ValueType.arrayOf(ValueType.STRING)).value(asList("page1group1item1","page1group1item2")).build())
        .disabledExpression(ImmutableIsDisabledOperator.of(ref("page1")))
        .activeExpression(ImmutableIsActiveOperator.of(ref("page1")))
        .build());
      itemVisitor.visitItem(ImmutableGroup.builder()
        .id(IdUtils.toId("page1group2"))
        .type("group")
        .itemsExpression(ImmutableConstant.builder().valueType(ValueType.arrayOf(ValueType.STRING)).value(asList()).build())
        .disabledExpression(ImmutableIsDisabledOperator.of(ref("page1")))
        .activeExpression(ImmutableIsActiveOperator.of(ref("page1")))
        .build());
      itemVisitor.visitItem(ImmutableGroup.builder()
        .id(IdUtils.toId("page2group1"))
        .type("group")
        .itemsExpression(ImmutableConstant.builder().valueType(ValueType.arrayOf(ValueType.STRING)).value(asList()).build())
        .disabledExpression(ImmutableIsDisabledOperator.of(ref("page2")))
        .activeExpression(ImmutableIsActiveOperator.of(ref("page2")))
        .build());
      itemVisitor.visitItem(ImmutableGroup.builder()
        .id(IdUtils.toId("page2group2"))
        .type("group")
        .itemsExpression(ImmutableConstant.builder().valueType(STRING_ARRAY_VALUE_TYPE).value(asList()).build())
        .disabledExpression(ImmutableIsDisabledOperator.of(ref("page2")))
        .activeExpression(ImmutableIsActiveOperator.of(ref("page2")))
        .build());


      itemVisitor.visitItem(ImmutableFormItem.builder()
        .id(IdUtils.toId("page1group1item1"))
        .type("boolean")
        .disabledExpression(ImmutableIsDisabledOperator.of(ref("page1group1")))
        .activeExpression(ImmutableIsActiveOperator.of(ref("page1group1")))
        .build());
      itemVisitor.visitItem(ImmutableFormItem.builder()
        .id(IdUtils.toId("page1group1item2"))
        .type("text")
        .disabledExpression(ImmutableIsDisabledOperator.of(ref("page1group1")))
        .activeExpression(ImmutableIsActiveOperator.of(ref("page1group1")))
        .build());

      itemVisitor.end();
    });

    visitor.end();
    assertThat(visitor.getInputUpdates()).hasSize(8);
    for (Map.Entry<EventMatcher, List<Command<?>>> entry : visitor.getInputUpdates().entrySet()) {
      System.err.println(entry.getKey());

      entry.getValue().forEach(updateCommand -> {
        System.err.println("  " + updateCommand);
      });

    }
    /*
IsActiveTargetEventMatcher{targetMatcher=TargetIdEventMatcher{targetId=ItemRef{id=page1group1}}}
  UpdateActivityCommand{expression=IsActiveOperator{itemId=ItemRef{id=page1group1}}, targetId=ItemRef{id=page1group1item2}, triggers=[Trigger{when=ITEM_ACTIVITY_CHANGED, event=ActiveUpdatedEvent{targetId=ItemRef{id=page1group1item2}}}]}
  UpdateActivityCommand{expression=IsActiveOperator{itemId=ItemRef{id=page1group1}}, targetId=ItemRef{id=page1group1item1}, triggers=[Trigger{when=ITEM_ACTIVITY_CHANGED, event=ActiveUpdatedEvent{targetId=ItemRef{id=page1group1item1}}}]}
IsDisabledTargetEventMatcher{targetMatcher=TargetIdEventMatcher{targetId=ItemRef{id=page1group1}}}
  UpdateDisabledCommand{expression=IsDisabledOperator{itemId=ItemRef{id=page1group1}}, targetId=ItemRef{id=page1group1item2}, triggers=[Trigger{when=ITEM_STATE_CHANGED, event=DisabledUpdatedEvent{targetId=ItemRef{id=page1group1item2}}}]}
  UpdateDisabledCommand{expression=IsDisabledOperator{itemId=ItemRef{id=page1group1}}, targetId=ItemRef{id=page1group1item1}, triggers=[Trigger{when=ITEM_STATE_CHANGED, event=DisabledUpdatedEvent{targetId=ItemRef{id=page1group1item1}}}]}
ActivePageEventMatcher{}
  UpdateDisabledCommand{expression=NotOnPageExpression{page=ItemRef{id=page1}}, targetId=ItemRef{id=page1}, triggers=[Trigger{when=ITEM_STATE_CHANGED, event=DisabledUpdatedEvent{targetId=ItemRef{id=page1}}}]}
  UpdateDisabledCommand{expression=NotOnPageExpression{page=ItemRef{id=page2}}, targetId=ItemRef{id=page2}, triggers=[Trigger{when=ITEM_STATE_CHANGED, event=DisabledUpdatedEvent{targetId=ItemRef{id=page2}}}]}
IsActiveTargetEventMatcher{targetMatcher=TargetIdEventMatcher{targetId=ItemRef{id=page1}}}
  UpdateActivityCommand{expression=IsActiveOperator{itemId=ItemRef{id=page1}}, targetId=ItemRef{id=page1group2}, triggers=[Trigger{when=ITEM_ACTIVITY_CHANGED, event=ActiveUpdatedEvent{targetId=ItemRef{id=page1group2}}}]}
  UpdateActivityCommand{expression=IsActiveOperator{itemId=ItemRef{id=page1}}, targetId=ItemRef{id=page1group1}, triggers=[Trigger{when=ITEM_ACTIVITY_CHANGED, event=ActiveUpdatedEvent{targetId=ItemRef{id=page1group1}}}]}
IsDisabledTargetEventMatcher{targetMatcher=TargetIdEventMatcher{targetId=ItemRef{id=page1}}}
  UpdateDisabledCommand{expression=IsDisabledOperator{itemId=ItemRef{id=page1}}, targetId=ItemRef{id=page1group1}, triggers=[Trigger{when=ITEM_STATE_CHANGED, event=DisabledUpdatedEvent{targetId=ItemRef{id=page1group1}}}]}
  UpdateDisabledCommand{expression=IsDisabledOperator{itemId=ItemRef{id=page1}}, targetId=ItemRef{id=page1group2}, triggers=[Trigger{when=ITEM_STATE_CHANGED, event=DisabledUpdatedEvent{targetId=ItemRef{id=page1group2}}}]}
IsDisabledTargetEventMatcher{targetMatcher=TargetIdEventMatcher{targetId=ItemRef{id=page2}}}
  UpdateDisabledCommand{expression=IsDisabledOperator{itemId=ItemRef{id=page2}}, targetId=ItemRef{id=page2group2}, triggers=[Trigger{when=ITEM_STATE_CHANGED, event=DisabledUpdatedEvent{targetId=ItemRef{id=page2group2}}}]}
  UpdateDisabledCommand{expression=IsDisabledOperator{itemId=ItemRef{id=page2}}, targetId=ItemRef{id=page2group1}, triggers=[Trigger{when=ITEM_STATE_CHANGED, event=DisabledUpdatedEvent{targetId=ItemRef{id=page2group1}}}]}
IsActiveTargetEventMatcher{targetMatcher=TargetIdEventMatcher{targetId=ItemRef{id=page2}}}
  UpdateActivityCommand{expression=IsActiveOperator{itemId=ItemRef{id=page2}}, targetId=ItemRef{id=page2group1}, triggers=[Trigger{when=ITEM_ACTIVITY_CHANGED, event=ActiveUpdatedEvent{targetId=ItemRef{id=page2group1}}}]}
  UpdateActivityCommand{expression=IsActiveOperator{itemId=ItemRef{id=page2}}, targetId=ItemRef{id=page2group2}, triggers=[Trigger{when=ITEM_ACTIVITY_CHANGED, event=ActiveUpdatedEvent{targetId=ItemRef{id=page2group2}}}]}
ErrorActivityEventMatcher{errorMatcher=AnyErrorEventMatcher{}}
  UpdateIsInvalidAnswersCommand{expression=NotOperator{expression=IsAnyInvalidAnswersOperator{itemId=ItemRef{id=questionnaire}}}, targetId=ItemRef{id=questionnaire}, triggers=[]}
    {
{
{
IsDisabledTargetEventMatcher{targetMatcher=TargetIdEventMatcher{targetId=page2}}=[
  UpdateDisabledCommand{expression=IsDisabledOperator{itemId=page2}, targetId=page2group1, triggers=[Trigger{when=ITEM_STATE_CHANGED, event=DisabledUpdatedEvent{target=TargetEvent{targetId=page2group1}}}]},
  UpdateDisabledCommand{expression=IsDisabledOperator{itemId=page2}, targetId=page2group2, triggers=[Trigger{when=ITEM_STATE_CHANGED, event=DisabledUpdatedEvent{target=TargetEvent{targetId=page2group2}}}]}],
IsActiveTargetEventMatcher{targetMatcher=TargetIdEventMatcher{targetId=page2}}=[
  UpdateActivityCommand{expression=IsActiveOperator{itemId=page2}, targetId=page2group1, triggers=[Trigger{when=ITEM_ACTIVITY_CHANGED, event=ActiveUpdatedEvent{target=TargetEvent{targetId=page2group1}}}]},
  UpdateActivityCommand{expression=IsActiveOperator{itemId=page2}, targetId=page2group2, triggers=[Trigger{when=ITEM_ACTIVITY_CHANGED, event=ActiveUpdatedEvent{target=TargetEvent{targetId=page2group2}}}]}],
IsDisabledTargetEventMatcher{targetMatcher=TargetIdEventMatcher{targetId=page1}}=[
  UpdateDisabledCommand{expression=IsDisabledOperator{itemId=page1}, targetId=page1group1, triggers=[Trigger{when=ITEM_STATE_CHANGED, event=DisabledUpdatedEvent{target=TargetEvent{targetId=page1group1}}}]},
  UpdateDisabledCommand{expression=IsDisabledOperator{itemId=page1}, targetId=page1group2, triggers=[Trigger{when=ITEM_STATE_CHANGED, event=DisabledUpdatedEvent{target=TargetEvent{targetId=page1group2}}}]}],
IsActiveTargetEventMatcher{targetMatcher=TargetIdEventMatcher{targetId=page1}}=[
  UpdateActivityCommand{expression=IsActiveOperator{itemId=page1}, targetId=page1group1, triggers=[Trigger{when=ITEM_ACTIVITY_CHANGED, event=ActiveUpdatedEvent{target=TargetEvent{targetId=page1group1}}}]},
  UpdateActivityCommand{expression=IsActiveOperator{itemId=page1}, targetId=page1group2, triggers=[Trigger{when=ITEM_ACTIVITY_CHANGED, event=ActiveUpdatedEvent{target=TargetEvent{targetId=page1group2}}}]}],
ActivePageEventMatcher{}=[
  UpdateDisabledCommand{expression=NotOnPageExpression{page=page1}, targetId=page1, triggers=[Trigger{when=ITEM_STATE_CHANGED, event=DisabledUpdatedEvent{target=TargetEvent{targetId=page1}}}]},
  UpdateDisabledCommand{expression=NotOnPageExpression{page=page2}, targetId=page2, triggers=[Trigger{when=ITEM_STATE_CHANGED, event=DisabledUpdatedEvent{target=TargetEvent{targetId=page2}}}]}],
ErrorActivityEventMatcher{errorMatcher=AnyErrorEventMatcher{}}=[
  UpdateIsInvalidAnswersCommand{expression=NotOperator{expression=IsAnyInvalidAnswersOperator{itemId=questionnaire}}, targetId=questionnaire, triggers=[]}],
IsDisabledTargetEventMatcher{targetMatcher=TargetIdEventMatcher{targetId=group1}}=[
  UpdateDisabledCommand{expression=IsDisabledOperator{itemId=group1}, targetId=page1group1item1, triggers=[Trigger{when=ITEM_STATE_CHANGED, event=DisabledUpdatedEvent{target=TargetEvent{targetId=page1group1item1}}}]},
  UpdateDisabledCommand{expression=IsDisabledOperator{itemId=group1}, targetId=page1group1item2, triggers=[Trigger{when=ITEM_STATE_CHANGED, event=DisabledUpdatedEvent{target=TargetEvent{targetId=page1group1item2}}}]}],
IsActiveTargetEventMatcher{targetMatcher=TargetIdEventMatcher{targetId=group1}}=[
  UpdateActivityCommand{expression=IsActiveOperator{itemId=group1}, targetId=page1group1item1, triggers=[Trigger{when=ITEM_ACTIVITY_CHANGED, event=ActiveUpdatedEvent{target=TargetEvent{targetId=page1group1item1}}}]},
  UpdateActivityCommand{expression=IsActiveOperator{itemId=group1}, targetId=page1group1item2, triggers=[Trigger{when=ITEM_ACTIVITY_CHANGED, event=ActiveUpdatedEvent{target=TargetEvent{targetId=page1group1item2}}}]}]}


     */
    assertThat(visitor.getItemCommands()).hasSize(9)
      .containsEntry(IdUtils.toId("questionnaire"), asList(
        updateGroupItems(ref("questionnaire"), stringArray("page1", "page2")),
        CommandFactory.updateIsInvalidAnswers(ref("questionnaire"), Operators.not(ImmutableIsAnyInvalidAnswersOperator.builder().build()))))
      .containsEntry(IdUtils.toId("page1"), asList(updateGroupItems(ref("page1"), stringArray("page1group1", "page1group2"))))
      .containsEntry(IdUtils.toId("page2"), asList(updateGroupItems(ref("page2"), stringArray("page2group1", "page2group2"))))
      .containsEntry(IdUtils.toId("page1group1"), asList(updateDisabled(ref("page1group1"), ImmutableIsDisabledOperator.of(ref("page1"))), updateGroupItems(ref("page1group1"), stringArray("page1group1item1","page1group1item2"))))
      .containsEntry(IdUtils.toId("page1group2"), asList(updateDisabled(ref("page1group2"), ImmutableIsDisabledOperator.of(ref("page1"))), updateGroupItems(ref("page1group2"), stringArray())))
      .containsEntry(IdUtils.toId("page2group1"), asList(updateDisabled(ref("page2group1"), ImmutableIsDisabledOperator.of(ref("page2"))), updateGroupItems(ref("page2group1"), stringArray())))
      .containsEntry(IdUtils.toId("page2group2"), asList(updateDisabled(ref("page2group2"), ImmutableIsDisabledOperator.of(ref("page2"))), updateGroupItems(ref("page2group2"), stringArray())))
      .containsEntry(IdUtils.toId("page1group1item1"), asList(updateDisabled(ref("page1group1item1"), ImmutableIsDisabledOperator.of(ref("group1")))))
      .containsEntry(IdUtils.toId("page1group1item2"), asList(updateDisabled(ref("page1group1item2"), ImmutableIsDisabledOperator.of(ref("group1")))))
    ;



    verifyNoMoreInteractions(program);
  }

  public ImmutableConstant<Object> stringArray(String... list) {
    return ImmutableConstant.builder().valueType(ValueType.arrayOf(ValueType.STRING)).value(asList(list)).build();
  }


}
