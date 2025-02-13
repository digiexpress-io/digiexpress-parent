package io.resys.hdes.ast.spi.returntypes;

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import io.resys.hdes.ast.api.HdesException;
import io.resys.hdes.ast.api.nodes.BodyNode.ContextTypeDef;
import io.resys.hdes.ast.api.nodes.BodyNode.Literal;
import io.resys.hdes.ast.api.nodes.BodyNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarType;
import io.resys.hdes.ast.api.nodes.BodyNode.TypeDef;
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
import io.resys.hdes.ast.api.nodes.ExpressionNode.MultiplicativeType;
import io.resys.hdes.ast.api.nodes.ExpressionNode.NegateUnary;
import io.resys.hdes.ast.api.nodes.ExpressionNode.NotUnary;
import io.resys.hdes.ast.api.nodes.ExpressionNode.OrExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PositiveUnary;
import io.resys.hdes.ast.api.nodes.HdesNode;
import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.nodes.HdesTree.TypeDefAccepts;
import io.resys.hdes.ast.api.nodes.HdesTree.TypeDefReturns;
import io.resys.hdes.ast.api.nodes.ImmutableErrorNode;
import io.resys.hdes.ast.api.nodes.ImmutableObjectDef;
import io.resys.hdes.ast.api.nodes.ImmutableScalarDef;
import io.resys.hdes.ast.api.nodes.ImmutableTypeDefAccepts;
import io.resys.hdes.ast.api.nodes.ImmutableTypeDefReturns;
import io.resys.hdes.ast.api.nodes.InvocationNode;
import io.resys.hdes.ast.api.visitors.ExpressionVisitor;
import io.resys.hdes.ast.spi.ImmutableHdesTree;

public class ReturnTypeExpVisitor implements ExpressionVisitor<TypeDefReturns, TypeDefReturns> {

  @Override
  public TypeDefReturns visitBody(ExpressionBody node, HdesTree ctx) {
    return visit(node.getValue(), ctx);
  }
  
  @Override
  public TypeDefReturns visitLiteral(Literal node, HdesTree ctx) {
    return ImmutableTypeDefReturns.builder()
        .returns(ImmutableScalarDef.builder()
            .array(false).required(true)
            .token(node.getToken())
            .name("")
            .type(node.getType())
            .context(ContextTypeDef.EXPRESSION)
            .build())
        .build();
  }
  
  @Override
  public TypeDefReturns visitAdditive(AdditiveExpression node, HdesTree ctx) {
    TypeDefReturns left = visit(node.getLeft(), ctx);
    TypeDefReturns right = visit(node.getRight(), ctx);
    
    ScalarType returnScalarType = null;
    if(left.getReturns() instanceof ScalarDef && right.getReturns() instanceof ScalarDef) {
      Set<ScalarType> scalars = new HashSet<>();
      scalars.add(((ScalarDef) left.getReturns()).getType());
      scalars.add(((ScalarDef) right.getReturns()).getType());
      
      if(scalars.size() == 1 && (scalars.contains(ScalarType.INTEGER) || scalars.contains(ScalarType.DECIMAL))) {
        returnScalarType = scalars.iterator().next(); 
      } else if(scalars.contains(ScalarType.INTEGER) && scalars.contains(ScalarType.DECIMAL)) {
        returnScalarType = ScalarType.DECIMAL;
      } else {
        throw new HdesException(ImmutableErrorNode.builder()
            .bodyId(ctx.get().body().getId().getValue())
            .target(node)
            .message("Expected DECIMAL or INTEGER type in additive operation but was: " + scalars + "!")
            .build());
      }
      
    } else {
      throw new HdesException(ImmutableErrorNode.builder()
          .bodyId(ctx.get().bodyId())
          .target(node)
          .message("Expected DECIMAL or INTEGER type in additive operation but was: OBJECT!")
          .build());
    }
    
    return ImmutableTypeDefReturns.builder()
        .addAllAccepts(left.getAccepts())
        .addAllAccepts(right.getAccepts())
        .returns(ImmutableScalarDef.builder()
            .array(false).required(true)
            .token(node.getToken())
            .name("")
            .type(returnScalarType)
            .context(ContextTypeDef.EXPRESSION)
            .build())
        .build();
  }

