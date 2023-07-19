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

import io.dialob.compiler.Utils;
import io.dialob.executor.model.ItemId;
import io.dialob.executor.model.ItemState;
import io.dialob.program.EvalContext;

import org.immutables.value.Value;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Value.Immutable
public interface SetAnswer extends AbstractUpdateCommand<ItemId, ItemState>, ItemUpdateCommand {

  @Value.Parameter(order = 1)
  @Nullable
  Object getAnswer();

  @Nonnull
  default ItemState update(@Nonnull EvalContext context, @Nonnull ItemState itemState) {
    if (canUpdate(context, itemState)) {
      return itemState.update()
        .setAnswer(getAnswer())
        .setValue(Utils.parse(itemState.getType(), getAnswer())).get();
    }
    return itemState;
  }

  default boolean canUpdate(@Nonnull EvalContext context, @Nonnull ItemState itemState) {
    return ( context.isActivating() || !itemState.isDisabled() && itemState.isActive() ) && Utils.isQuestionType(itemState);
  }

}
