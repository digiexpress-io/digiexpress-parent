package io.dialob.rule.parser.node;

import io.dialob.rule.parser.api.ValueType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class ValueTypeTest {

  @Test
  public void booleanIsParseAble() throws Exception {
    assertNull(ValueType.BOOLEAN.parseFromString(null));
    assertTrue((Boolean) ValueType.BOOLEAN.parseFromString("true"));
    assertFalse((Boolean) ValueType.BOOLEAN.parseFromString("false"));
  }

  @Test
  public void booleanReduction() {
    assertTrue(Arrays.asList(true, true, true).stream().reduce(ValueType.BOOLEAN.multOp()).get());
    assertFalse(Arrays.asList(true, false, true).stream().reduce(ValueType.BOOLEAN.multOp()).get());
    assertTrue(Arrays.asList(true, true, true).stream().reduce(ValueType.BOOLEAN.sumOp()).get());
    assertFalse(Arrays.asList(false, false, false).stream().reduce(ValueType.BOOLEAN.multOp()).get());
  }


  @Test
  public void integerIsParseable() throws Exception {
    assertNull(ValueType.INTEGER.parseFromString(null));
    Assertions.assertEquals(0, ValueType.INTEGER.parseFromString("0"));
  }

  @Test
  public void integerReduction() {
    assertEquals((Integer) 6, Arrays.asList(1, 2, 3).stream().reduce(ValueType.INTEGER.multOp()).get());
    assertEquals((Integer) 6, Arrays.asList(1, 2, 3).stream().reduce(ValueType.INTEGER.sumOp()).get());
    assertEquals((Integer) 24, Arrays.asList(1, 2, 3, 4).stream().reduce(ValueType.INTEGER.multOp()).get());
    assertEquals((Integer) 10, Arrays.asList(1, 2, 3, 4).stream().reduce(ValueType.INTEGER.sumOp()).get());
    assertEquals((Integer) 0, Arrays.asList(1, 2, 3, 4, 0).stream().reduce(ValueType.INTEGER.multOp()).get());
    assertEquals((Integer) 10, Arrays.asList(1, 2, 3, 4, 0).stream().reduce(ValueType.INTEGER.sumOp()).get());
  }


  @Test
  public void decimalIsParseAble() throws Exception {
    assertNull(ValueType.DECIMAL.parseFromString(null));
    Assertions.assertEquals(BigDecimal.valueOf(0.1), ValueType.DECIMAL.parseFromString("0.1"));
  }

  @Test
  public void dateIsParseAble() throws Exception {
    assertNull(ValueType.DATE.parseFromString(null));
    Assertions.assertEquals(LocalDate.of(2016, 2, 1), ValueType.DATE.parseFromString("2016-02-01"));
  }

  @Test
  public void timeIsParseAble() throws Exception {
    assertNull(ValueType.TIME.parseFromString(null));
    Assertions.assertEquals(LocalTime.of(23, 45, 10), ValueType.TIME.parseFromString("23:45:10"));
  }

  @Test
  public void periodIsParseAble() throws Exception {
    assertNull(ValueType.PERIOD.parseFromString(null));
    Assertions.assertEquals(Period.of(10, 3, 15), ValueType.PERIOD.parseFromString("P10Y3M15D"));
  }

  @Test
  public void durationIsParseAble() throws Exception {
    assertNull(ValueType.DURATION.parseFromString(null));
    Assertions.assertEquals(Duration.ofDays(100).plusHours(2), ValueType.DURATION.parseFromString("P100DT2H"));
  }

  @Test
  public void checkReturnTypeMapping() {
    Assertions.assertEquals(ValueType.STRING, ValueType.valueTypeOf(String.class));
    Assertions.assertEquals(ValueType.DECIMAL, ValueType.valueTypeOf(BigDecimal.class));
    Assertions.assertEquals(ValueType.INTEGER, ValueType.valueTypeOf(Integer.class));
    Assertions.assertEquals(ValueType.INTEGER, ValueType.valueTypeOf(int.class));
    Assertions.assertEquals(ValueType.DATE, ValueType.valueTypeOf(LocalDate.class));
    Assertions.assertEquals(ValueType.TIME, ValueType.valueTypeOf(LocalTime.class));
    Assertions.assertEquals(ValueType.DURATION, ValueType.valueTypeOf(Duration.class));
    Assertions.assertEquals(ValueType.PERIOD, ValueType.valueTypeOf(Period.class));
    Assertions.assertEquals(ValueType.BOOLEAN, ValueType.valueTypeOf(Boolean.class));
    Assertions.assertEquals(ValueType.BOOLEAN, ValueType.valueTypeOf(boolean.class));
    assertNull(ValueType.valueTypeOf(Class.class));
  }

  @Test
  public void plusTypeReturnTypes() {
    Assertions.assertEquals(ValueType.DATE, ValueType.DATE.plusType(ValueType.PERIOD));
    Assertions.assertEquals(ValueType.TIME, ValueType.TIME.plusType(ValueType.DURATION));
    Assertions.assertEquals(ValueType.INTEGER, ValueType.INTEGER.plusType(ValueType.INTEGER));
    Assertions.assertEquals(ValueType.DECIMAL, ValueType.INTEGER.plusType(ValueType.DECIMAL));
    Assertions.assertEquals(ValueType.DECIMAL, ValueType.DECIMAL.plusType(ValueType.INTEGER));
    Assertions.assertEquals(ValueType.DECIMAL, ValueType.DECIMAL.plusType(ValueType.DECIMAL));
    Assertions.assertEquals(ValueType.STRING, ValueType.STRING.plusType(ValueType.DECIMAL));
    Assertions.assertEquals(ValueType.STRING, ValueType.STRING.plusType(ValueType.PERIOD));
    Assertions.assertEquals(ValueType.STRING, ValueType.STRING.plusType(ValueType.DURATION));
    Assertions.assertEquals(ValueType.STRING, ValueType.STRING.plusType(ValueType.BOOLEAN));
    Assertions.assertEquals(ValueType.STRING, ValueType.TIME.plusType(ValueType.STRING));
    Assertions.assertEquals(ValueType.STRING, ValueType.DECIMAL.plusType(ValueType.STRING));
    Assertions.assertEquals(ValueType.STRING, ValueType.PERIOD.plusType(ValueType.STRING));
    Assertions.assertEquals(ValueType.STRING, ValueType.DURATION.plusType(ValueType.STRING));
    Assertions.assertEquals(ValueType.STRING, ValueType.BOOLEAN.plusType(ValueType.STRING));
    Assertions.assertEquals(ValueType.STRING, ValueType.TIME.plusType(ValueType.STRING));
  }
  @Test
  public void minusTypeReturnTypes() {
    Assertions.assertEquals(ValueType.PERIOD, ValueType.DATE.minusType(ValueType.DATE));
    Assertions.assertEquals(ValueType.DURATION, ValueType.TIME.minusType(ValueType.TIME));
    Assertions.assertEquals(ValueType.INTEGER, ValueType.INTEGER.minusType(ValueType.INTEGER));
    Assertions.assertEquals(ValueType.DECIMAL, ValueType.INTEGER.minusType(ValueType.DECIMAL));
    Assertions.assertEquals(ValueType.DECIMAL, ValueType.DECIMAL.minusType(ValueType.INTEGER));
    Assertions.assertEquals(ValueType.DECIMAL, ValueType.DECIMAL.minusType(ValueType.DECIMAL));
    Assertions.assertEquals(ValueType.TIME, ValueType.TIME.minusType(ValueType.DURATION));
    Assertions.assertEquals(ValueType.DATE, ValueType.DATE.minusType(ValueType.PERIOD));
  }
  @Test
  public void multipleTypeReturnTypes() {
    Assertions.assertEquals(ValueType.INTEGER, ValueType.INTEGER.multiplyType(ValueType.INTEGER));
    Assertions.assertEquals(ValueType.DECIMAL, ValueType.INTEGER.multiplyType(ValueType.DECIMAL));
    Assertions.assertEquals(ValueType.DECIMAL, ValueType.DECIMAL.multiplyType(ValueType.INTEGER));
    Assertions.assertEquals(ValueType.DECIMAL, ValueType.DECIMAL.multiplyType(ValueType.DECIMAL));
  }
  @Test
  public void divideTypeReturnTypes() {
    Assertions.assertEquals(ValueType.INTEGER, ValueType.INTEGER.divideByType(ValueType.INTEGER));
    Assertions.assertEquals(ValueType.DECIMAL, ValueType.INTEGER.divideByType(ValueType.DECIMAL));
    Assertions.assertEquals(ValueType.DECIMAL, ValueType.DECIMAL.divideByType(ValueType.INTEGER));
    Assertions.assertEquals(ValueType.DECIMAL, ValueType.DECIMAL.divideByType(ValueType.DECIMAL));
  }

  @Test
  public void valueTypeNames() {
    Assertions.assertEquals("INTEGER", ValueType.INTEGER.getName());
    Assertions.assertEquals("DATE", ValueType.DATE.getName());
    Assertions.assertEquals("DECIMAL", ValueType.DECIMAL.getName());
    Assertions.assertEquals("TIME", ValueType.TIME.getName());
    Assertions.assertEquals("PERIOD", ValueType.PERIOD.getName());
    Assertions.assertEquals("DURATION", ValueType.DURATION.getName());
    Assertions.assertEquals("DECIMAL", ValueType.DECIMAL.getName());
    Assertions.assertEquals("BOOLEAN", ValueType.BOOLEAN.getName());
    Assertions.assertEquals("[STRING]", ValueType.arrayOf(ValueType.STRING).getName());
  }
}
