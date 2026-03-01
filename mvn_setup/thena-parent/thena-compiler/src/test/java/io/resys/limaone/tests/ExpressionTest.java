package io.resys.limaone.tests;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.limaone.model.Parameter.ValueType;
import io.resys.limaone.program.ExpressionProgram;
import io.resys.limaone.spi.compiler.Compiler_Expression;
import io.resys.limaone.tests.support.DateParser;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ExpressionTest {

  @Test
  public void stringIn() throws IOException {
    final var expression = build(ValueType.STRING, "in[\"aaa\", \"bbb\"]");
    Assertions.assertEquals(true, expression.getValue("aaa"));
    Assertions.assertEquals(true, expression.getValue("bbb"));
    Assertions.assertEquals(false, expression.getValue("c"));
  }

  @Test
  public void stringNotIn() throws IOException {
    final var expression = build(ValueType.STRING, "not in[\"sdsd\", \"dsdx\"]");

    Assertions.assertEquals(true, expression.getValue("a"));
    Assertions.assertEquals(true, expression.getValue("c"));
    Assertions.assertEquals(false, expression.getValue("dsdx"));
  }

  @Test
  public void toNumberComparison() throws IOException {
    final var expression = build(ValueType.INTEGER, "<= 10");

    Assertions.assertEquals(true, expression.getValue(1));
    Assertions.assertEquals(false, expression.getValue(11));
  }

  @Test
  public void toNumberRange() throws IOException {
    var expression = build(ValueType.INTEGER, "[1..3]");

    // include start and end
    Assertions.assertEquals(true, expression.getValue(1));
    Assertions.assertEquals(true, expression.getValue(2));
    Assertions.assertEquals(true, expression.getValue(3));
    Assertions.assertEquals(false, expression.getValue(4));

    // exclude start and end
    expression = build(ValueType.INTEGER, "(1..3)");
    Assertions.assertEquals(false, expression.getValue(1));
    Assertions.assertEquals(true, expression.getValue(2));
    Assertions.assertEquals(false, expression.getValue(3));
    Assertions.assertEquals(false, expression.getValue(4));

    // exclude start and include end
    expression = build(ValueType.INTEGER, "(1..3]");
    Assertions.assertEquals(false, expression.getValue(1));
    Assertions.assertEquals(true, expression.getValue(2));
    Assertions.assertEquals(true, expression.getValue(3));
    Assertions.assertEquals(false, expression.getValue(4));

    // include start and exclude end
    expression = build(ValueType.INTEGER, "[1..3)");
    Assertions.assertEquals(true, expression.getValue(1));
    Assertions.assertEquals(true, expression.getValue(2));
    Assertions.assertEquals(false, expression.getValue(3));
    Assertions.assertEquals(false, expression.getValue(4));
  }

  @Test
  public void toBoolean() throws IOException {
    var expression = build(ValueType.BOOLEAN, "true");

    Assertions.assertEquals(true, expression.getValue(true));
    Assertions.assertEquals(false, expression.getValue(false));

    expression = build(ValueType.BOOLEAN, "false");
    Assertions.assertEquals(true, expression.getValue(false));

  }

  @Test
  public void toDate() throws IOException {
    // equals
    var expression = build(ValueType.DATE_TIME, "equals 2017-07-03T00:00:00Z");
    Assertions.assertEquals(true, expression.getValue(DateParser.parseLocalDateTime("2017-07-03T00:00:00Z")));

    // after
    expression = build(ValueType.DATE_TIME, "after 2017-07-03T00:00:00Z");
    Assertions.assertEquals(false, expression.getValue(DateParser.parseLocalDateTime("2017-07-03T00:00:00Z")));

    Assertions.assertEquals(true, expression.getValue(DateParser.parseLocalDateTime("2017-07-03T00:00:01Z")));

    // before
    expression = build(ValueType.DATE_TIME, "before 2017-07-03T00:00:00Z");
    Assertions.assertEquals(false, expression.getValue(DateParser.parseLocalDateTime("2017-07-03T00:00:00Z")));
    Assertions.assertEquals(true, expression.getValue(DateParser.parseLocalDateTime("2017-07-02T23:59:59Z")));


    // before between
    expression = build(ValueType.DATE_TIME, "between 2017-07-03T00:00:01Z and 2017-07-03T00:00:03Z");
    Assertions.assertEquals(false, expression.getValue(DateParser.parseLocalDateTime("2017-07-03T00:00:00Z")));
    Assertions.assertEquals(true, expression.getValue(DateParser.parseLocalDateTime("2017-07-03T00:00:01Z")));
    Assertions.assertEquals(true, expression.getValue(DateParser.parseLocalDateTime("2017-07-03T00:00:02Z")));
    Assertions.assertEquals(true, expression.getValue(DateParser.parseLocalDateTime("2017-07-03T00:00:03Z")));
    Assertions.assertEquals(false, expression.getValue(DateParser.parseLocalDateTime("2017-07-03T00:00:04Z")));
  }

  public LoggingDecisionTableExpression build(ValueType type, String src) {
    return new LoggingDecisionTableExpression(Compiler_Expression.build(src, type));
  }
  
  public static class LoggingDecisionTableExpression {
    private final ExpressionProgram delegate;
    public LoggingDecisionTableExpression(ExpressionProgram delegate) {
      this.delegate = delegate;
    }
    public String getSrc() {
      return delegate.getSrc();
    }
    public ValueType getType() {
      return delegate.getType();
    }
    public List<String> getConstants() {
      return delegate.getConstants();
    }
    public Object getValue(Object entity) {
      long start = System.nanoTime();
      final var result = delegate.run(entity);
      log.debug(delegate.getSrc() + " execution in nano: " + (System.nanoTime() - start));
      return result.getValue();
    }
  }
}
