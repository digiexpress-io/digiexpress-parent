package io.resys.hdes.client.spi.expression;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class OperationMap {
  private final static ExpressionParser PARSER = new SpelExpressionParser();

  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    public Operation<?> build(String src, Consumer<String> constants) {
      Expression exp = PARSER.parseExpression(Objects.requireNonNull(src, "expression can't be null!"));
      return (Map<String, Object> input) -> {
        StandardEvaluationContext context = createContext(input);
        return exp.getValue(context);
      };
    }
  }

  private static StandardEvaluationContext createContext(Map<String, Object> contextExpression) {
    StandardEvaluationContext context = new StandardEvaluationContext();
    context.setVariables(contextExpression);
    context.addPropertyAccessor(new PropertyAccessor() {

      @Override
      public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
        // TODO Auto-generated method stub
      }

      @Override
      public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
        return new TypedValue(contextExpression.get(name));
      }

      @Override
      public Class<?>[] getSpecificTargetClasses() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
        // TODO Auto-generated method stub
        return false;
      }

      @Override
      public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
        return contextExpression.containsKey(name);
      }
    });

    //contextExpression.forEach((key, value) -> context.setVariable(key, value));
    return context;
  }
}
