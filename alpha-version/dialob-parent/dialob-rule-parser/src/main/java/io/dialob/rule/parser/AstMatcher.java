/*
 * Copyright © 2015 - 2021 ReSys (info@dialob.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dialob.rule.parser;

import io.dialob.rule.parser.api.ValueType;
import io.dialob.rule.parser.node.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class AstMatcher implements ASTVisitor {

  private final List<Pair<Predicate<NodeBase>, UnaryOperator<NodeBase>>> matchers = new ArrayList<>();

  public NodeBase onVisitEnd(NodeBase baseNode) {
    return baseNode;
  }

  public void whenMatches(Predicate<NodeBase> matcher, UnaryOperator<NodeBase> thenAction) {
    matchers.add(new ImmutablePair<>(matcher, thenAction));
  }

  @Override
  public ASTVisitor visitCallExpr(@NotNull CallExprNode node) {
    checkMatchingBefore(node);
    return this;
  }


  @Override
  @NotNull
  public NodeBase endCallExpr(@NotNull CallExprNode node) {
    NodeBase nodeBase = checkMatching(node);
    if (nodeBase.getParent() == null) {
      // Last visit
      return onVisitEnd(nodeBase);
    }
    return nodeBase;
  }

  protected void checkIsThisLast(NodeBase nodeBase) {
    // placeholder
  }

  @Override
  @NotNull
  public NodeBase visitConstExpr(@NotNull ConstExprNode node) {
    NodeBase nodeBase = checkMatching(node);
    checkIsThisLast(node);
    return nodeBase;
  }

  @Override
  @NotNull
  public NodeBase visitIdExpr(@NotNull IdExprNode node) {
    NodeBase nodeBase = checkMatching(node);
    checkIsThisLast(node);
    return nodeBase;
  }

  protected void checkMatchingBefore(final NodeBase node) {
    // placeholder
  }

  @NotNull
  protected NodeBase checkMatching(final NodeBase node) {
    for (final Pair<Predicate<NodeBase>, UnaryOperator<NodeBase>> pair : matchers) {
      if (pair.getKey().test(node)) {
        return pair.getValue().apply(node);
      }
    }
    return node;
  }

  public static <T> Predicate<T> isNull() {
    return Objects::isNull;
  }

  public static <T> Predicate<T> is(@NotNull T equalsTo) {
    return object -> equalsTo == object || equalsTo.equals(object);
  }

  public static <T> Predicate<T> not(Predicate<T> predicate) {
    return object -> !predicate.test(object);
  }


  public static <T> Predicate<T> or(Predicate<T>... predicates) {
    return object -> {
      for (Predicate<T> predicate : predicates) {
        if (predicate.test(object)) {
          return true;
        }
      }
      return false;
    };
  }

  public static <T> Predicate<T> and(Predicate<T>... predicates) {
    return object -> {
      for (Predicate<T> predicate : predicates) {
        if (!predicate.test(object)) {
          return false;
        }
      }
      return true;
    };
  }


  public static Predicate<NodeBase> stringValue(Predicate<Object> predicate) {
    return node -> node instanceof ConstExprNode && predicate.test(((ConstExprNode) node).getValue());
  }

  public static Predicate<NodeBase> value(Predicate<Object> predicate) {
    return node -> node instanceof ConstExprNode && predicate.test(((ConstExprNode) node).getAsValueType());
  }

  public static Predicate<NodeBase> valueType(Predicate<ValueType> predicate) {
    return node -> predicate.test(node.getValueType());
  }

  public static Predicate<List<NodeBase>> count(Predicate<Integer> predicate) {
    return list -> predicate.test(list.size());
  }


  public static Predicate<NodeBase> parent(Predicate<NodeBase> predicate) {
    return node -> predicate.test(node.getParent());
  }

  public static Predicate<NodeBase> lhs(Predicate<NodeBase> predicate) {
    return node -> node instanceof CallExprNode && predicate.test(((CallExprNode) node).getLhs());
  }

  public static Predicate<NodeBase> rhs(Predicate<NodeBase> predicate) {
    return node -> node instanceof CallExprNode && predicate.test(((CallExprNode) node).getRhs());
  }


  public static Predicate<NodeBase> args(Predicate<List<NodeBase>> predicate) {
    return node -> node instanceof CallExprNode && predicate.test(node.getSubnodes());
  }

  public static Predicate<List<NodeBase>> length(Predicate<Integer> predicate) {
    return args -> args != null && predicate.test(args.size());
  }


  public static Predicate<List<NodeBase>> first(Predicate<NodeBase> predicate) {
    return args -> !args.isEmpty() && predicate.test(args.get(0));
  }

  public static Predicate<List<NodeBase>> last(Predicate<NodeBase> predicate) {
    return args -> !args.isEmpty() && predicate.test(args.get(args.size() - 1));
  }

  public static Predicate<List<NodeBase>> anyMatches(Predicate<NodeBase> predicate) {
    return args -> {
      if (args.isEmpty()) {
        return false;
      }
      for (NodeBase arg : args) {
        if (predicate.test(arg)) {
          return true;
        }
      }
      return false;
    };
  }

  public static Predicate<NodeBase> dependencies(Predicate<Map<String, ValueType>> predicate) {
    return node -> node instanceof CallExprNode && predicate.test(node.getDependencies());
  }

  public static <T> Predicate<Collection<T>> size(Predicate<Integer> predicate) {
    return args -> predicate.test(args.size());
  }

  public static Predicate<NodeBase> isChildOf(NodeBase parentNode) {
    return args -> {
      NodeBase node = args;
      while ((node = node.getParent()) != null) {
        if (node == parentNode) {
          return true;
        }
      }
      return false;
    };
  }

  public static <K,V> Predicate<Map<K,V>> keys(Predicate<Set<K>> predicate) {
    return args -> predicate.test(args.keySet());
  }

  public static <K,V> Predicate<Map<K,V>> values(Predicate<Collection<V>> predicate) {
    return args -> predicate.test(args.values());
  }


  public static <T> Predicate<Collection<T>> contains(Predicate<T> predicate) {
    return args -> {
      for (T t : args) {
        if (predicate.test(t)) {
          return true;
        }
      }
      return false;
    };
  }

  public static <T> Predicate<Collection<T>> contains(T t) {
    return args -> args.contains(t);
  }

  public static Predicate<List<NodeBase>> allMatches(Predicate<NodeBase> predicate) {
    return args -> {
      if (args.isEmpty()) {
        return false;
      }
      for (NodeBase arg : args) {
        if (!predicate.test(arg)) {
          return false;
        }
      }
      return true;
    };
  }


  public static Predicate<NodeBase> operCategory(Predicate<NodeOperator.Category> predicate) {
    return node -> node instanceof CallExprNode && predicate.test(node.getNodeOperator().getCategory());
  }

  public static Predicate<NodeBase> operator(Predicate<String> predicate) {
    return node -> node instanceof CallExprNode && predicate.test(node.getNodeOperator().getOperator());
  }


  public static Predicate<NodeBase> callNode() {
    return callNode(any());
  }

  public static Predicate<NodeBase> callNode(@NotNull Predicate<NodeBase> predicate) {
    return node -> node instanceof CallExprNode && predicate.test(node);
  }


  public static Predicate<NodeBase> idNode() {
    return idNode(any());
  }

  public static Predicate<NodeBase> idNode(@NotNull Predicate<NodeBase> predicate) {
    return node -> node instanceof IdExprNode && predicate.test(node);
  }

  public static Predicate<NodeBase> id(Predicate<String> predicate) {
    return node -> node instanceof IdExprNode && predicate.test(((IdExprNode) node).getId());
  }

  public static <T> Predicate<T> any() {
    return t -> true;
  }

  public static Predicate<NodeBase> constNode() {
    return constNode(any());
  }

  public static Predicate<NodeBase> constNode(@NotNull Predicate<NodeBase> predicate) {
    return node -> node instanceof ConstExprNode && predicate.test(node);
  }

  public static Predicate<NodeBase> anyNode() {
    return anyNode(any());
  }

  public static Predicate<NodeBase> anyNode(@NotNull Predicate<NodeBase> predicate) {
    return node -> node != null && predicate.test(node);
  }

  public ASTBuilder newASTBuilder() {
    return new ASTBuilder();
  }
}
