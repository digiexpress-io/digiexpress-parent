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
package io.dialob.program;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.dialob.compiler.DialobSessionUpdateHook;
import io.dialob.executor.AsyncFunctionCall;
import io.dialob.executor.ImmutableAsyncFunctionCall;
import io.dialob.executor.command.Command;
import io.dialob.executor.command.event.Event;
import io.dialob.executor.model.DialobSession;
import io.dialob.executor.model.ErrorId;
import io.dialob.executor.model.ErrorState;
import io.dialob.executor.model.IdUtils;
import io.dialob.executor.model.ImmutableItemStates;
import io.dialob.executor.model.ItemId;
import io.dialob.executor.model.ItemState;
import io.dialob.executor.model.ItemStates;
import io.dialob.executor.model.Scope;
import io.dialob.executor.model.ValueSetId;
import io.dialob.executor.model.ValueSetState;
import io.dialob.program.expr.OutputFormatter;
import io.dialob.rule.parser.function.FunctionRegistry;

public class DialobSessionEvalContext implements EvalContext {

  private static final DialobSessionUpdateHook DEFAULT_DIALOB_SESSION_EVAL_HOOKS = (dialobSession, update, delegate) -> delegate.accept(update);

  private final DialobSessionEvalContext parent;

  private final Scope scope;

  private final FunctionRegistry functionRegistry;

  private final DialobSession dialobSession;

  private final Consumer<Event> updatesConsumer;

  private final ItemStates originalStates;

  private final Set<ItemId> updatedItemIds;

  private final Set<ErrorId> updatedErrorIds;

  private final Set<ValueSetId> updatedValueSetIds;

  private final Map<ItemId, AsyncFunctionCall> pendingUpdates;

  private final Clock clock;

  private final boolean activating;

  private boolean didComplete;

  private DialobSessionUpdateHook dialobSessionUpdateHook;

  private String originalLanguage;

  public DialobSessionEvalContext(
    @Nonnull FunctionRegistry functionRegistry,
    @Nonnull DialobSession dialobSession,
    @Nonnull Consumer<Event> updatesConsumer,
    @Nonnull Clock clock,
    boolean activating,
    DialobSessionUpdateHook dialobSessionUpdateHook)
  {
    this.parent = null;
    this.scope = null;
    this.functionRegistry = functionRegistry;
    this.dialobSession = dialobSession;
    this.updatesConsumer = updatesConsumer;
    this.clock = clock;
    this.activating = activating;
    this.originalStates = ImmutableItemStates.builder().from(dialobSession).build();
    this.pendingUpdates = new HashMap<>();
    this.updatedItemIds = new HashSet<>();
    this.updatedErrorIds = new HashSet<>();
    this.updatedValueSetIds = new HashSet<>();
    this.dialobSessionUpdateHook = dialobSessionUpdateHook != null ? dialobSessionUpdateHook : DEFAULT_DIALOB_SESSION_EVAL_HOOKS;

  }


  private DialobSessionEvalContext(@Nonnull DialobSessionEvalContext parent, @Nonnull Scope scope)
  {
    this.parent = parent;
    this.scope = scope;
    this.functionRegistry = parent.functionRegistry;
    this.dialobSession = parent.dialobSession;
    this.updatesConsumer = parent.updatesConsumer;
    this.clock = parent.clock;
    this.activating = parent.activating;
    this.originalStates = parent.originalStates;
    this.pendingUpdates = parent.pendingUpdates;
    this.updatedItemIds = parent.updatedItemIds;
    this.updatedErrorIds = parent.updatedErrorIds;
    this.updatedValueSetIds = parent.updatedValueSetIds;
    this.dialobSessionUpdateHook = parent.dialobSessionUpdateHook;
  }


  public void applyAction(@Nonnull Command<?> action) {
    dialobSessionUpdateHook.hookAction(dialobSession, action, a -> this.dialobSession.applyUpdate(this, a));
  }

  @Override
  public EvalContext withScope(Scope scope) {
    return new DialobSessionEvalContext(this, scope);
  }

  @Override
  public EvalContext getParent() {
    return parent;
  }

  @Nonnull
  @Override
  public Optional<ItemState> getItemState(@Nonnull ItemId itemId) {
    if (IdUtils.QUESTIONNAIRE_ID.equals(itemId)) {
      return Optional.of(this.dialobSession.getRootItem());
    }
    return this.dialobSession.getItemState(scope(itemId, false));
  }

  @Nonnull
  @Override
  public Optional<ItemState> getOriginalItemState(@Nonnull ItemId itemId) {
    final ItemId scopedId = scope(itemId, false);
    ItemState originalState = this.originalStates.getItemStates().get(scopedId);
    if (originalState != null) {
      return Optional.of(originalState);
    }
    return Optional.empty();
  }

  @Nonnull
  @Override
  public Optional<ItemState> findPrototype(@Nonnull ItemId itemId) {
    return dialobSession.findPrototype(itemId);
  }

  @Nonnull
  @Override
  public Stream<ErrorState> findErrorPrototypes(@Nonnull ItemId itemId) {
    return dialobSession.findErrorPrototypes(itemId);
  }

  @Override
  @Nonnull
  public Optional<ValueSetState> getValueSetState(@Nonnull ValueSetId valueSetId) {
    return this.dialobSession.getValueSetState(valueSetId);
  }

  public Object getItemValue(ItemId itemId) {
    return getItemState(scope(itemId, false)).map(ItemState::getValue).orElse(null);
  }

