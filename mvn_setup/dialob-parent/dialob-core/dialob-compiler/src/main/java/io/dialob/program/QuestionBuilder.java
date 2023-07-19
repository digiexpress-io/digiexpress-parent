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

import static io.dialob.program.expr.arith.Operators.and;
import static io.dialob.program.expr.arith.Operators.isAnswered;
import static io.dialob.program.expr.arith.Operators.isRequired;
import static io.dialob.program.expr.arith.Operators.not;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.mutable.MutableObject;

import io.dialob.api.form.FormValidationError;
import io.dialob.compiler.Utils;
import io.dialob.compiler.spi.AliasesProvider;
import io.dialob.compiler.spi.ExpressionCompiler;
import io.dialob.executor.command.EventMatchers;
import io.dialob.executor.model.ItemId;
import io.dialob.executor.model.ItemRef;
import io.dialob.program.expr.arith.BooleanOperators;
import io.dialob.program.expr.arith.ImmutableConstant;
import io.dialob.program.expr.arith.ImmutableIsActiveOperator;
import io.dialob.program.expr.arith.ImmutableIsDisabledOperator;
import io.dialob.program.expr.arith.ImmutableIsInactiveOrNullOperator;
import io.dialob.program.expr.arith.LocalizedLabelOperator;
import io.dialob.program.expr.arith.Operators;
import io.dialob.program.model.Expression;
import io.dialob.program.model.ImmutableError;
import io.dialob.program.model.ImmutableFormItem;
import io.dialob.program.model.ImmutableLabel;
import io.dialob.rule.parser.api.ValueType;
import io.dialob.spi.Constants;

public class QuestionBuilder extends AbstractItemBuilder<QuestionBuilder,ProgramBuilder> implements ExpressionCompiler, BuilderParent, HasDefaultValue {

  public static final ImmutableLabel REQUIRED_LABEL = ImmutableLabel.builder()
    .putLabels("fi", "T\u00E4yt\u00E4 puuttuva tieto.")
    .putLabels("en", "Fill in the missing information.")
    .putLabels("sv", "Fyll i uppgift som saknas.")
    .build();

  private Object defaultValue;

  private String type;

  private String view;

  private String valueSetId;

  private List<io.dialob.program.model.Error> errors = new ArrayList<>();

  private List<ValidationBuilder> validationBuilders = new ArrayList<>();

  private Boolean required;

  private Expression requiredWhen;

  public QuestionBuilder(ProgramBuilder programBuilder, GroupBuilder hoistingGroupBuilder, String id) {
    super(programBuilder, programBuilder, hoistingGroupBuilder, id);
  }

  public ValidationBuilder addValidation(String errorCode) {
    ValidationBuilder validationBuilder = new ValidationBuilder(this, errorCode);
    validationBuilders.add(validationBuilder);
    return validationBuilder;
  }

  public QuestionBuilder setType(String type) {
    this.type = type;
    return this;
  }

  public QuestionBuilder setView(String view) {
    this.view = view;
    return this;
  }

  public QuestionBuilder setDefaultValue(Object defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }

  public QuestionBuilder setValueSet(String valueSetId) {
    this.valueSetId = valueSetId;
    return this;
  }

  @Override
  public Optional<ValueType> getValueType() {
    return Utils.mapQuestionTypeToValueType(type);
  }

  @Override
  public void beforeExpressionCompilation(Consumer<FormValidationError> errorConsumer) {
    super.beforeExpressionCompilation(errorConsumer);
    getDefaultValue().map(defaultValue -> {
      if (!getValueType().isPresent()) {
        return Utils.createError(getIdStr(), "VALUE_TYPE_NOT_SET");
      }
      Utils.validateDefaultValue(getIdStr(), getValueType().get(), defaultValue, errorConsumer::accept);
      return (FormValidationError) null;
    }).ifPresent(errorConsumer);
  }

