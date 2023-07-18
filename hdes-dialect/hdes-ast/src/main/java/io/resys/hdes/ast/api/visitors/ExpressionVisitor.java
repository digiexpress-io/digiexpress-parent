package io.resys.hdes.ast.api.visitors;


/*-
 * #%L
 * hdes-ast
 * %%
 * Copyright (C) 2020 Copyright 2020 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import io.resys.hdes.ast.api.nodes.BodyNode.Literal;
import io.resys.hdes.ast.api.nodes.ExpressionNode.AdditiveExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.AndExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.BetweenExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ConditionalExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.EqualityOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
import io.resys.hdes.ast.api.nodes.ExpressionNode.InExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.LambdaExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.StaticMethodExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.CallMethodExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.MultiplicativeExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.NegateUnary;
import io.resys.hdes.ast.api.nodes.ExpressionNode.NotUnary;
import io.resys.hdes.ast.api.nodes.ExpressionNode.OrExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PositiveUnary;
import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.nodes.InvocationNode;
import io.resys.hdes.ast.api.nodes.InvocationNode.EmptyPlaceholder;
import io.resys.hdes.ast.api.nodes.InvocationNode.NamedPlaceholder;
import io.resys.hdes.ast.api.nodes.InvocationNode.NestedInvocation;
import io.resys.hdes.ast.api.nodes.InvocationNode.Placeholder;
import io.resys.hdes.ast.api.nodes.InvocationNode.SimpleInvocation;

public interface ExpressionVisitor<T, R> extends HdesVisitor<T, R> {
  T visitInvocation(InvocationNode node, HdesTree ctx);
  T visitLiteral(Literal node, HdesTree ctx);
  R visitBody(ExpressionBody node, HdesTree ctx);
  T visitNot(NotUnary node, HdesTree ctx);
  T visitNegate(NegateUnary node, HdesTree ctx);
  T visitPositive(PositiveUnary node, HdesTree ctx);
  T visitEquality(EqualityOperation node, HdesTree ctx);
  T visitAnd(AndExpression node, HdesTree ctx);
  T visitOr(OrExpression node, HdesTree ctx);
  T visitIn(InExpression node, HdesTree ctx);
  T visitConditional(ConditionalExpression node, HdesTree ctx);
  T visitBetween(BetweenExpression node, HdesTree ctx);
  T visitAdditive(AdditiveExpression node, HdesTree ctx);
  T visitMultiplicative(MultiplicativeExpression node, HdesTree ctx);

  T visitMethod(CallMethodExpression node, HdesTree ctx);
  T visitMathMethod(StaticMethodExpression node, HdesTree ctx);
  T visitLambda(LambdaExpression node, HdesTree ctx);

  interface InvocationVisitor<T, R> extends HdesVisitor<T, R> {
    R visitBody(InvocationNode node, HdesTree ctx);

    T visitNested(NestedInvocation node, HdesTree ctx);
    T visitSimple(SimpleInvocation node, HdesTree ctx);
    T visitPlaceholder(Placeholder node, HdesTree ctx);
    T visitEmptyPlaceholder(EmptyPlaceholder node, HdesTree ctx);
    T visitNamedPlaceholder(NamedPlaceholder node, HdesTree ctx);
  }
}