  @Override
  public TypeDefReturns visitMultiplicative(MultiplicativeExpression node, HdesTree ctx) {
    TypeDefReturns left = visit(node.getLeft(), ctx);
    TypeDefReturns right = visit(node.getRight(), ctx);
    
    ScalarType returnScalarType = null;
    if(left.getReturns() instanceof ScalarDef && right.getReturns() instanceof ScalarDef) {
      Set<ScalarType> scalars = new HashSet<>();
      scalars.add(((ScalarDef) left.getReturns()).getType());
      scalars.add(((ScalarDef) right.getReturns()).getType());
      
      if(scalars.size() == 1 && scalars.contains(ScalarType.INTEGER) && scalars.contains(ScalarType.DECIMAL)) {
        returnScalarType = node.getType() == MultiplicativeType.DIVIDE ? ScalarType.DECIMAL : scalars.iterator().next(); 
      } else if(scalars.contains(ScalarType.INTEGER) && scalars.contains(ScalarType.DECIMAL)) {
        returnScalarType = ScalarType.DECIMAL;
      } else {
        throw new HdesException(ImmutableErrorNode.builder()
            .bodyId(ctx.get().bodyId())
            .target(node)
            .message("Expected DECIMAL or INTEGER type in multiplicative operation but was: " + scalars + "!")
            .build());
      }
      
    } else {
      throw new HdesException(ImmutableErrorNode.builder()
          .bodyId(ctx.get().bodyId())
          .target(node)
          .message("Expected DECIMAL or INTEGER type in multiplicative operation but was: OBJECT!")
          .build());
    }
    
    return ImmutableTypeDefReturns.builder()
        .addAllAccepts(left.getAccepts())
        .addAllAccepts(right.getAccepts())
        .returns(ImmutableScalarDef.builder()
            .array(false).required(true)
            .token(node.getToken())
            .name("")
            .type(returnScalarType)
            .context(ContextTypeDef.EXPRESSION)
            .build())
        .build();
  }
  
  @Override
  public TypeDefReturns visitConditional(ConditionalExpression node, HdesTree ctx) {
    TypeDefReturns left = visit(node.getLeft(), ctx);
    TypeDefReturns right = visit(node.getRight(), ctx);
    
    TypeDef produces = null;
    if(left.getReturns() instanceof ScalarDef && right.getReturns() instanceof ScalarDef) {
      Set<ScalarType> scalars = new HashSet<>();
      scalars.add(((ScalarDef) left.getReturns()).getType());
      scalars.add(((ScalarDef) right.getReturns()).getType());
      
      ScalarType returnScalarType = null;
      if(scalars.size() == 1) {
        returnScalarType = scalars.iterator().next(); 
      } else if(scalars.contains(ScalarType.INTEGER) && scalars.contains(ScalarType.DECIMAL)) {
        returnScalarType = ScalarType.DECIMAL;
      } else {
        throw new HdesException(ImmutableErrorNode.builder()
            .bodyId(ctx.get().bodyId())
            .target(node)
            .message("Expected same types in conditional operation but was: " + scalars + "!")
            .build());
      }
      produces = ImmutableScalarDef.builder()
        .context(ContextTypeDef.EXPRESSION)
        .array(false).required(true)
        .token(node.getToken())
        .name("")
        .type(returnScalarType)
        .build();
    } else if(left.getReturns().equals(right.getReturns())) {
      produces = left.getReturns();
    }
    
    if(produces == null) {
      throw new HdesException(ImmutableErrorNode.builder()
          .bodyId(ctx.get().bodyId())
          .target(node)
          .message("Expected scalar types in conditional operation but was: OBJECT!")
          .build());
    }
    
    return ImmutableTypeDefReturns.builder()
        .addAllAccepts(left.getAccepts())
        .addAllAccepts(right.getAccepts())
        .returns(produces)
        .build();
  }
  
  @Override
  public TypeDefReturns visitInvocation(InvocationNode node, HdesTree ctx) {    
    final TypeDef typeDef = ctx.any().build(node);
    return ImmutableTypeDefReturns.builder()
        .returns(typeDef)
        .addAccepts(ImmutableTypeDefAccepts.builder().invocation(node).node(typeDef).build())
        .build();
  }

