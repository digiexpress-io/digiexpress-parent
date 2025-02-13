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
import io.dialob.executor.model.IdUtils;
import io.dialob.executor.model.ItemId;
import io.dialob.executor.model.ItemIdPartial;
import io.dialob.executor.model.ItemState;
import io.dialob.program.EvalContext;
import io.dialob.program.model.Expression;
import io.dialob.rule.parser.api.ValueType;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.dialob.executor.command.EventMatchers.whenValueUpdated;
import static java.util.stream.Collectors.toList;

@Value.Immutable
public interface CollectRowFieldsOperator extends Expression {

  @Value.Parameter
  ItemId getItemId();

  @Value.Parameter
  ValueType getType();

  @Override
  default Object eval(@NotNull EvalContext evalContext) {
    return getItemId().getParent().flatMap(ItemId::getParent)
      .map(rgId -> (List<Integer>) evalContext.getItemValue(rgId)).orElse(Collections.emptyList())
      .stream().map(rowNumber -> IdUtils.withIndex(getItemId(), rowNumber))
    .map(evalContext::getItemValue).collect(toList());
  }

  @Nonnull
  @Override
  default Set<EventMatcher> getEvalRequiredConditions() {
    Set<EventMatcher> matchers = new HashSet<>();
    getItemId().getParent()
      .flatMap(ItemId::getParent)
      .map(EventMatchers::whenValueUpdated)
      .ifPresent(matchers::add);

//    matchers.add(EventMatchers.whenItemAdded(getItemId()));
  //  getItemId().getParent().ifPresent(rowId -> matchers.add(EventMatchers.whenItemRemoved(rowId)));

    matchers.add(EventMatchers.whenValueUpdated(getItemId()));

    return Collections.unmodifiableSet(matchers);
  }

  @Nonnull
  @Override
  default ValueType getValueType() {
    return ValueType.arrayOf(getType());
  }

}
