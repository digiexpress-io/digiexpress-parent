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

import java.util.Optional;
import java.util.function.Consumer;

import io.dialob.api.form.FormValidationError;
import io.dialob.api.form.ImmutableFormValidationError;
import io.dialob.compiler.Utils;
import io.dialob.executor.model.IdUtils;
import io.dialob.executor.model.ItemId;
import io.dialob.program.expr.arith.ImmutableContextVariableReference;
import io.dialob.program.model.Expression;
import io.dialob.program.model.ImmutableVariableItem;
import io.dialob.rule.parser.api.ValueType;

public class VariableBuilder extends AbstractItemBuilder<GroupBuilder, ProgramBuilder> implements HasDefaultValue {

  private Expression valueExpression;

  private Object defaultValue;

  private String type;

  private boolean context = false;

  private boolean published = false;

  public VariableBuilder(ProgramBuilder programBuilder, String id) {
    super(programBuilder, programBuilder, null, id);
  }

  public VariableBuilder setValueExpression(String valueExpression) {
    if (valueExpression != null) {
      compileExpression(valueExpression, expression -> this.valueExpression = expression, FormValidationError.Type.VARIABLE, Optional.empty());
    }
    return this;
  }

  public VariableBuilder setDefaultValue(Object defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }

  public VariableBuilder setType(String type) {
    this.type = type;
    return this;
  }

  @Override
  public Optional<ValueType> getValueType() {
    if (context) {
      return Utils.mapQuestionTypeToValueType(type);
    }
    return Optional.ofNullable(valueExpression != null ? valueExpression.getValueType() : null);
  }

  public VariableBuilder setContext(Boolean context) {
    if (context != null) {
      this.context = context;
    }
    return this;
  }

  public VariableBuilder setPublished(Boolean published) {
    if (published != null) {
      this.published = published;
    }
    return this;
  }

  @Override
  public void afterExpressionCompilation(Consumer<FormValidationError> errorConsumer) {
    super.afterExpressionCompilation(errorConsumer);
    final ItemId id = getId();
    if (context) {
      this.valueExpression = Utils.mapQuestionTypeToValueType(this.type)
        .map(valueType -> ImmutableContextVariableReference.builder().itemId(id).valueType(valueType).build()).orElse(null);
      if (valueExpression == null) {
        errorConsumer.accept(ImmutableFormValidationError.builder().itemId(getIdStr()).message("CONTEXT_VARIABLE_UNDEFINED_TYPE").type(FormValidationError.Type.VARIABLE).build());
        return;
      }
    }
    Optional<Object> resolvedDefaultValue;
    if (valueExpression == null) {
      errorConsumer.accept(ImmutableFormValidationError.builder().itemId(getIdStr()).message("RB_VARIABLE_NEEDS_EXPRESSION").type(FormValidationError.Type.VARIABLE).build());
      return;
    } else {
      resolvedDefaultValue = getDefaultValue()
        .map(value -> Utils.validateDefaultValue(IdUtils.toString(id), valueExpression.getValueType(), value, errorConsumer));
    }
    getProgramBuilder().addItem(
      ImmutableVariableItem.builder()
        .id(id)
        .type(context ? "context" : "variable")
        .isPrototype(false)
        .isPublished(this.published)
        .valueExpression(this.valueExpression)
        .defaultValue(resolvedDefaultValue).build());
  }

  @Override
  public Optional<Object> getDefaultValue() {
    return Optional.ofNullable(defaultValue);
  }
}
