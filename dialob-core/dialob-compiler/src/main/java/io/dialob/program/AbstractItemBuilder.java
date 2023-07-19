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

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import io.dialob.api.form.FormValidationError;
import io.dialob.api.form.ImmutableFormValidationError;
import io.dialob.compiler.spi.AliasesProvider;
import io.dialob.compiler.spi.ExpressionCompiler;
import io.dialob.executor.model.IdUtils;
import io.dialob.executor.model.ImmutableItemIdPartial;
import io.dialob.executor.model.ImmutableItemRef;
import io.dialob.executor.model.ItemId;
import io.dialob.program.expr.arith.BooleanOperators;
import io.dialob.program.expr.arith.LocalizedLabelOperator;
import io.dialob.program.model.Expression;
import io.dialob.program.model.ImmutableConditionalValue;
import io.dialob.program.model.ImmutableConstantValue;
import io.dialob.program.model.ImmutableLabel;
import io.dialob.program.model.Label;
import io.dialob.program.model.Value;
import io.dialob.rule.parser.api.ValueType;

@SuppressWarnings("unchecked")
public abstract class AbstractItemBuilder<T extends AbstractItemBuilder<T,P>,P extends ExpressionCompiler & BuilderParent> implements Builder<P>, AliasesProvider {

  public static final ImmutableLabel EMPTY_LABEL = ImmutableLabel.builder().build();

  private final ProgramBuilder programBuilder;

  private final GroupBuilder hoistingGroupBuilder;

  private final P parent;

  private final String id;

  private ItemId itemId;

  List<Value<String>> classNames = new ArrayList<>();

  protected Label label = EMPTY_LABEL;

  protected Label description = EMPTY_LABEL;

  protected Expression activeWhen;

  protected Map<String, ? extends Object> props = null;

  public AbstractItemBuilder(ProgramBuilder programBuilder, P parent, GroupBuilder hoistingGroupBuilder, @Nonnull String id) {
    this.programBuilder = programBuilder;
    this.parent = parent;
    this.hoistingGroupBuilder = hoistingGroupBuilder;
    this.id = requireNonNull(id);
    this.setActiveWhen(BooleanOperators.TRUE);
  }

  public ProgramBuilder getProgramBuilder() {
    return programBuilder;
  }

  P getParent() {
    return parent;
  }

  boolean compileExpression(@Nonnull String expression, @Nonnull Consumer<Expression> expressionConsumer, FormValidationError.Type type, Optional<Integer> index) {
    return compileExpression(expression, this, expressionConsumer, type, index);
  }

  boolean compileExpression(@Nonnull String expression, @Nonnull AliasesProvider aliasesProvider, @Nonnull Consumer<Expression> expressionConsumer, @Nonnull FormValidationError.Type type, Optional<Integer> index) {
    return getParent().compile(getId(), expression, aliasesProvider, expressionConsumer, type, index);
  }

  public T addClassname(@Nonnull String className) {
    return (T) addClassname(ImmutableConstantValue.<String>builder().value(className).valueType(ValueType.STRING).build());
  }

  public T addClassname(String when, @Nonnull String className) {
    if (!compileExpression(when, expression -> addClassname(ImmutableConditionalValue.<String>builder().when(expression).value(className).valueType(ValueType.STRING).build()), FormValidationError.Type.CLASSNAME, Optional.empty())) {
      addClassname(className);
    }
    return (T) this;
  }

  private T addClassname(Value<String> value) {
    classNames.add(value);
    return (T) this;
  }

  public T setLabel(Map<String, String> label) {
    this.label = ImmutableLabel.builder().putAllLabels(label).build();
    return (T) this;
  }

  public T setLabel(String language, String label) {
    this.label = ImmutableLabel.builder().from(this.label).putLabels(language, label).build();
    return (T) this;
  }

  public T setDescription(Map<String,String> description) {
    this.description = ImmutableLabel.builder().putAllLabels(description).build();
    return (T) this;
  }

  public T setDescription(String language, String description) {
    this.description = ImmutableLabel.builder().from(this.description).putLabels(language, description).build();
    return (T) this;
  }

  public T setActiveWhen(String activeWhen) {
    if (StringUtils.isNotBlank(activeWhen)) {
      compileExpression(activeWhen, this::setActiveWhen, getActiveWhenExpressionErrorType(), getIndex());
    }
    return (T) this;
  }

  protected Optional<Integer> getIndex() {
    return Optional.empty();
  }

  @NotNull
  protected FormValidationError.Type getActiveWhenExpressionErrorType() {
    return FormValidationError.Type.VISIBILITY;
  }

  public T setProps(Map<String, Object> props) {
    this.props = props;
    return (T) this;
  }

  public Map<String, ItemId> getAliases() {
    return Collections.emptyMap();
  }

  protected T setActiveWhen(Expression activeWhen) {
    this.activeWhen = activeWhen;
    return (T) this;
  }

  protected void requireBooleanExpression(Expression expression, FormValidationError.Type type, Consumer<FormValidationError> errorConsumer) {
    if (expression != null && expression.getValueType() != ValueType.BOOLEAN) {
      errorConsumer.accept(ImmutableFormValidationError.builder()
        .itemId(id)
        .message("BOOLEAN_EXPRESSION_EXPECTED")
        .type(type)
        .index(getIndex())
        .build());
    }
  }

  public T addClassnames(List<String> classNames) {
    classNames.stream()
      .map(className -> ImmutableConstantValue.<String>builder().value(className).valueType(ValueType.STRING).build())
      .forEach(this.classNames::add);
    return (T) this;
  }

  @Override
  public P build() {
    doBuild();
    return parent;
  }

  protected void doBuild() {
  }

  protected void beforeExpressionCompilation(Consumer<FormValidationError> errorConsumer) {
  }

  protected void afterExpressionCompilation(Consumer<FormValidationError> errorConsumer) {
    Objects.requireNonNull(id, "id may not be null");
    requireBooleanExpression(activeWhen, getActiveWhenExpressionErrorType(), errorConsumer);
  }

  @Nonnull
  public ItemId getId() {
    if (itemId == null) {
      setupId();
    }
    return requireNonNull(itemId);
  }

  String getIdStr() {
    return id;
  }

  void setupId() {
    // FormVisitor guarantees hierarchical visiting order
    this.itemId = findHostingRowgroupId().map(hostingGroup -> ImmutableItemRef.of(id, Optional.of(ImmutableItemIdPartial.of(Optional.of(hostingGroup))))).orElse((ImmutableItemRef) IdUtils.toId(id));
  }

  public Optional<ValueType> getValueType() {
    return Optional.empty();
  }

  LocalizedLabelOperator createLabelOperator(Map<String, String> label) {
    return createLabelOperator(Label.createLabel(label));
  }
  LocalizedLabelOperator createLabelOperator(Label label) {
    return LocalizedLabelOperator.createLocalizedLabelOperator(getProgramBuilder(), label);
  }

  public Optional<GroupBuilder> getHoistingGroup() {
    return Optional.ofNullable(hoistingGroupBuilder);
  }

  protected Optional<ItemId> findHostingRowgroupId() {
    return getHoistingGroup()
      .map(groupBuilder -> groupBuilder.getType() == GroupBuilder.Type.ROWGROUP ? groupBuilder : null)
      .map(AbstractItemBuilder::getId);
  }

}
