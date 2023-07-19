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
package io.dialob.test.executor.session.model;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableList;

import io.dialob.executor.command.Command;
import io.dialob.executor.command.SessionUpdateCommand;
import io.dialob.executor.command.Trigger;
import io.dialob.executor.model.DialobSession;
import io.dialob.executor.model.ErrorState;
import io.dialob.executor.model.IdUtils;
import io.dialob.executor.model.ImmutableItemStates;
import io.dialob.executor.model.ItemState;
import io.dialob.executor.model.ItemStates;
import io.dialob.executor.model.ValueSetState;
import io.dialob.program.EvalContext;

class DialobSessionTest {

  public static final ItemState ITEM_STATE = new ItemState(
    IdUtils.toId("q1"),
    null, "text", null,
    true, null, null, null,

    null, null);

  @Test
  public void noopCommandShouldNotTriggerAnyChanges() {
    List<ItemState> items = ImmutableList.of();
    List<ItemState> prototypes = ImmutableList.of();
    List<ValueSetState> valueSets = ImmutableList.of();
    List<ErrorState> errors = ImmutableList.of();
    DialobSession session = new DialobSession("tenant", "1", "2", "fi", items, prototypes, valueSets, errors, Collections.emptyList(), null, null, null);
    EvalContext context = Mockito.mock(EvalContext.class);
    Command<?> command = new SessionUpdateCommand() {

      @Override
      public List<Trigger<ItemStates>> getTriggers() {
        return ImmutableList.of();
      }

      @Nonnull
      @Override
      public ItemStates update(@Nonnull EvalContext context, @Nonnull ItemStates target) {
        return target;
      }
    };
    session.applyUpdate(context, command);

    verifyNoMoreInteractions(context);
  }

  @Test
  public void newItemsShouldTriggerUpdate() {
    List<ItemState> items = ImmutableList.of();
    List<ItemState> prototypes = ImmutableList.of();
    List<ValueSetState> valueSets = ImmutableList.of();
    List<ErrorState> errors = ImmutableList.of();
    DialobSession session = new DialobSession("tenant", "1", "2", "fi", items, prototypes, valueSets, errors, Collections.emptyList(), null, null, null);
    EvalContext context = Mockito.mock(EvalContext.class);
    Command<?> command = new SessionUpdateCommand() {

      @Override
      public List<Trigger<ItemStates>> getTriggers() {
        return ImmutableList.of();
      }

      @Nonnull
      @Override
      public ItemStates update(@Nonnull EvalContext context, @Nonnull ItemStates target) {
        return ImmutableItemStates.builder().from(target).putItemStates(ITEM_STATE.getId(), ITEM_STATE).build();
      }
    };
    session.applyUpdate(context, command);

    verify(context).registerUpdate(any(ItemState.class), isNull());
    verifyNoMoreInteractions(context);
  }

  @Test
  public void removedItemsShouldTriggerUpdate() {
    List<ItemState> items = ImmutableList.of(ITEM_STATE);
    List<ItemState> prototypes = ImmutableList.of();
    List<ValueSetState> valueSets = ImmutableList.of();
    List<ErrorState> errors = ImmutableList.of();
    DialobSession session = new DialobSession("tenant", "1", "2", "fi", items, prototypes, valueSets, errors, Collections.emptyList(), null, null, null);
    EvalContext context = Mockito.mock(EvalContext.class);
    Command<?> command = new SessionUpdateCommand() {

      @Override
      public List<Trigger<ItemStates>> getTriggers() {
        return ImmutableList.of();
      }

      @Nonnull
      @Override
      public ItemStates update(@Nonnull EvalContext context, @Nonnull ItemStates target) {
        return ImmutableItemStates.builder().build();
      }
    };
    session.applyUpdate(context, command);

    verify(context).registerUpdate(isNull(), any(ItemState.class));
    verifyNoMoreInteractions(context);
  }

  @Test
  public void itemUpdateShouldTriggerUpdate() {
    List<ItemState> items = ImmutableList.of(ITEM_STATE);
    List<ItemState> prototypes = ImmutableList.of();
    List<ValueSetState> valueSets = ImmutableList.of();
    List<ErrorState> errors = ImmutableList.of();
    DialobSession session = new DialobSession("tenant", "1", "2", "fi", items, prototypes, valueSets, errors, Collections.emptyList(), null, null, null);
    EvalContext context = Mockito.mock(EvalContext.class);
    Command<?> command = new SessionUpdateCommand() {

      @Override
      public List<Trigger<ItemStates>> getTriggers() {
        return ImmutableList.of();
      }

      @Nonnull
      @Override
      public ItemStates update(@Nonnull EvalContext context, @Nonnull ItemStates target) {
        return ImmutableItemStates.builder().putItemStates(ITEM_STATE.getId(), new ItemState(
          ITEM_STATE.getId(),
          null, "text", null,
          true, null, "hello", null,

          null, null)).build();
      }
    };
    session.applyUpdate(context, command);

    verify(context).registerUpdate(any(ItemState.class), any(ItemState.class));
    verifyNoMoreInteractions(context);
  }


}
