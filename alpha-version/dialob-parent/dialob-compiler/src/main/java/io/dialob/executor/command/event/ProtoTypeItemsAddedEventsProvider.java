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
package io.dialob.executor.command.event;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.immutables.value.Value;

import com.google.common.collect.Sets;

import io.dialob.executor.command.Triggers;
import io.dialob.executor.model.IdUtils;
import io.dialob.executor.model.ItemId;
import io.dialob.executor.model.ItemStates;

@Value.Immutable
public interface ProtoTypeItemsAddedEventsProvider extends Triggers.EventsProvider<ItemStates> {

  @Value.Parameter
  List<ItemId> getItemPrototypeIds();

  @Override
  default Stream<Event> createEvents(ItemStates originalState, ItemStates updatedState) {
    if (originalState == null && updatedState == null) {
      return getItemPrototypeIds().stream().map(itemId -> ImmutableItemAddedEvent.of(itemId, itemId));
    }
    if (updatedState == null) {
      return Stream.empty();
    }
    Set<ItemId> newItems = updatedState.getItemStates().keySet();
    if (originalState != null) {
      newItems = Sets.newHashSet(newItems);
      newItems.removeAll(originalState.getItemStates().keySet());
    }

    return newItems.stream()
      .flatMap(itemId ->
        getItemPrototypeIds().stream()
          .filter(itemPrototypeId -> IdUtils.matches(itemPrototypeId, itemId))
          .findFirst()
          .map(foundPrototypeId -> ImmutableItemAddedEvent.of(itemId, foundPrototypeId)).map(Stream::of).orElseGet(Stream::empty)
      );
  }
}
