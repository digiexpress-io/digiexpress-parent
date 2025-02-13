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

import io.dialob.api.form.FormValidationError;
import io.dialob.api.form.ImmutableFormValidationError;
import io.dialob.compiler.DialobProgramBuildException;
import io.dialob.compiler.spi.AliasesProvider;
import io.dialob.compiler.spi.ExpressionCompiler;
import io.dialob.executor.model.IdUtils;
import io.dialob.executor.model.ItemId;
import io.dialob.program.ddrl.DDRLExpressionCompiler;
import io.dialob.program.ddrl.UnknownValueTypeException;
import io.dialob.program.expr.DDRLOperatorFactory;
import io.dialob.program.expr.OperatorFactory;
import io.dialob.program.model.*;
import io.dialob.rule.parser.ParserUtil;
import io.dialob.rule.parser.api.RuleExpressionCompilerError;
import io.dialob.rule.parser.api.ValueType;
import io.dialob.rule.parser.api.VariableFinder;
import io.dialob.rule.parser.api.VariableNotDefinedException;
import io.dialob.rule.parser.function.FunctionRegistry;
import io.dialob.spi.Constants;
import lombok.extern.slf4j.Slf4j;
import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Value.Enclosing
public class ProgramBuilder implements ExpressionCompiler, BuilderParent, Builder<Program> {

  private final FunctionRegistry functionRegistry;

  private final OperatorFactory operatorFactory;

  private String id;

  private Item rootItem;

  private final List<Item> items = new ArrayList<>();

  private final Map<ItemId,AbstractItemBuilder<?,?>> types = new HashMap<>();

  private List<FormValidationError> errors = new ArrayList<>();

  private final List<ValueSet> valueSets = new ArrayList<>();

  public List<FormValidationError> getErrors() {
    return errors;
  }

  @Value.Immutable
  interface CompilableExpression {

    @Value.Parameter
    ItemId getItemId();

    @Value.Parameter
    String getExpression();

    @Value.Parameter
    AliasesProvider getAliasesProvider();

    @Value.Parameter
    Consumer<Expression> getExpressionConsumer();

    @Value.Parameter
    FormValidationError.Type getType();

    @Value.Parameter
    Optional<Integer> getIndex();

  }

  private final List<CompilableExpression> uncompiledExpressions = new ArrayList<>();

  private List<AbstractItemBuilder<?,ProgramBuilder>> builders = new ArrayList<>();

  public ProgramBuilder(@Nonnull FunctionRegistry functionRegistry) {
    this.functionRegistry = functionRegistry;
    this.operatorFactory = new DDRLOperatorFactory();
  }

  public void addItem(Item item) {
    // TODO verify conflicting id
    if ("questionnaire".equals(item.getType())) {
      assert rootItem == null;
      rootItem = item;
    } else {
      items.add(item);
    }
  }

  public void add(ValueSet valueSet) {
    valueSets.add(valueSet);
  }

  public ProgramBuilder startProgram() {
    return this;
  }

  public ProgramBuilder setId(String id) {
    this.id = id;
    return this;
  }

  @Nonnull
  private <T extends AbstractItemBuilder<?,ProgramBuilder>> T queue(@Nonnull T itemBuilder) {
    builders.add(itemBuilder);
    return itemBuilder;
  }

  public Optional<String> findValueSetIdForItem(@Nonnull ItemId itemId) {
    return findItemById(itemId).flatMap(abstractItemBuilder -> {
      if (abstractItemBuilder instanceof QuestionBuilder) {
        return ((QuestionBuilder) abstractItemBuilder).getValueSetId();
      }
      return Optional.empty();
    });
  }

  public Optional<Object> findDefaultValueForItem(@Nonnull ItemId itemId) {
    return findItemById(itemId).flatMap(abstractItemBuilder -> {
      if (abstractItemBuilder instanceof HasDefaultValue) {
        return ((HasDefaultValue) abstractItemBuilder).getDefaultValue();
      }
      return Optional.empty();
    });
  }

  public Optional<AbstractItemBuilder<?,?>> findItemById(@Nonnull ItemId itemId) {
    for (AbstractItemBuilder<?,?> item : builders) {
      if (itemId.equals(item.getId())) {
        return Optional.of(item);
      }
    }
    return Optional.empty();
  }

  public GroupBuilder addRoot() {
    return queue(new GroupBuilder(this, null, Constants.QUESTIONNAIRE).root());
  }

  public GroupBuilder addPage(String id) {
    return queue(new GroupBuilder(this, findHoistingGroup(Constants.QUESTIONNAIRE).orElse(null), id).page());
  }

  public GroupBuilder addGroup(String id) {
    return queue(new GroupBuilder(this, findHoistingGroup(id).orElse(null), id).group());
  }

