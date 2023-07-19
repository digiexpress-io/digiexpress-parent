package io.dialob.rule.parser.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public interface ASTVisitor {

  @Nullable
  default ASTVisitor visitCallExpr(@NotNull CallExprNode node) {
    return this;
  }

  @NotNull
  default NodeBase endCallExpr(@NotNull CallExprNode node) {
    return node;
  }

  @NotNull
  default NodeBase visitConstExpr(@NotNull ConstExprNode node) {
    return node;
  }

  @NotNull
  default NodeBase visitIdExpr(@NotNull IdExprNode node) {
    return node;
  }

}
