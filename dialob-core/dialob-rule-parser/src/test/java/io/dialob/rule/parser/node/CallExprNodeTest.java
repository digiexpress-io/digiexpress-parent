package io.dialob.rule.parser.node;

import io.dialob.rule.parser.api.ValueType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CallExprNodeTest {

  @Test
  public void testEquality() {
    CallExprNode node = new CallExprNode(null, NodeOperator.createNodeOperator("+"), ValueType.INTEGER, Span.undefined());
    CallExprNode node2;
    assertEquals(node, node);
    assertNotEquals(node, null);

    node = new CallExprNode(null, NodeOperator.createNodeOperator("+"), ValueType.INTEGER, Span.undefined());
    node.addSubnode(new IdExprNode(null, null, null, "var", ValueType.INTEGER, Span.undefined()));
    node2 = new CallExprNode(null, NodeOperator.createNodeOperator("+"), ValueType.INTEGER, Span.undefined());
    node2.addSubnode(new IdExprNode(null, null, null, "var", ValueType.INTEGER, Span.undefined()));
    assertEquals(node, node2);

    node = new CallExprNode(null, NodeOperator.createNodeOperator("+"), ValueType.INTEGER, Span.undefined());
    node.addSubnode(new IdExprNode(null, null, null, "var", ValueType.INTEGER, Span.undefined()));
    node2 = new CallExprNode(null, NodeOperator.createNodeOperator("-"), ValueType.INTEGER, Span.undefined());
    node2.addSubnode(new IdExprNode(null, null, null, "var", ValueType.INTEGER, Span.undefined()));
    assertNotEquals(node, node2);

    node = new CallExprNode(null, NodeOperator.createNodeOperator("+"), ValueType.INTEGER, Span.undefined());
    node.addSubnode(new IdExprNode(null, null, null, "var", ValueType.INTEGER, Span.undefined()));
    node2 = new CallExprNode(null, NodeOperator.createNodeOperator("+"), ValueType.INTEGER, Span.undefined());
    node2.addSubnode(new IdExprNode(null, null, null, "var1", ValueType.INTEGER, Span.undefined()));
    assertNotEquals(node, node2);

    node = new CallExprNode(null, NodeOperator.createNodeOperator("+"), ValueType.INTEGER, Span.undefined());
    node.addSubnode(new IdExprNode(null, null, null, "var", ValueType.INTEGER, Span.undefined()));
    node2 = new CallExprNode(null, NodeOperator.createNodeOperator("+"), ValueType.INTEGER, Span.undefined());
    assertNotEquals(node, node2);
  }

}