  public GroupBuilder addSurveyGroup(String id) {
    return queue(new GroupBuilder(this, findHoistingGroup(id).orElse(null), id).surveyGroup());
  }

  public GroupBuilder addRowGroup(String id) {
    return queue(new GroupBuilder(this, findHoistingGroup(id).orElse(null), id).rowgroup());
  }

  public QuestionBuilder addQuestion(String id) {
    return queue(new QuestionBuilder(this, findHoistingGroup(id).orElse(null), id));
  }

  public VariableBuilder addVariable(String id) {
    return queue(new VariableBuilder(this, id));
  }

  public ValueSetBuilder addValueSet(String id) {
    return queue(new ValueSetBuilder(this, id));
  }

  @Override
  public boolean compile(@Nonnull ItemId itemId,
                         @Nonnull String expression,
                         @Nonnull AliasesProvider aliasesProvider,
                         @Nonnull Consumer<Expression> expressionConsumer,
                         @Nonnull FormValidationError.Type type,
                         Optional<Integer> index) {
    if (isBlank(expression)) {
      return false;
    }
    uncompiledExpressions.add(ImmutableProgramBuilder.CompilableExpression.of(itemId, expression, aliasesProvider, expressionConsumer, type, index));
    return true;
  }


  private boolean compileExpressions() {
    DDRLExpressionCompiler ddrlExpressionCompiler = createDdrlExpressionCompiler();
    // TODO dependency ordering
    int expressionCount = uncompiledExpressions.size();
    // loop until all expressions have been compiled.. would not be necessary if compilation could be done in two phases.
    // 1. ast generation
    // 2. type analysis
    final List<FormValidationError> errors = new ArrayList<>();
    while (expressionCount > 0) {
      errors.clear();
      uncompiledExpressions.removeIf(compilableExpression ->
        compileExpression(compilableExpression.getItemId().getParent().map(IdUtils::toString).orElse(null), ddrlExpressionCompiler, compilableExpression, error ->
          errors.add(ImmutableFormValidationError.builder()
           .itemId(IdUtils.toString(compilableExpression.getItemId()))
           .type(compilableExpression.getType())
           .startIndex(error.getSpan().getStartIndex())
           .endIndex(error.getSpan().getStopIndex())
           .message(error.getErrorCode())
           .index(compilableExpression.getIndex())
           .build())
        ).map(expr -> {
          compilableExpression.getExpressionConsumer().accept(expr);
          return true;
        }).orElse(false));
      if (uncompiledExpressions.size() == expressionCount) {
        // could not compile any more expressions
        break;
      }
      expressionCount = uncompiledExpressions.size();
    }
    if (!uncompiledExpressions.isEmpty()) {
      this.errors.addAll(errors);
      log.debug("Could not compile all expressions: {}", uncompiledExpressions);
      return false;
    }
    ddrlExpressionCompiler.getAsyncFunctionVariableExpressions().entrySet().forEach(stringExpressionEntry -> {
      addItem(
        ImmutableVariableItem.builder()
          .id(IdUtils.toId(stringExpressionEntry.getKey()))
          .type("variable")
          .isPrototype(false)
          .isAsync(true)
          .valueExpression(stringExpressionEntry.getValue())
          .build());
    });
    return true;
  }

  private DDRLExpressionCompiler createDdrlExpressionCompiler() {
    return new DDRLExpressionCompiler(operatorFactory);
  }

  private Optional<Expression> compileExpression(@Nonnull String scope,
                                                 @Nonnull DDRLExpressionCompiler ddrlExpressionCompiler,
                                                 @Nonnull CompilableExpression compilableExpression,
                                                 @Nonnull Consumer<RuleExpressionCompilerError> errorConsumer) {
      // TODO maybe compiling 2 phases is more reliable
    try {
      ProgramVariableFinder variableFinder = new ProgramVariableFinder(compilableExpression.getAliasesProvider().getAliases(), scope);
      return ddrlExpressionCompiler.compile(variableFinder, compilableExpression.getExpression(), errorConsumer);
    } catch (UnknownValueTypeException e) {
      log.error("error: {}", e.getMessage());
    }
    return Optional.empty();
  }

  @Override
  public Program build() {
    Objects.requireNonNull(id, "id missing");

    builders.forEach(AbstractItemBuilder::setupId);

    this.types.clear();
    this.types.putAll(builders.stream()
      .filter(builder -> builder instanceof QuestionBuilder
        || builder instanceof VariableBuilder
        || (builder instanceof GroupBuilder && ((GroupBuilder) builder).getType() == GroupBuilder.Type.ROWGROUP))
      .collect(Collectors.toMap(AbstractItemBuilder::getId, builder -> builder)));
    beforeExpressionCompilation(errors::add);
    if (!compileExpressions()) {
      return null;
    }
    afterExpressionCompilation(errors::add);
    if (rootItem == null) {
      throw new DialobProgramBuildException("Form do not have root");
    }
    return ImmutableProgram.builder()
      .id(id)
      .rootItem(rootItem)
      .items(items)
      .valueSets(valueSets)
      .build();
  }

