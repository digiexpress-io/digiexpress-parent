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
package io.dialob.rule.parser.function;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultFunctionsTest {
  @Test
  public void testIsLyt() {
    assertTrue(DefaultFunctions.isLyt("1572860-0"));
    assertFalse(DefaultFunctions.isLyt("1572860-1"));
    assertFalse(DefaultFunctions.isLyt(null));
    assertFalse(DefaultFunctions.isLyt(""));
    assertFalse(DefaultFunctions.isLyt(" "));
    assertTrue(DefaultFunctions.isLyt("737546-2"));
    assertTrue(DefaultFunctions.isLyt("1961630-7"));
  }

  @Test
  public void testHetu() {
    assertTrue(DefaultFunctions.isHetu("010170-555D"));
    assertFalse(DefaultFunctions.isHetu(null));
    assertFalse(DefaultFunctions.isHetu(""));
    assertFalse(DefaultFunctions.isHetu("010170-555"));
    assertFalse(DefaultFunctions.isHetu("014070-555D"));
    assertFalse(DefaultFunctions.isHetu("010170-5555D"));
    assertTrue(DefaultFunctions.isHetu("010170A555D"));
    assertFalse(DefaultFunctions.isHetu("010A70A555D"));
    assertFalse(DefaultFunctions.isHetu("010170AA55D"));
    assertTrue(DefaultFunctions.isHetu("010170+555D"));
    assertFalse(DefaultFunctions.isHetu("010170#555D"));
  }

  @Test
  public void testIsIban() {
    assertTrue(DefaultFunctions.isIban("DE91100000000123456789"));
    assertTrue(DefaultFunctions.isIban("BR1500000000000010932840814P2"));
    assertTrue(DefaultFunctions.isIban("SE45 5000 0000 0583 9825 7466"));
    assertTrue(DefaultFunctions.isIban("FI21 1234 5600 0007 85"));
    assertTrue(DefaultFunctions.isIban("EE38 2200 2210 2014 5685"));
    assertFalse(DefaultFunctions.isIban("SE45 5000 0000 0583 9825 7467"));
    assertFalse(DefaultFunctions.isIban(""));
    assertFalse(DefaultFunctions.isIban(null));
    assertFalse(DefaultFunctions.isIban("foo"));
  }


}