  @Nonnull
  private ItemId scope(@Nonnull ItemId itemId, boolean ignoreScopeItems) {
    if (scope != null) {
      return scope.mapTo(itemId, ignoreScopeItems);
    }
    return itemId;
  }

  @Override
  public void registerUpdate(ItemState newState, ItemState oldState) {
    if (newState != oldState) {
      ItemId id;
      if (oldState != null) {
        id = oldState.getId();
      } else {
        id = newState.getId();
      }
      updatedItemIds.add(requireNonNull(id));
    }
  }

  @Override
  public void registerUpdate(ErrorState newState, ErrorState oldState) {
    if (newState != oldState) {
      ErrorId id;
      if (oldState != null) {
        id = oldState.getId();
      } else {
        id = newState.getId();
      }
      updatedErrorIds.add(requireNonNull(id));
    }
  }

  @Override
  public void registerUpdate(@Nonnull ValueSetState newState, ValueSetState oldState) {
    if (newState != oldState) {
      ValueSetId id;
      if (oldState != null) {
        id = oldState.getId();
      } else {
        id = newState.getId();
      }
      updatedValueSetIds.add(requireNonNull(id));
    }
  }

  public void accept(@Nonnull UpdatedItemsVisitor visitor) {
    visitor.start();
    if (originalLanguage != null) {
      visitor.visitSession().ifPresent(sessionUpdatesVisitor -> {
        sessionUpdatesVisitor.visitLanguageChange(originalLanguage, dialobSession.getLanguage());
        sessionUpdatesVisitor.end();
      });
    }
    visitor.visitUpdatedItems().ifPresent(updatedItemStateVisitor -> {
      for (ItemId updateItemId : this.updatedItemIds) {
        ItemState originalState = originalStates.getItemStates().get(updateItemId);
        Optional<ItemState> itemState1 = dialobSession.getItemState(updateItemId);
        if (itemState1.map(itemState -> itemState != originalState).orElse(originalState != null)) {
          updatedItemStateVisitor.visitUpdatedItemState(originalState, itemState1.orElse(null));
        }
      }
      updatedItemStateVisitor.end();
    });

    visitor.visitUpdatedErrorStates().ifPresent(updatedErrorStateVisitor -> {
      for (final ErrorId errorId : this.updatedErrorIds) {
        ErrorState originalState = originalStates.getErrorStates().get(errorId);
        Optional<ErrorState> itemState1 = dialobSession.getErrorState(errorId.getItemId(), errorId.getCode());
        if (itemState1.map(itemState -> itemState != originalState).orElse(originalState != null)) {
          updatedErrorStateVisitor.visitUpdatedErrorState(originalState, itemState1.orElse(null));
        }
      }
      updatedErrorStateVisitor.end();
    });

    visitor.visitUpdatedValueSets().ifPresent(updatedValueSetVisitor -> {
      for (final ValueSetId valueSetId : this.updatedValueSetIds) {
        ValueSetState originalState = originalStates.getValueSetStates().get(valueSetId);
        Optional<ValueSetState> valueSetState = dialobSession.getValueSetState(valueSetId);
        if (valueSetState.map(itemState -> itemState != originalState).orElse(originalState != null)) {
          updatedValueSetVisitor.visitUpdatedValueSet(originalState, valueSetState.orElse(null));
        }
      }
      updatedValueSetVisitor.end();
    });

    visitor.visitAsyncFunctionCalls().ifPresent(asyncFunctionCallVisitor -> {
      pendingUpdates.values().forEach(asyncFunctionCallVisitor::visitAsyncFunctionCall);
      asyncFunctionCallVisitor.end();
    });

    if (didComplete) {
      visitor.visitCompleted();
    }
    visitor.end();
  }

  @Override
  public String getLanguage() {
    return this.dialobSession.getLanguage();
  }

  @Override
  public void setLanguage(String language) {
    if (this.originalLanguage == null) {
      this.originalLanguage = this.dialobSession.getLanguage();
    }
    this.dialobSession.setLanguage(language);
  }

  @Nonnull
  @Override
  public Consumer<Event> getEventsConsumer() {
    return this.updatesConsumer;
  }

  @Override
  public Collection<ErrorState> getErrorStates() {
    return this.dialobSession.getErrorStates().values();
  }

  @Nonnull
  @Override
  public FunctionRegistry getFunctionRegistry() {
    return this.functionRegistry;
  }

  @Nonnull
  @Override
  public Clock getClock() {
    return this.clock;
  }


  @Override
  @Nonnull
  public OutputFormatter getOutputFormatter() {
    return new OutputFormatter(dialobSession.getLanguage());
  }

  @Override
  public boolean isActivating() {
    return activating;
  }

  @Override
  public Optional<ItemState> findHoistingGroup(ItemId id) {
    return this.dialobSession.findHoistingGroup(id);
  }

  @Override
  public ItemId mapTo(ItemId itemId, boolean ignoreScopeItems) {
    return scope(itemId, ignoreScopeItems);
  }

  @Override
  public boolean complete() {
    if (!this.dialobSession.isCompleted()) {
      if (this.dialobSession.complete()) {
        this.didComplete = true;
        return true;
      }
    }
    return false;
  }

  @Override
  public String queueAsyncFunctionCall(AsyncFunctionCall asyncFunctionCall) {
    return asyncFunctionCall.getTargetId().map(itemId -> {
      pendingUpdates.put(itemId, ((ImmutableAsyncFunctionCall) asyncFunctionCall)
        .withId(dialobSession.generateUpdateId()));
      return IdUtils.toString(itemId);
    }).orElse(null);
  }
}
