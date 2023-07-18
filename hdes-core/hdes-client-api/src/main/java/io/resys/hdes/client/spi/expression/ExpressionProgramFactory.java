package io.resys.hdes.client.spi.expression;

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
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.hdes.client.api.ast.TypeDef.ValueType;
import io.resys.hdes.client.api.exceptions.DecisionAstException;
import io.resys.hdes.client.api.programs.ExpressionProgram;
import io.resys.hdes.client.api.programs.ImmutableExpressionResult;
import io.resys.hdes.client.spi.util.HdesAssert;


public class ExpressionProgramFactory {

  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {

    private ObjectMapper objectMapper;
    private String src;
    private ValueType valueType;

    public Builder objectMapper(ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
      return this;
    }
    public Builder src(String src) {
      this.src = src;
      return this;
    }

    public Builder valueType(ValueType valueType) {
      this.valueType = valueType;
      return this;
    }

    @SuppressWarnings({ "rawtypes" })
    public ExpressionProgram build() {
      HdesAssert.notNull(src, () -> "src can't be null!");
      HdesAssert.notNull(valueType, () -> "valueType can't be null!");

      try {
        final List<String> constants = new ArrayList<>();
        final Consumer<String> constantsConsumer = (String value) -> {
          if (!StringUtils.isEmpty(value)) {
            constants.add(value);
          }
        };

        Operation operation = null;
        switch (valueType) {
        case MAP: 
          HdesAssert.notNull(objectMapper, () -> "objectMapper can't be null!");
          operation = OperationMap.builder().build(src, constantsConsumer);
          break;
        case FLOW_CONTEXT: 
          HdesAssert.notNull(objectMapper, () -> "objectMapper can't be null!");
          operation = OperationFlowContext.builder().build(src, constantsConsumer);
          break;
        case STRING:
          HdesAssert.notNull(objectMapper, () -> "objectMapper can't be null!");
          operation = OperationString.builder(objectMapper).build(src, constantsConsumer);
          break;
        case BOOLEAN:
          operation = OperationBoolean.builder().build(src, constantsConsumer);
          break;
        case INTEGER:
        case LONG:
        case DECIMAL:
          operation = OperationNumber.builder().build(src, valueType, constantsConsumer);
          break;
        case DATE:
        case DATE_TIME:
          operation = OperationDate.builder().build(src, valueType, constantsConsumer);
          break;
        default:
          throw new DecisionAstException("Unknown type: " + valueType + "!");
        }

        return new ImmutableExpressionProgram(operation, valueType, Collections.unmodifiableList(constants), src);
      } catch (Exception e) {
        throw new DecisionAstException(e.getMessage(), e);
      }
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
          .value(expression.apply(entity))
          .type(type)
          .build();
    }
  }
}
