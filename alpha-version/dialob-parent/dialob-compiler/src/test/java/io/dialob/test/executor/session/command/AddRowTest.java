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
package io.dialob.test.executor.session.command;

import io.dialob.executor.command.AddRow;
import io.dialob.executor.command.CommandFactory;
import io.dialob.executor.model.IdUtils;
import io.dialob.executor.model.ItemState;
import io.dialob.program.EvalContext;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AddRowTest {

  @Test
  public void shouldAddRow() {
    AddRow addRow = CommandFactory.addRow(IdUtils.toId("rows"));

    EvalContext context = Mockito.mock(EvalContext.class);

    ItemState itemState = new ItemState(IdUtils.toId("rows"), null, "rowgroup", null, true, null, null, null, null, null);
    assertNull(itemState.getValue());

    itemState = addRow.update(context, itemState);
    assertEquals(1, ((List<Integer>)itemState.getValue()).size());
    org.assertj.core.api.Assertions.assertThat(((List<Integer>)itemState.getValue())).containsExactly(
      0
    );

    itemState = addRow.update(context, itemState);
    org.assertj.core.api.Assertions.assertThat(((List<Integer>)itemState.getValue())).containsExactly(
      0,
      1
    );
  }

  @Test
  public void shouldNotAddRowIfRowsCannotBeAdded() {
    AddRow addRow = CommandFactory.addRow(IdUtils.toId("rows"));
    EvalContext context = Mockito.mock(EvalContext.class);

    ItemState itemState = new ItemState(IdUtils.toId("rows"), null, "rowgroup", null, true, null, null, null, null, null);
    itemState = itemState.update().setRowsCanBeAdded(false).get();

    assertEquals(0, itemState.getItems().size());
    itemState = addRow.update(context, itemState);
    assertEquals(0, itemState.getItems().size());
  }
}
