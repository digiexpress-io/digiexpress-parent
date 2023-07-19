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
package io.dialob.program.expr.arith;

import com.google.common.collect.ImmutableSet;

import io.dialob.executor.command.EventMatcher;
import io.dialob.executor.model.ItemId;
import io.dialob.executor.model.ItemState;
import io.dialob.program.EvalContext;
import io.dialob.program.model.Expression;
import io.dialob.rule.parser.api.ValueType;

import org.immutables.value.Value;

import javax.annotation.Nonnull;

import static io.dialob.executor.command.EventMatchers.whenDisabledUpdatedEvent;

import java.util.Set;

@Value.Immutable
public interface IsDisabledOperator extends Expression {

  @Value.Parameter
  ItemId getItemId();

  @Override
  default Boolean eval(@Nonnull EvalContext evalContext) {
    return evalContext.getItemState(this.getItemId()).map(ItemState::isDisabled).orElse(true);
  }

  @Override
  @Nonnull
  default ValueType getValueType() {
    return ValueType.BOOLEAN;
  }

  @Override
  @Nonnull
  default Set<EventMatcher> getEvalRequiredConditions() {
    return ImmutableSet.of(whenDisabledUpdatedEvent(getItemId()));
  }

}
