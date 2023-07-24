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

import io.dialob.executor.model.ItemId;
import io.dialob.executor.model.ItemIndex;
import io.dialob.executor.model.ItemState;
import io.dialob.program.EvalContext;

import org.immutables.value.Value;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Value.Immutable
public interface DeleteRow extends AbstractUpdateCommand<ItemId, ItemState>, ItemUpdateCommand {

  @Value.Parameter(order = 1)
  ItemId getToBeRemoved();

  @Override
  @Nonnull
  default ItemState update(@Nonnull EvalContext context, @Nonnull ItemState itemState) {
    List<Integer> rowNumbers = (List<Integer>) itemState.getValue();
    if (rowNumbers == null) {
      rowNumbers = Collections.emptyList();
    }
    rowNumbers = new ArrayList<>(rowNumbers);
    ItemId toBeRemoved = getToBeRemoved();

    var rowToBeRemoved = context.getItemState(toBeRemoved);
    if (rowToBeRemoved.isPresent() && !rowToBeRemoved.get().isRowCanBeRemoved()) {
      return itemState;
    }

    Integer rowToRemove = null;
    if (toBeRemoved instanceof ItemIndex) {
      rowToRemove = ((ItemIndex) toBeRemoved).getIndex();
    }
    rowNumbers.remove(rowToRemove);
    return itemState.update()
      .setAnswer(rowNumbers)
      .setValue(rowNumbers)
      .get();
  }

}
