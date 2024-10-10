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

import static io.dialob.executor.command.EventMatchers.whenValueUpdated;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.immutables.value.Value;

import com.google.common.collect.ImmutableSet;

import io.dialob.executor.model.ImmutableItemIndex;
import io.dialob.executor.model.ItemId;
import io.dialob.executor.model.ItemState;
import io.dialob.program.EvalContext;

@Value.Immutable
public interface InitRowGroupItemsCommand extends AbstractUpdateCommand<ItemId,ItemState>, ItemUpdateCommand {

  @Nonnull
  @Override
  default ItemState update(@Nonnull EvalContext context, @Nonnull ItemState itemState) {
    List<Integer> rowNumbers = (List<Integer>) itemState.getValue();
    if (rowNumbers == null) {
      rowNumbers = Collections.emptyList();
    }
    List<ItemId> newItems = rowNumbers.stream().map(row -> ImmutableItemIndex.of(row, Optional.of(getTargetId()))).collect(Collectors.toList());
    return itemState.update()
      .setItems(newItems)
      .get();
  }

  @Nonnull
  @Override
  default Set<EventMatcher> getEventMatchers() {
    return ImmutableSet.of(whenValueUpdated(getTargetId()));
  }
}
