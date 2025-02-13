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
package io.dialob.test.program.expr.arith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import io.dialob.program.EvalContext;
import io.dialob.program.expr.arith.FunctionCallOperator;
import io.dialob.program.expr.arith.ImmutableFunctionCallOperator;
import io.dialob.program.model.Expression;
import io.dialob.rule.parser.api.ValueType;
import io.dialob.rule.parser.function.FunctionRegistry;

class FunctionCallOperatorTest {

  @Test
  public void shouldInvokeFunction() {

    Expression arg1 = mock(Expression.class);
    EvalContext context = mock(EvalContext.class);
    FunctionRegistry functionRegistry = mock(FunctionRegistry.class);

    when(context.getFunctionRegistry()).thenReturn(functionRegistry);
    when(arg1.eval(context)).thenReturn("hello");

    FunctionCallOperator op = ImmutableFunctionCallOperator.builder()
      .functionName("func")
      .valueType(ValueType.STRING)
      .addArgs(arg1)
      .build();

    op.eval(context);

    verify(context).getFunctionRegistry();
    verify(arg1).eval(context);
    verify(functionRegistry).isAsyncFunction("func");
    verify(functionRegistry).invokeFunction(any(), eq("func"), eq("hello"));
    verifyNoMoreInteractions(arg1, context, functionRegistry);
  }

}
