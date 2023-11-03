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
package io.dialob.program;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import io.dialob.api.form.FormValidationError;
import io.dialob.executor.model.IdUtils;
import io.dialob.program.model.Expression;
import io.dialob.program.model.ImmutableConditionalValue;
import io.dialob.program.model.ImmutableConstantValue;
import io.dialob.program.model.ImmutableValueSet;
import io.dialob.program.model.Value;
import io.dialob.program.model.ValueSet;
import io.dialob.rule.parser.api.ValueType;

public class ValueSetEntryBuilder extends AbstractItemBuilder<ValueSetEntryBuilder, ValueSetBuilder> {

  private String key;

  private String when;

  private int index;

  public ValueSetEntryBuilder(ValueSetBuilder valueSetBuilder, String key, int index) {
    super(valueSetBuilder.getProgramBuilder(), valueSetBuilder, valueSetBuilder.getHoistingGroup().orElse(null), IdUtils.toString(valueSetBuilder.getId()) + ":" + index);
    this.key = Objects.requireNonNull(key, "Entry id may not be null");
    this.index = index;
  }

  @Override
  protected ValueSetEntryBuilder setActiveWhen(Expression activeWhen) {
    return super.setActiveWhen(activeWhen);
  }

  @Override
  protected void afterExpressionCompilation(Consumer<FormValidationError> errorConsumer) {
    requireBooleanExpression(activeWhen, getActiveWhenExpressionErrorType(), errorConsumer);
    Value<ValueSet.Entry> entryValue = null;
    if (activeWhen != null) {
      entryValue = ImmutableConditionalValue.<ValueSet.Entry>builder()
        .when(activeWhen)
        .value(ImmutableValueSet.Entry.builder()
          .key(key)
          .label(createLabelOperator(label))
          .build())
        .valueType(ValueType.STRING)
        .build();
    } else {
      entryValue = ImmutableConstantValue.<ValueSet.Entry>builder().value(
        ImmutableValueSet.Entry.builder()
          .key(key)
          .label(createLabelOperator(label))
          .build()).build();
    }
    getParent().addValueEntry(entryValue);
  }

  @Override
  public Optional<Integer> getIndex() {
    return Optional.of(index);
  }

  @Override
  protected FormValidationError.@NotNull Type getActiveWhenExpressionErrorType() {
    return FormValidationError.Type.VALUESET_ENTRY;
  }
}
