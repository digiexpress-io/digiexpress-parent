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

import static io.dialob.executor.command.EventMatchers.whenItemRemoved;
import static io.dialob.executor.command.EventMatchers.whenRowGroupItemsInit;
import static java.util.stream.Collectors.toMap;

import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;

import org.immutables.value.Value;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import io.dialob.executor.model.ImmutableItemStates;
import io.dialob.executor.model.ItemId;
import io.dialob.executor.model.ItemState;
import io.dialob.executor.model.ItemStates;
import io.dialob.program.EvalContext;

@Value.Immutable
public interface CreateRowGroupItemsFromPrototypeCommand extends SessionUpdateCommand, UpdateCommand<ItemId, ItemStates> {

  @Value.Parameter
  ItemId getItemPrototypeId();

  @Nonnull
  @Override
  default ItemStates update(@Nonnull final EvalContext context, @Nonnull final ItemStates itemStates) {
    final ItemState currentItemState = itemStates.getItemStates().get(getTargetId());
    Set<ItemId> currentItems = currentItemState != null ? Sets.newHashSet(currentItemState.getItems()) : ImmutableSet.of();
    Set<ItemId> originalItems = context.getOriginalItemState(getTargetId()).map(state -> (Set<ItemId>) Sets.newHashSet(state.getItems())).orElse(ImmutableSet.of());

    final Sets.SetView<ItemId> newItems = Sets.difference(currentItems, originalItems);
    final Sets.SetView<ItemId> removedItems = Sets.difference(originalItems, currentItems);
    if (newItems.isEmpty() && removedItems.isEmpty()) {
      return itemStates;
    }
    // remove removed items and errors related to those
    final ImmutableItemStates.Builder builder = ImmutableItemStates.builder()
      .from(itemStates)
      .itemStates(itemStates.getItemStates().values().stream().filter(item -> !removedItems.contains(item.getId())).collect(toMap(itemState -> Objects.requireNonNull(itemState.getId()), item -> item)))
      .errorStates(itemStates.getErrorStates().values().stream().filter(errorState -> !removedItems.contains(errorState.getId().getItemId())).collect(toMap(errorState -> Objects.requireNonNull(errorState.getId()), errorState -> errorState)));

    // add new items states
    newItems.stream()
      .flatMap(itemId -> context.findPrototype(itemId).map(prototype -> prototype.withId(itemId)).stream())
      .forEach(itemState -> builder.putItemStates(itemState.getId(), itemState));

    // add error states
    newItems.stream()
      .flatMap(itemId -> context.findErrorPrototypes(itemId).map(prototype -> prototype.withErrorId(prototype.getId().withItemId(itemId))))
      .forEach(errorState -> builder.putErrorStates(errorState.getId(), errorState));
    return builder.build();
  }

  @Nonnull
  @Override
  default Set<EventMatcher> getEventMatchers() {
    return ImmutableSet.of(
      whenRowGroupItemsInit(getItemPrototypeId()),
      whenItemRemoved(getItemPrototypeId())
    );
  }
}
