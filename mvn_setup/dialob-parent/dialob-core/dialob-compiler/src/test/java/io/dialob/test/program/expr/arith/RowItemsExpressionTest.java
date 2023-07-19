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
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;

import io.dialob.executor.model.IdUtils;
import io.dialob.executor.model.ItemId;
import io.dialob.program.EvalContext;
import io.dialob.program.expr.arith.ImmutableRowItemsExpression;
import io.dialob.program.expr.arith.RowItemsExpression;

class RowItemsExpressionTest {

  @Test
  public void test() {
    EvalContext context = Mockito.mock(EvalContext.class);
    when(context.mapTo(any(), eq(true))).then(AdditionalAnswers.returnsFirstArg());
    RowItemsExpression rowItemsExpression = ImmutableRowItemsExpression.builder().addItemIds(IdUtils.toId("q1")).build();
    Collection<ItemId> ids = rowItemsExpression.eval(context);
    Assertions.assertIterableEquals(Arrays.asList(IdUtils.toId("q1")), ids);


  }

}
