package io.dialob.rule.parser.node;

import io.dialob.rule.parser.api.ValueType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class IdExprNodeTest {

  @Test
  public void testEquality() {
    IdExprNode node = new IdExprNode(null, null, null, "var", ValueType.STRING, Span.undefined());
    assertEquals(node, node);
    assertNotEquals(node, null);
    assertEquals(node, new IdExprNode(null, null, null, "var", ValueType.STRING, Span.undefined()));
    assertEquals(node, new IdExprNode(null, null, null, "var", ValueType.STRING, Span.of(1,2)));
    assertNotEquals(node, new IdExprNode(null, null, null, "var1", ValueType.STRING, Span.undefined()));
    assertNotEquals(node, new IdExprNode(null, null, null, "var", ValueType.INTEGER, Span.undefined()));
    assertEquals(node, new IdExprNode(null, "", null, "var", ValueType.STRING, Span.undefined()));
    assertNotEquals(node, new IdExprNode(null, "aa", null, "var", ValueType.STRING, Span.undefined()));
    assertEquals(node, new IdExprNode(node, null, null, "var", ValueType.STRING, Span.undefined()));
  }

}
