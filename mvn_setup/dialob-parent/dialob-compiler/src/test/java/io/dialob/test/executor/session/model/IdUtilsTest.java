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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.dialob.executor.model.IdUtils;
import io.dialob.executor.model.ImmutableItemIdPartial;
import io.dialob.executor.model.ImmutableItemIndex;
import io.dialob.executor.model.ImmutableItemRef;
import io.dialob.executor.model.ItemId;
import io.dialob.executor.model.ValueSetId;

class IdUtilsTest {

  @Test
  public void testToString() {
    assertNull(IdUtils.toString((ItemId) null));
    assertNull(IdUtils.toString((ValueSetId) null));
    assertEquals("var1", IdUtils.toString(IdUtils.toId("var1")));
    assertEquals("1", IdUtils.toString(ImmutableItemIndex.of(1,Optional.empty())));
    assertEquals("10", IdUtils.toString(ImmutableItemIndex.of(10,Optional.empty())));
    assertEquals("10.var1", IdUtils.toString(ImmutableItemRef.of("var1", Optional.of(ImmutableItemIndex.of(10,Optional.empty())))));
    assertEquals("a.10.var1", IdUtils.toString(ImmutableItemRef.of("var1", Optional.of(ImmutableItemIndex.of(10,Optional.of(IdUtils.toId("a")))))));

    assertEquals("var1.10.a", IdUtils.toString(ImmutableItemRef.of("a", Optional.of(ImmutableItemIndex.of(10,Optional.of(IdUtils.toId("var1")))))));
    assertEquals("var1.10", IdUtils.toString(ImmutableItemIndex.of(10,Optional.of(IdUtils.toId("var1")))));
    assertEquals("var1.*", IdUtils.toString(ImmutableItemIdPartial.of(Optional.of(IdUtils.toId("var1")))));
  }


  @Test
  public void testToId() {
    assertNull(IdUtils.toIdNullable(null));
    assertEquals(IdUtils.toId("var1"), IdUtils.toId("var1"));
    assertEquals(ImmutableItemIndex.of(1,Optional.empty()), IdUtils.toId("1"));
    assertEquals(ImmutableItemIndex.of(10,Optional.empty()), IdUtils.toId("10"));
    assertEquals(ImmutableItemRef.of("var1", Optional.of(ImmutableItemIndex.of(10,Optional.empty()))), IdUtils.toId("10.var1"));
    assertEquals(ImmutableItemRef.of("var1", Optional.of(ImmutableItemIndex.of(10,Optional.of(IdUtils.toId("a"))))), IdUtils.toId("a.10.var1"));

    assertEquals(ImmutableItemRef.of("a", Optional.of(ImmutableItemIndex.of(10,Optional.of(IdUtils.toId("var1"))))), IdUtils.toId("var1.10.a"));
    assertEquals(ImmutableItemIndex.of(10,Optional.of(IdUtils.toId("var1"))), IdUtils.toId("var1.10"));
  }


  @Test
  public void testMatching() {
    assertTrue(IdUtils.matches(IdUtils.toId("g1"), IdUtils.toId("g1")));
    assertTrue(IdUtils.matches(IdUtils.toId("g1.*"), IdUtils.toId("g1.*")));
    assertTrue(IdUtils.matches(IdUtils.toId("g1.*"), IdUtils.toId("g1.0")));
    assertTrue(IdUtils.matches(IdUtils.toId("g1.0"), IdUtils.toId("g1.*")));
    assertTrue(IdUtils.matches(IdUtils.toId("g1.*.q"), IdUtils.toId("g1.*.q")));
    assertTrue(IdUtils.matches(IdUtils.toId("g1.*.q"), IdUtils.toId("g1.0.q")));
    assertTrue(IdUtils.matches(IdUtils.toId("g1.0.q"), IdUtils.toId("g1.*.q")));
    assertFalse(IdUtils.matches(IdUtils.toId("g2.0.q"), IdUtils.toId("g1.*.q")));
    assertFalse(IdUtils.matches(IdUtils.toId("g1.0.q1"), IdUtils.toId("g1.*.q")));
    assertFalse(IdUtils.matches(IdUtils.toId("g1"), IdUtils.toId("g1.*")));
    assertFalse(IdUtils.matches(IdUtils.toId("g1.*"), IdUtils.toId("g1")));
    assertFalse(IdUtils.matches(IdUtils.toId("q"), IdUtils.toId("*.q")));
    assertFalse(IdUtils.matches(IdUtils.toId("*.q"), IdUtils.toId("q")));
    assertFalse(IdUtils.matches(IdUtils.toId("g1"), IdUtils.toId("g1.0")));
    assertFalse(IdUtils.matches(IdUtils.toId("g1.0"), IdUtils.toId("g1")));
    assertFalse(IdUtils.matches(IdUtils.toId("g1"), IdUtils.toId("g1.0.q")));
    assertFalse(IdUtils.matches(IdUtils.toId("g1.0.q"), IdUtils.toId("g1")));
    assertFalse(IdUtils.matches(IdUtils.toId("g2"), IdUtils.toId("g1")));
  }
}
