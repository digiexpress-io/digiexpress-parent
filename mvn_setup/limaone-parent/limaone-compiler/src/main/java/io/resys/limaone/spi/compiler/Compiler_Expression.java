package io.resys.limaone.spi.compiler;

/*-
 * #%L
 * wrench-assets-dt
 * %%
 * Copyright (C) 2016 - 2019 Copyright 2016 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import io.resys.limaone.model.Parameter.ValueType;
import io.resys.limaone.program.ExpressionProgram;
import io.resys.limaone.program.ImmutableExpressionResult;
import io.resys.limaone.spi.LocalCache;
import io.resys.limaone.spi.LocalCache.ExpressionCacheKey;
import io.resys.limaone.spi.program.expression.ExpressionException;
import io.resys.limaone.spi.program.expression.Operation;
import io.resys.limaone.spi.program.expression.OperationBoolean;
import io.resys.limaone.spi.program.expression.OperationDate;
import io.resys.limaone.spi.program.expression.OperationContext;
import io.resys.limaone.spi.program.expression.OperationMap;
import io.resys.limaone.spi.program.expression.OperationNumber;
import io.resys.limaone.spi.program.expression.OperationString;


public class Compiler_Expression {

  public static ExpressionProgram build(String src, ValueType valueType) {
    Objects.requireNonNull(src, () -> "src can't be null!");
    Objects.requireNonNull(valueType, () -> "valueType can't be null!");
    final var key = new ExpressionCacheKey(src, valueType);
    final Function<ExpressionCacheKey, ExpressionProgram> mappingFunction = (k) -> buildProgram(k.getSrc(), k.getValueType()); 
    return LocalCache.computeIfAbsent(key, mappingFunction);
  }


  private static ExpressionProgram buildProgram(String src, ValueType valueType) {
    try {
      final List<String> constants = new ArrayList<>();
      final Consumer<String> constantsConsumer = (String value) -> {
        if (!StringUtils.isEmpty(value)) {
          constants.add(value);
        }
      };

      Optional<ValueType> override = Optional.empty();
      Operation<?> operation = null;
      switch (valueType) {
      case MAP: 
        operation = OperationMap.build(src, constantsConsumer);
        break;
      case FLOW_CONTEXT: 
        operation = OperationContext.build(src, constantsConsumer);
        break;
      case STRING:
        operation = OperationString.build(src, constantsConsumer);
        break;
      case BOOLEAN:
        operation = OperationBoolean.build(src, constantsConsumer);
        break;
      case INTEGER:
      case LONG:
      case DECIMAL:
        try {
          operation = OperationNumber.build(src, valueType, constantsConsumer);
        } catch(Exception e) {
          try {
            operation = OperationString.build(src, constantsConsumer);
            override = Optional.of(ValueType.STRING);
          } catch(Exception x) {
            throw e;
          }
        }
        break;
      case DATE:
      case DATE_TIME:
        operation = OperationDate.build(src, valueType, constantsConsumer);
        break;
      default:
        throw new ExpressionException("Unknown type: " + valueType + "!");
      }

      return new ImmutableExpressionProgram(operation, override.orElse(valueType), Collections.unmodifiableList(constants), src);
    } catch(ExpressionException e) {
      throw e;
    } catch (Exception e) {
      throw new ExpressionException(e.getMessage(), e);
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static class ImmutableExpressionProgram implements ExpressionProgram {
    private final Operation expression;
    private final String src;
    private final ValueType type;
    private final List<String> constants;

    public ImmutableExpressionProgram(Operation expression, ValueType type, List<String> constants, String src) {
      super();
      this.expression = expression;
      this.type = type;
      this.constants = constants;
      this.src = src;
    }
    @Override
    public String getSrc() {
      return src;
    }
    @Override
    public ValueType getType() {
      return type;
    }
    @Override
    public List<String> getConstants() {
      return constants;
    }
    @Override
    public ExpressionResult run(Object entity) {
      return ImmutableExpressionResult.builder()
          .constants(constants)
          .src(src)
          .value(expression.apply(entity))
          .type(type)
          .build();
    }
  }
}
