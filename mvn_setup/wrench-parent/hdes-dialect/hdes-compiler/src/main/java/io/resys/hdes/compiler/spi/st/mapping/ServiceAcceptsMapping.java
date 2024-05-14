package io.resys.hdes.compiler.spi.st.mapping;

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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.nodes.InvocationNode;
import io.resys.hdes.ast.api.nodes.InvocationNode.NestedInvocation;
import io.resys.hdes.ast.api.nodes.InvocationNode.SimpleInvocation;
import io.resys.hdes.ast.api.nodes.MappingNode.ExpressionMappingDef;
import io.resys.hdes.ast.api.nodes.MappingNode.FastMappingDef;
import io.resys.hdes.ast.api.nodes.MappingNode.FieldMappingDef;
import io.resys.hdes.ast.api.nodes.MappingNode.MappingDef;
import io.resys.hdes.ast.api.nodes.MappingNode.ObjectMappingDef;
import io.resys.hdes.ast.api.visitors.ServiceBodyVisitor.ServiceMappingDefVisitor;
import io.resys.hdes.compiler.spi.expressions.ExpressionFactory;
import io.resys.hdes.compiler.spi.spec.ImmutableSpec;
import io.resys.hdes.compiler.spi.st.mapping.ServiceMappingFactory.StMappingSpec;
import io.resys.hdes.compiler.spi.st.visitors.StSpec;
import io.resys.hdes.compiler.spi.units.CompilerNode.ServiceUnit;

public class ServiceAcceptsMapping implements ServiceMappingDefVisitor<StSpec, CodeBlock> {

  @Override
  public CodeBlock visitBody(ObjectMappingDef node, HdesTree ctx) {
    final var builder = CodeBlock.builder();
    visitObjectMappingDef(node, ctx).getValue().accept(builder);
    return builder.build();
  }

  @Override
  public StMappingSpec visitMappingDef(MappingDef node, HdesTree ctx) {
    if(node instanceof ExpressionMappingDef) {
      return visitExpressionMappingDef((ExpressionMappingDef) node, ctx);
    } else if(node instanceof FastMappingDef) {
      return visitFastMappingDef((FastMappingDef) node, ctx);
    } else if(node instanceof FieldMappingDef) {
      return visitFieldMappingDef((FieldMappingDef) node, ctx);
    } else if(node instanceof ObjectMappingDef) {
      return visitObjectMappingDef((ObjectMappingDef) node, ctx);
    }
    throw new IllegalArgumentException("not implemented"); 
  }
  
  @Override
  public StMappingSpec visitObjectMappingDef(ObjectMappingDef node, HdesTree ctx) {
    final var next = ctx.next(node);
    return ImmutableStMappingSpec.builder()
        .value(c -> {
          ServiceUnit unit = ctx.get().node(ServiceUnit.class);
          final var canonicalName = visitInvocation(unit.getBody().getCommand().getClassName());
          c.add("$T.builder()", ImmutableSpec.from(ClassName.bestGuess(canonicalName + "Mapping")));
          node.getValues().forEach(v -> visitMappingDef(v, next).getValue().accept(c));
          c.add(".build()");
        })
        .build();
  }

  @Override
  public StMappingSpec visitFieldMappingDef(FieldMappingDef node, HdesTree ctx) {
    return ImmutableStMappingSpec.builder()
        .value(code -> {
          final var body = CodeBlock.builder();
          visitMappingDef(node.getRight(), ctx).getValue().accept(body);
          code.add(".$L($L)", node.getLeft().getValue(), body.build());
        })
        .build();
  }
  
  @Override
  public StMappingSpec visitFastMappingDef(FastMappingDef node, HdesTree ctx) {
    final var def = ctx.returns().build(node.getValue()).getReturns();
    final var exp = ExpressionFactory.builder().body(node.getValue()).tree(ctx.next(node)).build().getValue();
    return ImmutableStMappingSpec.builder()
        .value(code -> code.add(".$L($L)", def.getName(), exp))
        .build();
  }
  
  @Override
  public StMappingSpec visitExpressionMappingDef(ExpressionMappingDef node, HdesTree ctx) {
    return ImmutableStMappingSpec.builder()
        .value(code -> code
            .add(ExpressionFactory.builder().body(node.getValue()).tree(ctx.next(node)).build()
            .getValue()))
        .build();
  }
  
  
  private String visitInvocation(InvocationNode invocation) {
    if(invocation instanceof SimpleInvocation) {
      SimpleInvocation simple = (SimpleInvocation) invocation;
      return simple.getValue();
    } else if(invocation instanceof NestedInvocation) {
      NestedInvocation nested = (NestedInvocation) invocation;
      String path = visitInvocation(nested.getPath());
      String value = visitInvocation(nested.getValue());
      return path + "." + value;
    }
    throw new IllegalArgumentException("Not supported invocation: " + invocation + "!"); 
  }
}
