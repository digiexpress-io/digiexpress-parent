package io.resys.wrench.assets.flow.spi.expressions;

/*-
 * #%L
 * wrench-component-flow
 * %%
 * Copyright (C) 2016 - 2017 Copyright 2016 ReSys OÜ
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

import java.io.Serializable;
import java.util.Map;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class MapVariableExpression {
  private final MapPropertyAccessor accessor = new MapPropertyAccessor();
  private final ExpressionParser parser;

  public MapVariableExpression(ExpressionParser parser) {
    super();
    this.parser = parser;
  }

  public boolean eval(String expression, Map<String, Serializable> context) {
    Expression exp = parser.parseExpression(expression);

    StandardEvaluationContext evalContext = new StandardEvaluationContext(context);
    evalContext.addPropertyAccessor(accessor);

    return (boolean) exp.getValue(evalContext);
  }
}