  @Override
  public TypeDefReturns visitNot(NotUnary node, HdesTree ctx) {
    TypeDefReturns result = visit(node.getValue(), ctx);
    
    if(result.getReturns() instanceof ScalarDef) {
      ScalarType type = ((ScalarDef) result.getReturns()).getType();
      if(type != ScalarType.BOOLEAN) {
        throw new HdesException(ImmutableErrorNode.builder()
            .bodyId(ctx.get().bodyId())
            .target(node)
            .message("Expected BOOLEAN type but was: " + type + "!")
            .build());
      }
    } else {
      throw new HdesException(ImmutableErrorNode.builder()
          .bodyId(ctx.get().bodyId())
          .target(node)
          .message("Expected BOOLEAN type but was: OBJECT!")
          .build());
    }
    
    return result;
  }
  
  @Override
  public TypeDefReturns visitIn(InExpression node, HdesTree ctx) {
    TypeDefReturns left = visit(node.getLeft(), ctx);
    List<HdesTree.TypeDefAccepts> accepts = new ArrayList<>();
    for(HdesNode next : node.getRight()) {
      TypeDefReturns right = visit(next, ctx);
      accepts.addAll(right.getAccepts());
      
      if(left.getReturns() instanceof ScalarDef && right.getReturns() instanceof ScalarDef) {
        Set<ScalarType> scalars = new HashSet<>();
        scalars.add(((ScalarDef) left.getReturns()).getType());
        scalars.add(((ScalarDef) right.getReturns()).getType());
        
        boolean sameComparisonType = scalars.size() == 1;
        boolean numericComparisonType = scalars.size() == 2 && scalars.contains(ScalarType.INTEGER) && scalars.contains(ScalarType.DECIMAL);
        
        Function<TypeDefReturns, ScalarType> toName = (t) -> ((ScalarDef) t.getReturns()).getType();
        
        if(!sameComparisonType && !numericComparisonType) {
          throw new HdesException(ImmutableErrorNode.builder()
              .bodyId(ctx.get().bodyId())
              .target(node)
              .message(new StringBuilder()
                  .append("Expected same types in 'IN' operation: ") 
                  .append(toName.apply(left))
                  .append(" and ").append(toName.apply(right))
                  .toString())
              .build());
          
        }
      } else {
        Function<TypeDefReturns, String> toName = (t) -> t.getReturns() instanceof ObjectDef ? "OBJECT" : "SCALAR";
        throw new HdesException(ImmutableErrorNode.builder()
          .bodyId(ctx.get().bodyId())
          .target(node)
          .message(new StringBuilder()
              .append("Expected same types in 'IN' operation: ") 
              .append(toName.apply(left))
              .append(" and ").append(toName.apply(right))
              .toString())
          .build());
      }
    }
    
    return ImmutableTypeDefReturns.builder()
        .addAllAccepts(left.getAccepts())
        .addAllAccepts(accepts)
        .returns(ImmutableScalarDef.builder()
            .context(ContextTypeDef.EXPRESSION)
            .array(false).required(true)
            .token(node.getToken())
            .name("")
            .type(ScalarType.BOOLEAN)
            .build())
        .build();
  }

  @Override
  public TypeDefReturns visitNegate(NegateUnary node, HdesTree ctx) {
    TypeDefReturns result = visit(node.getValue(), ctx);
    
    if(result.getReturns() instanceof ScalarDef) {
      ScalarType type = ((ScalarDef) result.getReturns()).getType();
      if(type != ScalarType.INTEGER && type != ScalarType.DECIMAL) {
        throw new HdesException(ImmutableErrorNode.builder()
            .bodyId(ctx.get().bodyId())
            .target(node)
            .message("Expected INTEGER or DECIMAL type but was: " + type + "!")
            .build());
      }
    } else {
      throw new HdesException(ImmutableErrorNode.builder()
          .bodyId(ctx.get().bodyId())
          .target(node)
          .message("Expected INTEGER or DECIMAL type but was: OBJECT!")
          .build());
    }
    return result;
  }

  @Override
  public TypeDefReturns visitPositive(PositiveUnary node, HdesTree ctx) {
    TypeDefReturns result = visit(node.getValue(), ctx);
    
    if(result.getReturns() instanceof ScalarDef) {
      ScalarType type = ((ScalarDef) result.getReturns()).getType();
      if(type != ScalarType.INTEGER && type != ScalarType.DECIMAL) {
        throw new HdesException(ImmutableErrorNode.builder()
            .bodyId(ctx.get().bodyId())
            .target(node)
            .message("Expected INTEGER or DECIMAL type but was: " + type + "!")
            .build());
      }
    } else {
      throw new HdesException(ImmutableErrorNode.builder()
          .bodyId(ctx.get().bodyId())
          .target(node)
          .message("Expected INTEGER or DECIMAL type but was: OBJECT!")
          .build());
    }
    return result;
  }

