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
package io.dialob.program.ddrl;

import static io.dialob.rule.parser.Expression.createExpression;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.common.collect.Maps;

import io.dialob.executor.model.IdUtils;
import io.dialob.program.ProgramBuilderException;
import io.dialob.program.expr.OperatorFactory;
import io.dialob.program.expr.arith.ImmutableConstant;
import io.dialob.program.expr.arith.Operators;
import io.dialob.program.expr.arith.StringOperators;
import io.dialob.program.expr.arith.TimeOperators;
import io.dialob.program.model.Expression;
import io.dialob.rule.parser.api.ImmutableRuleExpressionCompilerError;
import io.dialob.rule.parser.api.RuleExpressionCompilerError;
import io.dialob.rule.parser.api.VariableFinder;
import io.dialob.rule.parser.node.ASTVisitor;
import io.dialob.rule.parser.node.CallExprNode;
import io.dialob.rule.parser.node.ConstExprNode;
import io.dialob.rule.parser.node.IdExprNode;
import io.dialob.rule.parser.node.NodeBase;

public class DDRLExpressionCompiler {

  private final OperatorFactory operatorFactory;

  private final Map<NodeBase,String> asyncFunctionVariables;

  public DDRLExpressionCompiler(@Nonnull OperatorFactory operatorFactory) {
    this.operatorFactory = requireNonNull(operatorFactory);
    this.asyncFunctionVariables = Maps.newHashMap();
  }

  @Nonnull
  public <T> Optional<Expression> compile(
    @Nonnull VariableFinder variableFinder,
    @Nonnull String expressionString,
    @Nonnull Consumer<RuleExpressionCompilerError> errorConsumer)
  {
    final io.dialob.rule.parser.Expression expression = createExpression(variableFinder, asyncFunctionVariables, expressionString);
    expression.getErrors().forEach(errorConsumer::accept);
    final NodeBase ast = expression.getAst();
    if (ast == null || !expression.getErrors().isEmpty()) {
      return Optional.empty();
    }
    try {
      return Optional.of(convertToImmutableExpression(ast));
    } catch (ProgramBuilderException e) {
      errorConsumer.accept(ImmutableRuleExpressionCompilerError.builder()
        .errorCode(e.getMessage())
        .span(e.getNode().getSpan())
        .args(e.getArgs().toArray())
        .build()
      );
    }
    return Optional.empty();
  }

  @Nonnull
  private Expression convertToImmutableExpression(@Nonnull NodeBase ast) {
    ASTVisitorBuilder visitorBuilder = new ASTVisitorBuilder();
    ast.accept(visitorBuilder);
    final List<Expression> expressions = visitorBuilder.getExpressions();
    assert expressions.size() == 1;
    return expressions.get(0);
  }

  public Map<String, Expression> getAsyncFunctionVariableExpressions() {
    return asyncFunctionVariables.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue,
      nodeBaseStringEntry -> convertToImmutableExpression(nodeBaseStringEntry.getKey())));
  }

  private class ASTVisitorBuilder implements ASTVisitor {

    private final List<Expression> expressions = new ArrayList<>();

    private ASTVisitorBuilder builder;

    @Nonnull
    public List<Expression> getExpressions() {
      return expressions;
    }

    @Override
    public ASTVisitor visitCallExpr(@Nonnull CallExprNode node) {
      this.builder = new ASTVisitorBuilder();
      return builder;
    }

    @Override
    @Nonnull
    public NodeBase endCallExpr(@Nonnull CallExprNode node) {
      try {
        final Expression operator = operatorFactory.createOperator(
          requireNonNull(node.getValueType()),
          node.getNodeOperator().getOperator(),
          builder.getExpressions());
        this.expressions.add(operator);
      } catch (ProgramBuilderException e) {
        e.setNode(node);
        throw e;
      } catch(Exception e) {
        throw new RuntimeException("Failed to compile expression: " +node + " because: " + e.getMessage(), e);
      }
      builder = null;
      return node;
    }

    @Override
    @Nonnull
    public NodeBase visitConstExpr(@Nonnull ConstExprNode node) {
      expressions.add(ImmutableConstant.builder().valueType(requireNonNull(node.getValueType())).value(node.getAsValueType()).build());
      return node;
    }

    @Nonnull
    @Override
    public NodeBase visitIdExpr(@Nonnull IdExprNode node) {
      switch (node.getId()) {
        case "today":
          expressions.add(TimeOperators.today());
          break;
        case "now":
          expressions.add(TimeOperators.now());
          break;
        case "language":
          expressions.add(StringOperators.languageOperator());
          break;
        default:
          if (node.getValueType() == null) {
            throw new UnknownValueTypeException(node.getId());
          }
          expressions.add(Operators.var(IdUtils.toId(node.getScopeId(), node.getId()), node.getValueType()));
      }
      return node;
    }
  }
}
