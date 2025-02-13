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
package io.dialob.rule.parser.api;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;


public class ArrayValueTypeTest {

  @Test
  public void shouldGetArrayClassOfValueType() {
    assertSame(Integer[].class, ValueType.arrayOf(ValueType.INTEGER).getTypeClass());
    assertSame(String[].class, ValueType.arrayOf(ValueType.STRING).getTypeClass());
    assertSame(LocalDate[].class, ValueType.arrayOf(ValueType.DATE).getTypeClass());
    assertSame(LocalTime[].class, ValueType.arrayOf(ValueType.TIME).getTypeClass());
    assertSame(BigDecimal[].class, ValueType.arrayOf(ValueType.DECIMAL).getTypeClass());
    assertSame(Duration[].class, ValueType.arrayOf(ValueType.DURATION).getTypeClass());
    assertSame(Period[].class, ValueType.arrayOf(ValueType.PERIOD).getTypeClass());
    assertSame(Boolean[].class, ValueType.arrayOf(ValueType.BOOLEAN).getTypeClass());
  }

  @Test
  public void shouldGetArrayClassOfArray() {
    assertSame(Integer[][].class, ValueType.arrayOf(ValueType.arrayOf(ValueType.INTEGER)).getTypeClass());
  }

  @Test
  public void shouldParseArrayStrings() {
    assertArrayEquals(new Integer[] {1,2,3}, (Integer[]) ValueType.arrayOf(ValueType.INTEGER).parseFromString("[1,2,3]"));
  }
}
