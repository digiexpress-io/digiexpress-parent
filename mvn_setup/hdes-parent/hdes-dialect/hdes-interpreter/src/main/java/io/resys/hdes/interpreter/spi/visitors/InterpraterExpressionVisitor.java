package io.resys.hdes.interpreter.spi.visitors;

/*-
 * #%L
 * hdes-interpreter
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

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.immutables.value.Value;

import io.resys.hdes.ast.api.nodes.BodyNode.Literal;
import io.resys.hdes.ast.api.nodes.ExpressionNode.AdditiveExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.AndExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.BetweenExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ConditionalExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.EqualityOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.EqualityType;
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
import io.resys.hdes.ast.api.nodes.HdesNode;
import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.nodes.ImmutableEqualityOperation;
import io.resys.hdes.ast.api.nodes.InvocationNode;
import io.resys.hdes.ast.api.visitors.ExpressionVisitor;
import io.resys.hdes.executor.spi.operations.HdesOperationsGen;
import io.resys.hdes.interpreter.api.HdesInterpreter.InterpratedNode;
import io.resys.hdes.interpreter.api.HdesInterpreterException;
import io.resys.hdes.interpreter.spi.dataaccess.IteratorAccessNode;
import io.resys.hdes.interpreter.spi.visitors.InterpraterExpressionVisitor.LiteralInterpratedNode;

public class InterpraterExpressionVisitor implements ExpressionVisitor<LiteralInterpratedNode, LiteralInterpratedNode> {

  private final InterpraterInvocationVisitor invocationVisitor = new InterpraterInvocationVisitor(this);
  
  @Value.Immutable
  public static abstract class LiteralInterpratedNode implements InterpratedNode {
    public abstract Serializable getValue();
    
    @SuppressWarnings("unchecked")
    public <T> T as(Class<T> type) {  
      return (T) getValue();
    }
  }

  @Override
  public LiteralInterpratedNode visitBody(ExpressionBody node, HdesTree ctx) {
    return visit(node.getValue(), ctx);
  }
  
  @Override
  public LiteralInterpratedNode visitInvocation(InvocationNode node, HdesTree ctx) {
    return ImmutableLiteralInterpratedNode.builder()
        .value(invocationVisitor.visitBody(node, ctx).getValue())
        .build();
  }

  @Override
  public LiteralInterpratedNode visitLiteral(Literal node, HdesTree ctx) {
    var builder = ImmutableLiteralInterpratedNode.builder();
    switch (node.getType()) {
    case BOOLEAN:
      builder.value(Boolean.parseBoolean(node.getValue()));
      break;
    case DATE:
      builder.value(LocalDate.parse(node.getValue()));
      break;
    case DATETIME:
      builder.value(LocalDateTime.parse(node.getValue()));
      break;    
    case TIME:
      builder.value(LocalTime.parse(node.getValue()));
      break;
    case DECIMAL:
      builder.value(new BigDecimal(node.getValue()));
      break;
    case INTEGER:
      builder.value(Integer.parseInt(node.getValue()));
      break;
    case STRING:
      builder.value(node.getValue());
      break;
    default: throw new HdesInterpreterException(new StringBuilder()
        .append("Not implemented literal").append(System.lineSeparator()) 
        .append("  - type: ").append(node.getClass()).append(System.lineSeparator())
        .append("  - ast: ").append(node).toString());
    }
    
    return builder.build();
  }

  @Override
  public LiteralInterpratedNode visitNot(NotUnary node, HdesTree ctx) {
    LiteralInterpratedNode result = visit(node.getValue(), ctx.next(node));
    return ImmutableLiteralInterpratedNode.builder()
        .value(!result.as(Boolean.class))
        .build();
  }

  @Override
  public LiteralInterpratedNode visitNegate(NegateUnary node, HdesTree ctx) {
    LiteralInterpratedNode result = visit(node.getValue(), ctx.next(node));
    Serializable value = result.getValue();
    
    if(value instanceof BigDecimal) {
      return ImmutableLiteralInterpratedNode.builder()
          .value(result.as(BigDecimal.class).negate())
          .build();  
    } else if(value instanceof Integer) {
      return ImmutableLiteralInterpratedNode.builder()
          .value(-result.as(Integer.class))
          .build();
    }
    throw new HdesInterpreterException(new StringBuilder()
        .append("Not implemented literal").append(System.lineSeparator()) 
        .append("  - type: ").append(node.getClass()).append(System.lineSeparator())
        .append("  - ast: ").append(node).toString());
  }

  @Override
  public LiteralInterpratedNode visitPositive(PositiveUnary node, HdesTree ctx) {
    LiteralInterpratedNode result = visit(node.getValue(), ctx.next(node));
    Serializable value = result.getValue();
    
    if(value instanceof BigDecimal) {
      return ImmutableLiteralInterpratedNode.builder()
          .value(result.as(BigDecimal.class).plus())
          .build();  
    } else if(value instanceof Integer) {
      return ImmutableLiteralInterpratedNode.builder()
          .value(+result.as(Integer.class))
          .build();
    }
    throw new HdesInterpreterException(new StringBuilder()
        .append("Not implemented literal").append(System.lineSeparator()) 
        .append("  - type: ").append(node.getClass()).append(System.lineSeparator())
        .append("  - ast: ").append(node).toString());
  }

  @Override
  public LiteralInterpratedNode visitEquality(EqualityOperation node, HdesTree ctx) {
    var next = ctx.next(node);
    var leftSrc = visit(node.getLeft(), next);
    var rightSrc = visit(node.getRight(), next);
    
    Set<Class<?>> scalars = new HashSet<>();
    scalars.add(leftSrc.getValue().getClass());
    scalars.add(rightSrc.getValue().getClass());
    
    final Object left;
    final Object right;
    if(scalars.size() > 1) {
      left = toBigDecimal(leftSrc);
      right = toBigDecimal(rightSrc);
    } else {
      left = leftSrc.getValue();
      right = rightSrc.getValue();
    }
    
    switch (node.getType()) {
    case EQUAL: {
      if(left instanceof LocalTime) {
        boolean result = HdesOperationsGen.get().eq((LocalTime) left, (LocalTime) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      } else if(left instanceof LocalDateTime) {
        boolean result = HdesOperationsGen.get().eq((LocalDateTime) left, (LocalDateTime) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      } else if(left instanceof LocalDate) {
        boolean result = HdesOperationsGen.get().eq((LocalDate) left, (LocalDate) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      } else if(left instanceof Integer) {
        boolean result = HdesOperationsGen.get().eq((Integer) left, (Integer) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      } else if(left instanceof BigDecimal) {
        boolean result = HdesOperationsGen.get().eq((BigDecimal) left, (BigDecimal) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      } else if(left instanceof Boolean) {
        boolean result = (Boolean) left == (Boolean) right;
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      } else if(left instanceof String) {
        boolean result = ((String) left).equals((String) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      }
    };
    case NOTEQUAL: {
      if(left instanceof LocalTime) {
        boolean result = HdesOperationsGen.get().neq((LocalTime) left, (LocalTime) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      } else if(left instanceof LocalDateTime) {
        boolean result = HdesOperationsGen.get().neq((LocalDateTime) left, (LocalDateTime) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      } else if(left instanceof LocalDate) {
        boolean result = HdesOperationsGen.get().neq((LocalDate) left, (LocalDate) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      } else if(left instanceof Integer) {
        boolean result = HdesOperationsGen.get().neq((Integer) left, (Integer) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      } else if(left instanceof BigDecimal) {
        boolean result = HdesOperationsGen.get().neq((BigDecimal) left, (BigDecimal) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      } else if(left instanceof Boolean) {
        boolean result = (Boolean) left != (Boolean) right;
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      } else if(left instanceof String) {
        boolean result = !((String) left).equals((String) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      }
    }
    case GREATER: {
      if(left instanceof LocalTime) {
        boolean result = HdesOperationsGen.get().gt((LocalTime) left, (LocalTime) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      } else if(left instanceof LocalDateTime) {
        boolean result = HdesOperationsGen.get().gt((LocalDateTime) left, (LocalDateTime) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      } else if(left instanceof LocalDate) {
        boolean result = HdesOperationsGen.get().gt((LocalDate) left, (LocalDate) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      } else if(left instanceof Integer) {
        boolean result = HdesOperationsGen.get().gt((Integer) left, (Integer) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      } else if(left instanceof BigDecimal) {
        boolean result = HdesOperationsGen.get().gt((BigDecimal) left, (BigDecimal) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      }
    }
    case GREATER_THEN: {
      if(left instanceof LocalTime) {
        boolean result = HdesOperationsGen.get().gte((LocalTime) left, (LocalTime) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      } else if(left instanceof LocalDateTime) {
        boolean result = HdesOperationsGen.get().gte((LocalDateTime) left, (LocalDateTime) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      } else if(left instanceof LocalDate) {
        boolean result = HdesOperationsGen.get().gte((LocalDate) left, (LocalDate) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      } else if(left instanceof Integer) {
        boolean result = HdesOperationsGen.get().gte((Integer) left, (Integer) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      } else if(left instanceof BigDecimal) {
        boolean result = HdesOperationsGen.get().gte((BigDecimal) left, (BigDecimal) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      }
    }
    
    case LESS: {
      if(left instanceof LocalTime) {
        boolean result = HdesOperationsGen.get().lt((LocalTime) left, (LocalTime) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      } else if(left instanceof LocalDateTime) {
        boolean result = HdesOperationsGen.get().lt((LocalDateTime) left, (LocalDateTime) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      } else if(left instanceof LocalDate) {
        boolean result = HdesOperationsGen.get().lt((LocalDate) left, (LocalDate) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      } else if(left instanceof Integer) {
        boolean result = HdesOperationsGen.get().lt((Integer) left, (Integer) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      } else if(left instanceof BigDecimal) {
        boolean result = HdesOperationsGen.get().lt((BigDecimal) left, (BigDecimal) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      }
    }
    case LESS_THEN: {
      if(left instanceof LocalTime) {
        boolean result = HdesOperationsGen.get().lte((LocalTime) left, (LocalTime) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      } else if(left instanceof LocalDateTime) {
        boolean result = HdesOperationsGen.get().lte((LocalDateTime) left, (LocalDateTime) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      } else if(left instanceof LocalDate) {
        boolean result = HdesOperationsGen.get().lte((LocalDate) left, (LocalDate) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      } else if(left instanceof Integer) {
        boolean result = HdesOperationsGen.get().lte((Integer) left, (Integer) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      } else if(left instanceof BigDecimal) {
        boolean result = HdesOperationsGen.get().lte((BigDecimal) left, (BigDecimal) right);
        return ImmutableLiteralInterpratedNode.builder().value(result).build();
      }
    }}
    
    throw new HdesInterpreterException(new StringBuilder()
    .append("Not implemented literal").append(System.lineSeparator()) 
    .append("  - type: ").append(node.getClass()).append(System.lineSeparator())
    .append("  - ast: ").append(node).toString());
  }

  @Override
  public LiteralInterpratedNode visitAnd(AndExpression node, HdesTree ctx) {
    var next = ctx.next(node);
    var left = visit(node.getLeft(), next);
    var right = visit(node.getRight(), next);
    return ImmutableLiteralInterpratedNode.builder().value(left.as(Boolean.class) && right.as(Boolean.class)).build();
  }

  @Override
  public LiteralInterpratedNode visitOr(OrExpression node, HdesTree ctx) {
    var next = ctx.next(node);
    var left = visit(node.getLeft(), next);
    var right = visit(node.getRight(), next);
    return ImmutableLiteralInterpratedNode.builder().value(left.as(Boolean.class) || right.as(Boolean.class)).build();
  }

  @Override
  public LiteralInterpratedNode visitConditional(ConditionalExpression node, HdesTree ctx) {
    var next = ctx.next(node);
    var value = visit(node.getOperation(), next).as(Boolean.class);
    
    var left = visit(node.getLeft(), next);
    var right = visit(node.getRight(), next);
    
    Set<Class<?>> scalars = new HashSet<>();
    scalars.add(left.getValue().getClass());
    scalars.add(right.getValue().getClass());
    
    if(scalars.size() > 1) {
      var leftConverted = toBigDecimal(left);
      var rightConverted = toBigDecimal(right);
      var result = value ? leftConverted : rightConverted;
      return ImmutableLiteralInterpratedNode.builder().value(result).build();
    }
    return value ? left : right;
  }

  @Override
  public LiteralInterpratedNode visitBetween(BetweenExpression node, HdesTree ctx) {
    var next = ctx.next(node);
    var value = visit(node.getValue(), next);
    var left = visit(node.getLeft(), next);
    var right = visit(node.getRight(), next);

    if(value.getValue() instanceof LocalTime) {
      boolean result = HdesOperationsGen.get().between(value.as(LocalTime.class), left.as(LocalTime.class), right.as(LocalTime.class));
      return ImmutableLiteralInterpratedNode.builder().value(result).build();
    } else if(value.getValue() instanceof LocalDateTime) {
      boolean result = HdesOperationsGen.get().between(value.as(LocalDateTime.class), left.as(LocalDateTime.class), right.as(LocalDateTime.class));
      return ImmutableLiteralInterpratedNode.builder().value(result).build();
    } else if(value.getValue() instanceof LocalDate) {
      boolean result = HdesOperationsGen.get().between(value.as(LocalDate.class), left.as(LocalDate.class), right.as(LocalDate.class));
      return ImmutableLiteralInterpratedNode.builder().value(result).build();
    }
    
    Set<Class<?>> scalars = new HashSet<>();
    scalars.add(value.getValue().getClass());
    scalars.add(left.getValue().getClass());
    scalars.add(right.getValue().getClass());
    
    if(scalars.size() > 1) {
      var valueConverted = toBigDecimal(value);
      var leftConverted = toBigDecimal(left);
      var rightConverted = toBigDecimal(right);
      
      boolean result = HdesOperationsGen.get().between(valueConverted, leftConverted, rightConverted);
      return ImmutableLiteralInterpratedNode.builder().value(result).build();
    }
    
    if(value.getValue() instanceof Integer) {
      boolean result = HdesOperationsGen.get().between(value.as(Integer.class), left.as(Integer.class), right.as(Integer.class));
      return ImmutableLiteralInterpratedNode.builder().value(result).build();
    } else if(value.getValue() instanceof BigDecimal) {
      boolean result = HdesOperationsGen.get().between(value.as(BigDecimal.class), left.as(BigDecimal.class), right.as(BigDecimal.class));
      return ImmutableLiteralInterpratedNode.builder().value(result).build();
    } 

    throw new HdesInterpreterException(new StringBuilder()
      .append("Not implemented literal").append(System.lineSeparator()) 
      .append("  - type: ").append(node.getClass()).append(System.lineSeparator())
      .append("  - ast: ").append(node).toString());
  }

  @Override
  public LiteralInterpratedNode visitAdditive(AdditiveExpression node, HdesTree ctx) {
    var next = ctx.next(node);
    var leftSrc = visit(node.getLeft(), next);
    var rightSrc = visit(node.getRight(), next);
    
    Set<Class<?>> scalars = new HashSet<>();
    scalars.add(leftSrc.getValue().getClass());
    scalars.add(rightSrc.getValue().getClass());
    
    final Object left;
    final Object right;
    if(scalars.size() > 1) {
      left = toBigDecimal(leftSrc);
      right = toBigDecimal(rightSrc);
    } else {
      left = leftSrc.getValue();
      right = rightSrc.getValue();
    }
    
    final Serializable result;
    if(left instanceof Integer) {
      result = ((Integer) left) + ((Integer) right);
    } else {
      result = ((BigDecimal) left).add(((BigDecimal) right));
    }
    
    return ImmutableLiteralInterpratedNode.builder().value(result).build();
  }

  @Override
  public LiteralInterpratedNode visitMultiplicative(MultiplicativeExpression node, HdesTree ctx) {
    var next = ctx.next(node);
    var leftSrc = visit(node.getLeft(), next);
    var rightSrc = visit(node.getRight(), next);
    
    Set<Class<?>> scalars = new HashSet<>();
    scalars.add(leftSrc.getValue().getClass());
    scalars.add(rightSrc.getValue().getClass());
    
    final Object left;
    final Object right;
    if(scalars.size() > 1) {
      left = toBigDecimal(leftSrc);
      right = toBigDecimal(rightSrc);
    } else {
      left = leftSrc.getValue();
      right = rightSrc.getValue();
    }
    
    final Serializable result;
    if(left instanceof Integer) {
      result = ((Integer) left) * ((Integer) right);
    } else {
      result = ((BigDecimal) left).multiply(((BigDecimal) right));
    }
    
    return ImmutableLiteralInterpratedNode.builder().value(result).build();
  }

  @Override
  public LiteralInterpratedNode visitIn(InExpression node, HdesTree ctx) {
    final var next = ctx.next(node);
    boolean result = false;
    for(HdesNode right : node.getRight()) {
      EqualityOperation equality = ImmutableEqualityOperation.builder()
          .token(node.getToken())
          .left(node.getLeft())
          .right(right)
          .type(EqualityType.EQUAL)
          .build();
      
      Boolean value = (Boolean) visitEquality(equality, next).getValue();
      if(value) {
        result = true;
      }
    }
    
    return ImmutableLiteralInterpratedNode.builder().value(result).build();
  }

  @Override
  public LiteralInterpratedNode visitMethod(CallMethodExpression node, HdesTree ctx) {
    if(node instanceof LambdaExpression) {
      return visitLambda((LambdaExpression) node, ctx);
    } else if(node instanceof StaticMethodExpression) {
      return visitMathMethod((StaticMethodExpression) node, ctx);
    }
    throw new HdesInterpreterException(new StringBuilder()
        .append("Not implemented method").append(System.lineSeparator()) 
        .append("  - type: ").append(node.getClass()).append(System.lineSeparator())
        .append("  - ast: ").append(node).toString());
  }

  @Override
  public LiteralInterpratedNode visitLambda(LambdaExpression node, HdesTree ctx) {
    final var next = ctx.next(node);
    final Serializable type = visitInvocation(node.getType(), next).getValue();

    if(type instanceof List) {
      HdesNode lambda = node.getBody();
      List<Serializable> mappedData = new ArrayList<>();
      for(Object value : ((List) type)) {
        IteratorAccessNode iterator = IteratorAccessNode.builder().build((Serializable) value);
        LiteralInterpratedNode visited = visit(lambda, next.next(iterator));
        mappedData.add(visited.getValue());
      }
      return ImmutableLiteralInterpratedNode.builder().value((Serializable) mappedData).build();
    }
    
    throw new HdesInterpreterException(new StringBuilder()
        .append("Type: '").append(type).append("' is not list").append(System.lineSeparator()) 
        .append("  - value: ").append(node.getClass()).append(System.lineSeparator())
        .append("  - ast: ").append(node).toString());
  }
  
  @Override
  public LiteralInterpratedNode visitMathMethod(StaticMethodExpression node, HdesTree ctx) {
    final var next = ctx.next(node);
    final var result = ImmutableLiteralInterpratedNode.builder();
    final var value = HdesOperationsGen.get().math();
    
    node.getValues().forEach(n -> value.any(visit(n, next).getValue()));
    
    switch (node.getType()) {
    case SUM: return result.value(value.toNumber().sum()).build();
    case AVG: return result.value(value.toNumber().avg()).build();
    case MIN: return result.value(value.toNumber().min()).build();
    case MAX: return result.value(value.toNumber().max()).build();
    default: throw new HdesInterpreterException(new StringBuilder()
        .append("Not implemented formula method").append(System.lineSeparator()) 
        .append("  - value: ").append(node.getClass()).append(System.lineSeparator())
        .append("  - ast: ").append(node).toString());
    }
  }

  
  public LiteralInterpratedNode visit(HdesNode node, HdesTree ctx) {
    if (node instanceof InvocationNode) {
      return visitInvocation((InvocationNode) node, ctx);
    } else if(node instanceof CallMethodExpression) {
      return visitMethod((CallMethodExpression) node, ctx);
    } else if (node instanceof Literal) {
      return visitLiteral((Literal) node, ctx);
    } else if (node instanceof NotUnary) {
      return visitNot((NotUnary) node, ctx);
    } else if (node instanceof NegateUnary) {
      return visitNegate((NegateUnary) node, ctx);
    } else if (node instanceof PositiveUnary) {
      return visitPositive((PositiveUnary) node, ctx);
    } else if (node instanceof EqualityOperation) {
      return visitEquality((EqualityOperation) node, ctx);
    } else if (node instanceof AndExpression) {
      return visitAnd((AndExpression) node, ctx);
    } else if (node instanceof InExpression) {
      return visitIn((InExpression) node, ctx);
    } else if (node instanceof OrExpression) {
      return visitOr((OrExpression) node, ctx);
    } else if (node instanceof ConditionalExpression) {
      return visitConditional((ConditionalExpression) node, ctx);
    } else if (node instanceof BetweenExpression) {
      return visitBetween((BetweenExpression) node, ctx);
    } else if (node instanceof AdditiveExpression) {
      return visitAdditive((AdditiveExpression) node, ctx);
    } else if (node instanceof MultiplicativeExpression) {
      return visitMultiplicative((MultiplicativeExpression) node, ctx);
    } else if(node instanceof ExpressionBody) {
      return visitBody((ExpressionBody) node, ctx);
    }
    throw new HdesInterpreterException(new StringBuilder()
      .append("Not implemented expression").append(System.lineSeparator()) 
      .append("  - type: ").append(node.getClass()).append(System.lineSeparator())
      .append("  - ast: ").append(node).toString());
  }
  
  private BigDecimal toBigDecimal(LiteralInterpratedNode node) {
    if(node.getValue() instanceof Integer) {
      return new BigDecimal(node.as(Integer.class));
    }
    return node.as(BigDecimal.class);
  }
}
