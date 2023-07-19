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
package io.dialob.test.program;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import io.dialob.api.form.FormValidationError;
import io.dialob.executor.model.IdUtils;
import io.dialob.program.GroupBuilder;
import io.dialob.program.ProgramBuilder;
import io.dialob.program.QuestionBuilder;
import io.dialob.program.expr.arith.ImmutableRowItemsExpression;
import io.dialob.program.model.Group;
import io.dialob.program.model.Item;

public class GroupBuilderTest {

  @Test
  public void rowGroupGeneratesTeoGroups() {
    Consumer<FormValidationError> errorConsumer = mock(Consumer.class);
    ProgramBuilder programBuilder = Mockito.mock(ProgramBuilder.class);
    GroupBuilder hoistingGroupBuilder = Mockito.mock(GroupBuilder.class);
    QuestionBuilder qb = mock(QuestionBuilder.class);
    when(programBuilder.findItemBuilder("q1")).thenReturn(Optional.of(qb));
    when(programBuilder.findItemBuilder("q2")).thenReturn(Optional.of(qb));
    when(qb.getId()).thenReturn(IdUtils.toId("q1"), IdUtils.toId("q2"));

    GroupBuilder groupBuilder = new GroupBuilder(programBuilder, null, "group1");
    groupBuilder.rowgroup();
    groupBuilder.addItems(Arrays.asList("q1","q2"));
    groupBuilder.beforeExpressionCompilation(errorConsumer);
    groupBuilder.afterExpressionCompilation(errorConsumer);
    groupBuilder.build();


    final ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);

    verify(programBuilder, times(2)).addItem(captor.capture());
    List<Item> addedItems = captor.getAllValues();
    Assertions.assertEquals(IdUtils.toId("group1"), addedItems.get(0).getId());
//    Assertions.assertEquals(IdUtils.toId("group1"), ((Group)addedItems.get(0)).getItemsExpression());
    Assertions.assertEquals(IdUtils.toId("group1.*"), addedItems.get(1).getId());
    Assertions.assertEquals(
      ImmutableRowItemsExpression.builder()
        .addItemIds(
          IdUtils.toId("group1.*.q1"),
          IdUtils.toId("group1.*.q2")
        )
        .build(),
      ((Group)addedItems.get(1)).getItemsExpression());

    verify(qb, times(2)).getId();
    verify(programBuilder).findItemBuilder("q1");
    verify(programBuilder).findItemBuilder("q2");
    verifyNoMoreInteractions(programBuilder, hoistingGroupBuilder, qb, errorConsumer);

  }

}
