package io.dialob.rule.parser.node;

import io.dialob.rule.parser.api.ValueType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ASTBuilder {

  private final ASTBuilder parentScopeBuilder;

  private NodeBase rootNode = null;

  private NodeBase topNode = null;

  public ASTBuilder() {
    this(null);
  }

  public ASTBuilder(ASTBuilder parentScopeBuilder) {
    this.parentScopeBuilder = parentScopeBuilder;
  }

  public ASTBuilder getParentScopeBuilder() {
    return parentScopeBuilder;
  }

  private NodeBase peek() {
    return topNode;
  }

  private void push(NodeBase nodeBase) {
    if (rootNode == null) {
      rootNode = nodeBase;
    }
    topNode = nodeBase;
  }

  private NodeBase pop() {
    if (topNode == null) {
      throw new IllegalStateException("node stack underflow");
    }
    NodeBase parent = topNode.getParent();
    if (parent != null) {
      parent.addSubnode(topNode);
    }
    this.topNode = parent;
    return this.topNode;
  }

  public ASTBuilder notExprNode(Span span) {
    return callExprNode(NodeOperator.createNodeOperator("not"), ValueType.BOOLEAN, span);
  }

  public ASTBuilder logicExprNode(String operator, Span span) {
    return callExprNode(NodeOperator.createNodeOperator(operator), ValueType.BOOLEAN, span);
  }

  public ASTBuilder infixExprNode(String operator, Span span) {
    return callExprNode(NodeOperator.createNodeOperator(operator), null, span);
  }

  public ASTBuilder unaryExprNode(String operator, Span span) {
    return callExprNode(NodeOperator.createNodeOperator(operator), null, span);
  }

  public ASTBuilder callExprNode(String function, Span span) {
    push(new CallExprNode(peek(), NodeOperator.createNodeOperator(function), span));
    return this;
  }

  public ASTBuilder callExprNode(String function, ValueType type, Span span) {
    push(new CallExprNode(peek(), NodeOperator.createNodeOperator(function), type, span));
    return this;
  }

  public ASTBuilder callExprNode(NodeOperator nodeOperator, ValueType type, Span span) {
    push(new CallExprNode(peek(), nodeOperator, type, span));
    return this;
  }

  public ASTBuilder callExprNode(CallExprNode node) {
    push(new CallExprNode(peek(), node.getNodeOperator(), node.getValueType(), node.getSpan()));
    return this;
  }

  public ASTBuilder reducerExprNode(String operator, Span span) {
    push(new ReducerExprNode(peek(), NodeOperator.createNodeOperator(operator), span));
    return this;
  }

  public ASTBuilder relationExprNode(String text, Span span) {
    return callExprNode(NodeOperator.createNodeOperator(text), ValueType.BOOLEAN, span);
  }

  public ASTBuilder constExprNode(String text, String unit, ValueType type, Span span) {
    push(new ConstExprNode(peek(), text, unit, type, span));
    return this;
  }

  public ASTBuilder constExprNode(ConstExprNode constExprNode) {
    push(new ConstExprNode(peek(), constExprNode.getValue(), constExprNode.getUnit(), constExprNode.getValueType(), constExprNode.getSpan()));
    return this;
  }

  public ASTBuilder idExprNode(@Nullable String namespace, @NotNull String text, @Nullable ValueType valueType, @NotNull Span span) {
    return idExprNode(namespace, null, text, valueType, span);
  }

  public ASTBuilder idExprNode(@Nullable String namespace, @Nullable String scopeId, @NotNull String text, @Nullable ValueType valueType, @NotNull Span span) {
    push(new IdExprNode(peek(), namespace, scopeId, text, valueType, span));
    return this;
  }

  public ASTBuilder idExprNode(IdExprNode node) {
    push(new IdExprNode(peek(), node.getNamespace(), node.getScopeId(), node.getId(), node.getValueType(), node.getSpan()));
    return this;
  }

  public ASTBuilder setValueType(ValueType valueType) {
    topNode.setValueType(valueType);
    return this;
  }

  public ASTBuilder closeExpr() {
    pop();
    return this;
  }

  public NodeBase getTopNode() {
    return topNode;
  }

  public NodeBase build() {
    if (topNode != null) {
      throw new IllegalStateException("Unclosed expression");
    }
    return rootNode;
  }

  public ASTBuilder cloneNode(NodeBase node) {
    if (node instanceof CallExprNode) {
      ASTBuilder builder = callExprNode((CallExprNode) node);
      node.getSubnodes().forEach(subnode -> builder.cloneNode(subnode).closeExpr());
    }
    return exprNode(node);
  }

  public ASTBuilder exprNode(NodeBase node) {
    if (node instanceof IdExprNode) {
      return idExprNode((IdExprNode) node);
    }
    if (node instanceof ConstExprNode) {
      return constExprNode((ConstExprNode) node);
    }
    if (node instanceof CallExprNode) {
      return callExprNode((CallExprNode) node);
    }
    throw new IllegalStateException("Unknown node type " + this);
  }

  public ASTBuilder exprNodes(NodeBase... nodes) {
    ASTBuilder builder = this;
    for (NodeBase node : nodes) {
      List<NodeBase> subnodes = node.getSubnodes();
      builder = builder
        .exprNode(node)
        .exprNodes(subnodes.toArray(new NodeBase[0]))
        .closeExpr();
    }
    return builder;
  }
}