  @Override
  protected void afterExpressionCompilation(Consumer<FormValidationError> errorConsumer) {
    super.afterExpressionCompilation(errorConsumer);
    requireBooleanExpression(requiredWhen, FormValidationError.Type.REQUIREMENT, errorConsumer);

    Objects.requireNonNull(type, "type may not be null");
    final ItemId id = getId();
    boolean prototype = getId().isPartial();

    Optional<GroupBuilder> hoistingGroup = getHoistingGroup();
    final MutableObject<Expression> disabledExpression = new MutableObject<>(BooleanOperators.TRUE);
    hoistingGroup.ifPresent(hoistingGroupBuilder -> disabledExpression.setValue(ImmutableIsDisabledOperator.builder().itemId(hoistingGroupBuilder.getId()).build()));



    if (isRequiredDefined()) {
      createRequiredError(this::addError);
    }
    LocalizedLabelOperator labelOperator = createLabelOperator(label);
    LocalizedLabelOperator descriptionOperator = createLabelOperator(description);
    disabledExpression.setValue(legacyNoteVisibility(disabledExpression.getValue(), labelOperator));


    hoistingGroup.ifPresent(hoistingGroupBuilder -> {
      if (activeWhen == BooleanOperators.TRUE) {
        activeWhen = ImmutableIsActiveOperator.builder().itemId(hoistingGroupBuilder.getId()).build();
      } else {
        activeWhen = Operators.and(ImmutableIsActiveOperator.builder().itemId(hoistingGroupBuilder.getId()).build(), activeWhen);
      }
    });
    validationBuilders.forEach(validationBuilder -> validationBuilder.setPrototype(prototype));
    validationBuilders.forEach(validationBuilder -> validationBuilder.afterExpressionCompilation(errorConsumer));
    getProgramBuilder().addItem(
      ImmutableFormItem.builder()
        .id(id)
        .type(type)
        .view(view)
        .isPrototype(prototype)
        .valueType(Utils.mapQuestionTypeToValueType(type).orElse(null)) // 'note' do not have value
        .activeExpression(activeWhen)
        .requiredExpression(Optional.ofNullable(requiredWhen))
        .disabledExpression(disabledExpression.getValue())
        .className(ImmutableConstant.builder().value(classNames).valueType(ValueType.arrayOf(ValueType.STRING)).build())
        .addAllErrors(errors)
        .labelExpression(labelOperator)
        .descriptionExpression(descriptionOperator)
        .valueSetId(Optional.ofNullable(this.valueSetId))
        .defaultValue(Optional.ofNullable(defaultValue))
        .props(props)
        .build());
  }

  // Add weird backward compatible visibility logic for notes
  private Expression legacyNoteVisibility(Expression disabledExpression, LocalizedLabelOperator labelOperator) {
    if ("note".equals(type)) {
        List<Expression> expressions =  labelOperator.getEvalRequiredConditions()
          .stream()
          .filter(eventMatcher -> eventMatcher instanceof EventMatchers.TargetIdEventMatcher)
          .map(eventMatcher -> (EventMatchers.TargetIdEventMatcher) eventMatcher)
          .map(itemId -> getProgramBuilder()
            .findDefaultValueForItem(itemId.getTargetId())
            // If item has default (fallback) value, it's always available for expression
            .<Expression>map(defaultValue -> BooleanOperators.FALSE)
              .orElse(ImmutableIsInactiveOrNullOperator.of(itemId.getTargetId())))
          .filter(expression -> expression != BooleanOperators.FALSE)
          .collect(Collectors.toList());

      expressions.add(disabledExpression);
      disabledExpression = Operators.or(expressions.toArray(new Expression[0]));
  }
    return disabledExpression;
  }

  private boolean isRequiredDefined() {
    return required != null && required || requiredWhen != null;
  }

  private void createRequiredError(Consumer<io.dialob.program.model.Error> errorConsumer) {
    Expression expression;
    expression = not(isAnswered(getId()));
    if (requiredWhen != null) {
      // Should not return null when requiredWhen is not blank
      expression = and(expression, isRequired(getId()));
    }
    errorConsumer.accept(ImmutableError.builder()
      .itemId(getId())
      .code(Constants.ERROR_CODE_REQUIRED)
      .isPrototype(getId().isPartial())
      .validationExpression(Operators.and(ImmutableIsActiveOperator.builder().itemId(getId()).build(), expression))
      .disabledExpression(ImmutableIsDisabledOperator.builder().itemId(getId()).build())
      .label(createLabelOperator(REQUIRED_LABEL)).build());
  }

  @Override
  public boolean compile(@Nonnull ItemId itemId, @Nonnull String expression, @Nonnull AliasesProvider aliasesProvider, @Nonnull Consumer<Expression> expressionConsumer, FormValidationError.Type type, Optional<Integer> index) {
    return compileExpression(expression, aliasesProvider, expressionConsumer, type, index);
  }

  public void addError(io.dialob.program.model.Error error) {
    errors.add(error);
  }

  public QuestionBuilder setRequired(Boolean required) {
    this.required = required;
    this.requiredWhen = null;
    return this;
  }

  public QuestionBuilder setRequiredWhen(String requiredWhen) {
    if (requiredWhen != null) {
      compileExpression(requiredWhen, this, this::setRequiredWhen, FormValidationError.Type.REQUIREMENT, Optional.empty());
    }
    this.required = null;
    return this;
  }

  public QuestionBuilder setRequiredWhen(Expression requiredWhen) {
    this.requiredWhen = requiredWhen;
    this.required = null;
    return this;
  }

  public Optional<String> getValueSetId() {
    return Optional.ofNullable(valueSetId);
  }

  public Optional<Object> getDefaultValue() {
    if ("note".equals(type)) {
      return Optional.empty();
    }
    return Optional.ofNullable(defaultValue);
  }

  @Override
  public Map<String, ItemId> getAliases() {
    Map<String, ItemId> aliases = super.getAliases();
    if (getHoistingGroup().map(groupBuilder -> groupBuilder.getType() == GroupBuilder.Type.ROWGROUP).orElse(false)) {
      aliases = getHoistingGroup().map(GroupBuilder::getItemIds).map(itemIds -> itemIds.stream().collect(toMap(
        itemId -> ((ItemRef) itemId).getId(), itemId -> itemId))).orElse(aliases);
    }
    return aliases;
  }
}
