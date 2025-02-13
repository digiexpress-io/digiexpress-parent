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
package io.dialob.test.executor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.dialob.compiler.Utils;
import io.dialob.executor.model.IdUtils;
import io.dialob.executor.model.ItemState;

class UtilsTest {

  @Test
  public void shouldNotSerializeFalseInactiveState() {
    ItemState itemState = new ItemState(
      IdUtils.toId("id"),
      null,
      "text",
      null,
      null
    );
    Assertions.assertNull(Utils.toActionItem(itemState.update()
      .setActive(true).get(), null).getInactive());
    Assertions.assertTrue(Utils.toActionItem(itemState.update()
      .setActive(false).get(), null).getInactive());
  }
}