  @Override
  public TypeDefReturns visitEquality(EqualityOperation node, HdesTree ctx) {
    TypeDefReturns left = visit(node.getLeft(), ctx);
    TypeDefReturns right = visit(node.getRight(), ctx);
    
    if(left.getReturns() instanceof ScalarDef && right.getReturns() instanceof ScalarDef) {
      
      Set<ScalarType> scalars = new HashSet<>();
      scalars.add(((ScalarDef) left.getReturns()).getType());
      scalars.add(((ScalarDef) right.getReturns()).getType());
      
      boolean sameComparisonType = scalars.size() == 1;
      boolean numericComparisonType = scalars.contains(ScalarType.INTEGER) && scalars.contains(ScalarType.DECIMAL);
      
      if(!sameComparisonType && !numericComparisonType) {
        Function<TypeDefReturns, ScalarType> toName = (t) -> ((ScalarDef) t.getReturns()).getType();
        throw new HdesException(ImmutableErrorNode.builder()
            .bodyId(ctx.get().bodyId())
            .target(node)
            .message(new StringBuilder()
                .append("Expected same types in equality operation but was: ") 
                .append(toName.apply(left))
                .append(" ").append(node.getType().getValue()).append(" ")
                .append(toName.apply(right))
                .toString())
            .build());
        
      } else if((scalars.contains(ScalarType.BOOLEAN) || scalars.contains(ScalarType.STRING)) &&
          !(node.getType() == EqualityType.EQUAL || node.getType() == EqualityType.NOTEQUAL) ) {
        Function<TypeDefReturns, ScalarType> toName = (t) -> ((ScalarDef) t.getReturns()).getType();
        throw new HdesException(ImmutableErrorNode.builder()
            .bodyId(ctx.get().bodyId())
            .target(node)
            .message(new StringBuilder()
                .append("BOOLEAN and STRING can be used only in following equality operations: '=' or '!=' or 'not' but was: " ) 
                .append(toName.apply(left))
                .append(" ").append(node.getType().getValue()).append(" ")
                .append(toName.apply(right))
                .toString())
            .build());
      }
    } else {
      Function<TypeDefReturns, String> toName = (t) -> t.getReturns() instanceof ObjectDef ? "OBJECT" : "SCALAR";
      throw new HdesException(ImmutableErrorNode.builder()
        .bodyId(ctx.get().bodyId())
        .target(node)
        .message(new StringBuilder()
            .append("Expected scalar types in equality operation but was: ") 
            .append(toName.apply(left))
            .append(" ").append(node.getType().getValue()).append(" ")
            .append(toName.apply(right))
            .toString())
        .build());
    }
    
    return ImmutableTypeDefReturns.builder()
        .addAllAccepts(left.getAccepts())
        .addAllAccepts(right.getAccepts())
        .returns(ImmutableScalarDef.builder()
            .context(ContextTypeDef.EXPRESSION)
            .array(false).required(true)
            .token(node.getToken())
            .name("")
            .type(ScalarType.BOOLEAN)
            .build())
        .build();
  }

  @Override
  public TypeDefReturns visitAnd(AndExpression node, HdesTree ctx) {
    TypeDefReturns left = visit(node.getLeft(), ctx);
    TypeDefReturns right = visit(node.getRight(), ctx);
    
    if(left.getReturns() instanceof ScalarDef && right.getReturns() instanceof ScalarDef) {
      Set<ScalarType> scalars = new HashSet<>();
      scalars.add(((ScalarDef) left.getReturns()).getType());
      scalars.add(((ScalarDef) right.getReturns()).getType());
      
      boolean booleanComparison = scalars.size() == 1 && scalars.contains(ScalarType.BOOLEAN);
      
      if(!booleanComparison) {
        throw new HdesException(ImmutableErrorNode.builder()
            .bodyId(ctx.get().bodyId())
            .target(node)
            .message("Expected BOOLEAN types in equality operation but was: " + scalars + "!")
            .build());
      }
    }
    
    return ImmutableTypeDefReturns.builder()
        .addAllAccepts(left.getAccepts())
        .addAllAccepts(right.getAccepts())
        .returns(ImmutableScalarDef.builder()
            .context(ContextTypeDef.EXPRESSION)
            .array(false).required(true)
            .token(node.getToken())
            .name("")
            .type(ScalarType.BOOLEAN)
            .build())
        .build();
  }

