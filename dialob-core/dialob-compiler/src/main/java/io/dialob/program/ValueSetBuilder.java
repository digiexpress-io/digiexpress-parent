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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import io.dialob.api.form.FormValidationError;
import io.dialob.compiler.spi.AliasesProvider;
import io.dialob.compiler.spi.ExpressionCompiler;
import io.dialob.executor.model.ItemId;
import io.dialob.program.model.Expression;
import io.dialob.program.model.ImmutableValueSet;
import io.dialob.program.model.Value;
import io.dialob.program.model.ValueSet;

public class ValueSetBuilder extends AbstractItemBuilder<ValueSetBuilder, ProgramBuilder> implements ExpressionCompiler, BuilderParent {

  private List<ValueSetEntryBuilder> valueSetEntryBuilders = new ArrayList<>();

  private List<Value<ValueSet.Entry>> values = new ArrayList<>();

  public ValueSetBuilder(ProgramBuilder programBuilder, String id) {
    super(programBuilder, programBuilder, null, id);
  }

  @Override
  protected void afterExpressionCompilation(Consumer<FormValidationError> errorConsumer) {
    valueSetEntryBuilders.forEach(valueSetEntryBuilder -> valueSetEntryBuilder.afterExpressionCompilation(errorConsumer));
    getProgramBuilder().add(ImmutableValueSet.builder().id(getIdStr()).entries(values).build());
  }

  public ValueSetEntryBuilder addValue(String id) {
    ValueSetEntryBuilder entryBuilder = new ValueSetEntryBuilder(this, id, valueSetEntryBuilders.size());
    valueSetEntryBuilders.add(entryBuilder);
    return entryBuilder;
  }

  void addValueEntry(Value<ValueSet.Entry> entry) {
    values.add(entry);
  }

  @Override
  protected void doBuild() {
    valueSetEntryBuilders.forEach(AbstractItemBuilder::build);
  }

  @Override
  public boolean compile(@NotNull ItemId itemId, @NotNull String expression, @NotNull AliasesProvider aliasesProvider, @NotNull Consumer<Expression> consumer, @NotNull FormValidationError.Type type, Optional<Integer> index) {
    return getParent().compile(itemId, expression, aliasesProvider, consumer, type, index);
  }
}
