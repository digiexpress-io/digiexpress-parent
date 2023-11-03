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

import static io.dialob.executor.command.EventMatchers.whenItemsChanged;
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
public interface CreateRowGroupFromPrototypeCommand extends SessionUpdateCommand {

  @Value.Parameter
  ItemId getItemPrototypeId();

  @Nonnull
  @Override
  default ItemStates update(@Nonnull final EvalContext context, @Nonnull final ItemStates itemStates) {
    return getItemPrototypeId().getParent().flatMap(groupId -> {
      Set<ItemId> currentItems = Sets.newHashSet(itemStates.getItemStates().get(groupId).getItems());
      Set<ItemId> originalItems = context.getOriginalItemState(groupId).map(state -> (Set<ItemId>) Sets.newHashSet(state.getItems())).orElse(ImmutableSet.of());

      final Sets.SetView<ItemId> newItems = Sets.difference(currentItems, originalItems);
      final Sets.SetView<ItemId> removedItems = Sets.difference(originalItems, currentItems);
      return context.findPrototype(getItemPrototypeId()).map(prototypeState -> (ItemStates) ImmutableItemStates.builder()
        .from(itemStates)
        .itemStates(itemStates.getItemStates().values().stream().filter(item -> !removedItems.contains(item.getId())).collect(toMap(itemState -> Objects.requireNonNull(itemState.getId()), item -> item)))
        .putAllItemStates(newItems.stream().map(prototypeState::withId).collect(toMap(ItemState::getId, item -> item)))
        .build());
    }).orElse(itemStates);
  }

  @Nonnull
  @Override
  default Set<EventMatcher> getEventMatchers() {
    return getItemPrototypeId().getParent()
      .map(groupId -> ImmutableSet.of(whenItemsChanged(groupId)))
      .orElse(ImmutableSet.of());
  }
}
