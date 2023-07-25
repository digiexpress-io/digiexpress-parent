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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.google.common.collect.Maps;

import io.dialob.executor.command.event.Event;
import io.dialob.executor.model.DialobSession;
import io.dialob.executor.model.IdUtils;
import io.dialob.executor.model.ImmutableItemRef;
import io.dialob.executor.model.ItemId;
import io.dialob.executor.model.ItemState;
import io.dialob.program.DialobSessionEvalContext;
import io.dialob.program.EvalContext;
import io.dialob.rule.parser.function.FunctionRegistry;

class DialobSessionEvalContextTest {

  @Test
  public void shouldVisitUpdatedItems() {
    FunctionRegistry functionRegistry = Mockito.mock(FunctionRegistry.class);
    DialobSession dialobSession = Mockito.mock(DialobSession.class);
    Consumer<Event> updatesConsumer = Mockito.mock(Consumer.class);
    Clock clock = Clock.systemDefaultZone();

    ItemState originalState = Mockito.mock(ItemState.class);
    ItemState updatedState = Mockito.mock(ItemState.class);
    when(originalState.getId()).thenReturn(IdUtils.toId("is1"));

    final HashMap<ItemId, ItemState> itemStateHashMap = Maps.newHashMap();
    itemStateHashMap.put(IdUtils.toId("is1"), originalState);
    when(dialobSession.getItemStates()).thenReturn(itemStateHashMap);

    when(dialobSession.getItemState((ImmutableItemRef) IdUtils.toId("is1"))).thenReturn(Optional.of(updatedState));

    DialobSessionEvalContext context = new DialobSessionEvalContext(functionRegistry, dialobSession, updatesConsumer, clock, false, null);
    context.registerUpdate(updatedState,originalState);

    EvalContext.UpdatedItemsVisitor visitor = Mockito.mock(EvalContext.UpdatedItemsVisitor.class);
    EvalContext.UpdatedItemsVisitor.UpdatedItemStateVisitor updatedItemStateVisitor = Mockito.mock(EvalContext.UpdatedItemsVisitor.UpdatedItemStateVisitor.class);

    when(visitor.visitUpdatedItems()).thenReturn(Optional.of(updatedItemStateVisitor));
    context.accept(visitor);



    verify(originalState).getId();

    InOrder order = inOrder(visitor, updatedItemStateVisitor);

    order.verify(visitor).start();
    order.verify(visitor).visitUpdatedItems();
    order.verify(updatedItemStateVisitor).visitUpdatedItemState(originalState, updatedState);
    order.verify(updatedItemStateVisitor).end();
    order.verify(visitor).visitUpdatedErrorStates();
    order.verify(visitor).visitUpdatedValueSets();
    order.verify(visitor).end();
    order.verifyNoMoreInteractions();

    verify(dialobSession).getItemStates();
    verify(dialobSession).getErrorStates();
    verify(dialobSession).getValueSetStates();
    verify(dialobSession).getItemState(any());

    verifyNoMoreInteractions(originalState, dialobSession);

  }

  @Test
  public void shouldVisitCreatedItems() {
    FunctionRegistry functionRegistry = Mockito.mock(FunctionRegistry.class);
    DialobSession dialobSession = Mockito.mock(DialobSession.class);
    Consumer<Event> updatesConsumer = Mockito.mock(Consumer.class);
    Clock clock = Clock.systemDefaultZone();

    ItemState originalState = null;
    ItemState updatedState = Mockito.mock(ItemState.class);

    //when(originalState.getId()).thenReturn("is1");
    when(updatedState.getId()).thenReturn(IdUtils.toId("is1"));
    when(dialobSession.getItemState(IdUtils.toId("is1"))).thenReturn(Optional.of(updatedState));

    DialobSessionEvalContext context = new DialobSessionEvalContext(functionRegistry, dialobSession, updatesConsumer, clock, false, null);
    context.registerUpdate(updatedState,null);

    EvalContext.UpdatedItemsVisitor visitor = Mockito.mock(EvalContext.UpdatedItemsVisitor.class);
    EvalContext.UpdatedItemsVisitor.UpdatedItemStateVisitor updatedItemStateVisitor = Mockito.mock(EvalContext.UpdatedItemsVisitor.UpdatedItemStateVisitor.class);

    when(visitor.visitUpdatedItems()).thenReturn(Optional.of(updatedItemStateVisitor));
    context.accept(visitor);

    verify(updatedState).getId();

    InOrder order = inOrder(visitor, updatedItemStateVisitor);

    order.verify(visitor).start();
    order.verify(visitor).visitUpdatedItems();
    order.verify(updatedItemStateVisitor).visitUpdatedItemState(null, updatedState);
    order.verify(updatedItemStateVisitor).end();
    order.verify(visitor).visitUpdatedErrorStates();
    order.verify(visitor).visitUpdatedValueSets();
    order.verify(visitor).end();
    order.verifyNoMoreInteractions();

    verify(dialobSession).getItemStates();
    verify(dialobSession).getErrorStates();
    verify(dialobSession).getValueSetStates();
    verify(dialobSession).getItemState(any());

    verifyNoMoreInteractions(dialobSession);

  }

  @Test
  public void shouldVisitRemovedItems() {
    FunctionRegistry functionRegistry = Mockito.mock(FunctionRegistry.class);
    DialobSession dialobSession = Mockito.mock(DialobSession.class);
    Consumer<Event> updatesConsumer = Mockito.mock(Consumer.class);
    Clock clock = Clock.systemDefaultZone();

    ItemState originalState = Mockito.mock(ItemState.class);
    ItemState updatedState = null;

    final HashMap<ItemId, ItemState> itemStateHashMap = Maps.newHashMap();
    itemStateHashMap.put(IdUtils.toId("is1"), originalState);
    when(dialobSession.getItemStates()).thenReturn(itemStateHashMap);
    when(originalState.getId()).thenReturn(IdUtils.toId("is1"));

    when(dialobSession.getItemState((ImmutableItemRef) IdUtils.toId("is1"))).thenReturn(Optional.ofNullable(updatedState));

    DialobSessionEvalContext context = new DialobSessionEvalContext(functionRegistry, dialobSession, updatesConsumer, clock, false, null);
    context.registerUpdate(updatedState,originalState);

    EvalContext.UpdatedItemsVisitor visitor = Mockito.mock(EvalContext.UpdatedItemsVisitor.class);
    EvalContext.UpdatedItemsVisitor.UpdatedItemStateVisitor updatedItemStateVisitor = Mockito.mock(EvalContext.UpdatedItemsVisitor.UpdatedItemStateVisitor.class);

    when(visitor.visitUpdatedItems()).thenReturn(Optional.of(updatedItemStateVisitor));
    context.accept(visitor);



    verify(originalState).getId();

    InOrder order = inOrder(visitor, updatedItemStateVisitor);

    order.verify(visitor).start();
    order.verify(visitor).visitUpdatedItems();
    order.verify(updatedItemStateVisitor).visitUpdatedItemState(originalState, null);
    order.verify(updatedItemStateVisitor).end();
    order.verify(visitor).visitUpdatedErrorStates();
    order.verify(visitor).visitUpdatedValueSets();
    order.verify(visitor).end();
    order.verifyNoMoreInteractions();

    verify(dialobSession).getItemStates();
    verify(dialobSession).getErrorStates();
    verify(dialobSession).getValueSetStates();
    verify(dialobSession).getItemState(any());

    verifyNoMoreInteractions(originalState, dialobSession);

  }

}
