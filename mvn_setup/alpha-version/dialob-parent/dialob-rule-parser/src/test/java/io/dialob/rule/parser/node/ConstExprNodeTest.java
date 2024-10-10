package io.dialob.rule.parser.node;

import io.dialob.rule.parser.api.ValueType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ConstExprNodeTest {

  @Test
  public void testEquality() {
    ConstExprNode node = new ConstExprNode(null, "a", null, ValueType.STRING, Span.undefined());
    assertEquals(node, node);
    assertNotEquals(node, null);
    assertEquals(
      new ConstExprNode(null, "a", null, ValueType.STRING, Span.undefined()),
      new ConstExprNode(null, "a", null, ValueType.STRING, Span.undefined()));
    assertNotEquals(
      new ConstExprNode(null, "b", null, ValueType.STRING, Span.undefined()),
      new ConstExprNode(null, "a", null, ValueType.STRING, Span.undefined()));
    assertEquals(
      new ConstExprNode(null, "100", null, ValueType.INTEGER, Span.undefined()),
      new ConstExprNode(null, "100", null, ValueType.INTEGER, Span.undefined()));
    assertEquals(
      new ConstExprNode(null, "100", null, ValueType.INTEGER, Span.undefined()),
      new ConstExprNode(null, "100", null, ValueType.INTEGER, Span.of(1,2)));
    assertNotEquals(
      new ConstExprNode(null, "100", "%", ValueType.INTEGER, Span.undefined()),
      new ConstExprNode(null, "100", null, ValueType.INTEGER, Span.undefined()));

  }

}
