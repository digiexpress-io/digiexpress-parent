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

import io.dialob.executor.model.ValueSetId;
import io.dialob.executor.model.ValueSetState;
import io.dialob.program.EvalContext;
import io.dialob.program.model.ConditionalValue;
import io.dialob.program.model.Value;
import io.dialob.program.model.ValueSet;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@org.immutables.value.Value.Immutable
public interface UpdateValueSetCommand extends UpdateCommand<ValueSetId, ValueSetState> {

  @org.immutables.value.Value.Parameter(order = 1)
  List<Value<ValueSet.Entry>> getEntries();

  @Nonnull
  @Override
  default Set<EventMatcher> getEventMatchers() {
    return getEntries().stream().flatMap(entryValue -> {
      Stream<EventMatcher> eventMatchers = Stream.empty();
      if (entryValue instanceof ConditionalValue) {
        ConditionalValue conditionalValue = (ConditionalValue) entryValue;
        eventMatchers = conditionalValue.getWhen().getEvalRequiredConditions().stream();
      }
      // TODO collect label expressions
      return Stream.concat(Stream.of(EventMatchers.whenSessionLocaleUpdated()), eventMatchers);
    }).collect(Collectors.toSet());
  }

  @Nonnull
  @Override
  default ValueSetState update(@Nonnull EvalContext context, @Nonnull ValueSetState state) {
    final List<ValueSetState.Entry> entries =
      Stream.concat(
          this.getEntries().stream()
            .map(entryValue -> entryValue.eval(context))
            .filter(Objects::nonNull)
            .map(entry -> ValueSetState.Entry.of(entry.getKey(), (String) entry.getLabel().eval(context))),
          state.getEntries().stream().filter(ValueSetState.Entry::isProvided))
        .collect(Collectors.toList());
    return state.update().setEntries(entries).get();
  }

}
