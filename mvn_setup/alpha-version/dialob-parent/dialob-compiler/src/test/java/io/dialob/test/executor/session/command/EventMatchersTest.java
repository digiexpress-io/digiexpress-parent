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

import static io.dialob.executor.command.EventMatchers.anyError;
import static io.dialob.executor.command.EventMatchers.error;
import static io.dialob.executor.command.EventMatchers.errorActivity;
import static io.dialob.executor.command.EventMatchers.targetError;
import static io.dialob.executor.command.EventMatchers.whenActivePageUpdated;
import static io.dialob.executor.command.EventMatchers.whenActiveUpdated;
import static io.dialob.executor.command.EventMatchers.whenDisabledUpdatedEvent;
import static io.dialob.executor.command.EventMatchers.whenItemsChanged;
import static io.dialob.executor.command.Triggers.activePageUpdatedEvent;
import static io.dialob.executor.command.Triggers.activityUpdatedEvent;
import static io.dialob.executor.command.Triggers.availableItemsUpdatedEvent;
import static io.dialob.executor.command.Triggers.disabledUpdatedEvent;
import static io.dialob.executor.command.Triggers.errorActivityUpdatedEvent;
import static io.dialob.executor.command.Triggers.itemsChangedEvent;
import static io.dialob.executor.command.Triggers.onTarget;
import static io.dialob.executor.command.Triggers.validityUpdatedEvent;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.dialob.executor.model.IdUtils;
import io.dialob.executor.model.ImmutableErrorId;
import io.dialob.executor.model.ImmutableItemRef;
import io.dialob.executor.model.ItemId;

class EventMatchersTest {

  @Test
  public void activePageMatcher() {
    assertTrue(whenActivePageUpdated().matches(activePageUpdatedEvent()));
    assertFalse(whenActivePageUpdated().matches(availableItemsUpdatedEvent()));
  }

  @Test
  public void errorEventMatchers() {
    assertTrue(errorActivity(anyError()).matches(errorActivityUpdatedEvent(ImmutableErrorId.of(toRef("a"),"b"))));
    assertTrue(errorActivity(targetError(IdUtils.toId("a"))).matches(errorActivityUpdatedEvent(ImmutableErrorId.of(toRef("a"),"b"))));
    assertFalse(errorActivity(targetError(IdUtils.toId("b"))).matches(errorActivityUpdatedEvent(ImmutableErrorId.of(toRef("a"),"b"))));
    assertTrue(errorActivity(error(ImmutableErrorId.of(IdUtils.toId("a"),"b"))).matches(errorActivityUpdatedEvent(ImmutableErrorId.of(toRef("a"),"b"))));
    assertFalse(errorActivity(error(ImmutableErrorId.of(IdUtils.toId("b"),"a"))).matches(errorActivityUpdatedEvent(ImmutableErrorId.of(toRef("a"),"b"))));
  }

  private ItemId toRef(String a) {
    return (ImmutableItemRef) IdUtils.toId(a);
  }

  @Test
  public void isDisableEventMathcingTest() {
    assertTrue(whenDisabledUpdatedEvent(IdUtils.toId("page2")).matches(disabledUpdatedEvent(onTarget(toRef("page2")))));
    assertFalse(whenDisabledUpdatedEvent(IdUtils.toId("page2")).matches(disabledUpdatedEvent(onTarget(toRef("page1")))));
  }
  @Test
  public void isActiveEventMathcingTest() {
    assertTrue(whenActiveUpdated(IdUtils.toId("page2")).matches(activityUpdatedEvent(onTarget(toRef("page2")))));
    assertFalse(whenActiveUpdated(IdUtils.toId("page2")).matches(activityUpdatedEvent(onTarget(toRef("page1")))));
  }

  @Test
  public void validateUpdate() {
    assertFalse(whenActiveUpdated(IdUtils.toId("page2")).matches(validityUpdatedEvent(onTarget(toRef("page2")))));
    assertFalse(whenActiveUpdated(IdUtils.toId("page2")).matches(activityUpdatedEvent(onTarget(toRef("page1")))));
  }

  @Test
  public void itemsChanges() {
    assertTrue(whenItemsChanged(IdUtils.toId("g1")).matches(itemsChangedEvent(onTarget(toRef("g1")))));
    assertFalse(whenItemsChanged(IdUtils.toId("g1")).matches(itemsChangedEvent(onTarget(toRef("g2")))));
  }


}

