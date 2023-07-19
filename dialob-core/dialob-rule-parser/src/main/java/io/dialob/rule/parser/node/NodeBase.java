package io.dialob.rule.parser.node;

import io.dialob.rule.parser.api.ValueType;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class NodeBase implements TypedNode, Serializable {

  private static final long serialVersionUID = 1948018522089217375L;

  private NodeBase parent;

  private final Span span;

  private ValueType type;

  public NodeBase(NodeBase parent) {
    this(parent, null, null);
  }

  public NodeBase(NodeBase parent, Span span) {
    this(parent, span, null);
  }

  public NodeBase(NodeBase parent, Span span, ValueType type) {
    this.parent = parent;
    this.span = span;
    this.type = type;
  }

  public NodeBase getParent() {
    return parent;
  }

  public void setParent(NodeBase parent) {
    this.parent = parent;
  }

  public Span getSpan() {
    return span;
  }

  @Override
  public ValueType getValueType() {
    return type;
  }

  public void setValueType(@NotNull ValueType type) {
    this.type = Objects.requireNonNull(type);
  }

  @NotNull
  public Map<String, ValueType> getDependencies() {
    return Collections.emptyMap();
  }

  public Map<String, ValueType> getAllDependencies() {
    return getDependencies();
  }

  public boolean isConstant() {
    return false;
  }

  public boolean isIdentifier() {
    return false;
  }

  public NodeBase addSubnode(@NotNull NodeBase topNode) {
    throw new IllegalStateException("Cannot add subnodes on leaf node");
  }

  @NotNull
  public List<NodeBase> getSubnodes() {
    return Collections.emptyList();
  }

  @NotNull
  public abstract NodeOperator getNodeOperator();

  public abstract NodeBase accept(@NotNull ASTVisitor visitor);

  public String toTypedString() {
    return toString() + "[" + type + "]";
  }

  public String toString(String indent) {
    return indent + toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof NodeBase)) {
      return false;
    }
    NodeBase other = (NodeBase) obj;
    return other.type == type;
  }

  @Override
  public int hashCode() {
    return type != null ? type.hashCode() : 0;
  }

}
