package io.resys.hdes.compiler.spi.expressions.invocation;

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

import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.nodes.BodyNode.ContextTypeDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.BodyNode.TypeDef;
import io.resys.hdes.ast.api.nodes.ExpressionNode.LambdaExpression;
import io.resys.hdes.ast.api.nodes.FlowNode.StepCallDef;
import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.nodes.ImmutableObjectDef;
import io.resys.hdes.ast.api.nodes.InvocationNode;
import io.resys.hdes.ast.api.nodes.InvocationNode.EmptyPlaceholder;
import io.resys.hdes.ast.api.nodes.InvocationNode.NamedPlaceholder;
import io.resys.hdes.ast.api.nodes.InvocationNode.NestedInvocation;
import io.resys.hdes.ast.api.nodes.InvocationNode.Placeholder;
import io.resys.hdes.ast.api.nodes.InvocationNode.SimpleInvocation;
import io.resys.hdes.ast.api.visitors.ExpressionVisitor.InvocationVisitor;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.expressions.ExpressionFactory.ExpCode;
import io.resys.hdes.compiler.spi.expressions.ExpressionFactory.ExpObjectCode;
import io.resys.hdes.compiler.spi.expressions.ExpressionFactory.ExpScalarCode;
import io.resys.hdes.compiler.spi.expressions.ImmutableExpObjectCode;
import io.resys.hdes.compiler.spi.expressions.ImmutableExpScalarCode;
import io.resys.hdes.compiler.spi.spec.HdesDefSpec;
import io.resys.hdes.compiler.spi.spec.JavaSpecUtil;

public class DecisionTableInvocationVisitor implements InvocationVisitor<ExpCode, ExpCode> {
  public final static String ACCESS_OUTPUT_VALUE = "returns";
  public final static String ACCESS_CONSTANTS = "constants";
  public final static String ACCESS_INPUT_VALUE = HdesDefSpec.ACCESS_INPUT_VALUE;
  
  @Override
  public ExpCode visitBody(InvocationNode node, HdesTree ctx) {    
    if(node instanceof NestedInvocation) {
      return visitNested((NestedInvocation) node, ctx);
    } else if(node instanceof Placeholder) {
      return visitPlaceholder((Placeholder) node, ctx);
    } else if(node instanceof SimpleInvocation) {
      return visitSimple((SimpleInvocation) node, ctx);
    }
    throw new HdesCompilerException(HdesCompilerException.builder().unknownExpression(node));
  }

  @Override
  public ExpCode visitNested(NestedInvocation node, HdesTree ctx) {    
    final var pathDef = ctx.any().build(node.getPath());
    
    CodeBlock path = visitBody(node.getPath(), ctx).getValue();
    ExpCode value = visitBody(node.getValue(), ctx.next(pathDef));
    
    final var code = CodeBlock.builder().add(path).add(value.getValue()).build();
    if(value instanceof ExpScalarCode) {
      ExpScalarCode src = (ExpScalarCode) value;
      return ImmutableExpScalarCode.builder()
          .type(src.getType()).array(src.getArray())
          .value(code)
          .build();
    }
    
    ExpObjectCode src = (ExpObjectCode) value;
    return ImmutableExpObjectCode.builder()
        .array(src.getArray()).type(src.getType())
        .value(code)
        .build();
  }

  @Override
  public ExpCode visitSimple(SimpleInvocation node, HdesTree ctx) {
    TypeDef typeDef = ctx.any().build(node);
    CodeBlock.Builder value = CodeBlock.builder();
     
    if(typeDef.getContext() == ContextTypeDef.ACCEPTS) {
      value.add("$L.$L$L", ACCESS_INPUT_VALUE,
          JavaSpecUtil.methodCall(typeDef.getName()),
          typeDef.getRequired() ? "" : ".get()"); 
    } else if(typeDef.getContext() == ContextTypeDef.CONSTANTS) {
      value.add(node.getValue());
    } else if(typeDef.getContext() == ContextTypeDef.RETURNS) {
      value.add(".").add(JavaSpecUtil.getMethodName(node.getValue())).add("()");
    }
    
    return wrap(typeDef, value.build());
  }

  @Override
  public ExpCode visitPlaceholder(Placeholder node, HdesTree ctx) {
    if(node instanceof EmptyPlaceholder) {
      return visitEmptyPlaceholder((EmptyPlaceholder) node, ctx);
    } else if(node instanceof NamedPlaceholder) {
      return visitNamedPlaceholder((NamedPlaceholder) node, ctx);
    }
    throw new HdesCompilerException(HdesCompilerException.builder().unknownExpression(node));
  }

  @Override
  public ExpCode visitEmptyPlaceholder(EmptyPlaceholder node, HdesTree ctx) {
    ScalarDef header = ctx.get().node(ScalarDef.class);
    String name = JavaSpecUtil.methodCall(header.getName());
    CodeBlock value = CodeBlock.builder()
        .add("$L.$L", ACCESS_INPUT_VALUE, name + (header.getRequired() ? "" : ".get()"))
        .build();
    return wrap(header, value);
  }

  @Override
  public ExpCode visitNamedPlaceholder(NamedPlaceholder node, HdesTree ctx) {
    if(node.getValue().equals("constants")) {
      TypeDef def = ctx.returns().build(node).getReturns();
      final var value = CodeBlock.builder().add("constants");
      
      if(ctx.getValue() instanceof LambdaExpression) {
        value.add(".getValues().stream()");
      }
      return wrap(def, value.build());
    } else if(node.getValue().equals("matched")) {
      TypeDef def = ctx.returns().build(node).getReturns();
      final var value = CodeBlock.builder().add("returns");
      
      if(ctx.getValue() instanceof LambdaExpression) {
        value.add(".getValues().stream()");
      }
      return wrap(def, value.build());
    }
    
    throw new HdesCompilerException(HdesCompilerException.builder().unknownExpression(node));
  }

  protected static ExpCode wrap(TypeDef typeDef, CodeBlock value) {
    if(typeDef instanceof ScalarDef) {
      ScalarDef scalarNode = (ScalarDef) typeDef;
      return ImmutableExpScalarCode.builder()
          .type(scalarNode.getType()).array(typeDef.getArray())
          .value(value)
          .build();
    } else if(typeDef instanceof StepCallDef) {
      StepCallDef callDef = (StepCallDef) typeDef;
      ObjectDef type = ImmutableObjectDef.builder()
          .from(typeDef)
          .values(callDef.getValues())
          .build();
      return ImmutableExpObjectCode.builder()
          .array(typeDef.getArray()).type(type)
          .value(value)
          .build();
    }
    
    ObjectDef type = (ObjectDef) typeDef;
    return ImmutableExpObjectCode.builder()
        .array(typeDef.getArray()).type(type)
        .value(value)
        .build();
  }
}
