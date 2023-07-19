package io.dialob.rule.parser.node;

import io.dialob.rule.parser.api.ValueType;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true, of = {"id", "namespace"})
public class IdExprNode extends NodeBase {

  private static final long serialVersionUID = -3176730634816602189L;

  private final String scopeId;

  private final String id;

  private final String namespace;

  private final Map<String, ValueType> idSet;

  public IdExprNode(NodeBase parent, @Nullable String namespace, @Nullable String scopeId, @NotNull String id, @Nullable ValueType valueType, @NotNull Span span) {
    super(parent, span, valueType);
    this.id = Objects.requireNonNull(id);
    this.namespace = StringUtils.defaultString(namespace);
    Map<String, ValueType> idSet = new HashMap<>();
    idSet.put(this.id, valueType);
    this.idSet = Collections.unmodifiableMap(idSet);
    this.scopeId = scopeId;
  }

  @NotNull
  public String getId() {
    return id;
  }

  @Nullable
  public String getScopeId() {
    return scopeId;
  }

  @Nullable
  public String getNamespace() {
    return namespace;
  }

  public boolean isIdentifier() {
    return true;
  }

  @Override
  public String toString() {
    return id;
  }

  public String toTypedString() {
    return id + "[" + getValueType() + "]";
  }

  public NodeBase accept(@NotNull ASTVisitor visitor) {
    return visitor.visitIdExpr(this);
  }

  @Override
  @NotNull
  public Map<String, ValueType> getDependencies() {
    return idSet;
  }

  @NotNull
  @Override
  public NodeOperator getNodeOperator() {
    return NodeOperator.ID;
  }
}
