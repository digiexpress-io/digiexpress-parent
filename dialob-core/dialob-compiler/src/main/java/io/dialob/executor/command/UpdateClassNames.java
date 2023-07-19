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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import io.dialob.executor.model.ItemId;
import io.dialob.executor.model.ItemState;
import io.dialob.program.EvalContext;
import io.dialob.program.model.ConstantValue;
import io.dialob.program.model.Expression;

import org.immutables.value.Value;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Value.Immutable
public interface UpdateClassNames extends AbstractUpdateCommand<ItemId,ItemState>, ItemUpdateCommand {

  @Value.Parameter(order = 1)
  Expression getExpression();

  @Nonnull
  @Override
  default Set<EventMatcher> getEventMatchers() {
    Set<EventMatcher> eventMatchers = getExpression().getEvalRequiredConditions();
    if (getTargetId().isPartial()) {
      return Sets.union(eventMatchers, ImmutableSet.of(EventMatchers.whenItemAdded(getTargetId())));
    }
    return eventMatchers;
  }

  @Nonnull
  @Override
  default ItemState update(@Nonnull EvalContext context, @Nonnull ItemState itemState) {
    // classnames do not trigger dependencies
    return itemState.update().setClassNames(evalExpression(context)).get();
  }

  default List<String> evalExpression(EvalContext context) {
    List<ConstantValue<String>> stringValues = (List<ConstantValue<String>>) getExpression().eval(context);
    if (stringValues == null) {
      return Collections.emptyList();
    }
    return stringValues.stream().map(stringValue -> stringValue.eval(context)).collect(toList());
  }

}
