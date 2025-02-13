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
package io.dialob.test.executor.session.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;

import io.dialob.executor.model.IdUtils;
import io.dialob.executor.model.ItemState;
import io.dialob.program.EvalContext;

public class ItemStateTest {


  @Test
  public void shouldClone() {
    ItemState itemState = new ItemState(IdUtils.toId("question1"), null, "text", null, true, null, null, null, null, null);
    ItemState itemState2 = new ItemState(itemState);
    assertNotSame(itemState, itemState2);
    assertEquals(itemState, itemState2);
    assertEquals(itemState.hashCode(), itemState2.hashCode());
  }

  @Test
  public void shouldNotCreateNewIfUpdateHaveNotEffect() {
    ItemState itemState = new ItemState(IdUtils.toId("question1"), null, "text", null, true, null, null, null, null, null);
    EvalContext context = mock(EvalContext.class);
    assertSame(itemState,
      itemState.update()
        .setStatus(ItemState.Status.NEW)
        .get());
    verifyNoMoreInteractions(context);
  }


  @Test
  public void shouldCreateNewIfUpdateHaveNotEffect() {
    ItemState itemState = new ItemState(IdUtils.toId("question1"), null, "text", null, true, null, null, null, null, null);
    EvalContext context = mock(EvalContext.class);
    ItemState itemState1 = itemState.update()
      .setStatus(ItemState.Status.OK)
      .get();
    assertNotSame(itemState,
      itemState1);
    assertEquals(ItemState.Status.OK, itemState1.getStatus());
    verifyNoMoreInteractions(context);
  }

}
