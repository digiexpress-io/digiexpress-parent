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

import java.util.Set;

import javax.annotation.Nonnull;

import org.immutables.value.Value;

import io.dialob.executor.AsyncFunctionCall;
import io.dialob.executor.ImmutableAsyncFunctionCall;
import io.dialob.executor.model.ItemId;
import io.dialob.executor.model.ItemState;
import io.dialob.program.EvalContext;
import io.dialob.program.model.Expression;

@Value.Immutable
public interface VariableUpdateCommand extends AbstractUpdateCommand<ItemId,ItemState>, ItemUpdateCommand {

  @Value.Parameter(order = 1)
  Expression getExpression();

  @Nonnull
  @Override
  default Set<EventMatcher> getEventMatchers() {
    return getExpression().getEvalRequiredConditions();
  }

  @Nonnull
  @Override
  default ItemState update(@Nonnull EvalContext context, @Nonnull ItemState itemState) {
    final Object eval = getExpression().eval(context);
    // TODO handle multiple concurrent async updates?
    if (isPending(eval)) {
      context.queueAsyncFunctionCall(
        ((ImmutableAsyncFunctionCall) eval).withTargetId(getTargetId()));
      return itemState.update()
        .setStatus(ItemState.Status.PENDING).get();
    } else {
      return itemState.update()
        .setValue(eval).get();
    }
  }

  default boolean isPending(Object eval) {
    return eval instanceof AsyncFunctionCall;
  }

}
