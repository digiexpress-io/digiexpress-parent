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

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.dialob.program.EvalContext;
import io.dialob.program.expr.arith.EqOperator;
import io.dialob.program.expr.arith.ImmutableEqOperator;
import io.dialob.program.model.Expression;
import io.dialob.rule.parser.api.ValueType;

public class EqOperatorTest {

  @Test
  public void testLogic() {
    Expression lh = Mockito.mock(Expression.class);
    Expression rh = Mockito.mock(Expression.class);
    EqOperator operator = newEq(lh, rh);
    EvalContext context = Mockito.mock(EvalContext.class);

    when(lh.eval(context)).thenReturn("lr");
    when(rh.eval(context)).thenReturn("rr");
    Assertions.assertFalse(operator.eval(context));

    when(lh.eval(context)).thenReturn("same");
    when(rh.eval(context)).thenReturn("same");
    Assertions.assertTrue(operator.eval(context));

    // TODO should return null instead?
    when(lh.eval(context)).thenReturn("nonnull");
    when(rh.eval(context)).thenReturn(null);
    Assertions.assertFalse(operator.eval(context));

    when(lh.eval(context)).thenReturn(null);
    when(rh.eval(context)).thenReturn("nonnull");
    Assertions.assertFalse(operator.eval(context));

    when(lh.eval(context)).thenReturn(null);
    when(rh.eval(context)).thenReturn(null);
    Assertions.assertTrue(operator.eval(context));
  }

  protected EqOperator newEq(Expression lh, Expression rh) {
    return ImmutableEqOperator.builder().lhs(lh).rhs(rh).build();
  }

  @Test
  public void shouldEvalToBoolean() {
    Expression lh = Mockito.mock(Expression.class);
    Expression rh = Mockito.mock(Expression.class);
    EqOperator operator = newEq(lh, rh);
    Assertions.assertEquals(ValueType.BOOLEAN, operator.getValueType());
  }

}
