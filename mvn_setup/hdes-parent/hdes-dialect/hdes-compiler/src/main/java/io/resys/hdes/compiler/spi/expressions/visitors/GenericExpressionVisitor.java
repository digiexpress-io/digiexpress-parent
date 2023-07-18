package io.resys.hdes.compiler.spi.expressions.visitors;

/*-
 * #%L
 * hdes-compiler
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.nodes.BodyNode.Literal;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarType;
import io.resys.hdes.ast.api.nodes.BodyNode.TypeDef;
import io.resys.hdes.ast.api.nodes.ExpressionNode.AdditiveExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.AdditiveType;
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
import io.resys.hdes.ast.api.nodes.ExpressionNode.MultiplicativeType;
import io.resys.hdes.ast.api.nodes.ExpressionNode.NegateUnary;
import io.resys.hdes.ast.api.nodes.ExpressionNode.NotUnary;
import io.resys.hdes.ast.api.nodes.ExpressionNode.OrExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PositiveUnary;
import io.resys.hdes.ast.api.nodes.FlowNode.StepCallDef;
import io.resys.hdes.ast.api.nodes.HdesNode;
import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.nodes.ImmutableEqualityOperation;
import io.resys.hdes.ast.api.nodes.ImmutableObjectDef;
import io.resys.hdes.ast.api.nodes.InvocationNode;
import io.resys.hdes.ast.api.nodes.InvocationNode.StaticMethodType;
import io.resys.hdes.ast.api.visitors.ExpressionVisitor;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.expressions.ExpressionFactory.ExpCode;
import io.resys.hdes.compiler.spi.expressions.ExpressionFactory.ExpObjectCode;
import io.resys.hdes.compiler.spi.expressions.ExpressionFactory.ExpScalarCode;
import io.resys.hdes.compiler.spi.expressions.ExpressionFactory.ExpressionCallback;
import io.resys.hdes.compiler.spi.expressions.ImmutableExpObjectCode;
import io.resys.hdes.compiler.spi.expressions.ImmutableExpScalarCode;
import io.resys.hdes.compiler.spi.expressions.visitors.ScalarConverter.ScalarConverterCode;
import io.resys.hdes.compiler.spi.spec.JavaSpecUtil;
import io.resys.hdes.executor.spi.operations.HdesOperationsGen;


public class GenericExpressionVisitor implements ExpressionVisitor<ExpCode, ExpCode>, ExpressionCallback {
  
  public final static String ACCESS_SRC_VALUE = "src";
  public final static List<String> GLOBAL_METHODS = Arrays.asList("min", "max", "sum", "avg");
  public final static List<String> LAMBDA_METHODS = Arrays.asList("map");
  
  private final InvocationVisitor<ExpCode, ExpCode> resolver;

  public GenericExpressionVisitor(InvocationVisitor<ExpCode, ExpCode> resolver) {
    super();
    this.resolver = resolver;
  }

  @Override
  public ExpScalarCode visitBody(ExpressionBody node, HdesTree ctx) {
    return visitScalar(node.getValue(), ctx);
  }
  
  @Override
  public ExpScalarCode visitLiteral(Literal node, HdesTree ctx) {
    CodeBlock.Builder builder = CodeBlock.builder();
    switch (node.getType()) {
    case BOOLEAN:
      builder.add(node.getValue());
      break;
    case DATE:
      builder.add("$T.parse($S)", LocalDate.class, node.getValue());
      break;
    case DATETIME:
      builder.add("$T.parse($S)", LocalDateTime.class, node.getValue());
      break;
    case TIME:
      builder.add("$T.parse($S)", LocalTime.class, node.getValue());
      break;
    case DECIMAL:
      builder.add("new $T($S)", BigDecimal.class, node.getValue());
      break;
    case INTEGER:
      builder.add("$L", node.getValue());
      break;
    case STRING:
      builder.add("$S", node.getValue());
      break;
    default: throw new HdesCompilerException(HdesCompilerException.builder().unknownExpression(node));
    }

    return ImmutableExpScalarCode.builder().value(builder.build()).array(false).type(node.getType()).build();
  }

  @Override
  public ExpScalarCode visitNot(NotUnary node, HdesTree ctx) {
    ExpScalarCode children = visitScalar(node.getValue(), ctx);

    return ImmutableExpScalarCode.builder().type(ScalarType.BOOLEAN).array(false)
        .value(CodeBlock.builder().add("!").add(children.getValue()).build()).build();
  }

  @Override
  public ExpScalarCode visitNegate(NegateUnary node, HdesTree ctx) {
    ExpScalarCode spec = visitScalar(node.getValue(), ctx);

    CodeBlock.Builder value = CodeBlock.builder();
    if (spec.getType() == ScalarType.DECIMAL) {
      value.add("$L.negate()", spec.getValue());
    } else if (spec.getType() == ScalarType.INTEGER) {
      value.add("-$L", spec.getValue());
    } else {
      throw new HdesCompilerException(HdesCompilerException.builder().unknownExpression(node));
    }

    return ImmutableExpScalarCode.builder().value(value.build()).type(spec.getType()).build();
  }

  @Override
  public ExpScalarCode visitPositive(PositiveUnary node, HdesTree ctx) {
    ExpScalarCode spec = visitScalar(node.getValue(), ctx);

    CodeBlock.Builder value = CodeBlock.builder();
    if (spec.getType() == ScalarType.DECIMAL) {
      value.add("$L.plus()", spec.getValue());
    } else if (spec.getType() == ScalarType.INTEGER) {
      value.add("+$L", spec.getValue());
    } else {
      throw new HdesCompilerException(HdesCompilerException.builder().unknownExpression(node));
    }

    return ImmutableExpScalarCode.builder().value(value.build()).type(spec.getType()).build();
  }
  
  @Override
  public ExpCode visitInvocation(InvocationNode node, HdesTree ctx) {
    return resolver.visitBody(node, ctx);
  }
  
  @Override
  public ExpCode visitMethod(CallMethodExpression node, HdesTree ctx) {
    if(node instanceof LambdaExpression) {
      return visitLambda((LambdaExpression) node, ctx);
    } else if(node instanceof StaticMethodExpression) {
      return visitMathMethod((StaticMethodExpression) node, ctx);
    }
    throw new HdesCompilerException(HdesCompilerException.builder().unknownExpression(node));
  }
  
  @Override
  public ExpCode visitLambda(LambdaExpression node, HdesTree ctx) {
    var next = ctx.next(node);
    ExpCode type = visitAny(node.getType(), next);

    final var mapOrigin = ctx.any().build(node.getType());
    final var mapSource = ImmutableObjectDef.builder()
        .from(mapOrigin)
        .addAllValues(mapOrigin instanceof StepCallDef ? 
            ((StepCallDef) mapOrigin).getValues() : 
            Collections.emptyList())
        .name(node.getParam().getValue())
        .array(false)
        .build();
    
    next = next.next(mapSource);
    ExpCode body = visitAny(node.getBody(), next);
    CodeBlock value = CodeBlock.builder()
        .add("$L.map($L -> $L).collect($T.toList())", 
            type.getValue(), visitAny(node.getParam(), next).getValue(), 
            body.getValue(), Collectors.class)
        .build();
    
    if(body instanceof ExpScalarCode) {
      ExpScalarCode spec = (ExpScalarCode) body;
      return ImmutableExpScalarCode.builder().from(spec).value(value).build();
    }
    ExpObjectCode spec = (ExpObjectCode) body;
    return ImmutableExpObjectCode.builder().from(spec).value(value).build();
  }
  
  @Override
  public ExpCode visitMathMethod(StaticMethodExpression node, HdesTree ctx) {
    HdesTree next = ctx.next(node);
    CodeBlock.Builder params = CodeBlock.builder().add("$T.get().math()", HdesOperationsGen.class);
    boolean isDecimal = false;
    
    // Add math function parameters
    for (HdesNode ast : node.getValues()) {
      ExpCode spec = visitAny(ast, next);
      
      // Scalar ref
      if (spec instanceof ExpScalarCode) {
        ExpScalarCode runningValue = (ExpScalarCode) spec;
        if (runningValue.getType() == ScalarType.INTEGER) {
          params.add(".integer($L)", runningValue.getValue());
        } else if (runningValue.getType() == ScalarType.DECIMAL) {
          params.add(".decimal($L)", runningValue.getValue());
          isDecimal = true;
        }
        
        continue;
      }
    

      // Object based ref
      ExpObjectCode runningValue = (ExpObjectCode) spec;
      for (TypeDef typeDefNode : runningValue.getType().getValues()) {
        if (!(typeDefNode instanceof ScalarDef)) {
          continue;
        }
        ScalarType scalar = ((ScalarDef) typeDefNode).getType();
        if(typeDefNode.getName().isEmpty()) {
          if (scalar == ScalarType.INTEGER) {
            params.add(".integer($L)", runningValue.getValue());
          } else if (scalar == ScalarType.DECIMAL) {
            params.add(".decimal($L)", runningValue.getValue());
            isDecimal = true;
          }
          break;
        }
        
        String name = JavaSpecUtil.methodCall(typeDefNode.getName());
        if (scalar == ScalarType.INTEGER) {
          params.add(".integer($L.$L)", runningValue.getValue(), name);
        } else if (scalar == ScalarType.DECIMAL) {
          params.add(".decimal($L.$L)", runningValue.getValue(), name);
          isDecimal = true;
        }
      }
    }
    
    if (isDecimal) {
      params.add(".toDecimal()");
    } else {
      params.add(".toInteger()");
    }
    
    ScalarType returnType = isDecimal || node.getType() == StaticMethodType.AVG ? ScalarType.DECIMAL : ScalarType.INTEGER;
    String method = "";
    switch (node.getType()) {
    case AVG: method = "avg"; break;
    case MIN: method = "min"; break;
    case MAX: method = "max"; break;
    case SUM: method = "sum"; break;
    default: throw new HdesCompilerException(HdesCompilerException.builder().unknownExpression(node));
    }
    
    return ImmutableExpScalarCode.builder()
        .value(params.add(".$L()", method).build())
        .array(false)
        .type(returnType)
        .build();
  }

  @Override
  public ExpCode visitIn(InExpression node, HdesTree ctx) {
    final var next = ctx.next(node);
    CodeBlock.Builder body = CodeBlock.builder().add("(");
    
    int index = 0;
    for(HdesNode right : node.getRight()) {
      EqualityOperation equality = ImmutableEqualityOperation.builder()
          .token(node.getToken())
          .left(node.getLeft())
          .right(right)
          .type(EqualityType.EQUAL)
          .build();
      if(index > 0) {
        body.add(" || ");
      }
      body.add(visitEquality(equality, next).getValue());
      index++;
    }
    
    return ImmutableExpScalarCode.builder().value(body.add(")").build()).array(false).type(ScalarType.BOOLEAN).build();
  }
  
  @Override
  public ExpScalarCode visitEquality(EqualityOperation node, HdesTree ctx) {
    ScalarConverterCode betweenSpec = ScalarConverter.builder().src(node).value1(visitScalar(node.getLeft(), ctx))
        .value2(visitScalar(node.getRight(), ctx)).build();
    
    ScalarType commonType = betweenSpec.getType();
    CodeBlock left = betweenSpec.getValue1();
    CodeBlock right = betweenSpec.getValue2();

    CodeBlock.Builder body = CodeBlock.builder();
    switch (commonType) {
    case DECIMAL:
      if (node.getType() == EqualityType.EQUAL) {
        body.add("$L.compareTo($L) == 0", left, right);
      } else if (node.getType() == EqualityType.NOTEQUAL) {
        body.add("$L.compareTo($L) != 0", left, right);
      } else if (node.getType() == EqualityType.LESS) {
        body.add("$L.compareTo($L) < 0", left, right);
      } else if (node.getType() == EqualityType.LESS_THEN) {
        body.add("$L.compareTo($L) <= 0", left, right);
      } else if (node.getType() == EqualityType.GREATER) {
        body.add("$L.compareTo($L) > 0", left, right);
      } else if (node.getType() == EqualityType.GREATER_THEN) {
        body.add("$L.compareTo($L) >= 0", left, right);
      }
      break;
    case INTEGER:
      if (node.getType() == EqualityType.EQUAL) {
        body.add("Integer.compare($L, $L) == 0", left, right);
      } else if (node.getType() == EqualityType.NOTEQUAL) {
        body.add("Integer.compare($L, $L) != 0", left, right);
      } else if (node.getType() == EqualityType.LESS) {
        body.add("Integer.compare($L, $L) < 0", left, right);
      } else if (node.getType() == EqualityType.LESS_THEN) {
        body.add("Integer.compare($L, $L) <= 0", left, right);
      } else if (node.getType() == EqualityType.GREATER) {
        body.add("Integer.compare($L, $L) > 0", left, right);
      } else if (node.getType() == EqualityType.GREATER_THEN) {
        body.add("Integer.compare($L, $L) >= 0", left, right);
      }
      break;
    case DATETIME:
    case DATE:
    case TIME:
      if (node.getType() == EqualityType.EQUAL) {
        body.add("$L.isEqual($L)", left, right);
      } else if (node.getType() == EqualityType.NOTEQUAL) {
        body.add("!$L.isEqual($L)", left, right);
      } else if (node.getType() == EqualityType.LESS) {
        body.add("$L.isBefore($L)", left, right);
      } else if (node.getType() == EqualityType.LESS_THEN) {
        body.add("($L.isBefore($L) || $L.isEqual($L))", left, right, left, right);
      } else if (node.getType() == EqualityType.GREATER) {
        body.add("$L.isAfter($L)", left, right);
      } else if (node.getType() == EqualityType.GREATER_THEN) {
        body.add("($L.isAfter($L) || $L.isEqual($L))", left, right, left, right);
      }
      break;
    case STRING:
      if (node.getType() == EqualityType.EQUAL) {
        body.add("$L.equals($L)", left, right);
        break;
      } else  if (node.getType() == EqualityType.NOTEQUAL) {
        body.add("!$L.equals($L)", left, right);
        break;
      }
    default:
      throw new HdesCompilerException(HdesCompilerException.builder().unknownExpression(node));
    }
    return ImmutableExpScalarCode.builder().value(body.build()).array(false).type(commonType).build();

  }

  @Override
  public ExpScalarCode visitMultiplicative(MultiplicativeExpression node, HdesTree ctx) {

    ExpScalarCode left = visitScalar(node.getLeft(), ctx);
    ExpScalarCode right = visitScalar(node.getRight(), ctx);
    ScalarConverterCode spec = ScalarConverter.builder().src(node).value1(left).value2(right).build();

    CodeBlock.Builder value = CodeBlock.builder();
    if (spec.getType() == ScalarType.DECIMAL) {
      value.add("$L.$L($L)", spec.getValue1(), node.getType() == MultiplicativeType.MULTIPLY ? "multiply" : "divide",
          spec.getValue2());
    } else if (node.getType() == MultiplicativeType.MULTIPLY) {
      value.add("$L * $L", spec.getValue1(), spec.getValue2());
    } else {
      value.add("new $T($L).divide(new $T($L)))", BigDecimal.class, spec.getValue1(), BigDecimal.class,
          spec.getValue2());
    }

    return ImmutableExpScalarCode.builder().value(value.build()).type(spec.getType()).build();
  }

  @Override
  public ExpScalarCode visitAdditive(AdditiveExpression node, HdesTree ctx) {

    ExpScalarCode left = visitScalar(node.getLeft(), ctx);
    ExpScalarCode right = visitScalar(node.getRight(), ctx);
    ScalarConverterCode spec = ScalarConverter.builder().src(node).value1(left).value2(right).build();

    CodeBlock.Builder value = CodeBlock.builder();
    if (spec.getType() == ScalarType.DECIMAL) {
      value.add("$L.$L($L)", spec.getValue1(), node.getType() == AdditiveType.ADD ? "add" : "subtract",
          spec.getValue2());
    } else {
      value.add("$L $L $L", spec.getValue1(), node.getType() == AdditiveType.ADD ? "+" : "-", spec.getValue2());
    }

    return ImmutableExpScalarCode.builder().value(value.build()).array(false).type(spec.getType()).build();
  }

  @Override
  public ExpScalarCode visitAnd(AndExpression node, HdesTree ctx) {
    ExpScalarCode left = visitScalar(node.getLeft(), ctx);
    ExpScalarCode right = visitScalar(node.getRight(), ctx);

    return ImmutableExpScalarCode.builder()
        .array(false).type(ScalarType.BOOLEAN)
        .value(CodeBlock.builder().add(left.getValue()).add(" && ").add(right.getValue()).build()).build();
  }

  @Override
  public ExpScalarCode visitOr(OrExpression node, HdesTree ctx) {
    ExpScalarCode left = visitScalar(node.getLeft(), ctx);
    ExpScalarCode right = visitScalar(node.getRight(), ctx);
    return ImmutableExpScalarCode.builder()
        .array(false).type(ScalarType.BOOLEAN)
        .value(CodeBlock.builder().add(left.getValue()).add(" || ").add(right.getValue()).build()).build();
  }

  @Override
  public ExpScalarCode visitConditional(ConditionalExpression node, HdesTree ctx) {
    ExpScalarCode condition = visitScalar(node.getOperation(), ctx);
    ScalarConverterCode conversion = ScalarConverter.builder().src(node).value1(visitScalar(node.getLeft(), ctx))
        .value2(visitScalar(node.getRight(), ctx)).build();

    return ImmutableExpScalarCode.builder()
        .type(conversion.getType()).array(false)
        .value(CodeBlock.builder().add(condition.getValue()).add("?")
        .add(conversion.getValue1()).add(":").add(conversion.getValue2()).build()).type(conversion.getType()).build();
  }

  @Override
  public ExpScalarCode visitBetween(BetweenExpression node, HdesTree ctx) {

    ScalarConverterCode betweenSpec = ScalarConverter.builder().src(node).value1(visitScalar(node.getLeft(), ctx))
        .value2(visitScalar(node.getRight(), ctx)).build();
    ExpScalarCode valueSpec = visitScalar(node.getValue(), ctx);

    ScalarType commonType = betweenSpec.getType();
    CodeBlock left = betweenSpec.getValue1();
    CodeBlock right = betweenSpec.getValue2();
    CodeBlock value = valueSpec.getValue();

    if (valueSpec.getType() != betweenSpec.getType()) {
      ScalarConverterCode conversion1 = ScalarConverter.builder().src(node).value1(valueSpec)
          .value2(ImmutableExpScalarCode.builder().type(betweenSpec.getType()).value(left).build()).build();

      // new types
      value = conversion1.getValue1();
      left = conversion1.getValue2();

      ScalarConverterCode conversion2 = ScalarConverter.builder().src(node)
          .value1(ImmutableExpScalarCode.builder().type(conversion1.getType()).value(value).build())
          .value2(ImmutableExpScalarCode.builder().type(betweenSpec.getType()).value(right).build()).build();

      // new types
      commonType = conversion2.getType();
      right = conversion2.getValue2();
    }

    CodeBlock.Builder leftBuilder = CodeBlock.builder();
    CodeBlock.Builder rightBuilder = CodeBlock.builder();
    switch (commonType) {
    case DECIMAL:
      leftBuilder.add("$L.compareTo($L) <= 0", left, value);
      rightBuilder.add("$L.compareTo($L) >= 0", right, value);
      break;
    case INTEGER:
      leftBuilder.add("Integer.compare($L, $L) <= 0", left, value);
      rightBuilder.add("Integer.compare($L, $L) >= 0", right, value);
      break;
    case DATETIME:
    case DATE:
    case TIME:
      leftBuilder.add("($L.isBefore($L) || $L.isEqual($L))", left, value, left, value);
      rightBuilder.add("($L.isAfter($L) || $L.isEqual($L))", right, value, right, value);
      break;
    default: throw new HdesCompilerException(HdesCompilerException.builder().unknownExpression(node));
    }

    // lte(left, arg) && gte(right, arg)

    return ImmutableExpScalarCode.builder()
        .value(CodeBlock.builder().add(leftBuilder.build()).add(" && ").add(rightBuilder.build()).build())
        .array(false)
        .type(commonType).build();
  }

  private ExpScalarCode visitScalar(HdesNode node, HdesTree ctx) {
    ExpCode result = visitAny(node, ctx);
    if (result instanceof ExpScalarCode) {
      return (ExpScalarCode) result;
    }
    throw new HdesCompilerException(HdesCompilerException.builder().unknownExpression(node));
  }

  public ExpCode visitAny(HdesNode node, HdesTree ctx) {
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
    } else if (node instanceof OrExpression) {
      return visitOr((OrExpression) node, ctx);
    } else if (node instanceof InExpression) {
      return visitIn((InExpression) node, ctx);
    } else if (node instanceof ConditionalExpression) {
      return visitConditional((ConditionalExpression) node, ctx);
    } else if (node instanceof BetweenExpression) {
      return visitBetween((BetweenExpression) node, ctx);
    } else if (node instanceof AdditiveExpression) {
      return visitAdditive((AdditiveExpression) node, ctx);
    } else if (node instanceof MultiplicativeExpression) {
      return visitMultiplicative((MultiplicativeExpression) node, ctx);
    }
    throw new HdesCompilerException(HdesCompilerException.builder().unknownExpression(node));
  }
}
