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
package io.dialob.executor.command;

import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import org.immutables.value.Value;

import com.google.common.collect.ImmutableSet;

import io.dialob.executor.model.ErrorId;
import io.dialob.executor.model.ErrorState;
import io.dialob.executor.model.ItemId;
import io.dialob.program.model.Expression;

public interface ErrorUpdateCommand extends UpdateCommand<ErrorId,ErrorState> {

  @Value.Parameter(order = 1)
  Expression getExpression();

  @Nonnull
  @Override
  default Set<EventMatcher> getEventMatchers() {
    Set<EventMatcher> eventMatchers = getExpression().getEvalRequiredConditions();
    if (getTargetId().isPartial()) {
      ImmutableSet.Builder<EventMatcher> builder = ImmutableSet.<EventMatcher>builder();
      builder.addAll(eventMatchers);
      builder.add(EventMatchers.whenItemAdded(getTargetId().getItemId()));
      findConcreteItem(getTargetId()).map(EventMatchers::whenItemsChanged).ifPresent(builder::add);
      return builder.build();
    }
    return eventMatchers;
  }

  static Optional<ItemId> findConcreteItem(ItemId id) {
    if (id.isPartial()) {
      return id.getParent().flatMap(ErrorUpdateCommand::findConcreteItem);
    }
    return Optional.of(id);
  }

}