  @Override
  public TypeDefReturns visitOr(OrExpression node, HdesTree ctx) {
    TypeDefReturns left = visit(node.getLeft(), ctx);
    TypeDefReturns right = visit(node.getRight(), ctx);
    if(left.getReturns() instanceof ScalarDef && right.getReturns() instanceof ScalarDef) {
      Set<ScalarType> scalars = new HashSet<>();
      scalars.add(((ScalarDef) left.getReturns()).getType());
      scalars.add(((ScalarDef) right.getReturns()).getType());
      
      boolean booleanComparison = scalars.size() == 1 && scalars.contains(ScalarType.BOOLEAN);
      
      if(!booleanComparison) {
        throw new HdesException(ImmutableErrorNode.builder()
            .bodyId(ctx.get().bodyId())
            .target(node)
            .message("Expected BOOLEAN types in equality operation but was: " + scalars + "!")
            .build());
      }
    }
    
    return ImmutableTypeDefReturns.builder()
        .addAllAccepts(left.getAccepts())
        .addAllAccepts(right.getAccepts())
        .returns(ImmutableScalarDef.builder()
            .context(ContextTypeDef.EXPRESSION)
            .array(false).required(true)
            .token(node.getToken())
            .name("")
            .type(ScalarType.BOOLEAN)
            .build())
        .build();
  }

  @Override
  public TypeDefReturns visitBetween(BetweenExpression node, HdesTree ctx) {
    TypeDefReturns left = visit(node.getLeft(), ctx);
    TypeDefReturns right = visit(node.getRight(), ctx);
    TypeDefReturns value = visit(node.getValue(), ctx);
    
    if(left.getReturns() instanceof ScalarDef && right.getReturns() instanceof ScalarDef) {
      Set<ScalarType> scalars = new HashSet<>();
      scalars.add(((ScalarDef) left.getReturns()).getType());
      scalars.add(((ScalarDef) right.getReturns()).getType());
      scalars.add(((ScalarDef) value.getReturns()).getType());
      
      boolean sameComparisonType = scalars.size() == 1;
      boolean numericComparisonType = scalars.size() == 2 && scalars.contains(ScalarType.INTEGER) && scalars.contains(ScalarType.DECIMAL);
      
      
      Function<TypeDefReturns, ScalarType> toName = (t) -> ((ScalarDef) t.getReturns()).getType();
      
      if(!sameComparisonType && !numericComparisonType) {
        throw new HdesException(ImmutableErrorNode.builder()
            .bodyId(ctx.get().bodyId())
            .target(node)
            .message(new StringBuilder()
                .append("Expected same types in between operation: ") 
                .append(toName.apply(value))
                .append(" between ").append(toName.apply(left))
                .append(" and ").append(toName.apply(right))
                .toString())
            .build());
        
      } else if(scalars.contains(ScalarType.BOOLEAN) || scalars.contains(ScalarType.STRING)) {
        throw new HdesException(ImmutableErrorNode.builder()
            .bodyId(ctx.get().bodyId())
            .target(node)
            .message(new StringBuilder()
                .append("Can't use BOOLEAN, STRING scalar types in between operation: ") 
                .append(toName.apply(value))
                .append(" between ").append(toName.apply(left))
                .append(" and ").append(toName.apply(right))
                .toString())
            .build());
      }
      
    } else {
      Function<TypeDefReturns, String> toName = (t) -> t.getReturns() instanceof ObjectDef ? "OBJECT" : "SCALAR";
      throw new HdesException(ImmutableErrorNode.builder()
        .bodyId(ctx.get().bodyId())
        .target(node)
        .message(new StringBuilder()
            .append("Expected scalar types in between operation but was: ") 
            .append(toName.apply(value))
            .append(" between ").append(toName.apply(left))
            .append(" and ").append(toName.apply(right))
            .toString())
        .build());
    }
    
    return ImmutableTypeDefReturns.builder()
        .addAllAccepts(left.getAccepts())
        .addAllAccepts(right.getAccepts())
        .addAllAccepts(value.getAccepts())
        .returns(ImmutableScalarDef.builder()
            .context(ContextTypeDef.EXPRESSION)
            .array(false).required(true)
            .token(node.getToken())
            .name("")
            .type(ScalarType.BOOLEAN)
            .build())
        .build();
  }

