package io.resys.hdes.client.spi.expression;

import java.util.ArrayList;
import java.util.List;

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
import java.util.function.Consumer;

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelNode;
import org.springframework.expression.spel.ast.CompoundExpression;
import org.springframework.expression.spel.ast.PropertyOrFieldReference;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.Assert;

import io.resys.hdes.client.api.exceptions.ProgramException;

public class OperationFlowContext {
  private final static ExpressionParser PARSER = new SpelExpressionParser();
  private final static MapPropertyAccessor mapAccessor = new MapPropertyAccessor();
  private final static FlowContextPropertyAccessor contextAccessor = new FlowContextPropertyAccessor();
  
  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    public Operation<?> build(String src, Consumer<String> constants) {
      Assert.notNull(src, "expression can't be null!");
      SpelExpression exp = (SpelExpression) PARSER.parseExpression(src);
      
      List<String> inputs = new ArrayList<>();
      getInputs(exp.getAST(), inputs);
      inputs.forEach(constants);
      
      return (FlowTaskExpressionContext context) -> {
        try {
          StandardEvaluationContext evalContext = new StandardEvaluationContext(context);
          evalContext.addPropertyAccessor(mapAccessor);
          evalContext.addPropertyAccessor(contextAccessor);
          return (boolean) exp.getValue(evalContext);
        } catch(SpelEvaluationException e) {
          
          throw new ProgramException(
              "Expression: '" + src + "' failed. "  + System.lineSeparator() + 
              e.getMessage(), e);
        }
      };
    }
  }

  private static void getInputs(SpelNode node, List<String> inputs) {
    if(node instanceof CompoundExpression) {
      CompoundExpression ref = (CompoundExpression) node;
      inputs.add(ref.toStringAST());
      return;
    } else if(node instanceof PropertyOrFieldReference) {
      PropertyOrFieldReference ref = (PropertyOrFieldReference) node;
      inputs.add(ref.getName());
    }

    for(int index = 0; index < node.getChildCount(); index++) {
      getInputs(node.getChild(index), inputs);
    }
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static class MapPropertyAccessor implements PropertyAccessor {
    @Override
    public Class<?>[] getSpecificTargetClasses() {
      return new Class<?>[] { Map.class };
    }
    @Override
    public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
      return target instanceof Map;
    }
    @Override
    public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
       Object object = ((Map) target).get(name);
       return new TypedValue(object);
    }
    @Override
    public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
      return target instanceof Map;
    }
    @Override
    public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
      ((Map) target).put(name, newValue);
    }
  }

  
  private static class FlowContextPropertyAccessor implements PropertyAccessor {
    @Override
    public Class<?>[] getSpecificTargetClasses() {
      return new Class<?>[] { FlowTaskExpressionContext.class };
    }
    @Override
    public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
      return target instanceof FlowTaskExpressionContext;
    }
    @Override
    public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
       Object object = ((FlowTaskExpressionContext) target).apply(name);
       return new TypedValue(object);
    }
    @Override
    public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
      return false;
    }
    @Override
    public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {}
  }

  
  public interface FlowTaskExpressionContext {
    Object apply(String name);
  }
}
