package io.resys.hdes.ast.spi.returntypes;

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

import java.util.Optional;

import io.resys.hdes.ast.api.HdesException;
import io.resys.hdes.ast.api.nodes.BodyNode.ContextTypeDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.BodyNode.TypeDef;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.ExpressionNode.LambdaExpression;
import io.resys.hdes.ast.api.nodes.HdesNode;
import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.nodes.ImmutableErrorNode;
import io.resys.hdes.ast.api.nodes.InvocationNode;
import io.resys.hdes.ast.api.nodes.InvocationNode.EmptyPlaceholder;
import io.resys.hdes.ast.api.nodes.InvocationNode.NamedPlaceholder;
import io.resys.hdes.ast.api.nodes.InvocationNode.NestedInvocation;
import io.resys.hdes.ast.api.nodes.InvocationNode.Placeholder;
import io.resys.hdes.ast.api.nodes.InvocationNode.SimpleInvocation;
import io.resys.hdes.ast.api.nodes.ServiceNode.ServiceBody;
import io.resys.hdes.ast.api.visitors.ExpressionVisitor.InvocationVisitor;

public class ReturnTypeStVisitor implements InvocationVisitor<TypeDef, TypeDef> {

  @Override
  public TypeDef visitBody(InvocationNode node, HdesTree ctx) {
    if(node instanceof NestedInvocation) {
      return visitNested((NestedInvocation) node, ctx);
    } else if(node instanceof Placeholder) {
      return visitPlaceholder((Placeholder) node, ctx);
    } else if(node instanceof SimpleInvocation) {
      return visitSimple((SimpleInvocation) node, ctx);
    }
    
    throw new HdesException(unknownInvocation(node));
  }

  @Override
  public TypeDef visitNested(NestedInvocation node, HdesTree ctx) {
    HdesTree next = ctx.next(node);
    TypeDef parent = visitBody(node.getPath(), next);
    return visitBody(node.getValue(), next.next(parent));
  }
  
  @Override
  public TypeDef visitSimple(SimpleInvocation node, HdesTree ctx) {
    
    // find lambda node and one node after it
    HdesTree iterator = ctx;
    ObjectDef lambdaObject = null;
    do {
      
      if(iterator.getParent().get().getValue() instanceof LambdaExpression) {
        lambdaObject = (ObjectDef) iterator.getValue();
        break;
      }
      
      iterator = iterator.getParent().get();
    } while(!(iterator.getValue() instanceof ServiceBody));
    
    if(lambdaObject != null && lambdaObject.getName().equals(node.getValue())) {
      return lambdaObject;
    }
    
    ServiceBody body = ctx.get().node(ServiceBody.class);
    
    // ST accepted section
    Optional<TypeDef> accepted = body.getHeaders().getAcceptDefs().stream()
        .filter(t -> t.getName().equals(node.getValue()))
        .findFirst();
    if(accepted.isPresent()) {
      return accepted.get();
    }
    
    // ST returns section
    Optional<TypeDef> returns = body.getHeaders().getReturnDefs().stream()
        .filter(t -> t.getName().equals(node.getValue()))
        .findFirst();
    if(returns.isPresent()) {
      return returns.get();
    }
    
    throw new HdesException(
        ImmutableErrorNode.builder()
        .bodyId(ctx.get().body().getId().getValue())
        .target(node)
        .message("Can't find type def element: '" + node.getValue() + "'!")
        .build());
  }

  @Override
  public TypeDef visitPlaceholder(Placeholder node, HdesTree ctx) {
    if(node instanceof EmptyPlaceholder) {
      return visitEmptyPlaceholder((EmptyPlaceholder) node, ctx);
    } else if(node instanceof NamedPlaceholder) {
      return visitNamedPlaceholder((NamedPlaceholder) node, ctx);
    }
    throw new HdesException(unknownInvocation(node));
  }

  @Override
  public TypeDef visitEmptyPlaceholder(EmptyPlaceholder node, HdesTree ctx) {
    Optional<ScalarDef> scalar = ctx.find().node(ScalarDef.class);
    if(scalar.isEmpty()) {
      throw new HdesException(
        ImmutableErrorNode.builder()
        .bodyId(ctx.get().body().getId().getValue())
        .target(node)
        .message("Incorrect use of placeholder, it's not available in the given context!")
        .build());
    }
    return scalar.get();
  }

  @Override
  public TypeDef visitNamedPlaceholder(NamedPlaceholder node, HdesTree ctx) {
    if(node.getValue().equals("constants")) {
      DecisionTableBody body = ctx.get().node(DecisionTableBody.class);
      return body.getConstants();
    } else if(node.getValue().equals("matched")) {
      ScalarDef header = ctx.get().node(ScalarDef.class);
      DecisionTableBody body = (DecisionTableBody) ctx.get().node(DecisionTableBody.class);
      
      if(header.getContext() == ContextTypeDef.ACCEPTS) {
        throw new HdesException(
            ImmutableErrorNode.builder()
            .bodyId(ctx.get().body().getId().getValue())
            .target(node)
            .message("Placeholder: '" + node.getValue() + "' can't be used in 'accepts' element!")
            .build());
      }
      return body.getMatched();      
    }
    throw new HdesException(
      ImmutableErrorNode.builder()
      .bodyId(ctx.get().body().getId().getValue())
      .target(node)
      .message(
          "Unknown placeholder: '" + node.getValue() + "', " + 
          "placeholders available in the current context: " + 
              "'_constants', '_matched'" +  
          "!")
      .build());
  }
  
  private String unknownInvocation(HdesNode ast) {
    return new StringBuilder()
        .append("Unknown invocation type!").append(System.lineSeparator())
        .append(" AST: ").append(ast.getClass()).append(System.lineSeparator())
        .append("  - ").append(ast).append("!")
        .toString();
  }
}
