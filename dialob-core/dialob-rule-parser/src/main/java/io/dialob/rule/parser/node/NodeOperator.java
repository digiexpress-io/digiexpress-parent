package io.dialob.rule.parser.node;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class NodeOperator implements Serializable {

  private static final long serialVersionUID = 3766609755158172243L;

  public static final NodeOperator CONST = new NodeOperator("$const", Category.LEAF);

  public static final NodeOperator ID = new NodeOperator("$id", Category.LEAF);

  private final String operator;

  private final Category category;

  private NodeOperator(@NotNull String operator, @NotNull Category category) {
    this.operator = operator;
    this.category = category;
  }

  private static final Map<String, NodeOperator> OPERATORS;

  static {
    OPERATORS = ImmutableMap.<String, NodeOperator>builder()
      .put("neg", new NodeOperator("neg", Category.UNARY))
      .put("inv", new NodeOperator("inv", Category.UNARY))

      .put("not", new NodeOperator("not", Category.LOGICAL))  // ??
      .put("and", new NodeOperator("and", Category.LOGICAL))
      .put("or", new NodeOperator("or", Category.LOGICAL))

      .put("*", new NodeOperator("*", Category.INFIX))
      .put("/", new NodeOperator("/", Category.INFIX))
      .put("+", new NodeOperator("+", Category.INFIX))
      .put("-", new NodeOperator("-", Category.INFIX))

      .put("!=", new NodeOperator("!=", Category.RELATION))
      .put("=", new NodeOperator("=", Category.RELATION))
      .put("<", new NodeOperator("<", Category.RELATION))
      .put("<=", new NodeOperator("<=", Category.RELATION))
      .put(">", new NodeOperator(">", Category.RELATION))
      .put(">=", new NodeOperator(">=", Category.RELATION))
      .put("in", new NodeOperator("in", Category.RELATION))
      .put("matches", new NodeOperator("matches", Category.RELATION))
      .put("notIn", new NodeOperator("notIn", Category.RELATION))
      .put("notMatches", new NodeOperator("notMatches", Category.RELATION))
      .put("isAnswered", new NodeOperator("isAnswered", Category.RELATION))
      .put("isNotAnswered", new NodeOperator("isNotAnswered", Category.RELATION))
      .put("isValid", new NodeOperator("isValid", Category.RELATION))
      .put("isNotValid", new NodeOperator("isNotValid", Category.RELATION))

      .put("sumOf", new NodeOperator("sumOf", Category.FUNCTION))
      .put("minOf", new NodeOperator("minOf", Category.FUNCTION))
      .put("maxOf", new NodeOperator("maxOf", Category.FUNCTION))
      .put("allOf", new NodeOperator("allOf", Category.FUNCTION))
      .put("anyOf", new NodeOperator("anyOf", Category.FUNCTION))
      .build();
  }

  @NotNull
  public static NodeOperator createNodeOperator(@NotNull String operator) {
    NodeOperator nodeOperator = OPERATORS.get(operator);
    if (nodeOperator == null) {
      return new NodeOperator(operator, Category.FUNCTION);
    }
    return nodeOperator;
  }

  public NodeOperator not() {
    if (getCategory() == Category.RELATION || getCategory() == Category.LOGICAL) {
      switch (operator) {
        case "not":
          return null;
        case "and":
          return createNodeOperator("or");
        case "or":
          return createNodeOperator("and");
        case "!=":
          return createNodeOperator("=");
        case "=":
          return createNodeOperator("!=");
        case "<":
          return createNodeOperator(">=");
        case "<=":
          return createNodeOperator(">");
        case ">":
          return createNodeOperator("<=");
        case ">=":
          return createNodeOperator("<");
        case "in":
          return createNodeOperator("notIn");
        case "notIn":
          return createNodeOperator("in");
        case "matches":
          return createNodeOperator("notMatches");
        case "notMatches":
          return createNodeOperator("matches");
        case "isAnswered":
          return createNodeOperator("isNotAnswered");
        case "isNotAnswered":
          return createNodeOperator("isAnswered");
        default:
      }
    }
    throw new IllegalStateException("cannot not operator " + this);
  }

  public enum Category {
    FUNCTION,
    LOGICAL,  // and, or,, not
    UNARY,    // neg, not and inv == (1 / x)
    INFIX,    // +-*/
    RELATION, // < > = <= >=
    LEAF      // nop
  }

  @NotNull
  public String getOperator() {
    return operator;
  }

  @NotNull
  public Category getCategory() {
    return category;
  }

  public boolean isNegOp() {
    return "neg".equals(operator);
  }

  public boolean isInvOp() {
    return "inv".equals(operator);
  }

  public boolean isNotOp() {
    return "not".equals(operator);
  }

  public boolean isAndOp() {
    return "and".equals(operator);
  }

  public boolean isOrOp() {
    return "or".equals(operator);
  }

  public boolean isMultOp() {
    return "*".equals(operator);
  }

  public boolean isPlusOp() {
    return "+".equals(operator);
  }

  public boolean isMinusOp() {
    return "-".equals(operator);
  }

  public boolean isDivOp() {
    return "/".equals(operator);
  }

  public boolean isUnary() {
    return isNotOp() || isInvOp() || isNegOp();
  }

  public boolean isInfix() {
    return category == Category.INFIX;
  }

  public boolean isLogical() {
    return category == Category.LOGICAL;
  }

  public boolean isRelation() {
    return category == Category.RELATION;
  }

  public int getPrecedenceWeight() {
    if (isUnary()) {
      return 8;
    }
    if (isDivOp()) {
      return 7;
    }
    if (isMultOp()) {
      return 6;
    }
    if (isPlusOp() || isMinusOp()) {
      return 5;
    }
    if (this.category == Category.FUNCTION) {
      return 4;
    }
    if (this.category == Category.RELATION) {
      return 3;
    }
    if (isAndOp()) {
      return 2;
    }
    if (isOrOp()) {
      return 1;
    }
    return 0;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj instanceof NodeOperator) {
      NodeOperator other = (NodeOperator) obj;
      return other.getCategory() == getCategory() && operator.equals(other.getOperator());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return operator.hashCode() * 31 + category.hashCode();
  }

  @Override
  public String toString() {
    return operator;
  }
}
