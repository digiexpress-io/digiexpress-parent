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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.dialob.executor.model.IdUtils;
import io.dialob.executor.model.ImmutableItemRef;
import io.dialob.executor.model.ItemState;
import io.dialob.program.EvalContext;
import io.dialob.program.expr.arith.ImmutableIsNullOperator;
import io.dialob.program.expr.arith.IsNullOperator;

public class IsNullOperatorTest {

  @Test
  public void shouldInspectItemById() {

    IsNullOperator operator = ImmutableIsNullOperator.builder().itemId((ImmutableItemRef) IdUtils.toId("itemi")).build();
    EvalContext context = Mockito.mock(EvalContext.class);
    ItemState item = new ItemState(IdUtils.toId("itemi"), null, "text", null, null);

    when(context.getItemState((ImmutableItemRef) IdUtils.toId("itemi"))).thenReturn(Optional.of(item));

    assertTrue(operator.eval(context));

    item = item.update().setValue("nonnull").get();
    when(context.getItemState((ImmutableItemRef) IdUtils.toId("itemi"))).thenReturn(Optional.of(item));
    assertFalse(operator.eval(context));

  }

}
