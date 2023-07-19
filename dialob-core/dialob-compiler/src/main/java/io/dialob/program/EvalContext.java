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

import io.dialob.executor.AsyncFunctionCall;
import io.dialob.executor.command.event.Event;
import io.dialob.executor.model.*;
import io.dialob.program.expr.OutputFormatter;
import io.dialob.rule.parser.function.FunctionRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface EvalContext {

  @Nonnull
  Optional<ItemState> getItemState(@Nonnull ItemId itemId);

  @Nonnull
  Optional<ItemState> getOriginalItemState(@Nonnull ItemId itemId);

  @Nonnull
  Optional<ItemState> findPrototype(@Nonnull ItemId itemId);

  @Nonnull
  Stream<ErrorState> findErrorPrototypes(@Nonnull ItemId itemId);

  @Nonnull
  Optional<ValueSetState> getValueSetState(@Nonnull ValueSetId valueSetId);

  @Nullable
  Object getItemValue(ItemId itemId);

  EvalContext withScope(Scope scope);

  EvalContext getParent();

  void registerUpdate(ItemState newState, ItemState oldState);

  void registerUpdate(ErrorState newState, ErrorState oldState);

  void registerUpdate(@Nonnull ValueSetState newState, ValueSetState oldState);

  void accept(@Nonnull UpdatedItemsVisitor visitor);

  String getLanguage();

  void setLanguage(String language);

  @Nonnull
  Consumer<Event> getEventsConsumer();

  Collection<ErrorState> getErrorStates();

  @Nonnull
  FunctionRegistry getFunctionRegistry();

  @Nonnull
  default LocalDate today() {
    return LocalDate.now(getClock());
  }

  @Nonnull
  default LocalTime now() {
    return LocalTime.now(getClock());
  }

  @Nonnull
  Clock getClock();

  @Nonnull
  OutputFormatter getOutputFormatter();

  boolean isActivating();

  Optional<ItemState> findHoistingGroup(ItemId id);

  ItemId mapTo(ItemId itemId, boolean ignoreScopeItems);

  boolean complete();

  /**
   *
   * @param targetId
   * @param asyncFunctionCall
   * @return id of update
   */
  String queueAsyncFunctionCall(AsyncFunctionCall asyncFunctionCall);

  interface UpdatedItemsVisitor {

    @FunctionalInterface
    interface UpdatedSessionStateVisitor {
      void visitLanguageChange(@Nonnull String original, @Nonnull String updated);
      default void end() {}
    }

    Optional<UpdatedSessionStateVisitor> visitSession();

    @FunctionalInterface
    interface UpdatedItemStateVisitor {
      void visitUpdatedItemState(@Nullable ItemState original, @Nullable ItemState updated);
      default void end() {}
    }

    @FunctionalInterface
    interface UpdatedErrorStateVisitor {
      void visitUpdatedErrorState(@Nullable ErrorState original, @Nullable ErrorState updated);
      default void end() {}
    }

    @FunctionalInterface
    interface UpdatedValueSetVisitor {
      void visitUpdatedValueSet(@Nullable ValueSetState original, @Nullable ValueSetState updated);
      default void end() {}
    }

    @FunctionalInterface
    interface AsyncFunctionCallVisitor {
      void visitAsyncFunctionCall(@Nonnull AsyncFunctionCall asyncFunctionCall);
      default void end() {}
    }

    default void start() {}

    default Optional<UpdatedItemStateVisitor> visitUpdatedItems() {
      return Optional.empty();
    }

    default Optional<UpdatedErrorStateVisitor> visitUpdatedErrorStates() {
      return Optional.empty();
    }

    default Optional<UpdatedValueSetVisitor> visitUpdatedValueSets() {
      return Optional.empty();
    }

    default Optional<AsyncFunctionCallVisitor> visitAsyncFunctionCalls() {
      return Optional.empty();
    }

    default void visitCompleted() {}

    default void end() {}
  }

  abstract class AbstractDelegateUpdatedItemsVisitor implements UpdatedItemsVisitor {

    private final UpdatedItemsVisitor delegate;

    public AbstractDelegateUpdatedItemsVisitor(UpdatedItemsVisitor delegate) {
      this.delegate = delegate;
    }

    @Override
    public void start() {
      delegate.start();
    }

    @Override
    public Optional<UpdatedItemStateVisitor> visitUpdatedItems() {
      return delegate.visitUpdatedItems();
    }

    @Override
    public Optional<UpdatedErrorStateVisitor> visitUpdatedErrorStates() {
      return delegate.visitUpdatedErrorStates();
    }

    @Override
    public Optional<UpdatedValueSetVisitor> visitUpdatedValueSets() {
      return delegate.visitUpdatedValueSets();
    }

    @Override
    public Optional<AsyncFunctionCallVisitor> visitAsyncFunctionCalls() {
      return delegate.visitAsyncFunctionCalls();
    }

    @Override
    public void visitCompleted() {
      delegate.visitCompleted();
    }

    @Override
    public Optional<UpdatedSessionStateVisitor> visitSession() {
      return delegate.visitSession();
    }

    @Override
    public void end() {
      delegate.end();
    }


  }

}
