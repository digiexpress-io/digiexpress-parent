package io.resys.hdes.compiler.spi.fl.visitors.mapping;

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

import java.util.function.Consumer;

import org.immutables.value.Value;

import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.nodes.FlowNode.CallDef;
import io.resys.hdes.ast.api.nodes.FlowNode.EndPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.StepAs;
import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.nodes.MappingNode.ExpressionMappingDef;
import io.resys.hdes.ast.api.nodes.MappingNode.FastMappingDef;
import io.resys.hdes.ast.api.nodes.MappingNode.FieldMappingDef;
import io.resys.hdes.ast.api.nodes.MappingNode.MappingDef;
import io.resys.hdes.ast.api.nodes.MappingNode.ObjectMappingDef;
import io.resys.hdes.ast.api.nodes.ServiceNode.ServiceBody;
import io.resys.hdes.ast.api.visitors.FlowBodyVisitor.FlowMappingDefVisitor;
import io.resys.hdes.ast.api.visitors.FlowBodyVisitor.MappingEvent;
import io.resys.hdes.compiler.spi.expressions.ExpressionFactory;
import io.resys.hdes.compiler.spi.fl.visitors.FlSpec;
import io.resys.hdes.compiler.spi.fl.visitors.mapping.ServiceCallDefMappingDefVisitor.ServiceMappingSpec;
import io.resys.hdes.compiler.spi.spec.ImmutableSpec;
import io.resys.hdes.compiler.spi.units.CompilerNode;
import io.resys.hdes.compiler.spi.units.CompilerNode.ServiceUnit;

public class ServiceCallDefMappingDefVisitor implements FlowMappingDefVisitor<ServiceMappingSpec, CodeBlock> {


  @Value.Immutable
  public interface ServiceMappingSpec extends FlSpec {
    Consumer<CodeBlock.Builder> getValue();
  }
  
  @Override
  public CodeBlock visitBody(CallDef def, HdesTree ctx) {
    return visitBody(def, null, ctx);
  }
  
  @Override
  public CodeBlock visitBody(CallDef def, MappingEvent event, HdesTree ctx) {
    CompilerNode compilerNode = ctx.get().node(CompilerNode.class);
    String dependencyId = def.getId().getValue();
    ServiceBody body = (ServiceBody) ctx.getRoot().getBody(dependencyId);
    ServiceUnit unit = compilerNode.st(body);
    
    final var mapping = CodeBlock.builder();
    visitObjectMappingDef(def.getMapping(), ctx.next(def).next(unit)).getValue().accept(mapping);
        
    final var call = def.getIndex().map(index -> "call" + index).orElse("call");
    final var impl = unit.getType().getImpl().getName();
    
    final var methodName = mappingEvent(event, mapping.build()); 
    final var result = CodeBlock.builder()
        .addStatement("final var $L = new $T().$L", call, impl, methodName);
    
    return result.build();
  }
  
  private CodeBlock mappingEvent(MappingEvent event, CodeBlock mapping) {
    if(event == null) {
      return CodeBlock.builder().add("apply($L)", mapping).build();
    }
    switch (event) {
    case ON_ENTER: return CodeBlock.builder().add("onEnter($L)", mapping).build();
    case ON_COMPLETE: return CodeBlock.builder().add("onComplete(dataId, data, $L)", mapping).build();
    case ON_ERROR: return CodeBlock.builder().add("onError($L)", mapping).build();
    case ON_TIMEOUT: return CodeBlock.builder().add("onTimeout($L)", mapping).build();
    }
    throw new IllegalArgumentException("not implemented"); 
  }
  
  @Override
  public ServiceMappingSpec visitMappingDef(MappingDef node, HdesTree ctx) {
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
  public ServiceMappingSpec visitObjectMappingDef(ObjectMappingDef node, HdesTree ctx) {
    final var next = ctx.next(node);
    return ImmutableServiceMappingSpec.builder()
        .value(c -> {
          ServiceUnit unit = ctx.get().node(ServiceUnit.class);
          c.add("$T.builder()", ImmutableSpec.from(unit.getType().getAccepts().getName()));
          node.getValues().forEach(v -> visitMappingDef(v, next).getValue().accept(c));
          c.add(".build()");
        })
        .build();
  }

  @Override
  public ServiceMappingSpec visitFieldMappingDef(FieldMappingDef node, HdesTree ctx) {
    return ImmutableServiceMappingSpec.builder()
        .value(code -> {
          final var body = CodeBlock.builder();
          visitMappingDef(node.getRight(), ctx).getValue().accept(body);
          code.add(".$L($L)", node.getLeft().getValue(), body.build());
        })
        .build();
  }
  
  @Override
  public ServiceMappingSpec visitFastMappingDef(FastMappingDef node, HdesTree ctx) {
    final var def = ctx.returns().build(node.getValue()).getReturns();
    final var exp = ExpressionFactory.builder().body(node.getValue()).tree(ctx.next(node)).build().getValue();
    return ImmutableServiceMappingSpec.builder()
        .value(code -> code.add(".$L($L)", def.getName(), exp))
        .build();
  }
  
  @Override
  public ServiceMappingSpec visitExpressionMappingDef(ExpressionMappingDef node, HdesTree ctx) {
    return ImmutableServiceMappingSpec.builder()
        .value(code -> code
            .add(ExpressionFactory.builder().body(node.getValue()).tree(ctx.next(node)).build()
            .getValue()))
        .build();
  }

  @Override
  public CodeBlock visitBody(EndPointer node, HdesTree ctx) {
    throw new IllegalArgumentException("not implemented");
  }

  @Override
  public CodeBlock visitBody(StepAs def, HdesTree ctx) {
    throw new IllegalArgumentException("not implemented");
  }
}
