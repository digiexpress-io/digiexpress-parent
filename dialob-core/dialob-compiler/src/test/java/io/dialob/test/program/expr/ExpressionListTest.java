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
package io.dialob.test.program.expr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.dialob.program.EvalContext;
import io.dialob.program.expr.ExpressionList;
import io.dialob.program.expr.ImmutableExpressionList;
import io.dialob.program.model.Expression;

class ExpressionListTest {

  @Test
  public void shouldFlatLists() {
    Expression expr = Mockito.mock(Expression.class);
    EvalContext context = Mockito.mock(EvalContext.class);
    ExpressionList list = ImmutableExpressionList.builder()
      .addExpressions(expr)
      .build();

    Mockito.when(expr.eval(context)).thenReturn(Arrays.asList());
    Assertions.assertEquals(Arrays.asList(), list.eval(context));

    Mockito.when(expr.eval(context)).thenReturn(Arrays.asList("a"));
    Assertions.assertEquals(Arrays.asList("a"), list.eval(context));

    Mockito.when(expr.eval(context)).thenReturn("a");
    Assertions.assertEquals(Arrays.asList("a"), list.eval(context));


    List arrayList = new ArrayList<>();
    arrayList.add(null);
    Mockito.when(expr.eval(context)).thenReturn(arrayList);
    Assertions.assertEquals(Arrays.asList(), list.eval(context));
  }
}