  @Override
  public TypeDefReturns visitMethod(CallMethodExpression node, HdesTree ctx) {
    if(node instanceof LambdaExpression) {
      return visitLambda((LambdaExpression) node, ctx);
    } else if(node instanceof StaticMethodExpression) {
      return visitMathMethod((StaticMethodExpression) node, ctx);
    }
    throw new HdesException(unknownExpression(node));
  }

  @Override
  public TypeDefReturns visitMathMethod(StaticMethodExpression method, HdesTree ctx) {
    List<TypeDefAccepts> accepts = new ArrayList<>();
    var next = ctx.next(method);
    for(HdesNode child : method.getValues()) {
      TypeDefReturns children = visit(child, next);
      accepts.addAll(children.getAccepts());
    }
    
    Set<ScalarType> typesUsed = new HashSet<>();
    for(HdesNode value : method.getValues()) {
      TypeDefReturns params = ctx.returns().build(value);
      
      if(params.getReturns() instanceof ObjectDef) {
        ObjectDef objectDef = (ObjectDef) params.getReturns();
        for(TypeDef typeDef : objectDef.getValues()) {
          if(typeDef instanceof ScalarDef) {
            ScalarDef scalarDef = (ScalarDef) typeDef;
            typesUsed.add(scalarDef.getType());
          }
        }
        
      } else {
        ScalarDef scalarDef = (ScalarDef) params.getReturns();
        typesUsed.add(scalarDef.getType());
      }
    }
    
    ScalarType scalar = typesUsed.contains(ScalarType.DECIMAL) && 
        typesUsed.contains(ScalarType.INTEGER) ? ScalarType.DECIMAL : typesUsed.iterator().next();
    
    // figure return type
    ScalarDef typeDef = ImmutableScalarDef.builder()
        .array(false)
        .required(true)
        .token(method.getToken())
        .name(method.getType().name())
        .context(ContextTypeDef.ACCEPTS)
        .type(scalar)
        .build();

    return ImmutableTypeDefReturns.builder().returns(typeDef).accepts(accepts).build();
  }

  @Override
  public TypeDefReturns visitLambda(LambdaExpression lambda, HdesTree ctx) {
    HdesNode body = lambda.getBody();
    TypeDefReturns type = visit(lambda.getType(), ctx.getParent().get());
    
    TypeDef lambdaParam;
    if(type.getReturns() instanceof ScalarDef) {
      lambdaParam = ImmutableScalarDef.builder().from(type.getReturns()).name(lambda.getParam().getValue()).array(false).build();
    } else {
      lambdaParam = ImmutableObjectDef.builder().from(type.getReturns()).name(lambda.getParam().getValue()).array(false).build();
    }
    
    var next = ctx.next(lambda);
    TypeDef typeDef = visit(body, next.next(lambdaParam)).getReturns();
    TypeDef returns;
    if(typeDef instanceof ScalarDef) {
      returns = ImmutableScalarDef.builder().from(typeDef).array(true).build();
    } else {
      returns = ImmutableObjectDef.builder().from(typeDef).array(true).build();
    }
    
    return ImmutableTypeDefReturns.builder().returns(returns).accepts(type.getAccepts()).build();
  }
  

  public TypeDefReturns visit(HdesNode node, HdesTree parent) {
    HdesTree ctx = ImmutableHdesTree.builder().parent(parent).value(node).build();

    if (node instanceof InvocationNode) {
      return visitInvocation((InvocationNode) node, ctx);
    } else if (node instanceof CallMethodExpression) {
      return visitMethod((CallMethodExpression) node, ctx);
    } else if (node instanceof InExpression) {
      return visitIn((InExpression) node, ctx);
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
    throw new HdesException(unknownExpression(node));
  }
  
  private String unknownExpression(HdesNode ast) {
    return new StringBuilder()
        .append("Unknown expression AST: ").append(ast.getClass()).append(System.lineSeparator())
        .append("  - ").append(ast).append("!")
        .toString();
  }
}