  private void beforeExpressionCompilation(Consumer<FormValidationError> errorConsumer) {
    builders.forEach(programBuilderAbstractItemBuilder -> programBuilderAbstractItemBuilder.beforeExpressionCompilation(errorConsumer));
  }

  private void afterExpressionCompilation(Consumer<FormValidationError> errorConsumer) {
    builders.forEach(programBuilderAbstractItemBuilder -> programBuilderAbstractItemBuilder.afterExpressionCompilation(errorConsumer));
  }

  public Optional<GroupBuilder> findHoistingGroup(String id) {
    for (AbstractItemBuilder<?, ?> builder : builders) {
      if (builder instanceof GroupBuilder) {
        GroupBuilder groupBuilder = (GroupBuilder) builder;
        if (groupBuilder.hoistsItem(id)) {
          return Optional.of(groupBuilder);
        }
      }
    }
    return Optional.empty();
  }

  public Optional<AbstractItemBuilder<?,ProgramBuilder>> findItemBuilder(@Nonnull String id) {
    for (AbstractItemBuilder<?, ProgramBuilder> builder : builders) {
      if(id.equals(builder.getIdStr())) {
        return Optional.of(builder);
      }
    }
    return Optional.empty();
  }

  public Optional<ItemId> findHoistingGroupId(String id) {
    return findHoistingGroup(id).map(GroupBuilder::getId);
  }

  private class ProgramVariableFinder implements VariableFinder {

    private final Map<String,ItemId> aliases;

    private String scope;

    private ProgramVariableFinder(Map<String, ItemId> aliases, String scope) {
      this.aliases = aliases;
      this.scope = scope;
    }

    private ProgramVariableFinder withScope(String scope) {
      return new ProgramVariableFinder(this.aliases, scope);
    }

    @Override
    @Nullable
    public String getScope() {
      return scope;
    }

    @Override
    public ValueType typeOf(String variableName) throws VariableNotDefinedException {
      switch (variableName) {
        case "language":
          return ValueType.STRING;
      }
      AbstractItemBuilder<?, ?> abstractItemBuilder = findVariable(variableName, true);
      if (abstractItemBuilder != null) {
        Optional<ValueType> valueType = abstractItemBuilder.getValueType();
        if (valueType.isPresent()) {
          if (abstractItemBuilder.getId().isPartial() && !Objects.equals(scope, abstractItemBuilder.getId().getParent().map(IdUtils::toString).orElse(null))) {
            return ValueType.arrayOf(valueType.get());
          }
          return valueType.get();
        }
      }
      throw new VariableNotDefinedException(variableName);
    }

    @Override
    public Optional<String> findVariableScope(String variableName) {
      AbstractItemBuilder<?, ?> abstractItemBuilder = findVariable(variableName, true);
      if (abstractItemBuilder == null) {
        return Optional.empty();
      }
      ItemId itemId = abstractItemBuilder.getId();
      return itemId.getParent().map(IdUtils::toString).filter(varScope -> !Objects.equals(scope, varScope));
    }

    @Override
    @Nonnull
    public String mapAlias(String aliasName) {
      if (aliases.containsKey(aliasName)) {
        return IdUtils.toString(aliases.get(aliasName));
      }
      return aliasName;
    }

    @Override
    public ValueType returnTypeOf(String functionName, ValueType... argTypes) throws VariableNotDefinedException {
      if (ParserUtil.isReducerOperator(functionName)) {
        if (argTypes[0] != null && argTypes[0].isArray()) {
          return argTypes[0].getItemValueType();
        }
        return argTypes[0];
      }
      return functionRegistry.returnTypeOf(functionName, argTypes);
    }

    @Override
    public boolean isAsync(String functionName) {
      return functionRegistry.isAsyncFunction(functionName);
    }

  }

  protected AbstractItemBuilder<?, ?> findVariable(@NotNull String variableName, boolean includePrototypes) {
    AbstractItemBuilder<?, ?> abstractItemBuilder = types.get(IdUtils.toId(variableName));
    if (abstractItemBuilder != null) {
      return abstractItemBuilder;
    }
    if (includePrototypes) {
      return types.keySet().stream().filter(itemId -> variableName.equals(itemId.getValue())).findFirst().map(types::get).orElse(null);
    }
    return null;

  }
}
