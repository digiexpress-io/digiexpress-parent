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

import io.dialob.compiler.Utils;
import io.dialob.executor.command.EventMatcher;
import io.dialob.executor.command.EventMatchers;
import io.dialob.executor.model.ErrorState;
import io.dialob.executor.model.ItemId;
import io.dialob.program.EvalContext;
import io.dialob.program.model.Expression;
import io.dialob.rule.parser.api.ValueType;

import org.immutables.value.Value;

import javax.annotation.Nonnull;
import java.util.Set;

@Value.Immutable
public interface IsValidOperator extends Expression {

  @Value.Parameter
  ItemId getItemId();

  @Override
  default Boolean eval(@Nonnull EvalContext context) {
    ItemId itemId = context.mapTo(getItemId(), false);
    return context.getItemState(itemId).map(itemState -> {
      if (Utils.isQuestionType(itemState)) {
        return context.getErrorStates().stream()
          .filter(ErrorState::isActive)
          .map(ErrorState::getItemId)
          .noneMatch(itemId::equals);
      }
      return !itemState.isInvalidAnswers();
    }).orElse(true);
  }

  @Nonnull
  @Override
  default ValueType getValueType() {
    return ValueType.BOOLEAN;
  }

  @Nonnull
  @Override
  default Set<EventMatcher> getEvalRequiredConditions() {
    return ImmutableSet.of(EventMatchers.errorActivity(EventMatchers.targetError(getItemId())));
  }
}
